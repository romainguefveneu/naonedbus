package net.naonedbus.activity.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.naonedbus.NBApplication;
import net.naonedbus.R;
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
import net.naonedbus.bean.Equipement.Type;
import net.naonedbus.helper.SlidingMenuHelper;
import net.naonedbus.intent.IIntentParamKey;
import net.naonedbus.manager.impl.EquipementManager;
import net.naonedbus.provider.impl.MyLocationProvider;
import net.naonedbus.utils.DpiUtils;
import net.naonedbus.utils.GeoPointUtils;
import net.naonedbus.widget.BalloonOverlayView;
import net.simonvt.menudrawer.MenuDrawer;
import android.app.SearchManager;
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

import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.bugsense.trace.BugSenseHandler;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

public class MapActivity extends SherlockMapActivity {

	private static final String LOG_TAG = "MapActivity";

	public static enum Param implements IIntentParamKey {
		itemId, itemType
	};

	private static final int MENU_GROUP_TYPES = 1;
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

	private Set<Equipement.Type> mSelectedLayers;
	private int mOptionsSatellitePosition;

	private SharedPreferences mPreferences;

	private Integer mSelectedItemId;
	private TypeOverlayItem mSelectedItemType;

	/**
	 * Gestion du menu latéral.
	 */
	private MenuDrawer mMenuDrawer;
	private SlidingMenuHelper mSlidingMenuHelper;

	private MyLocationProvider myLocationProvider;

	private LinearLayout mLoaderView;
	private MapController mMapController;
	private MapView mMapView;
	private LoadLayers mLoadLayersTask;
	private RefreshMoveableLayers mRefreshMoveableLayersTask;
	private MyLocationOverlay mLocationOverlay;
	private BalloonOverlayView mBalloonOverlayView;

	private BasicOverlayItem mSelectedOverlayItem;

	private OnBasicItemTapListener mOnBasicItemTapListener = new OnBasicItemTapListener() {
		@Override
		public void onItemTap(BasicOverlayItem item) {

			final MapLayer mapLayer = mapLayerLoaders.get(item.getType());
			if (mapLayer != null) {

				unselectItems(item);

				if (item.equals(mSelectedOverlayItem) == false) {
					// Centrer la carte
					mMapController.animateTo(item.getPoint());
				}

				// Afficher la description
				final ItemSelectedInfo info = mapLayer.getItemInfo(MapActivity.this, item);
				showBalloon(info);

				mSelectedOverlayItem = item;
			}

		}
	};

	public MapActivity() {
		this.myLocationProvider = NBApplication.getLocationProvider();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(NBApplication.THEMES_MENU_RES[NBApplication.THEME]);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);

		mMenuDrawer = MenuDrawer.attach(this, MenuDrawer.MENU_DRAG_WINDOW);

		mSlidingMenuHelper = new SlidingMenuHelper(this);
		mSlidingMenuHelper.setupActionBar(getSupportActionBar());
		mSlidingMenuHelper.setupSlidingMenu(mMenuDrawer);

		final Location location = myLocationProvider.getLastKnownLocation();

		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		mBalloonOverlayView = new BalloonOverlayView(this, DpiUtils.getDpiFromPx(getApplicationContext(), 35));

		mLoaderView = (LinearLayout) findViewById(R.id.loader);

		mMapView = (MapView) findViewById(R.id.map_view);
		mMapView.setBuiltInZoomControls(true);
		mMapView.getOverlays().add(new MapTouchOverlay());

		mLocationOverlay = new MyLocationOverlay(getApplicationContext(), mMapView);
		mLocationOverlay.enableMyLocation();
		mMapView.getOverlays().add(mLocationOverlay);

		mMapController = mMapView.getController();
		mMapController.setZoom(17);

		setSelectedItem(getIntent());

		// Afficher si besoin l'information sur l'activation du GPS
		if (myLocationProvider.isProviderEnabled() == false) {
			showGpsDialog();

			if (mSelectedItemId == null) {
				// Si le GPS n'est pas activé, sélectionner par défaut la
				// station Commerce.
				final EquipementManager equipementManager = EquipementManager.getInstance();
				final Equipement commerce = equipementManager.getEquipementsByName(getContentResolver(),
						Equipement.Type.TYPE_ARRET, "COMMERCE").get(0);
				mSelectedItemId = commerce.getId();
				mSelectedItemType = TypeOverlayItem.TYPE_STATION;
			}

		} else if (mSelectedItemId == null) {
			if (location != null) {
				mMapController.animateTo(GeoPointUtils.getGeoPoint(location));
			}
		}

		mSelectedLayers = new HashSet<Equipement.Type>();
		final Equipement.Type[] types = Equipement.Type.values();
		for (Equipement.Type type : types) {
			if (isLayerPreferenceEnabled(type.getId())) {
				mSelectedLayers.add(type);
			}
		}

		loadLayers(location);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		final MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.activity_map, menu);

		if (myLocationProvider.isProviderEnabled() == false) {
			menu.findItem(R.id.menu_location).setVisible(false);
		}

		final SubMenu filterSubMenu = menu.findItem(R.id.menu_layers).getSubMenu();
		final Equipement.Type[] types = Equipement.Type.values();
		for (Equipement.Type type : types) {
			final MenuItem item = filterSubMenu.add(MENU_GROUP_TYPES, type.getId(), 0, type.getTitleRes());
			item.setCheckable(true);
			item.setChecked(mSelectedLayers.contains(type));
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getGroupId() == MENU_GROUP_TYPES) {

			final Equipement.Type type = Equipement.Type.getTypeById(item.getItemId());

			item.setChecked(!item.isChecked());
			setLayerPreference(type.getId(), item.isChecked());

			if (item.isChecked()) {
				mSelectedLayers.add(type);
			} else {
				mSelectedLayers.remove(type);
			}

			loadLayers(null);

		} else {

			switch (item.getItemId()) {
			case android.R.id.home:
				mMenuDrawer.toggleMenu();
				break;
			case R.id.menu_location:
				final Location location = myLocationProvider.getLastKnownLocation();
				if (location != null) {
					mMapController.animateTo(GeoPointUtils.getGeoPoint(location));
					refreshMoveableLayers(location);
				}
				break;
			case R.id.menu_search:
				onSearchRequested();
				break;
			default:
				break;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mSlidingMenuHelper.onPostCreate(getIntent(), mMenuDrawer, savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mSlidingMenuHelper.onSaveInstanceState(outState);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		mSlidingMenuHelper.onWindowFocusChanged(hasFocus, mMenuDrawer);
	}

	@Override
	protected void onDestroy() {
		mLocationOverlay.disableMyLocation();
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
	 * Show the menu when menu button pressed, hide it when back is pressed
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU || (mMenuDrawer.isMenuVisible() && keyCode == KeyEvent.KEYCODE_BACK)) {
			mMenuDrawer.toggleMenu();
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
				mSelectedItemId = Integer.valueOf(intent.getStringExtra(SearchManager.QUERY));
				mSelectedItemType = TypeOverlayItem.getById(Integer.valueOf(intent
						.getStringExtra(SearchManager.EXTRA_DATA_KEY)));
			}
		} else {
			// Définir l'élément sélectionné par défaut
			if (getIntent().hasExtra(Param.itemId.toString()) && getIntent().hasExtra(Param.itemType.toString())) {
				mSelectedItemId = (Integer) getIntent().getSerializableExtra(Param.itemId.toString());
				mSelectedItemType = TypeOverlayItem.getById((Integer) getIntent().getSerializableExtra(
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
		mSelectedItemId = Integer.valueOf(intent.getStringExtra(SearchManager.QUERY));
		mSelectedItemType = TypeOverlayItem
				.getById(Integer.valueOf(intent.getStringExtra(SearchManager.EXTRA_DATA_KEY)));

		selectedDefaultItem();
	}

	/**
	 * Sélectionné l'élément passé en extra d'Intent.
	 */
	private void selectedDefaultItem() {
		if (mSelectedItemType != null) {
			if (mapOverlays.containsKey(mSelectedItemType) == false) {
				// Charger le claque correspondant au type de l'élément.
				mSelectedLayers.add(Type.getTypeById(mSelectedItemType.getId()));
				loadLayers(getMapCenterLocation());
			} else {
				final MapLayer mapLayer = mapLayerLoaders.get(mSelectedItemType);
				BasicItemizedOverlay mapOverlay = mapOverlays.get(mSelectedItemType);
				BasicOverlayItem item = mapOverlay.getItemById(mSelectedItemId);

				if (item == null) {
					// Si besoin, recharger le layer en précisant l'élément
					// recherché
					final MapLayer layerLoader = mapLayerLoaders.get(mSelectedItemType);
					mapOverlay = layerLoader.getOverlay(getApplicationContext(), mSelectedItemId);
					mMapView.getOverlays().remove(mapOverlays.get(mSelectedItemType));
					mMapView.getOverlays().add(mapOverlay);
					mapOverlays.put(mSelectedItemType, mapOverlay);

					item = mapOverlay.getItemById(mSelectedItemId);

					// Dernier recours si l'élément n'est toujours pas trouvé
					if (item == null) {
						Toast.makeText(getApplicationContext(), getString(R.string.msg_element_not_localized),
								Toast.LENGTH_LONG).show();
						return;
					}
				}
				// Centrer la carte
				mMapController.animateTo(item.getPoint());

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
	 * Indique si le calque est activé ou non dans les préférences.
	 * 
	 * @param id
	 * @return Vrai si le calque est activé.
	 */
	private boolean isLayerPreferenceEnabled(Integer id) {
		if (id == TypeOverlayItem.TYPE_STATION.getId()) {
			return mPreferences.getBoolean(PREF_MAP_LAYER + id, true);
		} else {
			return mPreferences.getBoolean(PREF_MAP_LAYER + id, false);
		}
	}

	/**
	 * Changer la valeur d'activation d'un calque.
	 * 
	 * @param id
	 * @param enabled
	 */
	private void setLayerPreference(final Integer id, final boolean enabled) {
		mPreferences.edit().putBoolean(PREF_MAP_LAYER + id, enabled).commit();
	}

	/**
	 * Calculer la position du centre de la map
	 * 
	 * @return Location du centre de la map
	 */
	private Location getMapCenterLocation() {
		final GeoPoint geoPoint = mMapView.getMapCenter();
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

		mBalloonOverlayView.setData(info);
		mBalloonOverlayView.setVisibility(View.VISIBLE);
		mMapView.removeView(mBalloonOverlayView);
		mMapView.addView(mBalloonOverlayView, new MapView.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT, info.getGeoPoint(), MapView.LayoutParams.BOTTOM_CENTER));

	}

	/**
	 * Masquer l'infobulle et déselectionner l'élément courant.
	 */
	private void hideBalloon() {
		mMapView.removeView(mBalloonOverlayView);
		unselectItems(null);
		mSelectedOverlayItem = null;
		mSelectedItemId = null;
		mSelectedItemType = null;
	}

	/**
	 * Déselectionner les éléments sauf celui passé en paramètre.
	 * 
	 * @param current
	 *            L'élément dont la sélection doit être conservée. Peut être
	 *            null.
	 */
	private void unselectItems(BasicOverlayItem current) {
		for (final Overlay overlay : mMapView.getOverlays()) {
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
		if (mLoadLayersTask != null) {
			mLoadLayersTask.cancel(true);
		}
		mLoadLayersTask = (LoadLayers) new LoadLayers().execute(location);
	}

	/**
	 * @return Vrai si la tache de chargement est en cours.
	 */
	private boolean isLayersLoading() {
		return (mLoadLayersTask != null && mLoadLayersTask.getStatus() != AsyncTask.Status.FINISHED && mLoadLayersTask
				.getStatus() == AsyncTask.Status.FINISHED);
	}

	private class LoaderInfo {
		Type layerType;
		TypeOverlayItem overlayType;
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
			mLoaderView.setVisibility(View.VISIBLE);
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
				for (Type layerType : Type.values()) {

					type = TypeOverlayItem.getById(layerType.getId());
					layerLoader = mapLayerLoaders.get(type);

					if (mSelectedLayers.contains(layerType)) {
						if (mSelectedItemId == null || !mSelectedItemType.equals(type)) {
							overlay = layerLoader.getOverlay(MapActivity.this, location);
						} else {
							overlay = layerLoader.getOverlay(MapActivity.this, mSelectedItemId);
						}
						overlay.setOnBasicItemTapListener(mOnBasicItemTapListener);
					}

					final LoaderInfo info = new LoaderInfo();
					info.layerType = layerType;
					info.overlayType = type;
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
			Type layerType;
			TypeOverlayItem overlayType;
			BasicItemizedOverlay overlay;

			for (LoaderInfo loaderInfo : values) {
				layerType = loaderInfo.layerType;
				overlayType = loaderInfo.overlayType;
				overlay = loaderInfo.overlay;

				// Changer les préférences
				setLayerPreference(overlayType.getId(), mSelectedLayers.contains(layerType));

				if (mapOverlays.containsKey(overlayType) && !mSelectedLayers.contains(layerType)) {
					// Effacer les couches
					mMapView.getOverlays().remove(mapOverlays.get(overlayType));
					mapOverlays.remove(overlayType);
				} else if (mapOverlays.containsKey(overlayType) == false && mSelectedLayers.contains(layerType)) {
					// Redessiner les couches
					mapOverlays.put(overlayType, overlay);
					mMapView.getOverlays().add(overlay);
				}
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mMapView.postInvalidate();
			mLoaderView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_out));
			mLoaderView.setVisibility(View.GONE);

			// Masquer la description si l'élément sélectionné ne fait plus
			// partie de la carte
			if (mSelectedOverlayItem != null && !mapOverlays.containsKey(mSelectedOverlayItem.getType())) {
				mMapView.removeView(mBalloonOverlayView);
			} else {
				selectedDefaultItem();
			}
		}
	}

	/**
	 * Charger les nouveaux calques et effacer les anciens.
	 */
	private void refreshMoveableLayers(final Location location) {
		if (mRefreshMoveableLayersTask != null) {
			mRefreshMoveableLayersTask.cancel(true);
		}
		mRefreshMoveableLayersTask = (RefreshMoveableLayers) new RefreshMoveableLayers().execute(location);
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
							overlay.setOnBasicItemTapListener(mOnBasicItemTapListener);

							final LoaderInfo info = new LoaderInfo();
							info.overlayType = type;
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
				type = loaderInfo.overlayType;
				overlay = loaderInfo.overlay;

				mMapView.getOverlays().remove(mapOverlays.get(type));
				mMapView.getOverlays().add(overlay);
				mapOverlays.put(type, overlay);

				// Gérer la sélection
				if (mSelectedOverlayItem != null && mSelectedOverlayItem.getType().equals(overlay.getType())) {
					if (overlay.getItemById(mSelectedOverlayItem.getId()) == null) {
						mBalloonOverlayView.setVisibility(View.GONE);
					} else {
						mBalloonOverlayView.setVisibility(View.VISIBLE);
						overlay.setFocus(mSelectedOverlayItem);
					}
				}
			}
			mMapView.postInvalidate();
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
