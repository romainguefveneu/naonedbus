package net.naonedbus.card.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import net.naonedbus.R;
import net.naonedbus.card.Card;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.LoaderManager;
import android.view.View;
import android.widget.ImageView;

public class MapCard extends Card {

	private static final String MAP_URL = "http://maps.google.com/maps/api/staticmap?center=%f,%f&zoom=14&scale=2&size=%dx%d&sensor=false&markers=color:blue%%7C%f,%f";

	private static interface Callback {
		void onBitmapLoaded(Bitmap bitmap);
	}

	private final Float mLatitude;
	private final Float mLongitude;

	public MapCard(final Context context, final LoaderManager loaderManager, final Float latitude, final Float longitude) {
		super(context, loaderManager, R.string.card_plan_title, R.layout.card_map);

		mLatitude = latitude;
		mLongitude = longitude;
	}

	@Override
	protected void bindView(final Context context, final View view) {
		final ImageView imageView = (ImageView) view;
		final String url = String.format(Locale.ENGLISH, MAP_URL, mLatitude, mLongitude, 600, 300, mLatitude,
				mLongitude);

		new DownloadFile(url, new Callback() {
			@Override
			public void onBitmapLoaded(final Bitmap bitmap) {
				imageView.setImageBitmap(bitmap);
				showContent();
			}
		}).execute();
	}

	private class DownloadFile extends AsyncTask<Void, Void, Bitmap> {

		private final Callback mCallback;
		private final String mUrl;

		public DownloadFile(final String url, final Callback callback) {
			mUrl = url;
			mCallback = callback;
		}

		@Override
		protected Bitmap doInBackground(final Void... src) {
			try {
				final URL url = new URL(mUrl);
				final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setDoInput(true);
				connection.connect();
				final InputStream input = connection.getInputStream();
				final Bitmap myBitmap = BitmapFactory.decodeStream(input);
				return myBitmap;
			} catch (final IOException e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(final Bitmap result) {
			mCallback.onBitmapLoaded(result);
		}
	}

}
