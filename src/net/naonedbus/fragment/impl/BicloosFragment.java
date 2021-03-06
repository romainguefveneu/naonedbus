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

import java.util.Comparator;
import java.util.List;

import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.activity.impl.BiclooDetailActivity;
import net.naonedbus.activity.impl.MapActivity;
import net.naonedbus.bean.Bicloo;
import net.naonedbus.bean.Equipement;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.comparator.BiclooComparator;
import net.naonedbus.comparator.BiclooDistanceComparator;
import net.naonedbus.fragment.CustomListFragment;
import net.naonedbus.helper.StateHelper;
import net.naonedbus.intent.ParamIntent;
import net.naonedbus.manager.impl.BiclooManager;
import net.naonedbus.manager.impl.BiclooManager.BiclooObserver;
import net.naonedbus.manager.impl.FavoriBiclooManager;
import net.naonedbus.provider.impl.NaoLocationManager;
import net.naonedbus.provider.impl.NaoLocationManager.NaoLocationListener;
import net.naonedbus.widget.adapter.impl.BiclooArrayAdapter;
import net.naonedbus.widget.indexer.ArraySectionIndexer;
import net.naonedbus.widget.indexer.impl.BiclooDistanceIndexer;
import net.naonedbus.widget.indexer.impl.BiclooNomIndexer;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class BicloosFragment extends CustomListFragment {

	protected final static String BUNDKE_KEY_FORCE_UDPATE = "forceUpdate";
	private final BiclooObserver mBiclooObserver = new BiclooObserver(new Handler()) {
		@Override
		public void onChange() {
			if (isAdded() && !getLoaderManager().hasRunningLoaders() && getListAdapter() != null) {
				refreshContent();
			}
		}
	};

	private final static int SORT_NOM = 0;
	private final static int SORT_DISTANCE = 1;
	private final static SparseIntArray MENU_MAPPING = new SparseIntArray();
	static {
		MENU_MAPPING.append(SORT_NOM, R.id.menu_sort_name);
		MENU_MAPPING.append(SORT_DISTANCE, R.id.menu_sort_distance);
	}

	protected final SparseArray<Comparator<Bicloo>> mComparators;
	protected final SparseArray<ArraySectionIndexer<Bicloo>> mIndexers;
	protected int mCurrentSortPreference = SORT_NOM;

	private final BiclooManager mBiclooManager;
	private final FavoriBiclooManager mFavoriBiclooManager;
	private final NaoLocationManager mLocationProvider;

	private MenuItem mRefreshMenuItem;
	private StateHelper mStateHelper;
	private DistanceTask mLoaderDistance;

	public BicloosFragment() {
		super(R.layout.fragment_listview_section);

		mBiclooManager = BiclooManager.getInstance();
		mFavoriBiclooManager = FavoriBiclooManager.getInstance();

		mLocationProvider = NBApplication.getLocationProvider();

		mComparators = new SparseArray<Comparator<Bicloo>>();
		mComparators.append(SORT_NOM, new BiclooComparator());
		mComparators.append(SORT_DISTANCE, new BiclooDistanceComparator());

		mIndexers = new SparseArray<ArraySectionIndexer<Bicloo>>();
		mIndexers.append(SORT_NOM, new BiclooNomIndexer());
		mIndexers.append(SORT_DISTANCE, new BiclooDistanceIndexer());
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		registerForContextMenu(getListView());

		mBiclooManager.registerObserver(mBiclooObserver);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		mLocationProvider.addListener(mLocationListener);
		mLocationListener.onLocationChanged(mLocationProvider.getLastLocation());

		// Gestion du tri par défaut
		mStateHelper = new StateHelper(getActivity());
		mCurrentSortPreference = mStateHelper.getSortType(this, SORT_NOM);
	}

	@Override
	public void onStart() {
		super.onStart();
		loadContent();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (isNotUpToDate() && (getListAdapter() != null && getListAdapter().isEmpty())) {
			final Bundle bundle = new Bundle();
			bundle.putBoolean(BUNDKE_KEY_FORCE_UDPATE, true);
			refreshContent(bundle);
		}
	}

	@Override
	protected boolean isNotUpToDate() {
		return mBiclooManager.isNotUpToDate();
	}

	@Override
	public void onPause() {
		mStateHelper.setSortType(this, mCurrentSortPreference);
		super.onPause();
	}

	@Override
	public void onDestroy() {
		mLocationProvider.removeListener(mLocationListener);
		mBiclooManager.unregisterObserver(mBiclooObserver);
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_sort_name:
			item.setChecked(true);
			mCurrentSortPreference = SORT_NOM;
			sort();
			return true;
		case R.id.menu_sort_distance:
			item.setChecked(true);
			mCurrentSortPreference = SORT_DISTANCE;
			sort();
			return true;
		case R.id.menu_refresh:
			final Bundle bundle = new Bundle();
			bundle.putBoolean(BUNDKE_KEY_FORCE_UDPATE, true);
			refreshContent(bundle);
			return true;
		default:
			return false;
		}
	}

	/**
	 * Listener de changement de coordonnées GPS
	 */
	private final NaoLocationListener mLocationListener = new NaoLocationListener() {
		@Override
		public void onLocationChanged(final Location location) {
			final BiclooDistanceComparator comparator = (BiclooDistanceComparator) mComparators.get(SORT_DISTANCE);
			comparator.setReferentiel(location);

			if (mCurrentSortPreference == SORT_DISTANCE) {
				refreshDistance();
			}
		}

		@Override
		public void onDisconnected() {
			final BiclooDistanceComparator comparator = (BiclooDistanceComparator) mComparators.get(SORT_DISTANCE);
			comparator.setReferentiel(null);
			if (mCurrentSortPreference == SORT_DISTANCE) {
				mCurrentSortPreference = SORT_NOM;
				sort();
			}
		}

		@Override
		public void onConnecting() {

		}

		@Override
		public void onLocationTimeout() {

		}

	};

	/**
	 * Trier les équipements selon les préférences.
	 */
	private void sort() {
		final BiclooArrayAdapter adapter = (BiclooArrayAdapter) getListAdapter();
		setIndexerAndComparator(adapter);
		adapter.notifyDataSetChanged();
	}

	/**
	 * Définir l'indexer et comparator en fonction de
	 * {@link #mCurrentSortPreference}.
	 * 
	 * @param adapter
	 */
	private void setIndexerAndComparator(final BiclooArrayAdapter adapter) {
		final Comparator<Bicloo> comparator;
		final ArraySectionIndexer<Bicloo> indexer;

		if (mCurrentSortPreference == SORT_DISTANCE && !mLocationProvider.isEnabled()) {
			// Tri par défaut si pas le localisation
			comparator = mComparators.get(SORT_NOM);
			indexer = mIndexers.get(SORT_NOM);
		} else {
			comparator = mComparators.get(mCurrentSortPreference);
			indexer = mIndexers.get(mCurrentSortPreference);
		}

		adapter.sort(comparator);
		adapter.setIndexer(indexer);
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		final AdapterView.AdapterContextMenuInfo cmi = (AdapterView.AdapterContextMenuInfo) menuInfo;

		final Bicloo bicloo = (Bicloo) getListAdapter().getItem(cmi.position);

		final android.view.MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.fragment_bicloos_contextual, menu);

		menu.setHeaderTitle(bicloo.getName());
		menu.findItem(R.id.menu_favori).setTitle(
				isFavori(bicloo) ? R.string.action_favori_remove : R.string.action_favori_add);

	}

	@Override
	public boolean onContextItemSelected(final android.view.MenuItem item) {
		final AdapterView.AdapterContextMenuInfo cmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

		final Bicloo bicloo = (Bicloo) getListAdapter().getItem(cmi.position);

		switch (item.getItemId()) {
		case R.id.menu_show_plan:
			final ParamIntent intent = new ParamIntent(getActivity(), MapActivity.class);
			intent.putExtra(MapFragment.PARAM_ITEM_ID, bicloo.getId());
			intent.putExtra(MapFragment.PARAM_ITEM_TYPE, Equipement.Type.TYPE_BICLOO.getId());
			startActivity(intent);
			break;
		case R.id.menu_favori:
			if (isFavori(bicloo)) {
				removeFromFavoris(bicloo);
			} else {
				addToFavoris(bicloo);
			}
			break;
		default:
			break;
		}

		return true;
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
		final Bicloo bicloo = (Bicloo) getListAdapter().getItem(position);

		final ParamIntent intent = new ParamIntent(getActivity(), BiclooDetailActivity.class);
		intent.putExtra(BiclooDetailActivity.PARAM_BICLOO, bicloo);
		startActivity(intent);
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_bicloos, menu);
		mRefreshMenuItem = menu.findItem(R.id.menu_refresh);
		menu.findItem(MENU_MAPPING.get(mCurrentSortPreference)).setChecked(true);

		if (getLoaderManager().hasRunningLoaders())
			showResfrehMenuLoader();

		super.onCreateOptionsMenu(menu, inflater);
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

	/**
	 * Actualiser les informations de distance.
	 */
	private void refreshDistance() {
		if (mLocationProvider.isEnabled() && getListAdapter() != null
				&& (mLoaderDistance == null || mLoaderDistance.getStatus() == AsyncTask.Status.FINISHED)) {
			mLoaderDistance = (DistanceTask) new DistanceTask().execute();
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
	protected void onPostExecute() {
		hideResfrehMenuLoader();
	}

	@Override
	protected AsyncResult<ListAdapter> loadContent(final Context context, final Bundle bundle) {
		final AsyncResult<ListAdapter> result = new AsyncResult<ListAdapter>();
		try {
			final BiclooManager biclooManager = BiclooManager.getInstance();

			if (bundle != null && bundle.getBoolean(BUNDKE_KEY_FORCE_UDPATE, false)) {
				biclooManager.clearCache();
			}

			final List<Bicloo> bicloos = biclooManager.getAll(context);

			setDistances(bicloos);

			final BiclooArrayAdapter adapter = new BiclooArrayAdapter(context, bicloos);
			adapter.setIndexer(mIndexers.get(mCurrentSortPreference));
			adapter.sort(mComparators.get(mCurrentSortPreference));

			result.setResult(adapter);
		} catch (final Exception e) {
			result.setException(e);
		}
		return result;
	}

	protected void setDistances(final List<Bicloo> bicloos) {
		final Location currentLocation = mLocationProvider.getLastLocation();

		if (currentLocation != null) {
			for (final Bicloo item : bicloos) {
				if (item.getLocation() != null) {
					item.setDistance(currentLocation.distanceTo(item.getLocation()));
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

			final Location currentLocation = mLocationProvider.getLastLocation();

			if (currentLocation != null) {
				for (int i = 0; i < adapter.getCount(); i++) {
					final Bicloo item = (Bicloo) adapter.getItem(i);
					if (item.getLocation() != null) {
						item.setDistance(currentLocation.distanceTo(item.getLocation()));
					}
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(final Void result) {
			final BaseAdapter adapter = (BaseAdapter) getListAdapter();
			adapter.notifyDataSetChanged();
		}

	}

	private boolean isFavori(final Bicloo bicloo) {
		final Bicloo b = mFavoriBiclooManager.getSingle(getActivity().getContentResolver(), bicloo.getId());
		return (b != null);
	}

	private void addToFavoris(final Bicloo bicloo) {
		mFavoriBiclooManager.add(getActivity().getContentResolver(), bicloo);
	}

	private void removeFromFavoris(final Bicloo bicloo) {
		mFavoriBiclooManager.remove(getActivity().getContentResolver(), bicloo.getId());
	}
}
