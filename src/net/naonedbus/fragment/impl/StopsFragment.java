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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.activity.impl.StopDetailActivity;
import net.naonedbus.activity.impl.StopsActivity.OnDirectionChanged;
import net.naonedbus.activity.impl.SendNewsActivity;
import net.naonedbus.activity.impl.MapActivity;
import net.naonedbus.activity.impl.PlanActivity;
import net.naonedbus.activity.map.overlay.TypeOverlayItem;
import net.naonedbus.bean.Direction;
import net.naonedbus.bean.Route;
import net.naonedbus.bean.Stop;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.comparator.StopComparator;
import net.naonedbus.comparator.StopOrderComparator;
import net.naonedbus.fragment.CustomListFragment;
import net.naonedbus.helper.StateHelper;
import net.naonedbus.manager.impl.StopManager;
import net.naonedbus.manager.impl.StopBookmarkManager;
import net.naonedbus.provider.impl.MyLocationProvider;
import net.naonedbus.provider.impl.MyLocationProvider.MyLocationListener;
import net.naonedbus.utils.InfoDialogUtils;
import net.naonedbus.widget.adapter.impl.StopArrayAdapter;
import net.naonedbus.widget.adapter.impl.StopArrayAdapter.ViewType;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class StopsFragment extends CustomListFragment implements OnDirectionChanged, MyLocationListener {

	public static final String PARAM_ROUTE = "route";

	private final static int SORT_NAME = 0;
	private final static int SORT_ORDER = 1;
	private final static int FILTER_ALL = 2;
	private final static int FILTER_BOOKMARKS = 3;
	private final static SparseIntArray MENU_MAPPING = new SparseIntArray();
	static {
		MENU_MAPPING.append(SORT_NAME, R.id.menu_sort_name);
		MENU_MAPPING.append(SORT_ORDER, R.id.menu_sort_ordre);
		MENU_MAPPING.append(FILTER_ALL, R.id.menu_filter_all);
		MENU_MAPPING.append(FILTER_BOOKMARKS, R.id.menu_filter_favoris);
	}

	private interface DistanceTaskCallback {
		void onNearestStationFound(Integer position);

		void onPostExecute();
	}

	protected final SparseArray<Comparator<Stop>> mComparators;
	protected int mCurrentSort;

	private final StopBookmarkManager mStopBookmarkManager;
	private StateHelper mStateHelper;
	private final MyLocationProvider mLocationProvider;
	private DistanceTask mDistanceTask;
	private DistanceTaskCallback mDistanceTaskCallback;
	private Integer mNearestStopPosition;
	private StopArrayAdapter mAdapter;
	private int mCurrentFilter = FILTER_ALL;

	private List<Stop> mStops;

	private Route mRoute;
	private Direction mDirection;

	public StopsFragment() {
		super(R.layout.fragment_listview);
		mLocationProvider = NBApplication.getLocationProvider();
		mLocationProvider.addListener(this);

		mComparators = new SparseArray<Comparator<Stop>>();
		mComparators.append(SORT_NAME, new StopComparator());
		mComparators.append(SORT_ORDER, new StopOrderComparator());

		mStopBookmarkManager = StopBookmarkManager.getInstance();
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		registerForContextMenu(getListView());

		mRoute = getArguments().getParcelable(PARAM_ROUTE);

		mStateHelper = new StateHelper(getActivity());
		mCurrentSort = mStateHelper.getSortType(this, SORT_NAME);
		setCurrentFilter(mStateHelper.getFilterType(this, FILTER_ALL));

		mStops = new ArrayList<Stop>();
		mAdapter = new StopArrayAdapter(getActivity(), mStops);
		if (mCurrentSort == SORT_ORDER) {
			mAdapter.setViewType(ViewType.TYPE_METRO);
		}

		mDistanceTaskCallback = new DistanceTaskCallback() {
			@Override
			public void onNearestStationFound(final Integer position) {
				mNearestStopPosition = position;
				final StopArrayAdapter adapter = (StopArrayAdapter) getListAdapter();
				if (adapter != null && mNearestStopPosition != null) {
					adapter.setNearestPosition(mNearestStopPosition);
					adapter.notifyDataSetChanged();
				}
			}

			@Override
			public void onPostExecute() {
				final StopArrayAdapter adapter = (StopArrayAdapter) getListAdapter();
				if (adapter != null) {
					adapter.notifyDataSetChanged();
				}
			}
		};
	}

	@Override
	public void onStart() {
		super.onStart();
		mLocationProvider.start();
	}

	@Override
	public void onStop() {
		super.onStop();

		// Save state
		mStateHelper.setSortType(this, mCurrentSort);
		mStateHelper.setFilterType(this, mCurrentFilter);

		mLocationProvider.stop();
		if (mDistanceTask != null) {
			mDistanceTask.cancel(true);
		}
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_stops, menu);
		menu.findItem(MENU_MAPPING.get(mCurrentSort)).setChecked(true);
		menu.findItem(MENU_MAPPING.get(mCurrentFilter)).setChecked(true);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onPrepareOptionsMenu(final Menu menu) {
		menu.findItem(R.id.menu_location).setVisible(mLocationProvider.isProviderEnabled());
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_sort_name:
			item.setChecked(true);
			changeSortOrder(SORT_NAME, ViewType.TYPE_STANDARD);
			break;
		case R.id.menu_sort_ordre:
			item.setChecked(true);
			changeSortOrder(SORT_ORDER, ViewType.TYPE_METRO);
			break;
		case R.id.menu_filter_all:
			if (mCurrentFilter != FILTER_ALL) {
				item.setChecked(true);
				setCurrentFilter(FILTER_ALL);
				refreshContent();
			}
			break;
		case R.id.menu_filter_favoris:
			if (mCurrentFilter != FILTER_BOOKMARKS) {
				item.setChecked(true);
				setCurrentFilter(FILTER_BOOKMARKS);
				refreshContent();
			}
			break;
		case R.id.menu_show_plan:
			menuShowPlan();
			break;
		case R.id.menu_location:
			menuLocation();
			break;
		case R.id.menu_comment:
			menuComment();
			break;
		}
		return false;
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		final AdapterView.AdapterContextMenuInfo cmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
		final Stop stop = (Stop) getListView().getItemAtPosition(cmi.position);

		final android.view.MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.fragment_stops_contextual, menu);

		menu.setHeaderTitle(stop.getName());

		final android.view.MenuItem menuFavori = menu.findItem(R.id.menu_favori);
		if (mStopBookmarkManager.isFavori(getActivity().getContentResolver(), stop.getId())) {
			menuFavori.setTitle(R.string.remove_bookmark);
		} else {
			menuFavori.setTitle(R.string.add_bookmark);
		}
	}

	@Override
	public boolean onContextItemSelected(final android.view.MenuItem item) {
		final AdapterView.AdapterContextMenuInfo cmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		final Stop stop = (Stop) getListView().getItemAtPosition(cmi.position);

		switch (item.getItemId()) {
		case R.id.menu_show_plan:
			menuShowMap(stop);
			break;
		case R.id.menu_favori:
			if (mStopBookmarkManager.isFavori(getActivity().getContentResolver(), stop.getId())) {
				removeFromFavoris(stop);
			} else {
				addToFavoris(stop);
			}
			break;
		case R.id.menu_comment:
			menuComment(stop);
			break;
		default:
			break;
		}

		return true;
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
		super.onListItemClick(l, v, position, id);
		final Stop stop = (Stop) l.getItemAtPosition(position);

		final Intent intent = new Intent(getActivity(), StopDetailActivity.class);
		intent.putExtra(StopDetailActivity.PARAM_LIGNE, mRoute);
		intent.putExtra(StopDetailActivity.PARAM_SENS, mDirection);
		intent.putExtra(StopDetailActivity.PARAM_ARRET, stop);

		startActivity(intent);
	}

	private void menuShowPlan() {
		final Intent intent = new Intent(getActivity(), PlanActivity.class);
		intent.putExtra(PlanActivity.PARAM_CODE_LIGNE, mRoute.getCode());
		startActivity(intent);
	}

	private void menuComment() {
		final Intent intent = new Intent(getActivity(), SendNewsActivity.class);
		intent.putExtra(SendNewsActivity.PARAM_LIGNE, mRoute);
		intent.putExtra(SendNewsActivity.PARAM_SENS, mDirection);
		startActivity(intent);
	}

	private void menuComment(final Stop stop) {
		final Intent intent = new Intent(getActivity(), SendNewsActivity.class);
		intent.putExtra(SendNewsActivity.PARAM_LIGNE, mRoute);
		intent.putExtra(SendNewsActivity.PARAM_SENS, mDirection);
		intent.putExtra(SendNewsActivity.PARAM_ARRET, stop);
		startActivity(intent);
	}

	private void addToFavoris(final Stop stop) {
		mStopBookmarkManager.addFavori(getActivity().getContentResolver(), stop);
		Toast.makeText(getActivity(), R.string.bookmark_added, Toast.LENGTH_SHORT).show();
	}

	private void removeFromFavoris(final Stop stop) {
		mStopBookmarkManager.removeFavori(getActivity().getContentResolver(), stop.getId());
		Toast.makeText(getActivity(), R.string.bookmark_removed, Toast.LENGTH_SHORT).show();
	}

	private void menuShowMap(final Stop stop) {
		final Intent intent = new Intent(getActivity(), MapActivity.class);
		intent.putExtra(MapFragment.PARAM_ITEM_ID, stop.getIdStation());
		intent.putExtra(MapFragment.PARAM_ITEM_TYPE, TypeOverlayItem.TYPE_STATION.getId());
		startActivity(intent);
	}

	@TargetApi(11)
	private void menuLocation() {
		if (mNearestStopPosition != null) {
			final ListView listView = getListView();
			final int listViewHeight = listView.getHeight();
			final int itemHeight = listView.getChildAt(0).getHeight();

			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
				listView.setSelectionFromTop(mNearestStopPosition, (listViewHeight - itemHeight) / 2);
			} else {
				listView.smoothScrollToPositionFromTop(mNearestStopPosition, (listViewHeight - itemHeight) / 2);
			}
		}
	}

	/**
	 * Définir le filtre courant.
	 * 
	 * @param filter
	 */
	private void setCurrentFilter(final int filter) {
		mCurrentFilter = filter;

		if (mCurrentFilter == FILTER_ALL) {
			setEmptyMessageValues(R.string.no_data, R.string.sorry_bit_silly, R.drawable.sad_face);
		} else {
			setEmptyMessageValues(R.string.no_bookmark, R.string.no_bookmarked_stops,
					R.drawable.favori);
		}
	}

	/**
	 * Changer l'ordre de tri des arrêts.
	 * 
	 * @param sortOrder
	 *            L'id du comparator
	 * @param viewType
	 *            Le type de vue de l'adapter
	 */
	private void changeSortOrder(final int sortOrder, final ViewType viewType) {
		final StopArrayAdapter adapter = (StopArrayAdapter) getListAdapter();
		mCurrentSort = sortOrder;

		adapter.setViewType(viewType);
		getListView().setSelection(0);

		sort();
		loadDistances();

		if (viewType == ViewType.TYPE_METRO) {
			InfoDialogUtils.showIfNecessary(getActivity(), R.string.sort_by_location,
					R.string.sort_by_location_message);
		}
	}

	/**
	 * Trier les parkings selon les préférences.
	 */
	private void sort() {
		final StopArrayAdapter adapter = (StopArrayAdapter) getListAdapter();
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
	private void sort(final StopArrayAdapter adapter) {
		final Comparator<Stop> comparator = mComparators.get(mCurrentSort);
		if (comparator != null) {
			adapter.sort(comparator);
		}
	}

	@Override
	protected AsyncResult<ListAdapter> loadContent(final Context context, final Bundle bundle) {
		final AsyncResult<ListAdapter> result = new AsyncResult<ListAdapter>();
		try {
			final StopManager arretManager = StopManager.getInstance();
			final List<Stop> arrets;
			if (mCurrentFilter == FILTER_ALL) {
				arrets = arretManager.getAll(context.getContentResolver(), mDirection.getRouteCode(), mDirection.getCode());
			} else {
				arrets = arretManager.getBookmarks(context.getContentResolver(), mDirection.getRouteCode(),
						mDirection.getCode());
			}

			mStops.clear();
			mStops.addAll(arrets);

			result.setResult(mAdapter);
		} catch (final Exception e) {
			result.setException(e);
		}
		return result;
	}

	@Override
	protected void onPostExecute() {
		sort();
		loadDistances();
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onDirectionChanged(final Direction direction) {
		if (direction.equals(mDirection) == false) {
			mDirection = direction;
			refreshContent();
		}
	}

	/**
	 * Lancer le calcul des distances.
	 */
	private void loadDistances() {
		if (mDistanceTask != null) {
			mDistanceTask.cancel(true);
		}
		if (getListAdapter() != null) {
			mDistanceTask = (DistanceTask) new DistanceTask(mDistanceTaskCallback,
					mLocationProvider.getLastKnownLocation(), getListAdapter()).execute();
		}
	}

	/**
	 * Classe de calcul de la distance des arrêts.
	 */
	private class DistanceTask extends AsyncTask<Void, Void, Integer> {

		private DistanceTaskCallback mCallback;
		private final ListAdapter mAdapter;
		private final Location mCurrentLocation;

		public DistanceTask(final DistanceTaskCallback callback, final Location currentLocation,
				final ListAdapter adapter) {
			mCallback = callback;
			mCurrentLocation = currentLocation;
			mAdapter = adapter;
		}

		@Override
		protected Integer doInBackground(final Void... params) {
			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

			Stop stop;
			Integer nearestPosition = null;
			Float nearestDistance = Float.MAX_VALUE;
			Float distance = null;
			final Location equipementLocation = new Location(LocationManager.GPS_PROVIDER);

			if (mCurrentLocation != null) {
				for (int i = 0; i < mAdapter.getCount(); i++) {
					stop = (Stop) mAdapter.getItem(i);
					equipementLocation.setLatitude(stop.getLatitude());
					equipementLocation.setLongitude(stop.getLongitude());

					distance = mCurrentLocation.distanceTo(equipementLocation);
					stop.setDistance(distance);

					if (distance < nearestDistance) {
						nearestDistance = distance;
						nearestPosition = i;
					}

					if (isCancelled()) {
						break;
					}
				}
			}
			return nearestPosition;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			mCallback = null;
		}

		@Override
		protected void onPostExecute(final Integer result) {
			if (!isCancelled() && mCallback != null) {
				mCallback.onPostExecute();
				mCallback.onNearestStationFound(result);
			}
		}

	}

	@Override
	public void onLocationChanged(final Location location) {
		loadDistances();
	}

	@Override
	public void onLocationDisabled() {

	}

}
