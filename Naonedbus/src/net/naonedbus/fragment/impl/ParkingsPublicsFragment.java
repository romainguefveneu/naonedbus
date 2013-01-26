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
import net.naonedbus.helper.StateHelper;
import net.naonedbus.intent.ParamIntent;
import net.naonedbus.manager.impl.ParkingPublicManager;
import net.naonedbus.provider.impl.MyLocationProvider;
import net.naonedbus.provider.impl.MyLocationProvider.MyLocationListener;
import net.naonedbus.widget.adapter.impl.ParkingPublicArrayAdapter;
import net.naonedbus.widget.indexer.ArraySectionIndexer;
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
	private final static SparseArray<ArraySectionIndexer<ParkingPublic>> indexers = new SparseArray<ArraySectionIndexer<ParkingPublic>>();
	static {
		indexers.append(SORT_NOM, new ParkingNomIndexer());
		indexers.append(SORT_DISTANCE, new ParkingDistanceIndexer());
		indexers.append(SORT_PLACES, new ParkingPlaceIndexer());
	}

	private StateHelper mStateHelper;
	private MyLocationProvider myLocationProvider;
	private ParkingDistance loaderDistance;
	private int mCurrentSort = SORT_NOM;

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

		mStateHelper = new StateHelper(getActivity());
		mCurrentSort = mStateHelper.getSortType(this, SORT_NOM);
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
	public void onStop() {
		mStateHelper.setSortType(this, mCurrentSort);
		super.onStop();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu) {
		final MenuInflater menuInflater = getSherlockActivity().getSupportMenuInflater();
		menuInflater.inflate(R.menu.fragment_parkings_publics, menu);
		menu.findItem(mCurrentSort).setChecked(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		item.setChecked(true);

		switch (item.getItemId()) {
		case R.id.menu_sort_name:
			mCurrentSort = SORT_NOM;
			sort();
			break;
		case R.id.menu_sort_distance:
			mCurrentSort = SORT_DISTANCE;
			sort();
			break;
		case R.id.menu_sort_parking_places:
			mCurrentSort = SORT_PLACES;
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
			Collections.sort(parkings, comparators.get(mCurrentSort));
			final ParkingPublicArrayAdapter adapter = new ParkingPublicArrayAdapter(context, parkings);
			adapter.setIndexer(indexers.get(mCurrentSort));

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
		final ArraySectionIndexer<ParkingPublic> indexer;

		if (mCurrentSort == SORT_DISTANCE && !myLocationProvider.isProviderEnabled()) {
			// Tri par défaut si pas le localisation
			comparator = comparators.get(SORT_NOM);
			indexer = indexers.get(SORT_NOM);
		} else {
			comparator = comparators.get(mCurrentSort);
			indexer = indexers.get(mCurrentSort);
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

			if (mCurrentSort == SORT_DISTANCE) {
				refreshDistance();
			}
		}

		@Override
		public void onLocationDisabled() {
			final ParkingDistanceComparator comparator = (ParkingDistanceComparator) comparators.get(SORT_DISTANCE);
			comparator.setReferentiel(null);
			if (mCurrentSort == SORT_DISTANCE) {
				mCurrentSort = SORT_NOM;
				sort();
			}
		}
	};

	/**
	 * Retourne le comparateur en cours, ou le comparateur par nom si non
	 * trouvé.
	 * 
	 * @return le comparateur en cours, ou le comparateur par nom si non trouvé.
	 */
	private Comparator<ParkingPublic> getCurrentComparator() {
		final Comparator<ParkingPublic> comparator = comparators.get(mCurrentSort);
		return (comparator == null) ? comparators.get(SORT_NOM) : comparator;
	}

}
