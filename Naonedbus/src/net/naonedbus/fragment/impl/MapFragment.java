package net.naonedbus.fragment.impl;

import java.util.ArrayList;
import java.util.List;

import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.bean.Arret;
import net.naonedbus.intent.IIntentParamKey;
import net.naonedbus.manager.impl.ArretManager;
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
import com.google.android.gms.maps.model.LatLng;
import com.twotoasters.clusterkraf.Clusterkraf;
import com.twotoasters.clusterkraf.InputPoint;

public class MapFragment extends SherlockFragment {

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

		final ArretManager manager = ArretManager.getInstance();
		final List<Arret> items = manager.getAll(getActivity().getContentResolver());

		for (final Arret item : items) {
			final LatLng latLng = new LatLng(item.getLatitude(), item.getLongitude());
			mInputPoints.add(new InputPoint(latLng));
		}

		if (mGoogleMap != null && mInputPoints != null && mInputPoints.size() > 0) {

			// customize the options before you construct a Clusterkraf instance
			mClusterkraf = new Clusterkraf(mGoogleMap, mOptions, mInputPoints);
		}
	}

}
