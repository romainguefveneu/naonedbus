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
package net.naonedbus.fragment;

import java.util.Comparator;
import java.util.List;

import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.activity.impl.MapActivity;
import net.naonedbus.bean.Equipment;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.comparator.EquipmentComparator;
import net.naonedbus.comparator.EquipmentDistanceComparator;
import net.naonedbus.fragment.impl.MapFragment;
import net.naonedbus.helper.StateHelper;
import net.naonedbus.manager.impl.EquipmentManager;
import net.naonedbus.manager.impl.EquipmentManager.SubType;
import net.naonedbus.provider.impl.MyLocationProvider;
import net.naonedbus.provider.impl.MyLocationProvider.MyLocationListener;
import net.naonedbus.widget.adapter.impl.EquipmentArrayAdapter;
import net.naonedbus.widget.indexer.ArraySectionIndexer;
import net.naonedbus.widget.indexer.impl.EquipmentDistanceIndexer;
import net.naonedbus.widget.indexer.impl.EquipmentNomIndexer;
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

public abstract class EquipmentFragment extends CustomListFragment {

	private final static int SORT_NAME = 0;
	private final static int SORT_DISTANCE = 1;
	private final static SparseIntArray MENU_MAPPING = new SparseIntArray();
	static {
		MENU_MAPPING.append(SORT_NAME, R.id.menu_sort_name);
		MENU_MAPPING.append(SORT_DISTANCE, R.id.menu_sort_distance);
	}

	protected final SparseArray<Comparator<Equipment>> mComparators;
	protected final SparseArray<ArraySectionIndexer<Equipment>> mIndexers;

	protected MyLocationProvider mLocationProvider;
	protected int currentSortPreference = SORT_NAME;

	private StateHelper mStateHelper;
	private DistanceTask mLoaderDistance;
	private final Equipment.Type mType;
	private SubType mSubType;

	public EquipmentFragment(final int layoutId, final Equipment.Type type) {
		super(layoutId);

		mType = type;
		mLocationProvider = NBApplication.getLocationProvider();

		mIndexers = new SparseArray<ArraySectionIndexer<Equipment>>();
		mIndexers.append(SORT_NAME, new EquipmentNomIndexer());
		mIndexers.append(SORT_DISTANCE, new EquipmentDistanceIndexer());

		mComparators = new SparseArray<Comparator<Equipment>>();
		mComparators.append(SORT_NAME, new EquipmentComparator<Equipment>());
		mComparators.append(SORT_DISTANCE, new EquipmentDistanceComparator<Equipment>());
	}

	public EquipmentFragment(final int layoutId, final Equipment.Type type, final SubType sousType) {
		this(layoutId, type);
		mSubType = sousType;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		mLocationProvider.addListener(locationListener);

		// Initaliser le comparator avec la position actuelle.
		locationListener.onLocationChanged(mLocationProvider.getLastKnownLocation());

		// Gestion du tri par défaut
		mStateHelper = new StateHelper(getActivity());
		currentSortPreference = mStateHelper.getSortType(this, SORT_NAME);
	}

	@Override
	public void onStart() {
		super.onStart();
		loadContent();
	}

	@Override
	public void onPause() {
		mStateHelper.setSortType(this, currentSortPreference);
		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mLocationProvider.removeListener(locationListener);
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
		final Equipment equipment = (Equipment) getListAdapter().getItem(position);
		final Intent intent = new Intent(getActivity(), MapActivity.class);
		intent.putExtra(MapFragment.PARAM_ITEM_ID, equipment.getId());
		intent.putExtra(MapFragment.PARAM_ITEM_TYPE, equipment.getType().getId());
		startActivity(intent);
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_equipements, menu);
		menu.findItem(MENU_MAPPING.get(currentSortPreference)).setChecked(true);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		item.setChecked(true);

		switch (item.getItemId()) {
		case R.id.menu_sort_name:
			currentSortPreference = SORT_NAME;
			sort();
			return true;
		case R.id.menu_sort_distance:
			currentSortPreference = SORT_DISTANCE;
			sort();
			return true;
		default:
			return false;
		}
	}

	/**
	 * Actualiser les informations de distance.
	 */
	private void refreshDistance() {
		if (mLocationProvider.isProviderEnabled() && getListAdapter() != null
				&& (mLoaderDistance == null || mLoaderDistance.getStatus() == AsyncTask.Status.FINISHED)) {
			mLoaderDistance = (DistanceTask) new DistanceTask().execute();
		}
	}

	@Override
	protected AsyncResult<ListAdapter> loadContent(final Context context, final Bundle bundle) {
		final AsyncResult<ListAdapter> result = new AsyncResult<ListAdapter>();
		try {
			final EquipmentManager equipementManager = EquipmentManager.getInstance();
			final List<Equipment> equipements;

			if (mSubType == null) {
				equipements = equipementManager.getByType(context.getContentResolver(), mType);
			} else {
				equipements = equipementManager.getByType(context.getContentResolver(), mType, mSubType);
			}

			setDistances(equipements);

			final EquipmentArrayAdapter adapter = new EquipmentArrayAdapter(context, equipements);
			adapter.setIndexer(mIndexers.get(currentSortPreference));
			adapter.sort(mComparators.get(currentSortPreference));

			result.setResult(adapter);

		} catch (final Exception e) {
			result.setException(e);
		}
		return result;
	}

	protected void setDistances(final List<Equipment> equipements) {
		final Location location = new Location(LocationManager.GPS_PROVIDER);
		final Location currentLocation = mLocationProvider.getLastKnownLocation();

		if (currentLocation != null) {
			for (final Equipment item : equipements) {
				final double latitude = item.getLatitude();
				final double longitude = item.getLongitude();
				if (latitude != 0) {
					location.setLatitude(latitude);
					location.setLongitude(longitude);
					item.setDistance(currentLocation.distanceTo(location));
				}
			}
		}
	}

	/**
	 * Classe de calcul de la distance des équipements.
	 * 
	 * @author romain
	 */
	private class DistanceTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(final Void... params) {
			final ListAdapter adapter = getListAdapter();

			final Location equipmentLocation = new Location(LocationManager.GPS_PROVIDER);
			final Location currentLocation = mLocationProvider.getLastKnownLocation();

			if (currentLocation != null) {
				for (int i = 0; i < adapter.getCount(); i++) {
					final Equipment item = (Equipment) adapter.getItem(i);
					final double latitude = item.getLatitude();
					final double longitude = item.getLongitude();
					if (latitude != 0) {
						equipmentLocation.setLatitude(latitude);
						equipmentLocation.setLongitude(longitude);
						item.setDistance(currentLocation.distanceTo(equipmentLocation));
					}
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(final Void result) {
			final EquipmentArrayAdapter adapter = (EquipmentArrayAdapter) getListAdapter();
			adapter.notifyDataSetChanged();
		}

	}

	/**
	 * Listener de changement de coordonnées GPS
	 */
	private final MyLocationListener locationListener = new MyLocationListener() {
		@Override
		public void onLocationChanged(final Location location) {
			final EquipmentDistanceComparator<Equipment> comparator = (EquipmentDistanceComparator<Equipment>) mComparators
					.get(SORT_DISTANCE);
			comparator.setReferentiel(location);

			if (currentSortPreference == SORT_DISTANCE) {
				refreshDistance();
			}
		}

		@Override
		public void onLocationDisabled() {
			final EquipmentDistanceComparator<Equipment> comparator = (EquipmentDistanceComparator<Equipment>) mComparators
					.get(SORT_DISTANCE);
			comparator.setReferentiel(null);
			if (currentSortPreference == SORT_DISTANCE) {
				currentSortPreference = SORT_NAME;
				sort();
			}
		}
	};

	/**
	 * Trier les équipements selon les préférences.
	 */
	private void sort() {
		final EquipmentArrayAdapter adapter = (EquipmentArrayAdapter) getListAdapter();
		setIndexerAndComparator(adapter);
		adapter.notifyDataSetChanged();
	}

	/**
	 * Définir l'indexer et comparator en fonction de
	 * {@link #currentSortPreference}.
	 * 
	 * @param adapter
	 */
	private void setIndexerAndComparator(final EquipmentArrayAdapter adapter) {
		final Comparator<Equipment> comparator;
		final ArraySectionIndexer<Equipment> indexer;

		if (currentSortPreference == SORT_DISTANCE && !mLocationProvider.isProviderEnabled()) {
			// Tri par défaut si pas le localisation
			comparator = mComparators.get(SORT_NAME);
			indexer = mIndexers.get(SORT_NAME);
		} else {
			comparator = mComparators.get(currentSortPreference);
			indexer = mIndexers.get(currentSortPreference);
		}

		adapter.sort(comparator);
		adapter.setIndexer(indexer);
	}

	/**
	 * Ajouter un comparator.
	 * 
	 * @param key
	 * @param comparator
	 */
	protected void addComparator(final int key, final Comparator<Equipment> comparator) {
		mComparators.put(key, comparator);
	}

	/**
	 * Ajouter un indexer.
	 * 
	 * @param key
	 * @param indexer
	 */
	protected void addIndexer(final int key, final ArraySectionIndexer<Equipment> indexer) {
		mIndexers.put(key, indexer);
	}

}
