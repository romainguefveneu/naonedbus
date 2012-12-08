package net.naonedbus.fragment.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.activity.impl.ParkingDetailActivity;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.bean.parking.Parking;
import net.naonedbus.bean.parking.pub.ParkingPublic;
import net.naonedbus.comparator.ParkingComparator;
import net.naonedbus.comparator.ParkingDistanceComparator;
import net.naonedbus.comparator.ParkingPlacesComparator;
import net.naonedbus.fragment.CustomListFragment;
import net.naonedbus.intent.ParamIntent;
import net.naonedbus.manager.impl.ParkingPublicManager;
import net.naonedbus.provider.impl.MyLocationProvider;
import net.naonedbus.provider.impl.MyLocationProvider.MyLocationListener;
import net.naonedbus.widget.adapter.impl.ParkingPublicArrayAdapter;
import net.naonedbus.widget.indexer.CustomSectionIndexer;
import net.naonedbus.widget.indexer.impl.ParkingDistanceIndexer;
import net.naonedbus.widget.indexer.impl.ParkingNomIndexer;
import net.naonedbus.widget.indexer.impl.ParkingPlaceIndexer;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class ParkingsPublicsFragment extends CustomListFragment {

	private final static int SORT_NOM = R.id.menu_sort_name;
	private final static int SORT_DISTANCE = R.id.menu_sort_distance;
	private final static int SORT_PLACES = R.id.menu_sort_parking_places;
	private final static SparseArray<Comparator<ParkingPublic>> comparators = new SparseArray<Comparator<ParkingPublic>>();
	static {
		comparators.append(SORT_NOM, new ParkingComparator());
		comparators.append(SORT_DISTANCE, new ParkingDistanceComparator());
		comparators.append(SORT_PLACES, new ParkingPlacesComparator());
	}
	private final static SparseArray<CustomSectionIndexer<ParkingPublic>> indexers = new SparseArray<CustomSectionIndexer<ParkingPublic>>();
	static {
		indexers.append(SORT_NOM, new ParkingNomIndexer());
		indexers.append(SORT_DISTANCE, new ParkingDistanceIndexer());
		indexers.append(SORT_PLACES, new ParkingPlaceIndexer());
	}

	private MyLocationProvider myLocationProvider;
	private ParkingDistance loaderDistance;
	private int currentSortPreference = SORT_NOM;

	public ParkingsPublicsFragment() {
		super(R.string.title_fragment_parkings_publics, R.layout.fragment_listview_section);
		myLocationProvider = NBApplication.getLocationProvider();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		myLocationProvider.addListener(locationListener);

		// Initaliser le comparator avec la position actuelle.
		locationListener.onLocationChanged(myLocationProvider.getLastKnownLocation());
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getListView().setDividerHeight(0);
		loadContent();
	}

	@Override
	public void onResume() {
		super.onResume();
		myLocationProvider.removeListener(locationListener);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu) {
		final MenuInflater menuInflater = getSherlockActivity().getSupportMenuInflater();
		menuInflater.inflate(R.menu.fragment_parkings_publics, menu);
		menu.findItem(currentSortPreference).setChecked(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		item.setChecked(true);

		switch (item.getItemId()) {
		case R.id.menu_sort_name:
			currentSortPreference = SORT_NOM;
			sort();
			break;
		case R.id.menu_sort_distance:
			currentSortPreference = SORT_DISTANCE;
			sort();
			break;
		case R.id.menu_sort_parking_places:
			currentSortPreference = SORT_PLACES;
			sort();
			break;
		}
		return false;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		final Parking parking = (Parking) getListAdapter().getItem(position);

		final ParamIntent intent = new ParamIntent(getActivity(), ParkingDetailActivity.class);
		intent.putExtraSerializable(ParkingDetailActivity.Param.parking, parking);
		startActivity(intent);
	}

	private void refreshDistance() {
		if (myLocationProvider.isProviderEnabled() && getListAdapter() != null
				&& (loaderDistance == null || loaderDistance.getStatus() == AsyncTask.Status.FINISHED)) {
			loaderDistance = (ParkingDistance) new ParkingDistance().execute();
		}
	}

	@Override
	protected AsyncResult<ListAdapter> loadContent(final Context context) {
		final AsyncResult<ListAdapter> result = new AsyncResult<ListAdapter>();
		try {

			final ParkingPublicManager parkingPublicManager = ParkingPublicManager.getInstance();
			final List<ParkingPublic> parkings = parkingPublicManager.getAll(context.getContentResolver());
			Collections.sort(parkings, comparators.get(currentSortPreference));
			final ParkingPublicArrayAdapter adapter = new ParkingPublicArrayAdapter(context, parkings);
			adapter.setIndexer(indexers.get(currentSortPreference));

			result.setResult(adapter);
		} catch (Exception exception) {
			result.setException(exception);
		}
		return result;
	}

	@Override
	protected void onPostExecute() {
		refreshDistance();
	}

	/**
	 * Classe de calcul de la distance des parkings.
	 * 
	 * @author romain
	 */
	private class ParkingDistance extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			final ListAdapter adapter = getListAdapter();

			final Location parkingLocation = new Location(LocationManager.GPS_PROVIDER);
			final Location currentLocation = myLocationProvider.getLastKnownLocation();

			if (currentLocation != null) {

				for (int i = 0; i < adapter.getCount(); i++) {
					final ParkingPublic item = (ParkingPublic) adapter.getItem(i);
					if (item.getLatitude() != null) {
						parkingLocation.setLatitude(item.getLatitude());
						parkingLocation.setLongitude(item.getLongitude());

						item.setDistance(currentLocation.distanceTo(parkingLocation));
					} else {
						item.setDistance(null);
					}
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			sort();
		}

	}

	/**
	 * Trier les parkings selon les préférences.
	 */
	private void sort() {
		final ParkingPublicArrayAdapter adapter = (ParkingPublicArrayAdapter) getListAdapter();
		if (adapter != null) {
			sort(adapter);
			adapter.notifyDataSetChanged();
		}
	}

	/**
	 * Trier les parkings selon les préférences.
	 * 
	 * @param adapter
	 */
	private void sort(ParkingPublicArrayAdapter adapter) {
		final Comparator<ParkingPublic> comparator;
		final CustomSectionIndexer<ParkingPublic> indexer;

		if (currentSortPreference == SORT_DISTANCE && !myLocationProvider.isProviderEnabled()) {
			// Tri par défaut si pas le localisation
			comparator = comparators.get(SORT_NOM);
			indexer = indexers.get(SORT_NOM);
		} else {
			comparator = comparators.get(currentSortPreference);
			indexer = indexers.get(currentSortPreference);
		}

		adapter.sort(comparator);
		adapter.setIndexer(indexer);
	}

	/**
	 * Listener de changement de coordonnées GPS
	 */
	private MyLocationListener locationListener = new MyLocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			final ParkingDistanceComparator comparator = (ParkingDistanceComparator) comparators.get(SORT_DISTANCE);
			comparator.setReferentiel(location);

			if (currentSortPreference == SORT_DISTANCE) {
				refreshDistance();
			}
		}

		@Override
		public void onLocationDisabled() {
			final ParkingDistanceComparator comparator = (ParkingDistanceComparator) comparators.get(SORT_DISTANCE);
			comparator.setReferentiel(null);
			if (currentSortPreference == SORT_DISTANCE) {
				currentSortPreference = SORT_NOM;
				sort();
			}
		}
	};

	/**
	 * Listener ce changement de paramètres
	 */
	// private OnSharedPreferenceChangeListener onSharedPreferenceChangeListener
	// = new OnSharedPreferenceChangeListener() {
	// @Override
	// public void onSharedPreferenceChanged(SharedPreferences
	// sharedPreferences, String key) {
	// if (NBApplication.PREF_PARKINGS_SORT.equals(key)) {
	// currentSortPreference =
	// Integer.valueOf(preferences.getString(NBApplication.PREF_PARKINGS_SORT,
	// "0"));
	// sort();
	// }
	// }
	// };

}
