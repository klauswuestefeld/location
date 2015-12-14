package io.github.felipebueno.location;

import android.util.Log;

public final class LogUtils {

	static final String TAG = "LOCATION_DEBUG";

	public static void log(Object caller, String msg) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, caller.getClass().getSimpleName() + ": " + msg);
	}

}
