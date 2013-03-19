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
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ImageView;

public class MapCard extends Card<Bitmap> {

	private static final String MAP_URL = "http://maps.google.com/maps/api/staticmap?center=%f,%f&zoom=14&scale=2&size=%dx%d&sensor=false&markers=color:blue%%7C%f,%f";
	private static final String PARAM_URL = "url";

	private final Float mLatitude;
	private final Float mLongitude;

	private ImageView mImageView;

	public MapCard(final Context context, final LoaderManager loaderManager, final Float latitude, final Float longitude) {
		super(context, loaderManager, R.string.card_plan_title, R.layout.card_map);

		mLatitude = latitude;
		mLongitude = longitude;
	}

	@Override
	protected void bindView(final Context context, final View view) {
		final String url = String.format(Locale.ENGLISH, MAP_URL, mLatitude, mLongitude, 600, 300, mLatitude,
				mLongitude);

		mImageView = (ImageView) view;

		final Bundle bundle = new Bundle();
		bundle.putString(PARAM_URL, url);

		initLoader(bundle, this).forceLoad();
	}

	@Override
	public Loader<Bitmap> onCreateLoader(final int id, final Bundle bundle) {
		return new LoaderTask(getContext(), bundle.getString(PARAM_URL));
	}

	@Override
	public void onLoadFinished(final Loader<Bitmap> loader, final Bitmap bitmap) {
		mImageView.setImageBitmap(bitmap);
		showContent();
	}

	private static class LoaderTask extends AsyncTaskLoader<Bitmap> {

		private final String mUrl;

		public LoaderTask(final Context context, final String url) {
			super(context);
			mUrl = url;
		}

		@Override
		public Bitmap loadInBackground() {
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

	}

}