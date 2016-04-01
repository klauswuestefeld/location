package io.github.felipebueno.location.followme;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.HashMap;

import io.github.felipebueno.location.R;
import io.github.felipebueno.location.followme.FollowMeService.LocalBinder;
import sneer.android.Message;
import sneer.android.PartnerSession;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static io.github.felipebueno.location.LocationUtils.LATITUDE;
import static io.github.felipebueno.location.LocationUtils.LONGITUDE;
import static io.github.felipebueno.location.LocationUtils.SESSION_DISCARDED;
import static io.github.felipebueno.location.LogUtils.log;
import static io.github.felipebueno.location.followme.FollowMeService.isRunning;

public class FollowMeActivity extends Activity {

	private static final int MAX_SIZE = 640;
	private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 42;

	public static PartnerSession session;
	public static double myLatitude;
	public static double myLongitude;

	private double theirLatitude;
	private double theirLongitude;
	private ImageView map;
	private FollowMeService localService;

	private final ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LocalBinder returnLocalService = (FollowMeService.LocalBinder) service;
			localService = returnLocalService.getService();
			log(FollowMeActivity.this, "onServiceConnected.localService->" + localService);
		}

		@Override public void onServiceDisconnected(ComponentName name) {}
	};
	private Intent service;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_follow_me);

		service = new Intent(this, FollowMeService.class);

		map = (ImageView) findViewById(R.id.map_view);

		showProgressBar();
		startSession();

		if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED)
			ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_FINE_LOCATION);
	}

	private void startSession() {
		session = PartnerSession.join(this, new PartnerSession.Listener() {
			@Override
			public void onUpToDate() {
				FollowMeActivity.this.onUpToDate();
			}

			@Override
			public void onMessage(Message message) {
				FollowMeActivity.this.onMessage(message);
			}
		});
		if (!session.wasStartedByMe()) {
			setTitle("Following for 1h");
		}
	}

	private void onUpToDate() {
		map.post(new Runnable() {
			@Override
			public void run() {
				int width = map.getMeasuredWidth();
				int height = map.getMeasuredHeight();

				showProgressBar();

				new MapDownloader(map, width, height, FollowMeActivity.this).execute(
						getMapURL(width, height)
				);

			}
		});
		log(this, "onUpToDate()");
	}

	public void onMessage(Message message) {
		HashMap<String, Double> m = (HashMap<String, Double>) message.payload();
		log(this, "onMessage(message)->" + m);

		Double discarded = m.get(SESSION_DISCARDED);
		if ((discarded != null) && (discarded == 1)) {
			log(this, SESSION_DISCARDED);
			stopService(service);
			finish();
			return;
		}

		if (message.wasSentByMe()) {
			myLatitude = m.get(LATITUDE);
			myLongitude = m.get(LONGITUDE);
		} else {
			theirLatitude = m.get(LATITUDE);
			theirLongitude = m.get(LONGITUDE);
		}
	}

	protected String getMapURL(int width, int height) {
		log(this, String.valueOf(width));
		log(this, String.valueOf(height));
		if (width > height) {
			height = MAX_SIZE * height / width;
			width = MAX_SIZE;
		} else {
			width = MAX_SIZE * width / height;
			height = MAX_SIZE;
		}

		log(this, String.valueOf(width));
		log(this, String.valueOf(height));

		String url = "https://maps.googleapis.com/maps/api/staticmap";
		url += "?size=" + width + "x" + height + "&scale=2";
		url += "&maptype=roadmap";
		url += "&markers=size:mid%7Ccolor:red%7C" + myLatitude + "," + myLongitude;

		if (theirLatitude != 0.0)
			url += "&markers=size:mid%7Ccolor:blue%7C" + theirLatitude + "," + theirLongitude;

		log(this, "getMapURL: url->" + url);

		return url;
	}

	private void showProgressBar() {
		log(this, "showProgressBar()");
		setProgressBarIndeterminate(true);
		setProgressBarIndeterminateVisibility(true);
		setProgressBarVisibility(true);
	}

	@Override
	protected void onResume() {
		if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
			if (!isRunning) {
				log(this, "startService()");
				startService(service);
			}
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (isRunning)
			finish();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		if (session != null)
			session.close();
		super.onDestroy();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		if (requestCode == PERMISSIONS_REQUEST_FINE_LOCATION) {
			if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
				if (!isRunning) {
					log(this, "startService()");
					startService(service);
				}
			} else {
				Toast.makeText(this, "You must grant access to your device's location to use this app", Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

}
