package net.naonedbus.fragment.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.naonedbus.BuildConfig;
import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.bean.Bicloo;
import net.naonedbus.bean.Equipement;
import net.naonedbus.bean.Equipement.Type;
import net.naonedbus.bean.parking.Parking;
import net.naonedbus.bean.parking.pub.ParkingPublic;
import net.naonedbus.bean.parking.relai.ParkingRelai;
import net.naonedbus.loader.MapLoader;
import net.naonedbus.loader.MapLoader.MapLoaderCallback;
import net.naonedbus.manager.impl.EquipementManager;
import net.naonedbus.map.ToastedMarkerOptionsChooser;
import net.naonedbus.map.layer.BiclooMapLayer;
import net.naonedbus.map.layer.EquipementMapLayer;
import net.naonedbus.map.layer.ParkingMapLayer;
import net.naonedbus.map.layer.ProxyInfoWindowAdapter;
import net.naonedbus.provider.impl.MyLocationProvider;
import net.naonedbus.widget.adapter.impl.EquipementCursorAdapter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.location.Location;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnSuggestionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.twotoasters.clusterkraf.Clusterkraf;
import com.twotoasters.clusterkraf.InputPoint;

public class MapFragment extends SherlockFragment implements MapLoaderCallback, OnSuggestionListener {

	private static String LOG_TAG = "MapFragment";
	private static boolean DBG = BuildConfig.DEBUG;

	private static final int MENU_GROUP_TYPES = 1;
	private static final String PREF_MAP_LAYER = "map.layer.";

	final Map<Class<?>, Equipement.Type> mTypeMap = new HashMap<Class<?>, Equipement.Type>();
	final Map<Equipement.Type, List<InputPoint>> mInputPoints = new HashMap<Equipement.Type, List<InputPoint>>();
	final com.twotoasters.clusterkraf.Options mOptions = new com.twotoasters.clusterkraf.Options();

	private SharedPreferences mPreferences;
	private SupportMapFragment mSupportMapFragment;
	private GoogleMap mGoogleMap;
	private Clusterkraf mClusterkraf;
	private MapLoader mMapLoader;
	private MenuItem mSearchMenuItem;
	private MenuItem mRefreshMenuItem;

	private EquipementCursorAdapter mSearchAdapter;

	private final Set<Equipement.Type> mSelectedTypes = new HashSet<Equipement.Type>();

	private final MyLocationProvider mLocationProvider;

	public MapFragment() {
		mLocationProvider = NBApplication.getLocationProvider();

		mTypeMap.put(Bicloo.class, Type.TYPE_BICLOO);
		mTypeMap.put(Parking.class, Type.TYPE_PARKING);
		mTypeMap.put(ParkingPublic.class, Type.TYPE_PARKING);
		mTypeMap.put(ParkingRelai.class, Type.TYPE_PARKING);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		if (container == null) // must put this in
			return null;

		final View view = inflater.inflate(R.layout.fragment_map, container, false);

		mSupportMapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.map);
		mGoogleMap = mSupportMapFragment.getMap();

		mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

		final Equipement.Type[] types = Equipement.Type.values();
		for (final Equipement.Type type : types) {
			if (isLayerPreferenceEnabled(type.getId())) {
				mSelectedTypes.add(type);
			}
		}

		initMap();
		initClusterkraf(inflater);

		return view;
	}

	@Override
	public void onDestroyView() {
		if (isResumed()) {
			final Fragment fragment = (getFragmentManager().findFragmentById(R.id.map));
			final FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
			ft.remove(fragment);
			ft.commit();
		}
		super.onDestroyView();
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.fragment_map, menu);
		mRefreshMenuItem = menu.findItem(R.id.menu_refresh);
		mRefreshMenuItem.setVisible(mMapLoader != null && mMapLoader.getStatus() != Status.FINISHED);

		if (DBG)
			Log.d(LOG_TAG, "onCreateOptionsMenu " + mRefreshMenuItem.isVisible() + " "
					+ (mMapLoader == null ? null : mMapLoader.getStatus()));

		final EquipementManager manager = EquipementManager.getInstance();
		final Cursor cursor = manager.getCursor(getActivity().getContentResolver());
		mSearchAdapter = new EquipementCursorAdapter(getActivity(), cursor);

		mSearchMenuItem = (MenuItem) menu.findItem(R.id.menu_search);
		final SearchView searchView = (SearchView) mSearchMenuItem.getActionView();
		searchView.setSuggestionsAdapter(mSearchAdapter);
		searchView.setOnSuggestionListener(this);

		final SubMenu filterSubMenu = menu.findItem(R.id.menu_layers).getSubMenu();
		final Equipement.Type[] types = Equipement.Type.values();
		for (final Equipement.Type type : types) {
			final MenuItem item = filterSubMenu.add(MENU_GROUP_TYPES, type.getId(), 0, type.getTitleRes());
			item.setCheckable(true);
		}
	}

	@Override
	public void onPrepareOptionsMenu(final Menu menu) {
		final Equipement.Type[] types = Equipement.Type.values();
		final SubMenu filterSubMenu = menu.findItem(R.id.menu_layers).getSubMenu();

		for (final Equipement.Type type : types) {
			final MenuItem item = filterSubMenu.findItem(type.getId());
			item.setChecked(mSelectedTypes.contains(type));
		}
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (item.getGroupId() == MENU_GROUP_TYPES) {
			final Equipement.Type type = Equipement.Type.getTypeById(item.getItemId());

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
			if (item.getItemId() == R.id.menu_satellite) {
				item.setChecked(!item.isChecked());
				if (item.isChecked()) {
					mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
				} else {
					mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
				}
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onSuggestionSelect(final int position) {
		onSearchItemClick(position);
		return true;
	}

	@Override
	public boolean onSuggestionClick(final int position) {
		onSearchItemClick(position);
		return true;
	}

	private void initMap() {
		final UiSettings uiSettings = mGoogleMap.getUiSettings();
		uiSettings.setScrollGesturesEnabled(true);
		uiSettings.setZoomGesturesEnabled(true);
		uiSettings.setCompassEnabled(true);

		mGoogleMap.setMyLocationEnabled(true);

		final Location currenLocation = mLocationProvider.getLastKnownLocation();
		if (currenLocation != null) {
			final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(
					currenLocation.getLatitude(), currenLocation.getLongitude()), 15);
			mGoogleMap.animateCamera(cameraUpdate);
		}
	}

	private void initClusterkraf(final LayoutInflater inflater) {
		if (mGoogleMap != null) {
			final ToastedMarkerOptionsChooser markerOptionsChooser = new ToastedMarkerOptionsChooser(getActivity());
			markerOptionsChooser.setDefaultMapLayer(new EquipementMapLayer(inflater));
			markerOptionsChooser.registerMapLayer(Type.TYPE_BICLOO, new BiclooMapLayer(inflater));
			markerOptionsChooser.registerMapLayer(Type.TYPE_PARKING, new ParkingMapLayer(inflater));

			ProxyInfoWindowAdapter infoWindowAdapter = new ProxyInfoWindowAdapter(getActivity());
			infoWindowAdapter.setDefaultMapLayer(new EquipementMapLayer(inflater));
			infoWindowAdapter.registerMapLayer(Type.TYPE_BICLOO, new BiclooMapLayer(inflater));
			infoWindowAdapter.registerMapLayer(Type.TYPE_PARKING, new ParkingMapLayer(inflater));

			mOptions.setPixelDistanceToJoinCluster(80);
			mOptions.setMarkerOptionsChooser(markerOptionsChooser);
			mOptions.setOnInfoWindowClickDownstreamListener(infoWindowAdapter);

			// customize the options before you construct a Clusterkraf instance
			mClusterkraf = new Clusterkraf(mGoogleMap, mOptions, null);
			mClusterkraf.setClusterkrafInfoWindowAdapter(infoWindowAdapter);

			loadMarkers();
		}
	}

	private void onSearchItemClick(final int position) {
		final EquipementManager manager = EquipementManager.getInstance();
		final CursorWrapper cursorWrapper = (CursorWrapper) mSearchAdapter.getItem(position);
		final Equipement equipement = manager.getSingleFromCursorWrapper(cursorWrapper);
		selectEquipement(equipement);

		mSearchMenuItem.collapseActionView();
	}

	private void selectEquipement(final Equipement equipement) {
		final InputPoint inputPoint = findInputPoint(equipement);

		Log.d(LOG_TAG, "selectEquipement " + equipement + " = " + inputPoint);

		if (inputPoint != null) {
			mClusterkraf.showInfoWindow(inputPoint);
		} else {

		}

	}

	private InputPoint findInputPoint(final Equipement equipement) {
		final List<InputPoint> inputPoints = mInputPoints.get(equipement.getType());

		if (inputPoints != null) {
			for (final InputPoint inputPoint : inputPoints) {
				final Equipement item = (Equipement) inputPoint.getTag();
				if (item.getNormalizedNom().equals(equipement.getNormalizedNom())) {
					return inputPoint;
				}
			}
		}
		return null;
	}

	private void loadMarkers() {
		synchronized (mClusterkraf) {
			mClusterkraf.clear();
		}

		if (DBG)
			Log.d(LOG_TAG, "loadMarkers " + mSelectedTypes);

		final Equipement.Type[] types = mSelectedTypes.toArray(new Equipement.Type[mSelectedTypes.size()]);
		mMapLoader = new MapLoader(getActivity(), this);
		mMapLoader.execute(types);
	}

	private void loadMarkers(final Equipement.Type type) {
		mMapLoader = new MapLoader(getActivity(), this);
		mMapLoader.execute(type);
	}

	private void removeMarkers(final Equipement.Type type) {
		final List<InputPoint> inputPoints = mInputPoints.get(type);
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

			Type type;
			Object tag = result.get(0).getTag();
			if (mTypeMap.containsKey(tag.getClass())) {
				type = mTypeMap.get(tag.getClass());
			} else {
				final Equipement markerInfo = (Equipement) tag;
				type = markerInfo.getType();
			}

			if (DBG)
				Log.d(LOG_TAG, "onLayerLoaded " + type);

			mInputPoints.put(type, result);

			synchronized (mClusterkraf) {
				mClusterkraf.addAll(result);
			}
		}
	}

	@Override
	public void onMapLoaderStart() {
		if (DBG)
			Log.d(LOG_TAG, "onMapLoaderStart " + mRefreshMenuItem);

		if (mRefreshMenuItem != null) {
			mRefreshMenuItem.setVisible(true);
		}
	}

	@Override
	public void onMapLoaderEnd() {
		if (DBG)
			Log.d(LOG_TAG, "onMapLoaderEnd " + mRefreshMenuItem);

		if (mRefreshMenuItem != null) {
			mRefreshMenuItem.setVisible(false);
		}
	}

}
