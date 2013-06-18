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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.naonedbus.R;
import net.naonedbus.activity.map.overlay.ParkingItemizedOverlay;
import net.naonedbus.activity.map.overlay.item.BasicOverlayItem;
import net.naonedbus.bean.parking.Parking;
import net.naonedbus.bean.parking.pub.ParkingPublic;
import net.naonedbus.bean.parking.pub.ParkingPublicStatut;
import net.naonedbus.bean.parking.relai.ParkingRelai;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.utils.FontUtils;
import net.naonedbus.utils.ParkingUtils;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
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
import com.ocpsoft.pretty.time.PrettyTime;

public class ParkingDetailActivity extends SherlockMapActivity {

	public static final String PARAM_PARKING = "parking";

	protected static final String NAVIGATION_INTENT = "google.navigation:q=%f,%f";
	protected static final String SMS_NAVIGATION_URL = "maps.google.com/?q=%f,%f";
	protected static final PrettyTime PRETTY_TIME = new PrettyTime(Locale.getDefault());

	private final Map<Class<?>, ParkingDetailAdapter> mAadapterMap = new HashMap<Class<?>, ParkingDetailAdapter>();

	protected TextView mParkingTitle;
	protected TextView mParkingAdresse;
	protected TextView mParkingDescription;
	protected TextView mPlacesDisponibles;
	protected TextView mPlacesTotales;
	protected TextView mItemDate;
	protected TextView mItemTelephone;
	protected TextView mMessage;
	protected MapView mMapView;
	protected MapController mMapController;

	private Parking mParking;

	public ParkingDetailActivity() {
		mAadapterMap.put(ParkingRelai.class, new ParkingRelaiDetailAdapter());
		mAadapterMap.put(ParkingPublic.class, new ParkingPublicDetailAdapter());
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_parking_detail);

		final Typeface robotoBold = FontUtils.getRobotoBoldCondensed(getApplicationContext());
		final Typeface robotoMedium = FontUtils.getRobotoMedium(getApplicationContext());
		final Typeface robotoLight = FontUtils.getRobotoLight(getApplicationContext());

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mItemTelephone = (TextView) findViewById(R.id.itemTelephone);

		mParkingTitle = (TextView) findViewById(R.id.itemTitle);
		mParkingDescription = (TextView) findViewById(R.id.itemDescription);
		mParkingAdresse = (TextView) findViewById(R.id.itemAddress);
		mPlacesDisponibles = (TextView) findViewById(R.id.placesDisponibles);
		mPlacesTotales = (TextView) findViewById(R.id.placesTotales);
		mItemDate = (TextView) findViewById(R.id.itemDate);
		mMessage = (TextView) findViewById(R.id.message);

		mParkingTitle.setTypeface(robotoBold);
		mPlacesDisponibles.setTypeface(robotoLight);
		mPlacesTotales.setTypeface(robotoLight);
		mItemDate.setTypeface(robotoMedium);
		mItemTelephone.setTypeface(robotoLight);
		mParkingAdresse.setTypeface(robotoMedium);

		mMapView = (MapView) findViewById(R.id.map_view);
		mMapView.setBuiltInZoomControls(true);
		mMapController = mMapView.getController();
		mMapController.setZoom(17);

		mParking = (Parking) getIntent().getSerializableExtra(PARAM_PARKING);
		if (mParking != null) {
			loadParking(mParking);
		} else {
			throw new IllegalArgumentException("Un parking doit être renseigné.");
		}

	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		final MenuInflater menuInflater = getSupportMenuInflater();
		menuInflater.inflate(R.menu.activity_parking_detail, menu);
		menu.findItem(R.id.menu_phone).setVisible(
				mParking.getTelephone() != null && mParking.getTelephone().length() != 0);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case R.id.menu_navigation:
			startNavigation(mParking);
			break;
		case R.id.menu_share:
			shareComment(mParking);
			break;
		case R.id.menu_phone:
			final Uri telUri = Uri.parse("tel:" + mParking.getTelephone());
			startActivity(new Intent(Intent.ACTION_DIAL, telUri));
		}
		return super.onOptionsItemSelected(item);
	}

	private void loadParking(final Parking parking) {

		mParkingTitle.setText(parking.getNom());
		mParkingAdresse.setText(parking.getAdresse());

		if (parking.getTelephone() == null || parking.getTelephone().length() == 0) {
			mItemTelephone.setVisibility(View.GONE);
			findViewById(R.id.itemTelephoneLabel).setVisibility(View.GONE);
		} else {
			mItemTelephone.setText(parking.getTelephone());
		}

		if (parking.getLatitude() != null) {
			mMapController.animateTo(getGeoPoint(parking));

			final ParkingItemizedOverlay itemizedOverlay = new ParkingItemizedOverlay(getResources());
			final BasicOverlayItem parkingOverlayItem = new BasicOverlayItem(getGeoPoint(parking), parking.getNom(),
					null);
			itemizedOverlay.addOverlay(parkingOverlayItem);
			itemizedOverlay.setFocus(parkingOverlayItem);

			mMapView.getOverlays().add(itemizedOverlay);
			mMapView.postInvalidate();
		} else {
			mMapView.setVisibility(View.GONE);
			mMessage.setVisibility(View.VISIBLE);
			mParkingAdresse.setVisibility(View.GONE);
		}

		if (mAadapterMap.containsKey(parking.getClass())) {
			mAadapterMap.get(parking.getClass()).setObject(parking, this);
		}
	}

	/**
	 * Démarrer Google Navigation vers le parking
	 * 
	 * @param parking
	 */
	private void startNavigation(final Parking parking) {
		final Uri uri = Uri.parse(String.format(Locale.ENGLISH, NAVIGATION_INTENT, parking.getLatitude(),
				parking.getLongitude()));
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
	private void shareComment(final Parking parking) {
		final Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, getParkingInformation(parking));
		startActivity(Intent.createChooser(shareIntent, getString(R.string.action_share)));
	}

	/**
	 * Récupérer un texte descriptif du parking
	 * 
	 * @param parking
	 * @return
	 */
	private String getParkingInformation(final Parking parking) {
		if (mAadapterMap.containsKey(parking.getClass())) {
			final ParkingDetailAdapter adapter = mAadapterMap.get(parking.getClass());
			return adapter.getParkingInformation(this, parking);
		}
		return "";
	}

	private GeoPoint getGeoPoint(final Parking parking) {
		return new GeoPoint((int) (parking.getLatitude() * 1E6), (int) (parking.getLongitude() * 1E6));
	}

}

/**
 * Adapter pour les différents types de parkings.
 * 
 * @author romain
 * 
 */
interface ParkingDetailAdapter {
	/**
	 * Renseigner les différents champs de l'application en fonction du type de
	 * parking.
	 * 
	 * @param parking
	 * @param activity
	 */
	void setObject(Parking parking, ParkingDetailActivity activity);

	/**
	 * Créer le détail d'un parking.
	 * 
	 * @param parking
	 * @return Les détails du parking sous forme de chaîne.
	 */
	String getParkingInformation(Context context, Parking parking);
}

/**
 * Adapter pour le type <code>ParkingPublic</code>.
 * 
 * @author romain
 * 
 */
class ParkingPublicDetailAdapter implements ParkingDetailAdapter {

	@Override
	public void setObject(final Parking p, final ParkingDetailActivity activity) {
		final ParkingPublic parking = (ParkingPublic) p;

		int couleur;
		String detail;

		if (parking.getStatut() == ParkingPublicStatut.OUVERT) {
			final int placesDisponibles = parking.getPlacesDisponibles();
			couleur = activity.getResources().getColor(ParkingUtils.getSeuilCouleurId(placesDisponibles));
			detail = activity.getString(ParkingUtils.getSeuilTextId(placesDisponibles));
		} else {
			detail = activity.getString(parking.getStatut().getTitleRes());
			couleur = activity.getResources().getColor(parking.getStatut().getColorRes());
		}

		activity.mParkingDescription.setText(detail);
		activity.mParkingDescription.setBackgroundDrawable(ColorUtils.getRoundedGradiant(couleur));

		if (parking.getStatut().equals(ParkingPublicStatut.INVALIDE)
				|| parking.getStatut().equals(ParkingPublicStatut.ABONNES)) {
			activity.mPlacesDisponibles.setText("\u2026");
			activity.mPlacesTotales.setText("\u2026");
		} else {
			activity.mPlacesDisponibles.setText(String.valueOf(parking.getPlacesDisponibles()));
			activity.mPlacesTotales.setText(String.valueOf(parking.getPlacesTotales()));
		}

		activity.mItemDate.setText(ParkingDetailActivity.PRETTY_TIME.format(parking.getUpdateDate()));
	}

	@Override
	public String getParkingInformation(final Context context, final Parking p) {
		final ParkingPublic parking = (ParkingPublic) p;

		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder
				.append(context.getString(R.string.parking))
				.append(" ")
				.append(parking.getNom())
				.append("\n")
				.append(context.getString(R.string.parking_titre_places_disponibles))
				.append(" ")
				.append(parking.getPlacesDisponibles())
				.append("\n")
				.append(context.getString(R.string.parking_titre_places_totales))
				.append(" ")
				.append(parking.getPlacesTotales())
				.append("\n")
				.append(context.getString(R.string.parking_titre_mise_a_jour))
				.append(" ")
				.append(parking.getHorodatage())
				.append("\n")
				.append(String.format(Locale.ENGLISH, ParkingDetailActivity.SMS_NAVIGATION_URL, parking.getLatitude(),
						parking.getLongitude()));

		return stringBuilder.toString();
	}
}

class ParkingRelaiDetailAdapter implements ParkingDetailAdapter {

	@Override
	public void setObject(final Parking p, final ParkingDetailActivity activity) {
		final Context context = activity.getApplicationContext();

		activity.mParkingDescription.setText("\u2022 " + activity.getString(R.string.parking_relai));
		activity.mParkingDescription.setBackgroundDrawable(ColorUtils.getRoundedGradiant(context.getResources()
				.getColor(R.color.parking_state_blue)));
	}

	@Override
	public String getParkingInformation(final Context context, final Parking p) {
		final ParkingRelai parking = (ParkingRelai) p;

		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder
				.append(context.getString(R.string.parking))
				.append(" ")
				.append(parking.getNom())
				.append("\n")
				.append(context.getString(R.string.parking_titre_places_disponibles))
				.append("\n")
				.append(context.getString(R.string.parking_titre_mise_a_jour))
				.append("\n")
				.append(String.format(Locale.ENGLISH, ParkingDetailActivity.SMS_NAVIGATION_URL, parking.getLatitude(),
						parking.getLongitude()));

		return stringBuilder.toString();
	}

}
