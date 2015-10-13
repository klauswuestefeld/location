package felipebueno.location.followme;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

public class MapDownloader extends AsyncTask<String, Void, Bitmap> {

	private final Activity activity;
	ImageView bmImage;
	int width, height;
	private String url;

	public MapDownloader(ImageView bmImage, int width, int height, Activity activity) {
		this.width = width;
		this.height = height;
		this.bmImage = bmImage;
		this.activity = activity;
	}

	protected Bitmap doInBackground(String... urls) {
		url = urls[0];

		Bitmap mIcon = null;
		try {
			InputStream in = new java.net.URL(url).openStream();
			mIcon = BitmapFactory.decodeStream(in);
		} catch (Exception e) {
			Log.e("Error", e.getMessage());
		}
		return mIcon;
	}

	protected void onPostExecute(Bitmap result) {
		Bitmap newbitMap = Bitmap.createScaledBitmap(result, width, height, true);
		bmImage.setImageBitmap(newbitMap);
		activity.setProgressBarIndeterminateVisibility(false);
		activity.setProgressBarVisibility(false);
	}

}
