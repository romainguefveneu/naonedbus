package net.naonedbus.fragment.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.bean.Equipement;
import net.naonedbus.intent.IIntentParamKey;
import net.naonedbus.loader.MapLoader;
import net.naonedbus.loader.MapLoader.MapLoaderCallback;
import net.naonedbus.map.MarkerInfo;
import net.naonedbus.map.ToastedMarkerOptionsChooser;
import net.naonedbus.provider.impl.MyLocationProvider;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.twotoasters.clusterkraf.Clusterkraf;
import com.twotoasters.clusterkraf.InputPoint;
import com.twotoasters.clusterkraf.MarkerOptionsChooser;

public class MapFragment extends SherlockFragment implements MapLoaderCallback {

	private static final String LOG_TAG = "MapActivity";

	public static enum Param implements IIntentParamKey {
		itemId, itemType
	};

	private static final int MENU_GROUP_TYPES = 1;
	private static final int MENU_ID_SATELLITE = Integer.MAX_VALUE;
	private static final String PREF_MAP_LAYER = "map.layer.";

	final Map<Equipement.Type, List<InputPoint>> mInputPoints = new HashMap<Equipement.Type, List<InputPoint>>();
	final com.twotoasters.clusterkraf.Options mOptions = new com.twotoasters.clusterkraf.Options();

	private SharedPreferences mPreferences;
	private SupportMapFragment mSupportMapFragment;
	private GoogleMap mGoogleMap;
	private Clusterkraf mClusterkraf;
	private MapLoader mMapLoader;

	private final Set<Equipement.Type> mSelectedTypes = new HashSet<Equipement.Type>();

	private final MyLocationProvider mLocationProvider;

	public MapFragment() {
		mLocationProvider = NBApplication.getLocationProvider();
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {
		if (container == null) // must put this in
			return null;

		final View view = inflater.inflate(R.layout.fragment_map, container,
				false);

		mSupportMapFragment = (SupportMapFragment) getFragmentManager()
				.findFragmentById(R.id.map);
		mGoogleMap = mSupportMapFragment.getMap();
		mGoogleMap.setMyLocationEnabled(true);

		mPreferences = PreferenceManager
				.getDefaultSharedPreferences(getActivity());

		final Equipement.Type[] types = Equipement.Type.values();
		for (final Equipement.Type type : types) {
			if (isLayerPreferenceEnabled(type.getId())) {
				mSelectedTypes.add(type);
			}
		}

		initMap();
		initClusterkraf();

		return view;
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.activity_map, menu);

		// if (mLocationProvider.isProviderEnabled() == false) {
		// menu.findItem(R.id.menu_location).setVisible(false);
		// }

		final SubMenu filterSubMenu = menu.findItem(R.id.menu_layers)
				.getSubMenu();
		final Equipement.Type[] types = Equipement.Type.values();
		for (final Equipement.Type type : types) {
			final MenuItem item = filterSubMenu.add(MENU_GROUP_TYPES,
					type.getId(), 0, type.getTitleRes());
			item.setCheckable(true);
		}

		// final MenuItem item = filterSubMenu.add(0, MENU_ID_SATELLITE, 0,
		// R.string.map_calque_satellite);
		// item.setCheckable(true);
		// item.setChecked(mMapView.isSatellite());
	}

	@Override
	public void onPrepareOptionsMenu(final Menu menu) {
		final Equipement.Type[] types = Equipement.Type.values();
		final SubMenu filterSubMenu = menu.findItem(R.id.menu_layers)
				.getSubMenu();

		for (final Equipement.Type type : types) {
			final MenuItem item = filterSubMenu.findItem(type.getId());
			item.setChecked(mSelectedTypes.contains(type));
		}

	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (item.getGroupId() == MENU_GROUP_TYPES) {
			final Equipement.Type type = Equipement.Type.getTypeById(item
					.getItemId());

			item.setChecked(!item.isChecked());

			setLayerPreference(type.getId(), item.isChecked());

			if (item.isChecked()) {
				mSelectedTypes.add(type);
				loadMarkers(type);
			} else {
				mSelectedTypes.remove(type);
				removeMarkers(type);
			}

		} else {

		}
		return super.onOptionsItemSelected(item);
	}

	private void initMap() {
		final UiSettings uiSettings = mGoogleMap.getUiSettings();
		uiSettings.setScrollGesturesEnabled(true);
		uiSettings.setZoomGesturesEnabled(true);
		uiSettings.setCompassEnabled(true);

		final Location currenLocation = mLocationProvider
				.getLastKnownLocation();
		if (currenLocation != null) {
			final CameraUpdate cameraUpdate = CameraUpdateFactory
					.newLatLngZoom(new LatLng(currenLocation.getLatitude(),
							currenLocation.getLongitude()), 15);
			mGoogleMap.animateCamera(cameraUpdate);
		}
	}

	private void initClusterkraf() {
		if (mGoogleMap != null) {
			final MarkerOptionsChooser markerOptionsChooser = new ToastedMarkerOptionsChooser(
					getActivity());
			mOptions.setMarkerOptionsChooser(markerOptionsChooser);
			mOptions.setPixelDistanceToJoinCluster(100);

			// customize the options before you construct a Clusterkraf instance
			mClusterkraf = new Clusterkraf(mGoogleMap, mOptions, null);

			loadMarkers();
		}
	}

	private void loadMarkers() {
		synchronized (mClusterkraf) {
			mClusterkraf.clear();
		}

		final Equipement.Type[] types = mSelectedTypes
				.toArray(new Equipement.Type[mSelectedTypes.size()]);
		mMapLoader = new MapLoader(getActivity(), this);
		mMapLoader.execute(types);
	}

	private void loadMarkers(Equipement.Type type) {
		mMapLoader = new MapLoader(getActivity(), this);
		mMapLoader.execute(type);
	}

	private void removeMarkers(Equipement.Type type) {
		List<InputPoint> inputPoints = mInputPoints.get(type);
		mClusterkraf.removeAll(inputPoints);
	}

	/**
	 * Indique si le calque est activé ou non dans les préférences.
	 * 
	 * @param id
	 * @return Vrai si le calque est activé.
	 */
	private boolean isLayerPreferenceEnabled(final Integer id) {
		if (id == Equipement.Type.TYPE_ARRET.getId()) {
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

	@Override
	public void onLayerLoaded(final ArrayList<InputPoint> result) {
		if (result != null && !result.isEmpty()) {
			MarkerInfo markerInfo = (MarkerInfo) result.get(0).getTag();
			mInputPoints.put(markerInfo.getType(), result);

			synchronized (mClusterkraf) {
				mClusterkraf.addAll(result);
			}
		}
	}

}
