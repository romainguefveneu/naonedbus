package net.naonedbus.fragment.impl;

import java.util.ArrayList;

import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.bean.Equipement.Type;
import net.naonedbus.intent.IIntentParamKey;
import net.naonedbus.loader.MapLoader;
import net.naonedbus.loader.MapLoader.MapLoaderCallback;
import net.naonedbus.map.ToastedMarkerOptionsChooser;
import net.naonedbus.provider.impl.MyLocationProvider;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
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

	final ArrayList<InputPoint> mInputPoints = new ArrayList<InputPoint>();
	final com.twotoasters.clusterkraf.Options mOptions = new com.twotoasters.clusterkraf.Options();

	private SharedPreferences mPreferences;
	private SupportMapFragment mSupportMapFragment;
	private GoogleMap mGoogleMap;
	private Clusterkraf mClusterkraf;
	private MapLoader mMapLoader;

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
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		if (container == null) // must put this in
			return null;

		final View view = inflater.inflate(R.layout.fragment_map, container, false);

		mSupportMapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.map);
		mGoogleMap = mSupportMapFragment.getMap();
		mGoogleMap.setMyLocationEnabled(true);

		mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

		initMap();
		initClusterkraf();

		return view;
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.activity_map, menu);

		if (mLocationProvider.isProviderEnabled() == false) {
			menu.findItem(R.id.menu_location).setVisible(false);
		}

		// final MenuItem item = filterSubMenu.add(0, MENU_ID_SATELLITE, 0,
		// R.string.map_calque_satellite);
		// item.setCheckable(true);
		// item.setChecked(mMapView.isSatellite());
	}

	@Override
	public void onPrepareOptionsMenu(final Menu menu) {

	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	private void initMap() {
		final UiSettings uiSettings = mGoogleMap.getUiSettings();
		uiSettings.setAllGesturesEnabled(false);
		uiSettings.setScrollGesturesEnabled(true);
		uiSettings.setZoomGesturesEnabled(true);
	}

	private void initClusterkraf() {
		if (mGoogleMap != null) {
			final MarkerOptionsChooser markerOptionsChooser = new ToastedMarkerOptionsChooser(getActivity());
			mOptions.setMarkerOptionsChooser(markerOptionsChooser);
			mOptions.setPixelDistanceToJoinCluster(100);

			// customize the options before you construct a Clusterkraf instance
			mClusterkraf = new Clusterkraf(mGoogleMap, mOptions, null);

			mMapLoader = new MapLoader(getActivity(), this);
			mMapLoader.execute(Type.TYPE_BICLOO, Type.TYPE_MARGUERITE, Type.TYPE_PARKING);
		}
	}

	@Override
	public void onLayerLoaded(final ArrayList<InputPoint> result) {
		mClusterkraf.addAll(result);
	}

}
