/**
 *  Copyright (C) 2011 Romain Guefveneu
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

import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.activity.map.overlay.ParkingItemizedOverlay;
import net.naonedbus.activity.map.overlay.item.BasicOverlayItem;
import net.naonedbus.bean.parking.Parking;
import net.naonedbus.bean.parking.pub.ParkingPublic;
import net.naonedbus.bean.parking.pub.ParkingPublicStatut;
import net.naonedbus.bean.parking.relai.ParkingRelai;
import net.naonedbus.helper.SlidingMenuHelper;
import net.naonedbus.intent.IIntentParamKey;
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

/**
 * @author romain.guefveneu
 * 
 */
public class ParkingDetailActivity extends SherlockMapActivity {

	public static enum Param implements IIntentParamKey {
		parking, parkingRelai
	};

	private static final Map<Class<?>, ParkingDetailAdapter> adapterMap = new HashMap<Class<?>, ParkingDetailAdapter>();
	static {
		adapterMap.put(ParkingRelai.class, new ParkingRelaiDetailAdapter());
		adapterMap.put(ParkingPublic.class, new ParkingPublicDetailAdapter());
	}

	protected static final String NAVIGATION_INTENT = "google.navigation:q=%f,%f";
	protected static final String SMS_NAVIGATION_URL = "maps.google.com/?q=%f,%f";
	protected static final PrettyTime PRETTY_TIME = new PrettyTime(Locale.FRANCE);

	private SlidingMenuHelper slidingMenuHelper;

	protected TextView parkingTitle;
	protected TextView parkingDescription;
	protected TextView placesDisponibles;
	protected TextView placesTotales;
	protected TextView majDate;
	protected TextView itemTelephone;
	protected TextView message;
	protected MapView mapView;
	protected MapController mapController;

	private Parking parking;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(NBApplication.THEME);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_parking_detail);

		final Typeface robotoBold = FontUtils.getRobotoBoldCondensed(getApplicationContext());
		final Typeface robotoLight = FontUtils.getRobotoLight(getApplicationContext());

		slidingMenuHelper = new SlidingMenuHelper(this);
		slidingMenuHelper.setupActionBar(getSupportActionBar());

		getSupportActionBar().setIcon(R.drawable.ic_launcher);

		itemTelephone = (TextView) findViewById(R.id.itemTelephone);

		parkingTitle = (TextView) findViewById(R.id.itemTitle);
		parkingDescription = (TextView) findViewById(R.id.itemDescription);
		placesDisponibles = (TextView) findViewById(R.id.placesDisponibles);
		placesTotales = (TextView) findViewById(R.id.placesTotales);
		majDate = (TextView) findViewById(R.id.majDate);
		message = (TextView) findViewById(R.id.message);

		parkingTitle.setTypeface(robotoBold);
		placesDisponibles.setTypeface(robotoLight);
		placesTotales.setTypeface(robotoLight);
		majDate.setTypeface(robotoLight);
		itemTelephone.setTypeface(robotoLight);

		mapView = (MapView) findViewById(R.id.map_view);
		mapView.setBuiltInZoomControls(true);
		mapController = mapView.getController();
		mapController.setZoom(17);

		parking = (Parking) getIntent().getSerializableExtra(Param.parking.toString());
		if (parking != null) {
			loadParking(parking);
		} else {
			throw new IllegalArgumentException("Un parking doit être renseigné.");
		}

	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		final MenuInflater menuInflater = getSupportMenuInflater();
		menuInflater.inflate(R.menu.activity_parking_detail, menu);
		menu.findItem(R.id.menu_phone).setVisible(
				parking.getTelephone() != null && parking.getTelephone().length() != 0);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case R.id.menu_navigation:
			startNavigation(parking);
			break;
		case R.id.menu_share:
			shareComment(parking);
			break;
		case R.id.menu_phone:
			final Uri telUri = Uri.parse("tel:" + parking.getTelephone());
			startActivity(new Intent(Intent.ACTION_DIAL, telUri));
		}
		return super.onOptionsItemSelected(item);
	}

	private void loadParking(final Parking parking) {

		parkingTitle.setText(parking.getNom());

		if (parking.getTelephone() == null || parking.getTelephone().length() == 0) {
			itemTelephone.setText(R.string.msg_nothing_telephone);
		} else {
			itemTelephone.setText(parking.getTelephone());
		}

		if (parking.getLatitude() != null) {
			mapController.animateTo(getGeoPoint(parking));

			final ParkingItemizedOverlay itemizedOverlay = new ParkingItemizedOverlay(getResources());
			final BasicOverlayItem parkingOverlayItem = new BasicOverlayItem(getGeoPoint(parking), parking.getNom(),
					null);
			itemizedOverlay.addOverlay(parkingOverlayItem);
			itemizedOverlay.setFocus(parkingOverlayItem);

			mapView.getOverlays().add(itemizedOverlay);
			mapView.postInvalidate();
		} else {
			mapView.setVisibility(View.GONE);
			message.setVisibility(View.VISIBLE);
		}

		if (adapterMap.containsKey(parking.getClass())) {
			adapterMap.get(parking.getClass()).setObject(parking, this);
		}
	}

	/**
	 * Démarrer Google Navigation vers le parking
	 * 
	 * @param parking
	 */
	private void startNavigation(Parking parking) {
		final Uri uri = Uri.parse(String.format(Locale.ENGLISH, NAVIGATION_INTENT, parking.getLatitude(),
				parking.getLongitude()));
		final Intent i = new Intent(Intent.ACTION_VIEW, uri);
		try {
			startActivity(i);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(getApplicationContext(), R.string.msg_error_navigation, Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Proposer de partager l'information
	 */
	private void shareComment(Parking parking) {
		Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
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
	private String getParkingInformation(Parking parking) {
		if (adapterMap.containsKey(parking.getClass())) {
			final ParkingDetailAdapter adapter = adapterMap.get(parking.getClass());
			return adapter.getParkingInformation(this, parking);
		}
		return "";
	}

	private GeoPoint getGeoPoint(Parking parking) {
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
	public void setObject(Parking p, ParkingDetailActivity activity) {
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

		activity.parkingDescription.setText(detail);
		activity.parkingDescription.setBackgroundDrawable(ColorUtils.getRoundedGradiant(couleur));

		if (parking.getStatut().equals(ParkingPublicStatut.INVALIDE)
				|| parking.getStatut().equals(ParkingPublicStatut.ABONNES)) {
			activity.placesDisponibles.setText("\u2026");
			activity.placesTotales.setText("\u2026");
		} else {
			activity.placesDisponibles.setText(String.valueOf(parking.getPlacesDisponibles()));
			activity.placesTotales.setText(String.valueOf(parking.getPlacesTotales()));
		}

		activity.majDate.setText(ParkingDetailActivity.PRETTY_TIME.format(parking.getUpdateDate()));
	}

	@Override
	public String getParkingInformation(Context context, Parking p) {
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
	public void setObject(Parking p, ParkingDetailActivity activity) {
		final Context context = activity.getApplicationContext();

		activity.parkingDescription.setText("\u2022 " + activity.getString(R.string.parking_relai));
		activity.parkingDescription.setBackgroundDrawable(ColorUtils.getRoundedGradiant(context.getResources()
				.getColor(R.color.parking_state_blue)));
	}

	@Override
	public String getParkingInformation(Context context, Parking p) {
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
