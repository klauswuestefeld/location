package io.github.felipebueno.location;

import android.location.LocationListener;
import android.os.Looper;
import android.util.Log;

import java.util.List;

import static io.github.felipebueno.location.LogUtils.TAG;

public class LocationUtils {

	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";
	public static final String SESSION_DISCARDED = "session discarded";

	public static void initProviders(LocationManager locationManager, Long minTime, LocationListener listener, Looper looper) {
		List<String> providers = locationManager.getAllProviders();
		boolean hasFused = false;
		for (String provider : providers) {
			if (LocationManager.FUSED_PROVIDER.equals(provider)) {
				hasFused = true;
				break;
			}
		}

		if (hasFused) {
			try {
				locationManager.requestLocationUpdates(LocationManager.FUSED_PROVIDER, minTime, 0.0F, listener, looper);
			} catch (IllegalArgumentException e) {
				Log.e(TAG, "failed to request a location with fused provider" + e);
			}
		}

		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, 0.0F, listener, looper);
		try {
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, 0.0F, listener, looper);
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "failed to request a location with network provider" + e);
		}
	}

}
