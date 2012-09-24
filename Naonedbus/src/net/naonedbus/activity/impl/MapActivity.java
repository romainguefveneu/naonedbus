package net.naonedbus.activity.impl;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.activity.SlidingSherlockMapActivity;
import net.naonedbus.activity.map.layerloader.ItemSelectedInfo;
import net.naonedbus.activity.map.layerloader.MapLayer;
import net.naonedbus.activity.map.layerloader.impl.BiclooMapLayer;
import net.naonedbus.activity.map.layerloader.impl.CovoiturageMapLayer;
import net.naonedbus.activity.map.layerloader.impl.LilaMapLayer;
import net.naonedbus.activity.map.layerloader.impl.MargueriteMapLayer;
import net.naonedbus.activity.map.layerloader.impl.ParkingMapLayer;
import net.naonedbus.activity.map.layerloader.impl.StationMapLayer;
import net.naonedbus.activity.map.overlay.BasicItemizedOverlay;
import net.naonedbus.activity.map.overlay.BasicItemizedOverlay.OnBasicItemTapListener;
import net.naonedbus.activity.map.overlay.TypeOverlayItem;
import net.naonedbus.activity.map.overlay.item.BasicOverlayItem;
import net.naonedbus.bean.Equipement;
import net.naonedbus.bean.TypeEquipement;
import net.naonedbus.helper.SlidingMenuHelper;
import net.naonedbus.intent.IIntentParamKey;
import net.naonedbus.manager.impl.EquipementManager;
import net.naonedbus.manager.impl.TypeEquipementManager;
import net.naonedbus.provider.impl.MyLocationProvider;
import net.naonedbus.utils.DpiUtils;
import net.naonedbus.utils.GeoPointUtils;
import net.naonedbus.widget.BalloonOverlayView;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.bugsense.trace.BugSenseHandler;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

public class MapActivity extends SlidingSherlockMapActivity {

	private static final String LOG_TAG = MapActivity.class.getSimpleName();

	public static enum Param implements IIntentParamKey {
		itemId, itemType
	};

	private static final int ACTION_LAYERS = 0;
	private static final int ACTION_CENTER_MAP = 1;
	private static final int ACTION_SEARCH = 2;

	private static final String PREF_MAP_LAYER = "map.layer.";

	private static final Map<TypeOverlayItem, MapLayer> mapLayerLoaders = new LinkedHashMap<TypeOverlayItem, MapLayer>();
	static {
		mapLayerLoaders.put(TypeOverlayItem.TYPE_STATION, new StationMapLayer());
		mapLayerLoaders.put(TypeOverlayItem.TYPE_PARKING, new ParkingMapLayer());
		mapLayerLoaders.put(TypeOverlayItem.TYPE_BICLOO, new BiclooMapLayer());
		mapLayerLoaders.put(TypeOverlayItem.TYPE_MARGUERITE, new MargueriteMapLayer());
		mapLayerLoaders.put(TypeOverlayItem.TYPE_COVOITURAGE, new CovoiturageMapLayer());
		mapLayerLoaders.put(TypeOverlayItem.TYPE_LILA, new LilaMapLayer());
	}

	private final Map<TypeOverlayItem, BasicItemizedOverlay> mapOverlays = new HashMap<TypeOverlayItem, BasicItemizedOverlay>();

	private String[] optionsLabels;
	private boolean[] optionsValues;
	private int optionsSatellitePosition;

	private SharedPreferences preferences;

	private Integer selectedItemId;
	private TypeOverlayItem selectedItemType;

	private MyLocationProvider myLocationProvider;

	private LinearLayout loaderView;
	private MapController mapController;
	private MapView mapView;
	private LoadLayers loadLayersTask;
	private RefreshMoveableLayers refreshMoveableLayersTask;
	private MyLocationOverlay myLocOverlay;
	private BalloonOverlayView balloonOverlayView;

	private BasicOverlayItem selectedOverlayItem;

	private OnBasicItemTapListener onBasicItemTapListener = new OnBasicItemTapListener() {
		@Override
		public void onItemTap(BasicOverlayItem item) {

			final MapLayer mapLayer = mapLayerLoaders.get(item.getType());
			if (mapLayer != null) {

				unselectItems(item);

				if (item.equals(selectedOverlayItem) == false) {
					// Centrer la carte
					mapController.animateTo(item.getPoint());
				}

				// Afficher la description
				final ItemSelectedInfo info = mapLayer.getItemInfo(MapActivity.this, item);
				showBalloon(info);

				selectedOverlayItem = item;
			}

		}
	};

	/**
	 * Gestion du menu latéral.
	 */
	private SlidingMenuHelper slidingMenuHelper;

	public MapActivity() {
		this.slidingMenuHelper = new SlidingMenuHelper(this);
		this.myLocationProvider = NBApplication.getLocationProvider();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		setBehindContentView(R.layout.menu);
		slidingMenuHelper.setupActionBar(getSupportActionBar());
		slidingMenuHelper.setupSlidingMenu(getSlidingMenu());

		final Location location = myLocationProvider.getLastKnownLocation();

		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		balloonOverlayView = new BalloonOverlayView(this, DpiUtils.getDpiFromPx(getApplicationContext(), 35));

		loaderView = (LinearLayout) findViewById(R.id.loader);

		mapView = (MapView) findViewById(R.id.map_view);
		mapView.setBuiltInZoomControls(true);
		mapView.getOverlays().add(new MapTouchOverlay());

		myLocOverlay = new MyLocationOverlay(getApplicationContext(), mapView);
		myLocOverlay.enableMyLocation();
		mapView.getOverlays().add(myLocOverlay);

		mapController = mapView.getController();
		mapController.setZoom(17);

		setSelectedItem(getIntent());

		// Afficher si besoin l'information sur l'activation du GPS
		if (myLocationProvider.isProviderEnabled() == false) {
			showGpsDialog();

			if (selectedItemId == null) {
				// Si le GPS n'est pas activé, sélectionner par défaut la
				// station Commerce.
				final EquipementManager equipementManager = EquipementManager.getInstance();
				final Equipement commerce = equipementManager.getEquipementsByName(getContentResolver(),
						Equipement.Type.TYPE_ARRET, "COMMERCE").get(0);
				selectedItemId = commerce.getId();
				selectedItemType = TypeOverlayItem.TYPE_STATION;
			}

		} else if (selectedItemId == null) {
			if (location != null) {
				mapController.animateTo(GeoPointUtils.getGeoPoint(location));
			}
		}

		initOptions();
		loadLayers(location);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, ACTION_LAYERS, 0, "Calques").setIcon(R.drawable.ic_action_layers)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		if (myLocationProvider.isProviderEnabled() == true) {
			menu.add(0, ACTION_CENTER_MAP, 0, "Centrer").setIcon(R.drawable.ic_menu_compass)
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			toggle();
			break;
		case ACTION_LAYERS:
			showOptions();
			break;
		case ACTION_CENTER_MAP:
			final Location location = myLocationProvider.getLastKnownLocation();
			if (location != null) {
				mapController.animateTo(GeoPointUtils.getGeoPoint(location));
				refreshMoveableLayers(location);
			}
			break;
		case ACTION_SEARCH:
			onSearchRequested();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		slidingMenuHelper.onPostCreate(getIntent(), getSlidingMenu(), savedInstanceState);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		slidingMenuHelper.onWindowFocusChanged(hasFocus, getSlidingMenu());
	}

	@Override
	protected void onDestroy() {
		myLocOverlay.disableMyLocation();
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		myLocationProvider.stop();
		super.onStop();
	}

	@Override
	protected void onResume() {
		myLocationProvider.start();
		super.onResume();
	}

	/**
	 * Show the menu when menu button pressed.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			toggle();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public void onNewIntent(Intent intent) {
		setIntent(intent);
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			if (intent.getData() == null) {
				// Démarrer l'activitiée de recherche
				// final Intent newIntent = new Intent(MapActivity.this,
				// SearchActivity.class);
				// newIntent.fillIn(intent, Intent.FILL_IN_ACTION |
				// Intent.FILL_IN_DATA);
				// startActivity(newIntent);
			} else {
				// Afficher l'élément sélectionné
				changeSelectedItem(intent);
			}
		}
	}

	/**
	 * Définir les <code>selectedItemId</code> et <code>selectedItemType</code>
	 * en fonction des éléments fournis dans l'intent.
	 * 
	 * @param intent
	 */
	private void setSelectedItem(Intent intent) {
		final String queryAction = intent.getAction();

		if (Intent.ACTION_SEARCH.equals(queryAction)) {
			// Trouver l'élement recherché
			if (intent.getData() == null) {
				final String query = intent.getStringExtra(SearchManager.QUERY).trim();
				Toast.makeText(getApplicationContext(), "Aucun équipement ne correspond à \"" + query + "\"",
						Toast.LENGTH_LONG).show();
			} else {
				selectedItemId = Integer.valueOf(intent.getStringExtra(SearchManager.QUERY));
				selectedItemType = TypeOverlayItem.getById(Integer.valueOf(intent
						.getStringExtra(SearchManager.EXTRA_DATA_KEY)));
			}
		} else {
			// Définir l'élément sélectionné par défaut
			if (getIntent().hasExtra(Param.itemId.toString()) && getIntent().hasExtra(Param.itemType.toString())) {
				selectedItemId = (Integer) getIntent().getSerializableExtra(Param.itemId.toString());
				selectedItemType = TypeOverlayItem.getById((Integer) getIntent().getSerializableExtra(
						Param.itemType.toString()));
			}
		}

	}

	/**
	 * Changer les <code>selectedItemId</code> et <code>selectedItemType</code>
	 * et l'élement sélectionné sur la carte en fonction d'une nouvelle intent.
	 * 
	 * @param intent
	 */
	private void changeSelectedItem(Intent intent) {
		selectedItemId = Integer.valueOf(intent.getStringExtra(SearchManager.QUERY));
		selectedItemType = TypeOverlayItem
				.getById(Integer.valueOf(intent.getStringExtra(SearchManager.EXTRA_DATA_KEY)));

		selectedDefaultItem();
	}

	/**
	 * Sélectionné l'élément passé en extra d'Intent.
	 */
	private void selectedDefaultItem() {
		if (selectedItemType != null) {
			if (mapOverlays.containsKey(selectedItemType) == false) {
				// Charger le claque correspondant au type de l'élément.
				optionsValues[selectedItemType.getId()] = true;
				loadLayers(getMapCenterLocation());
			} else {
				final MapLayer mapLayer = mapLayerLoaders.get(selectedItemType);
				BasicItemizedOverlay mapOverlay = mapOverlays.get(selectedItemType);
				BasicOverlayItem item = mapOverlay.getItemById(selectedItemId);

				if (item == null) {
					// Si besoin, recharger le layer en précisant l'élément
					// recherché
					final MapLayer layerLoader = mapLayerLoaders.get(selectedItemType);
					mapOverlay = layerLoader.getOverlay(getApplicationContext(), selectedItemId);
					mapView.getOverlays().remove(mapOverlays.get(selectedItemType));
					mapView.getOverlays().add(mapOverlay);
					mapOverlays.put(selectedItemType, mapOverlay);

					item = mapOverlay.getItemById(selectedItemId);

					// Dernier recours si l'élément n'est toujours pas trouvé
					if (item == null) {
						Toast.makeText(getApplicationContext(), getString(R.string.msg_element_not_localized),
								Toast.LENGTH_LONG).show();
						return;
					}
				}
				// Centrer la carte
				mapController.animateTo(item.getPoint());

				// Afficher la description
				final ItemSelectedInfo info = mapLayer.getItemInfo(MapActivity.this, item);
				showBalloon(info);
				mapOverlay.setFocus(item);

				refreshMoveableLayers(GeoPointUtils.getLocation(info.getGeoPoint()));
			}
		}
	}

	/**
	 * Afficher la digalogue d'information sur l'activation du GPS.
	 */
	private void showGpsDialog() {
		// final AlertDialog dialog = InfoDialogUtils.getDialog(this,
		// R.string.information,
		// R.string.msg_location_disabled_full);
		//
		// dialog.setButton2("Services de localisation", new OnClickListener() {
		// @Override
		// public void onClick(DialogInterface dialog, int which) {
		// startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
		// }
		// });
		//
		// dialog.show();
	}

	/**
	 * Charger les options
	 */
	private void initOptions() {
		final TypeEquipementManager typeEquipementManager = TypeEquipementManager.getInstance();

		optionsLabels = new String[mapLayerLoaders.size() + 1];
		optionsValues = new boolean[optionsLabels.length];
		optionsSatellitePosition = optionsLabels.length - 1;

		// Satellite
		optionsLabels[optionsSatellitePosition] = getString(R.string.map_calque_satellite);
		optionsValues[optionsSatellitePosition] = (mapView.isSatellite() || preferences.getBoolean(
				NBApplication.PREF_MAP_SATELLITE, false));

		// Options
		int i = 0;
		for (final TypeOverlayItem typeOverlay : mapLayerLoaders.keySet()) {
			final TypeEquipement type = typeEquipementManager.getSingle(getContentResolver(), typeOverlay.getId());
			optionsLabels[i++] = type.nom;
			optionsValues[typeOverlay.getId()] = (mapOverlays.containsKey(typeOverlay) || isLayerPreferenceEnabled(typeOverlay
					.getId()));
		}
	}

	/**
	 * Indique si le calque est activé ou non dans les préférences.
	 * 
	 * @param id
	 * @return Vrai si le calque est activé.
	 */
	private boolean isLayerPreferenceEnabled(Integer id) {
		if (id == TypeOverlayItem.TYPE_STATION.getId()) {
			return preferences.getBoolean(PREF_MAP_LAYER + id, true);
		} else {
			return preferences.getBoolean(PREF_MAP_LAYER + id, false);
		}
	}

	/**
	 * Changer la valeur d'activation d'un calque.
	 * 
	 * @param id
	 * @param enabled
	 */
	private void setLayerPreference(final Integer id, final boolean enabled) {
		preferences.edit().putBoolean(PREF_MAP_LAYER + id, enabled).commit();
	}

	/**
	 * Afficher le menu d'options
	 */
	private void showOptions() {

		final AlertDialog dialog = new AlertDialog.Builder(this).setIcon(R.drawable.da_ic_dialog_menu_generic)
				.setTitle(R.string.map_calques)
				.setMultiChoiceItems(optionsLabels, optionsValues, new OnMultiChoiceClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					}
				}).setPositiveButton(android.R.string.ok, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Vue satellite ?
						if (mapView.isSatellite() != optionsValues[optionsSatellitePosition]) {
							mapView.setSatellite(optionsValues[optionsSatellitePosition]);
						}
						loadLayers(null);
					}
				}).setNegativeButton(android.R.string.cancel, null).create();
		dialog.show();
	}

	/**
	 * Calculer la position du centre de la map
	 * 
	 * @return Location du centre de la map
	 */
	private Location getMapCenterLocation() {
		final GeoPoint geoPoint = mapView.getMapCenter();
		final Location location = new Location(LocationManager.GPS_PROVIDER);
		location.setLatitude(geoPoint.getLatitudeE6() / 1E6);
		location.setLongitude(geoPoint.getLongitudeE6() / 1E6);
		return location;
	}

	/**
	 * Afficher l'information de l'élément sélectionné.
	 * 
	 * @param itemSelectedInfo
	 */
	private void showBalloon(ItemSelectedInfo info) {

		balloonOverlayView.setData(info);
		balloonOverlayView.setVisibility(View.VISIBLE);
		mapView.removeView(balloonOverlayView);
		mapView.addView(balloonOverlayView, new MapView.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT, info.getGeoPoint(), MapView.LayoutParams.BOTTOM_CENTER));

	}

	/**
	 * Masquer l'infobulle et déselectionner l'élément courant.
	 */
	private void hideBalloon() {
		mapView.removeView(balloonOverlayView);
		unselectItems(null);
		selectedOverlayItem = null;
		selectedItemId = null;
		selectedItemType = null;
	}

	/**
	 * Déselectionner les éléments sauf celui passé en paramètre.
	 * 
	 * @param current
	 *            L'élément dont la sélection doit être conservée. Peut être
	 *            null.
	 */
	private void unselectItems(BasicOverlayItem current) {
		for (final Overlay overlay : mapView.getOverlays()) {
			if (overlay instanceof BasicItemizedOverlay) {
				final BasicItemizedOverlay basicOverlay = (BasicItemizedOverlay) overlay;
				if (current == null || current.getType().equals(basicOverlay.getType()) == false) {
					basicOverlay.resetFocus();
				}
			}
		}
	}

	/**
	 * Charger les nouveaux calques et effacer les anciens.
	 */
	private void loadLayers(final Location location) {
		if (loadLayersTask != null) {
			loadLayersTask.cancel(true);
		}
		loadLayersTask = (LoadLayers) new LoadLayers().execute(location);
	}

	/**
	 * @return Vrai si la tache de chargement est en cours.
	 */
	private boolean isLayersLoading() {
		return (loadLayersTask != null && loadLayersTask.getStatus() != AsyncTask.Status.FINISHED && loadLayersTask
				.getStatus() == AsyncTask.Status.FINISHED);
	}

	private class LoaderInfo {
		TypeOverlayItem type;
		BasicItemizedOverlay overlay;
	}

	/**
	 * Chargement des calques.
	 * 
	 * @author romain.guefveneu
	 */
	private class LoadLayers extends AsyncTask<Location, LoaderInfo, Void> {

		@Override
		protected void onPreExecute() {
			loaderView.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Location... params) {

			try {

				final Location location = (params[0] == null) ? getMapCenterLocation() : params[0];
				BasicItemizedOverlay overlay = null;
				MapLayer layerLoader = null;
				TypeOverlayItem type = null;

				// Gestion des calques
				for (int typeId = 0; typeId < optionsValues.length - 1; typeId++) {

					type = TypeOverlayItem.getById(typeId);
					layerLoader = mapLayerLoaders.get(type);

					if (optionsValues[typeId]) {
						if (selectedItemId == null || !selectedItemType.equals(type)) {
							overlay = layerLoader.getOverlay(MapActivity.this, location);
						} else {
							overlay = layerLoader.getOverlay(MapActivity.this, selectedItemId);
						}
						overlay.setOnBasicItemTapListener(onBasicItemTapListener);
					}

					final LoaderInfo info = new LoaderInfo();
					info.type = type;
					info.overlay = overlay;
					publishProgress(info);

				}

			} catch (Exception e) {
				BugSenseHandler.sendException(e);
				Log.e(LOG_TAG, "Erreur lors du chargement du calque.", e);
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(LoaderInfo... values) {
			super.onProgressUpdate(values);
			TypeOverlayItem type;
			BasicItemizedOverlay overlay;

			for (LoaderInfo loaderInfo : values) {
				type = loaderInfo.type;
				overlay = loaderInfo.overlay;

				// Changer les préférences
				setLayerPreference(type.getId(), optionsValues[type.getId()]);

				if (mapOverlays.containsKey(type) && optionsValues[type.getId()] == false) {
					// Effacer les couches
					mapView.getOverlays().remove(mapOverlays.get(type));
					mapOverlays.remove(type);
				} else if (mapOverlays.containsKey(type) == false && optionsValues[type.getId()]) {
					// Redessiner les couches
					mapOverlays.put(type, overlay);
					mapView.getOverlays().add(overlay);
				}
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mapView.postInvalidate();
			loaderView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_out));
			loaderView.setVisibility(View.GONE);

			// Masquer la description si l'élément sélectionné ne fait plus
			// partie de la carte
			if (selectedOverlayItem != null && !mapOverlays.containsKey(selectedOverlayItem.getType())) {
				mapView.removeView(balloonOverlayView);
			} else {
				selectedDefaultItem();
			}
		}
	}

	/**
	 * Charger les nouveaux calques et effacer les anciens.
	 */
	private void refreshMoveableLayers(final Location location) {
		if (refreshMoveableLayersTask != null) {
			refreshMoveableLayersTask.cancel(true);
		}
		refreshMoveableLayersTask = (RefreshMoveableLayers) new RefreshMoveableLayers().execute(location);
	}

	/**
	 * Classe de rafraichissement des couches gérant la position.
	 * 
	 * @author romain
	 * 
	 */
	private class RefreshMoveableLayers extends AsyncTask<Location, LoaderInfo, Void> {

		@Override
		protected Void doInBackground(Location... params) {
			Thread.currentThread().setPriority(Thread.NORM_PRIORITY);

			try {

				final Location currentLocation = (params[0] == null) ? getMapCenterLocation() : params[0];
				if (currentLocation != null) {
					for (Entry<TypeOverlayItem, BasicItemizedOverlay> entry : mapOverlays.entrySet()) {
						final TypeOverlayItem type = entry.getKey();
						final MapLayer layerLoader = mapLayerLoaders.get(type);

						if (layerLoader.isMoveable()) {

							final BasicItemizedOverlay overlay = layerLoader.getOverlay(MapActivity.this,
									currentLocation);
							overlay.setOnBasicItemTapListener(onBasicItemTapListener);

							final LoaderInfo info = new LoaderInfo();
							info.type = type;
							info.overlay = overlay;
							publishProgress(info);

						}
					}
				}

			} catch (Exception e) {
				BugSenseHandler.sendException(e);
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(LoaderInfo... values) {
			super.onProgressUpdate(values);
			TypeOverlayItem type;
			BasicItemizedOverlay overlay;

			for (LoaderInfo loaderInfo : values) {
				type = loaderInfo.type;
				overlay = loaderInfo.overlay;

				mapView.getOverlays().remove(mapOverlays.get(type));
				mapView.getOverlays().add(overlay);
				mapOverlays.put(type, overlay);

				// Gérer la sélection
				if (selectedOverlayItem != null && selectedOverlayItem.getType().equals(overlay.getType())) {
					if (overlay.getItemById(selectedOverlayItem.getId()) == null) {
						balloonOverlayView.setVisibility(View.GONE);
					} else {
						balloonOverlayView.setVisibility(View.VISIBLE);
						overlay.setFocus(selectedOverlayItem);
					}
				}
			}
			mapView.postInvalidate();
		}
	}

	/**
	 * Classe de gestion du déplacement
	 * 
	 * @author romain
	 */
	private class MapTouchOverlay extends com.google.android.maps.Overlay {
		private Location lastLocation = new Location(LocationManager.GPS_PROVIDER);

		@Override
		public boolean onTouchEvent(MotionEvent event, MapView mapView) {
			if (event.getAction() == MotionEvent.ACTION_UP) {
				final Location currentLocation = getMapCenterLocation();
				final float distance = currentLocation.distanceTo(lastLocation);
				lastLocation = currentLocation;

				if (distance > 300 && isLayersLoading() == false) {
					refreshMoveableLayers(null);
				}
			}
			return false;
		}

		@Override
		public boolean onTap(GeoPoint p, MapView mapView) {
			hideBalloon();
			return super.onTap(p, mapView);
		}
	}
}
