package felipebueno.location.followme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static felipebueno.location.LogUtils.log;

public class FollowMeServiceKiller extends BroadcastReceiver {

	public static FollowMeService victim;

	@Override
	public void onReceive(Context context, Intent intent) {
		victim.stopForeground(true);
		victim.stopSelf();
		FollowMeService.isRunning = false;
		log(this, "FollowMeServiceKiller called. Values: victim.isRunning?->" + FollowMeService.isRunning);
	}

}
