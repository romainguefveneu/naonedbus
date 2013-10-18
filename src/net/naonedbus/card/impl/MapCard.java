/**
 * Copyright (C) 2013 Romain Guefveneu.
 *   
 *  This file is part of naonedbus.
 *   
 *  Naonedbus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  Naonedbus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.naonedbus.card.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import net.naonedbus.R;
import net.naonedbus.activity.impl.MapActivity;
import net.naonedbus.card.Card;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

public class MapCard extends Card<Bitmap> {

	private static final String MAP_URL = "http://maps.google.com/maps/api/staticmap?zoom=16&scale=1&size=%dx%d&sensor=true&markers=color:blue%%7C%f,%f&format=jpg";
	private static final String MAP_URL_CURRENT_LOCATION = "http://maps.google.com/maps/api/staticmap?scale=1&size=%dx%d&sensor=true&markers=color:blue%%7C%f,%f&center=%f,%f&format=jpg";
	private static final String PARAM_URL = "url";

	private final Float mLatitude;
	private final Float mLongitude;

	private Location mCurrentLocation;

	private ImageView mImageView;

	public MapCard(final Context context, final LoaderManager loaderManager, final FragmentManager fragmentManager,
			final Float latitude, final Float longitude) {
		super(context, loaderManager, fragmentManager, R.string.card_plan_title, R.layout.card_map);

		mLatitude = latitude;
		mLongitude = longitude;

		setActionDividerVisible(false);
	}

	public void setCurrentLocation(final Location currentLocation) {
//		mCurrentLocation = currentLocation;
	}

	@Override
	protected Intent getMoreIntent() {
		final Intent intent = new Intent(getContext(), MapActivity.class);
		intent.putExtra(Intent.EXTRA_TITLE, R.string.card_more_map);
		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, R.drawable.ic_card_navigate);
		return intent;
	}

	@Override
	protected void bindView(final Context context, final View base, final View view) {

		mImageView = (ImageView) view;
		showContent();
		final ViewTreeObserver obs = mImageView.getViewTreeObserver();
		obs.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				if (mImageView.getWidth() != 0) {
					mImageView.getViewTreeObserver().removeOnPreDrawListener(this);
					fillView(mImageView);
				}
				return true;
			}
		});

	}

	private void fillView(final ImageView imageView) {
		final String url;
		if (mCurrentLocation == null) {
			url = String.format(Locale.ENGLISH, MAP_URL, imageView.getMeasuredWidth(), imageView.getMeasuredHeight(),
					mLatitude, mLongitude);
		} else {
			url = String.format(Locale.ENGLISH, MAP_URL_CURRENT_LOCATION, imageView.getMeasuredWidth(),
					imageView.getMeasuredHeight(), mLatitude, mLongitude, mCurrentLocation.getLatitude(),
					mCurrentLocation.getLongitude());
		}

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
