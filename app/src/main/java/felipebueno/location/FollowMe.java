package felipebueno.location;

import android.app.Application;
import android.content.Intent;

public class FollowMe extends Application {

	private Intent service;

	@Override
	public void onCreate() {
		super.onCreate();

		if (!FollowMeService.isRunning) {
			service = new Intent(this, FollowMeService.class);
			startService(service);
		}
	}

}
