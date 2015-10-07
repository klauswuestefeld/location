package felipebueno.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static felipebueno.location.LocationUtils.TAG;

public class FollowMeServiceKiller extends BroadcastReceiver {

	public static FollowMeService victim;

	@Override
	public void onReceive(Context context, Intent intent) {
		victim.stopForeground(true);
		victim.stopSelf();
		FollowMeService.isRunning = false;
		if (BuildConfig.DEBUG)
			Log.d(TAG, "FollowMeServiceKiller called. Values: victim.isRunning?->" + FollowMeService.isRunning);
	}

}
