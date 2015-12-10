package felipebueno.location.followme;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import felipebueno.location.LocationManager;
import felipebueno.location.R;

import static felipebueno.location.LocationUtils.LATITUDE;
import static felipebueno.location.LocationUtils.LONGITUDE;
import static felipebueno.location.LocationUtils.initProviders;
import static felipebueno.location.LogUtils.log;
import static felipebueno.location.followme.FollowMeActivity.myLatitude;
import static felipebueno.location.followme.FollowMeActivity.myLongitude;
import static felipebueno.location.followme.FollowMeActivity.session;

public class FollowMeService extends Service implements LocationListener {

	public static final int SERVICE_ID = 1234;
	private static final Long THIRD_SECONDS = 10000L;//30000L;
	private static final long ONE_HOUR = (60 * 1000);//(60 * 1000 * 60);
	public static boolean isRunning;
	private final IBinder mBinder = new LocalBinder();
	private Handler mainHandler = new Handler(Looper.getMainLooper());
	private volatile LocationManager locationManager;

	@Override
	public IBinder onBind(Intent intent) {
		log(this, "onBind()");
		return mBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		log(this, "onStartCommand()");

		Thread t = new Thread() {
			@Override
			public void run() {
				NotificationCompat.Builder builder = new NotificationCompat.Builder(FollowMeService.this);
				builder.setSmallIcon(R.mipmap.ic_launcher)
						.setContentTitle("Location")
						.setContentText("Sending your GPS location...")
						.setOnlyAlertOnce(true);

				locationManager = LocationManager.getInstance(getApplicationContext());
				initProviders(locationManager, THIRD_SECONDS, FollowMeService.this, Looper.getMainLooper());

				if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
					Toast.makeText(FollowMeService.this, "No GPS available", Toast.LENGTH_LONG).show();
					stopSelf();
//					return Service.START_NOT_STICKY;
				}

				startForeground(SERVICE_ID, builder.build());
				startKillAlarm();
				isRunning = true;
			}
		};

		t.start();
		return Service.START_STICKY;
	}

	private void startKillAlarm() {
		FollowMeServiceKiller.victim = this;
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(this, FollowMeServiceKiller.class);
		PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
		alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + ONE_HOUR, alarmIntent);
	}

	@Override
	public void onDestroy() {
		log(this, "onDestroy()");
		if (locationManager != null)
			locationManager.removeUpdates(this);
		super.onDestroy();
	}

	@Override
	public void onLocationChanged(final Location location) {
		log(this, "onLocationChanged()");

		mainHandler.post(new Runnable() {
			@Override
			public void run() {
				Map<String, Double> map = new HashMap<>();
				if (session.wasStartedByMe()) {
					map.put(LATITUDE, location.getLatitude());
					map.put(LONGITUDE, location.getLongitude());
					session.send(map);
				} else {
					log(this, "session not started by my. Won't send my location");
					myLatitude = location.getLatitude();
					myLongitude = location.getLongitude();
				}
			}
		});
	}

	@Override
	public void onProviderDisabled(String arg0) {
		log(this, "onProviderDisabled()");
	}

	@Override
	public void onProviderEnabled(String arg0) {
		log(this, "onProviderEnabled()");
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		log(this, "onStatusChanged()");
	}

	public class LocalBinder extends Binder {
		public FollowMeService getService() {
			return FollowMeService.this;
		}
	}

}
