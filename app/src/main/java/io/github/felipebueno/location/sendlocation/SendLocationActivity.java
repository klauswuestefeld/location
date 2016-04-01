package io.github.felipebueno.location.sendlocation;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import io.github.felipebueno.location.LocationManager;
import io.github.felipebueno.location.LocationUtils;
import io.github.felipebueno.location.R;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class SendLocationActivity extends Activity implements LocationListener {

	static final long MIN_TIME = 1000L;
	private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 42;
	private static LocationManager locationManager;

	private Location latestLocation;
	private TextView textAccuracy;
	private Button sendButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location);

		textAccuracy = (TextView) findViewById(R.id.textAccuracy);
		sendButton = (Button) findViewById(R.id.buttonSend);
		sendButton.setEnabled(false);

		if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED)
			ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_FINE_LOCATION);
	}

	@Override
	protected void onPause() {
		if (locationManager != null) {
			locationManager.removeUpdates(this);
			finish();
		}
		super.onPause();
	}

	public void onSendClicked(View view) {
		Intent msg = getIntent().getParcelableExtra("SEND_MESSAGE");
		if (msg != null) {
			String url = "I'm here:\nhttps://google.com/maps/place/" + latestLocation.getLatitude() + "," + latestLocation.getLongitude();
			startService(msg.setAction(url));
		}
		finish();
	}

	public void onCancelClicked(View view) {
		finish();
	}

	@Override
	public void onLocationChanged(Location location) {
		latestLocation = location;
		updateTextAccuracy();
	}

	private void updateTextAccuracy() {
		textAccuracy.post(new Runnable() {
			@Override
			public void run() {
				sendButton.setEnabled(true);
				textAccuracy.setText("Accuracy " + (int) latestLocation.getAccuracy() + " meters");
			}
		});
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		if (requestCode == PERMISSIONS_REQUEST_FINE_LOCATION) {
			if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
				locationManager = LocationManager.getInstance(getApplicationContext());
				LocationUtils.initProviders(locationManager, MIN_TIME, this, getMainLooper());
			} else {
				Toast.makeText(this, "You must grant access to your device's location to use this app", Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	@Override public void onStatusChanged(String provider, int status, Bundle extras) {}
	@Override public void onProviderEnabled(String provider) {}
	@Override public void onProviderDisabled(String provider) {}

}
