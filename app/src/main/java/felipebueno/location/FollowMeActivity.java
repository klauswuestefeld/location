package felipebueno.location;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Map;

import sneer.android.Message;
import sneer.android.PartnerSession;

import static felipebueno.location.LocationUtils.LATITUDE;
import static felipebueno.location.LocationUtils.LONGITUDE;
import static felipebueno.location.LocationUtils.TAG;
import static felipebueno.location.LocationUtils.initProviders;

public class FollowMeActivity extends Activity implements LocationListener {

	private static final int MAX_SIZE = 640;
	static final long MIN_TIME = 30000L;
	private Intent service;

	private LocationManager locationManager;
	private double myLatitude;
	private double myLongitude;
	private double theirLatitude;
	private double theirLongitude;
	private ImageView map;
	public static PartnerSession session;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.activity_follow_me);
		map = (ImageView) findViewById(R.id.map_view);

		showProgressBar();

		startSession();

		if (!FollowMeService.isRunning)
			service = new Intent(this, FollowMeService.class);
	}

	private void startSession() {
		session = PartnerSession.join(this, new PartnerSession.Listener() {
			@Override
			public void onUpToDate() {
				refresh();
			}

			@Override
			public void onMessage(Message message) {
				handle(message);
			}
		});
	}

	private void refresh() {
		if (locationManager == null) {
			locationManager = LocationManager.getInstance(getApplicationContext());
			initProviders(locationManager, MIN_TIME, this);
		}

		map.post(new Runnable() {
			@Override
			public void run() {
				int width = map.getMeasuredWidth();
				int height = map.getMeasuredHeight();

				showProgressBar();

				new MapDownloader(map, width, height, FollowMeActivity.this, session).execute(
					getMapURL(width, height)
				);
			}
		});
		if (BuildConfig.DEBUG)
			Log.d(TAG, getClass().getSimpleName() + "refresh()->called");
	}

	private void handle(Message message) {
		HashMap<String, Double> m = (HashMap<String, Double>) message.payload();

		if (message.wasSentByMe()) {
			myLatitude = m.get(LATITUDE);
			myLongitude = m.get(LONGITUDE);
		} else {
			theirLatitude = m.get(LATITUDE);
			theirLongitude = m.get(LONGITUDE);
		}

		if (BuildConfig.DEBUG)
			Log.d(TAG, getClass().getSimpleName() + "handle(message)->m" + m);
	}

	protected String getMapURL(int width, int height) {
		if (width > height) {
			width = MAX_SIZE;
			height = MAX_SIZE * height / width;
		} else {
			height = MAX_SIZE;
			width = MAX_SIZE * width / height;
		}

		String url = "https://maps.googleapis.com/maps/api/staticmap";
		url += "?size=" + width + "x" + height + "&scale=2";
		url += "&maptype=roadmap";
		url += "&markers=size:mid%7Ccolor:red%7C" + myLatitude + "," + myLongitude;

		if (theirLatitude != 0.0)
			url += "&markers=size:mid%7Ccolor:blue%7C" + theirLatitude + "," + theirLongitude;

		return url;
	}

	private void showProgressBar() {
		setProgressBarIndeterminate(true);
		setProgressBarIndeterminateVisibility(true);
		setProgressBarVisibility(true);
	}

	@Override
	protected void onPause() {
		super.onPause();
		finish();
	}

	@Override
	protected void onDestroy() {
		if (session != null)
			session.close();
		if (!FollowMeService.isRunning)
			startService(service);
		super.onDestroy();
	}

	@Override
	public void onLocationChanged(Location location) {
		Map<String, Double> m = new HashMap<>();
		m.put(LATITUDE, location.getLatitude());
		m.put(LONGITUDE, location.getLongitude());
		session.send(m);
		if (BuildConfig.DEBUG)
			Log.d(TAG, getClass().getSimpleName() + "onLocationChanged(1) session.send()->called");
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

}
