/**
 * Copyright (C) 2013 Romain Guefveneu.
 *   
 *  This file is part of naonedbus.
 *   
 *  Naonedbus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  Naonedbus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
import net.naonedbus.manager.impl.ParkingPublicManager;
import net.naonedbus.provider.impl.MyLocationProvider;
import net.naonedbus.provider.impl.MyLocationProvider.MyLocationListener;
import net.naonedbus.widget.adapter.impl.ParkingPublicArrayAdapter;
import net.naonedbus.widget.indexer.ArraySectionIndexer;
import net.naonedbus.widget.indexer.impl.ParkingDistanceIndexer;
import net.naonedbus.widget.indexer.impl.ParkingNomIndexer;
import net.naonedbus.widget.indexer.impl.ParkingPlaceIndexer;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class ParkingsPublicsFragment extends CustomListFragment {

	private final static int SORT_NOM = 0;
	private final static int SORT_DISTANCE = 1;
	private final static int SORT_PLACES = 2;

	private final static SparseIntArray MENU_MAPPING = new SparseIntArray();
	static {
		MENU_MAPPING.append(SORT_NOM, R.id.menu_sort_name);
		MENU_MAPPING.append(SORT_DISTANCE, R.id.menu_sort_distance);
		MENU_MAPPING.append(SORT_PLACES, R.id.menu_sort_parking_places);
	}

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

	private MenuItem mRefreshMenuItem;
	private StateHelper mStateHelper;
	private final MyLocationProvider myLocationProvider;
	private ParkingDistance loaderDistance;
	private int mCurrentSort = SORT_NOM;

	public ParkingsPublicsFragment() {
		super(R.layout.fragment_listview_section);
		myLocationProvider = NBApplication.getLocationProvider();
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		myLocationProvider.addListener(locationListener);

		// Initaliser le comparator avec la position actuelle.
		locationListener.onLocationChanged(myLocationProvider.getLastKnownLocation());

		mStateHelper = new StateHelper(getActivity());
		mCurrentSort = mStateHelper.getSortType(this, SORT_NOM);

		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
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
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_parkings_publics, menu);
		mRefreshMenuItem = menu.findItem(R.id.menu_refresh);
		menu.findItem(MENU_MAPPING.get(mCurrentSort)).setChecked(true);

		if (getLoaderManager().hasRunningLoaders())
			showResfrehMenuLoader();

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
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
		case R.id.menu_refresh:
			refreshContent();
			break;
		}
		return false;
	}

	private void showResfrehMenuLoader() {
		if (mRefreshMenuItem != null) {
			mRefreshMenuItem.setActionView(R.layout.action_item_refresh);
		}
	}

	private void hideResfrehMenuLoader() {
		if (mRefreshMenuItem != null) {
			mRefreshMenuItem.setActionView(null);
		}
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
		super.onListItemClick(l, v, position, id);
		final Parking parking = (Parking) getListAdapter().getItem(position);

		final Intent intent = new Intent(getActivity(), ParkingDetailActivity.class);
		intent.putExtra(ParkingDetailActivity.PARAM_PARKING, parking);
		startActivity(intent);
	}

	private void refreshDistance() {
		if (myLocationProvider.isProviderEnabled() && getListAdapter() != null
				&& (loaderDistance == null || loaderDistance.getStatus() == AsyncTask.Status.FINISHED)) {
			loaderDistance = (ParkingDistance) new ParkingDistance().execute();
		}
	}

	@Override
	protected void onPreExecute() {
		if (getListAdapter() == null) {
			showLoader();
		}
		showResfrehMenuLoader();
	}

	@Override
	protected AsyncResult<ListAdapter> loadContent(final Context context, final Bundle bundle) {
		final AsyncResult<ListAdapter> result = new AsyncResult<ListAdapter>();
		try {

			final ParkingPublicManager parkingPublicManager = ParkingPublicManager.getInstance();
			final List<ParkingPublic> parkings = parkingPublicManager.getAll(context);
			Collections.sort(parkings, comparators.get(mCurrentSort));
			final ParkingPublicArrayAdapter adapter = new ParkingPublicArrayAdapter(context, parkings);
			adapter.setIndexer(indexers.get(mCurrentSort));

			result.setResult(adapter);
		} catch (final Exception exception) {
			result.setException(exception);
		}
		return result;
	}

	@Override
	protected void onPostExecute() {
		refreshDistance();
		hideResfrehMenuLoader();

	}

	/**
	 * Classe de calcul de la distance des parkings.
	 * 
	 * @author romain
	 */
	private class ParkingDistance extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(final Void... params) {
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
		protected void onPostExecute(final Void result) {
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
	private void sort(final ParkingPublicArrayAdapter adapter) {
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
	private final MyLocationListener locationListener = new MyLocationListener() {
		@Override
		public void onLocationChanged(final Location location) {
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

		@Override
		public void onLocationConnecting() {
			
		}
	};

}
