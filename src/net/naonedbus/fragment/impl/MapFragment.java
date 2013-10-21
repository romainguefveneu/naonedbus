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
import net.naonedbus.activity.impl.MapActivity;
import net.naonedbus.bean.Equipment;
import net.naonedbus.bean.Equipment.Type;
import net.naonedbus.loader.MapLoader;
import net.naonedbus.loader.MapLoader.MapLoaderCallback;
import net.naonedbus.manager.impl.EquipmentManager;
import net.naonedbus.map.ToastedMarkerOptionsChooser;
import net.naonedbus.map.layer.BiclooMapLayer;
import net.naonedbus.map.layer.EquipementMapLayer;
import net.naonedbus.map.layer.ParkingMapLayer;
import net.naonedbus.map.layer.ProxyInfoWindowAdapter;
import net.naonedbus.provider.impl.MyLocationProvider;
import net.naonedbus.widget.adapter.impl.EquipmentCursorAdapter;
import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnSuggestionListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.twotoasters.clusterkraf.Clusterkraf;
import com.twotoasters.clusterkraf.InputPoint;

public class MapFragment extends SherlockFragment implements MapLoaderCallback, OnSuggestionListener {

	public static final String PARAM_ITEM_ID = "itemId";
	public static final String PARAM_ITEM_TYPE = "itemType";

	private static String LOG_TAG = "MapFragment";
	private static boolean DBG = BuildConfig.DEBUG;

	private static final int MENU_GROUP_TYPES = 1;
	private static final String PREF_MAP_LAYER = "map.layer.";

	private final Map<Equipment.Type, List<InputPoint>> mInputPoints = new HashMap<Equipment.Type, List<InputPoint>>();
	private final com.twotoasters.clusterkraf.Options mOptions = new com.twotoasters.clusterkraf.Options();

	private final Set<Equipment.Type> mSelectedTypes = new HashSet<Equipment.Type>();
	private final MyLocationProvider mLocationProvider;

	private SharedPreferences mPreferences;
	private SupportMapFragment mSupportMapFragment;
	private GoogleMap mGoogleMap;
	private Clusterkraf mClusterkraf;
	private MapLoader mMapLoader;
	private MenuItem mSearchMenuItem;
	private MenuItem mRefreshMenuItem;

	private EquipmentCursorAdapter mSearchAdapter;

	private Equipment mLastSearchedItem;
	private Integer mParamItemId;
	private Integer mParamItemType;

	private boolean mGooglePlayServiceAvailable;
	private boolean mIsMapActivity;

	public MapFragment() {
		mLocationProvider = NBApplication.getLocationProvider();
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setRetainInstance(true);
		setHasOptionsMenu(true);

		if (getArguments() != null && getArguments().containsKey(PARAM_ITEM_ID)) {
			mParamItemId = getArguments().getInt(PARAM_ITEM_ID);
			mParamItemType = getArguments().getInt(PARAM_ITEM_TYPE);
			mSelectedTypes.add(Equipment.Type.getTypeById(mParamItemType));
		}
	}

	@Override
	public void onAttach(final Activity activity) {
		super.onAttach(activity);
		mIsMapActivity = activity instanceof MapActivity;
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		if (container == null) // must put this in
			return null;

		final View view = inflater.inflate(R.layout.fragment_map, container, false);

		mSupportMapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.map);
		mGoogleMap = mSupportMapFragment.getMap();

		mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

		final Equipment.Type[] types = Equipment.Type.values();
		for (final Equipment.Type type : types) {
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
		if (!mIsMapActivity) {
			final Fragment fragment = (getFragmentManager().findFragmentById(R.id.map));
			if (fragment != null) {
				final FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
				ft.remove(fragment);
				ft.commitAllowingStateLoss();
			}
		}
		super.onDestroyView();
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		if (mGooglePlayServiceAvailable) {
			inflater.inflate(R.menu.fragment_map, menu);
			mRefreshMenuItem = menu.findItem(R.id.menu_refresh);

			final EquipmentManager manager = EquipmentManager.getInstance();
			final Cursor cursor = manager.getCursor(getActivity().getContentResolver());
			mSearchAdapter = new EquipmentCursorAdapter(getActivity(), cursor);

			mSearchMenuItem = menu.findItem(R.id.menu_search);
			final SearchView searchView = (SearchView) mSearchMenuItem.getActionView();
			searchView.setSuggestionsAdapter(mSearchAdapter);
			searchView.setOnSuggestionListener(this);

			final SubMenu filterSubMenu = menu.findItem(R.id.menu_layers).getSubMenu();
			final Equipment.Type[] types = Equipment.Type.values();
			for (final Equipment.Type type : types) {
				final MenuItem item = filterSubMenu.add(MENU_GROUP_TYPES, type.getId(), 0, type.getTitleRes());
				item.setCheckable(true);
			}
		}
	}

	@Override
	public void onPrepareOptionsMenu(final Menu menu) {
		if (mGooglePlayServiceAvailable) {
			final Equipment.Type[] types = Equipment.Type.values();
			final SubMenu filterSubMenu = menu.findItem(R.id.menu_layers).getSubMenu();

			for (final Equipment.Type type : types) {
				final MenuItem item = filterSubMenu.findItem(type.getId());
				item.setChecked(mSelectedTypes.contains(type));
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (item.getGroupId() == MENU_GROUP_TYPES) {
			final Equipment.Type type = Equipment.Type.getTypeById(item.getItemId());

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
		final int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
		if (resultCode == ConnectionResult.SUCCESS) {
			mGooglePlayServiceAvailable = true;

			final UiSettings uiSettings = mGoogleMap.getUiSettings();
			uiSettings.setScrollGesturesEnabled(true);
			uiSettings.setZoomGesturesEnabled(true);
			uiSettings.setCompassEnabled(true);

			mGoogleMap.setMyLocationEnabled(true);

			final Location currenLocation = mLocationProvider.getLastKnownLocation();
			if (currenLocation != null) {
				final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
						new LatLng(currenLocation.getLatitude(), currenLocation.getLongitude()), 15);
				mGoogleMap.animateCamera(cameraUpdate);
			}
		} else {
			mGooglePlayServiceAvailable = false;

			GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(), 1).show();
			if (mRefreshMenuItem != null) {
				mRefreshMenuItem.setVisible(false);
			}
		}
	}

	private void initClusterkraf(final LayoutInflater inflater) {
		if (mGoogleMap != null) {
			final ToastedMarkerOptionsChooser markerOptionsChooser = new ToastedMarkerOptionsChooser(getActivity());
			markerOptionsChooser.setDefaultMapLayer(new EquipementMapLayer(inflater));
			markerOptionsChooser.registerMapLayer(Type.TYPE_BICLOO, new BiclooMapLayer(inflater));
			markerOptionsChooser.registerMapLayer(Type.TYPE_PARK, new ParkingMapLayer(inflater));

			final ProxyInfoWindowAdapter infoWindowAdapter = new ProxyInfoWindowAdapter(getActivity());
			infoWindowAdapter.setDefaultMapLayer(new EquipementMapLayer(inflater));
			infoWindowAdapter.registerMapLayer(Type.TYPE_BICLOO, new BiclooMapLayer(inflater));
			infoWindowAdapter.registerMapLayer(Type.TYPE_PARK, new ParkingMapLayer(inflater));

			mOptions.setPixelDistanceToJoinCluster(60);
			mOptions.setZoomToBoundsPadding(30);
			mOptions.setMarkerOptionsChooser(markerOptionsChooser);
			mOptions.setOnInfoWindowClickDownstreamListener(infoWindowAdapter);

			// customize the options before you construct a Clusterkraf instance
			mClusterkraf = new Clusterkraf(mGoogleMap, mOptions, null);
			mClusterkraf.setClusterkrafInfoWindowAdapter(infoWindowAdapter);

			loadMarkers();
		}
	}

	private void onSearchItemClick(final int position) {
		final EquipmentManager manager = EquipmentManager.getInstance();
		final CursorWrapper cursorWrapper = (CursorWrapper) mSearchAdapter.getItem(position);
		final Equipment equipment = manager.getSingleFromCursorWrapper(cursorWrapper);
		selectEquipement(equipment);

		mSearchMenuItem.collapseActionView();
	}

	private void selectEquipement(final Equipment equipment) {
		final Type type = equipment.getType();

		if (mInputPoints.get(type) == null) {
			mLastSearchedItem = equipment;
			mSelectedTypes.add(type);
			setLayerPreference(type.getId(), true);
			loadMarkers();
		} else {
			final InputPoint inputPoint = findInputPoint(equipment);

			if (inputPoint != null) {
				mClusterkraf.showInfoWindow(inputPoint);
			} else {
				Toast.makeText(getActivity(), getString(R.string.msg_element_not_localized), Toast.LENGTH_LONG).show();
			}
		}
	}

	private InputPoint findInputPoint(final Equipment equipment) {
		final List<InputPoint> inputPoints = mInputPoints.get(equipment.getType());

		if (inputPoints != null) {
			for (final InputPoint inputPoint : inputPoints) {
				final Equipment item = (Equipment) inputPoint.getTag();
				if (item.getNormalizedName().equals(equipment.getNormalizedName())) {
					return inputPoint;
				}
			}
		}
		return null;
	}

	private InputPoint findInputPoint(final Type itemType, final int itemId) {
		final List<InputPoint> inputPoints = mInputPoints.get(itemType);

		if (inputPoints != null) {
			for (final InputPoint inputPoint : inputPoints) {
				final Equipment item = (Equipment) inputPoint.getTag();
				if (item.getId() == itemId) {
					return inputPoint;
				}
			}
		}
		return null;
	}

	private void loadMarkers() {
		if (DBG)
			Log.d(LOG_TAG, "loadMarkers " + mSelectedTypes);

		synchronized (mClusterkraf) {
			mClusterkraf.clear();
		}

		final Equipment.Type[] types = mSelectedTypes.toArray(new Equipment.Type[mSelectedTypes.size()]);
		mMapLoader = new MapLoader(getActivity(), this);
		mMapLoader.execute(types);
	}

	private void loadMarkers(final Equipment.Type type) {
		mMapLoader = new MapLoader(getActivity(), this);
		mMapLoader.execute(type);
	}

	private void removeMarkers(final Equipment.Type type) {
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
		if (id == Equipment.Type.TYPE_STOP.getId()) {
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

			final Equipment tag = (Equipment) result.get(0).getTag();
			final Type type = tag.getType();

			if (DBG)
				Log.d(LOG_TAG, "onLayerLoaded " + type);

			mInputPoints.put(type, result);

			synchronized (mClusterkraf) {
				mClusterkraf.addAll(result);
			}

			if (mParamItemType != null && type.getId() == mParamItemType) {
				final InputPoint inputPoint = findInputPoint(Type.getTypeById(mParamItemType), mParamItemId);
				if (inputPoint != null) {
					mClusterkraf.showInfoWindow(inputPoint);
				}
				mParamItemType = null;
				mParamItemId = null;
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

		if (mLastSearchedItem != null) {
			selectEquipement(mLastSearchedItem);
			mLastSearchedItem = null;
		}
	}

}
