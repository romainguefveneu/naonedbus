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
package net.naonedbus.activity.impl;

import java.util.Locale;

import net.naonedbus.R;
import net.naonedbus.activity.map.overlay.BiclooItemizedOverlay;
import net.naonedbus.activity.map.overlay.item.BasicOverlayItem;
import net.naonedbus.bean.Bicloo;
import net.naonedbus.manager.impl.BiclooBookmarkManager;
import net.naonedbus.utils.FontUtils;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

public class BiclooDetailActivity extends SherlockMapActivity {

	public static final String PARAM_BICLOO = "bicloo";

	protected static final String NAVIGATION_INTENT = "google.navigation:q=%f,%f";
	protected static final String SMS_NAVIGATION_URL = "maps.google.com/?q=%f,%f";

	private BiclooBookmarkManager mBiclooBookmarkManager;

	protected TextView mTitle;
	protected TextView mAdresse;
	protected TextView mPaiement;
	protected TextView mBicloosDisponibles;
	protected TextView mPlacesDisponibles;
	protected TextView mItemDate;
	protected TextView mMessage;
	protected MapView mMapView;
	protected MapController mMapController;

	private Bicloo mBicloo;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bicloo_detail);

		final Typeface robotoBold = FontUtils.getRobotoBoldCondensed(getApplicationContext());
		final Typeface robotoMedium = FontUtils.getRobotoMedium(getApplicationContext());
		final Typeface robotoLight = FontUtils.getRobotoLight(getApplicationContext());

		mBiclooBookmarkManager = BiclooBookmarkManager.getInstance();

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mTitle = (TextView) findViewById(R.id.itemTitle);
		mAdresse = (TextView) findViewById(R.id.itemAddress);
		mPaiement = (TextView) findViewById(R.id.stationPaiement);
		mBicloosDisponibles = (TextView) findViewById(R.id.bicloosDisponibles);
		mPlacesDisponibles = (TextView) findViewById(R.id.placesDisponibles);
		mItemDate = (TextView) findViewById(R.id.itemDate);
		mMessage = (TextView) findViewById(R.id.message);

		mTitle.setTypeface(robotoBold);
		mPaiement.setTypeface(robotoLight);
		mBicloosDisponibles.setTypeface(robotoLight);
		mPlacesDisponibles.setTypeface(robotoLight);
		mItemDate.setTypeface(robotoMedium);
		mAdresse.setTypeface(robotoMedium);

		mMapView = (MapView) findViewById(R.id.map_view);
		mMapView.setBuiltInZoomControls(true);
		mMapController = mMapView.getController();
		mMapController.setZoom(17);

		mBicloo = (Bicloo) getIntent().getParcelableExtra(PARAM_BICLOO);
		if (mBicloo != null) {
			loadParking(mBicloo);
		} else {
			throw new IllegalArgumentException("Un bicloo doit être renseigné.");
		}

	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		final MenuInflater menuInflater = getSupportMenuInflater();
		menuInflater.inflate(R.menu.activity_bicloo_detail, menu);

		final MenuItem menuFavori = menu.findItem(R.id.menu_favori);
		final int icon = isFavori() ? R.drawable.ic_action_important : R.drawable.ic_action_not_important;
		menuFavori.setIcon(icon);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu menu) {
		final MenuItem menuFavori = menu.findItem(R.id.menu_favori);

		final int icon = isFavori() ? R.drawable.ic_action_important : R.drawable.ic_action_not_important;
		menuFavori.setIcon(icon);

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case R.id.menu_navigation:
			startNavigation(mBicloo);
			break;
		case R.id.menu_share:
			shareComment(mBicloo);
			break;
		case R.id.menu_favori:
			onStarClick();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void loadParking(final Bicloo bicloo) {

		mTitle.setText(bicloo.getName());
		mAdresse.setText(bicloo.getAddress());

		mBicloosDisponibles.setText(String.valueOf(bicloo.getAvailableBike()));
		mPlacesDisponibles.setText(String.valueOf(bicloo.getAvailableBikeStands()));

		mPaiement.setText(bicloo.isBanking() ? R.string.bicloo_paiement_disponible
				: R.string.bicloo_paiement_indisponible);
		mItemDate.setText(DateUtils.getRelativeTimeSpanString(bicloo.getLastUpdate(), System.currentTimeMillis(),
				DateUtils.MINUTE_IN_MILLIS).toString());

		if (bicloo.getLocation() != null) {
			mMapController.animateTo(getGeoPoint(bicloo));

			final BiclooItemizedOverlay itemizedOverlay = new BiclooItemizedOverlay(getResources());
			final BasicOverlayItem biclooOverlayItem = new BasicOverlayItem(getGeoPoint(bicloo), bicloo.getName(), null);
			itemizedOverlay.addOverlay(biclooOverlayItem);
			itemizedOverlay.setFocus(biclooOverlayItem);

			mMapView.getOverlays().add(itemizedOverlay);
			mMapView.postInvalidate();
		} else {
			mMapView.setVisibility(View.GONE);
			mMessage.setVisibility(View.VISIBLE);
			mAdresse.setVisibility(View.GONE);
		}

	}

	/**
	 * Démarrer Google Navigation vers le bicloo.
	 */
	private void startNavigation(final Bicloo bicloo) {
		final Uri uri = Uri.parse(String.format(Locale.ENGLISH, NAVIGATION_INTENT, bicloo.getLocation().getLatitude(),
				bicloo.getLocation().getLongitude()));
		final Intent i = new Intent(Intent.ACTION_VIEW, uri);
		try {
			startActivity(i);
		} catch (final ActivityNotFoundException e) {
			Toast.makeText(getApplicationContext(), R.string.msg_error_navigation, Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Proposer de partager l'information
	 */
	private void shareComment(final Bicloo bicloo) {
		final Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, getBiclooInformation(bicloo));
		startActivity(Intent.createChooser(shareIntent, getString(R.string.action_share)));
	}

	@SuppressLint("NewApi")
	private void onStarClick() {
		if (isFavori()) {
			removeFromFavoris();
			Toast.makeText(this, R.string.toast_favori_retire, Toast.LENGTH_SHORT).show();
		} else {
			addToFavoris();
			Toast.makeText(this, R.string.toast_favori_ajout, Toast.LENGTH_SHORT).show();
		}

		invalidateOptionsMenu();
	}

	private String getBiclooInformation(final Bicloo bicloo) {
		final int availableBikes = bicloo.getAvailableBike();
		final int availableStands = bicloo.getAvailableBikeStands();
		final String bikes = getResources().getQuantityString(R.plurals.bicloo_velos_disponibles, availableBikes,
				availableBikes);
		final String stands = getResources().getQuantityString(R.plurals.bicloo_places_disponibles, availableStands,
				availableStands);

		final String description = getResources().getQuantityString(R.plurals.bicloo, availableBikes + availableStands,
				bikes, stands);

		final double latitude = bicloo.getLocation().getLatitude();
		final double longitude = bicloo.getLocation().getLongitude();

		final StringBuilder builder = new StringBuilder();
		builder.append(bicloo.getName()).append("\n").append(description).append("\n").append(bicloo.getAddress())
				.append("\n")
				.append(String.format(Locale.ENGLISH, ParkDetailActivity.SMS_NAVIGATION_URL, latitude, longitude));
		return builder.toString();
	}

	private boolean isFavori() {
		final Bicloo bicloo = mBiclooBookmarkManager.getSingle(getContentResolver(), mBicloo.getId());
		return (bicloo != null);
	}

	private void addToFavoris() {
		mBiclooBookmarkManager.add(getContentResolver(), mBicloo);
	}

	private void removeFromFavoris() {
		mBiclooBookmarkManager.remove(getContentResolver(), mBicloo.getId());
	}

	private GeoPoint getGeoPoint(final Bicloo bicloo) {
		return new GeoPoint((int) (bicloo.getLocation().getLatitude() * 1E6), (int) (bicloo.getLocation()
				.getLongitude() * 1E6));
	}
}
