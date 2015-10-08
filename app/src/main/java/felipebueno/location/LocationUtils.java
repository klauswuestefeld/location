package felipebueno.location;

import android.location.LocationListener;
import android.os.Looper;
import android.util.Log;

import java.util.List;

import static felipebueno.location.LogUtils.*;

public class LocationUtils {

	static final String LATITUDE = "latitude";
	static final String LONGITUDE = "longitude";

	static void initProviders(LocationManager locationManager, Long minTime, LocationListener listener, Looper looper) {
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
