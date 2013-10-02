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
import net.naonedbus.bean.Equipement;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.comparator.EquipementComparator;
import net.naonedbus.comparator.EquipementDistanceComparator;
import net.naonedbus.fragment.impl.MapFragment;
import net.naonedbus.helper.StateHelper;
import net.naonedbus.manager.impl.EquipementManager;
import net.naonedbus.manager.impl.EquipementManager.SousType;
import net.naonedbus.provider.impl.MyLocationProvider;
import net.naonedbus.provider.impl.MyLocationProvider.MyLocationListener;
import net.naonedbus.widget.adapter.impl.EquipementArrayAdapter;
import net.naonedbus.widget.indexer.ArraySectionIndexer;
import net.naonedbus.widget.indexer.impl.EquipementDistanceIndexer;
import net.naonedbus.widget.indexer.impl.EquipementNomIndexer;
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

public abstract class EquipementFragment extends CustomListFragment {

	private final static int SORT_NOM = 0;
	private final static int SORT_DISTANCE = 1;
	private final static SparseIntArray MENU_MAPPING = new SparseIntArray();
	static {
		MENU_MAPPING.append(SORT_NOM, R.id.menu_sort_name);
		MENU_MAPPING.append(SORT_DISTANCE, R.id.menu_sort_distance);
	}

	private static int COUNT = 0;

	protected final SparseArray<Comparator<Equipement>> comparators;
	protected final SparseArray<ArraySectionIndexer<Equipement>> indexers;

	protected MyLocationProvider myLocationProvider;
	protected int currentSortPreference = SORT_NOM;

	private StateHelper mStateHelper;
	private DistanceTask mLoaderDistance;
	private final Equipement.Type mType;
	private SousType mSousType;

	protected int localCount = -1;

	public EquipementFragment(final int layoutId, final Equipement.Type type) {
		super(layoutId);

		mType = type;
		myLocationProvider = NBApplication.getLocationProvider();

		indexers = new SparseArray<ArraySectionIndexer<Equipement>>();
		indexers.append(SORT_NOM, new EquipementNomIndexer());
		indexers.append(SORT_DISTANCE, new EquipementDistanceIndexer());

		comparators = new SparseArray<Comparator<Equipement>>();
		comparators.append(SORT_NOM, new EquipementComparator<Equipement>());
		comparators.append(SORT_DISTANCE, new EquipementDistanceComparator<Equipement>());

		localCount = COUNT++;
	}

	public EquipementFragment(final int layoutId, final Equipement.Type type, final SousType sousType) {
		this(layoutId, type);
		mSousType = sousType;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		myLocationProvider.addListener(locationListener);

		// Initaliser le comparator avec la position actuelle.
		locationListener.onLocationChanged(myLocationProvider.getLastKnownLocation());

		// Gestion du tri par défaut
		mStateHelper = new StateHelper(getActivity());
		currentSortPreference = mStateHelper.getSortType(this, SORT_NOM);
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
		myLocationProvider.removeListener(locationListener);
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
		final Equipement equipement = (Equipement) getListAdapter().getItem(position);
		final Intent intent = new Intent(getActivity(), MapActivity.class);
		intent.putExtra(MapFragment.PARAM_ITEM_ID, equipement.getId());
		intent.putExtra(MapFragment.PARAM_ITEM_TYPE, equipement.getType().getId());
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
			currentSortPreference = SORT_NOM;
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
		if (myLocationProvider.isProviderEnabled() && getListAdapter() != null
				&& (mLoaderDistance == null || mLoaderDistance.getStatus() == AsyncTask.Status.FINISHED)) {
			mLoaderDistance = (DistanceTask) new DistanceTask().execute();
		}
	}

	@Override
	protected AsyncResult<ListAdapter> loadContent(final Context context, final Bundle bundle) {
		final AsyncResult<ListAdapter> result = new AsyncResult<ListAdapter>();
		try {
			final EquipementManager equipementManager = EquipementManager.getInstance();
			final List<Equipement> equipements;

			if (mSousType == null) {
				equipements = equipementManager.getEquipementsByType(context.getContentResolver(), mType);
			} else {
				equipements = equipementManager.getEquipementsByType(context.getContentResolver(), mType, mSousType);
			}

			setDistances(equipements);

			final EquipementArrayAdapter adapter = new EquipementArrayAdapter(context, equipements);
			adapter.setIndexer(indexers.get(currentSortPreference));
			adapter.sort(comparators.get(currentSortPreference));

			result.setResult(adapter);

		} catch (final Exception e) {
			result.setException(e);
		}
		return result;
	}

	protected void setDistances(final List<Equipement> equipements) {
		final Location location = new Location(LocationManager.GPS_PROVIDER);
		final Location currentLocation = myLocationProvider.getLastKnownLocation();

		if (currentLocation != null) {
			for (final Equipement item : equipements) {
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

			final Location equipementLocation = new Location(LocationManager.GPS_PROVIDER);
			final Location currentLocation = myLocationProvider.getLastKnownLocation();

			if (currentLocation != null) {
				for (int i = 0; i < adapter.getCount(); i++) {
					final Equipement item = (Equipement) adapter.getItem(i);
					final double latitude = item.getLatitude();
					final double longitude = item.getLongitude();
					if (latitude != 0) {
						equipementLocation.setLatitude(latitude);
						equipementLocation.setLongitude(longitude);
						item.setDistance(currentLocation.distanceTo(equipementLocation));
					}
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(final Void result) {
			final EquipementArrayAdapter adapter = (EquipementArrayAdapter) getListAdapter();
			adapter.notifyDataSetChanged();
		}

	}

	/**
	 * Listener de changement de coordonnées GPS
	 */
	private final MyLocationListener locationListener = new MyLocationListener() {
		@Override
		public void onLocationChanged(final Location location) {
			final EquipementDistanceComparator<Equipement> comparator = (EquipementDistanceComparator<Equipement>) comparators
					.get(SORT_DISTANCE);
			comparator.setReferentiel(location);

			if (currentSortPreference == SORT_DISTANCE) {
				refreshDistance();
			}
		}

		@Override
		public void onLocationDisabled() {
			final EquipementDistanceComparator<Equipement> comparator = (EquipementDistanceComparator<Equipement>) comparators
					.get(SORT_DISTANCE);
			comparator.setReferentiel(null);
			if (currentSortPreference == SORT_DISTANCE) {
				currentSortPreference = SORT_NOM;
				sort();
			}
		}
	};

	/**
	 * Trier les équipements selon les préférences.
	 */
	private void sort() {
		final EquipementArrayAdapter adapter = (EquipementArrayAdapter) getListAdapter();
		setIndexerAndComparator(adapter);
		adapter.notifyDataSetChanged();
	}

	/**
	 * Définir l'indexer et comparator en fonction de
	 * {@link #currentSortPreference}.
	 * 
	 * @param adapter
	 */
	private void setIndexerAndComparator(final EquipementArrayAdapter adapter) {
		final Comparator<Equipement> comparator;
		final ArraySectionIndexer<Equipement> indexer;

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
	 * Ajouter un comparator.
	 * 
	 * @param key
	 * @param comparator
	 */
	protected void addComparator(final int key, final Comparator<Equipement> comparator) {
		comparators.put(key, comparator);
	}

	/**
	 * Ajouter un indexer.
	 * 
	 * @param key
	 * @param indexer
	 */
	protected void addIndexer(final int key, final ArraySectionIndexer<Equipement> indexer) {
		indexers.put(key, indexer);
	}

}
