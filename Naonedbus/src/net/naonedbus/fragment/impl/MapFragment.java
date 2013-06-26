package net.naonedbus.fragment.impl;

import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.intent.IIntentParamKey;
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

public class MapFragment extends SherlockFragment {

	private static final String LOG_TAG = "MapActivity";

	public static enum Param implements IIntentParamKey {
		itemId, itemType
	};

	private static final int MENU_GROUP_TYPES = 1;
	private static final int MENU_ID_SATELLITE = Integer.MAX_VALUE;
	private static final String PREF_MAP_LAYER = "map.layer.";

	private SharedPreferences mPreferences;

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

		mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

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

}
