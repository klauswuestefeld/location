package io.github.felipebueno.location.followme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.HashMap;
import java.util.Map;

import static io.github.felipebueno.location.LocationUtils.SESSION_DISCARDED;
import static io.github.felipebueno.location.LogUtils.log;

public class FollowMeServiceKiller extends BroadcastReceiver {

	public static FollowMeService victim;

	@Override
	public void onReceive(Context context, Intent intent) {
		victim.stopForeground(true);
		victim.stopSelf();
		FollowMeService.isRunning = false;
		Map<String, Double> map = new HashMap<>();
		map.put(SESSION_DISCARDED, (double) 1);
		FollowMeActivity.session.send(map);
		log(this, "FollowMeServiceKiller intent->" + intent);
	}

}
