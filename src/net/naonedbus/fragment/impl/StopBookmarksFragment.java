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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.naonedbus.BuildConfig;
import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.activity.impl.BookmarkGroupsActivity;
import net.naonedbus.activity.impl.MapActivity;
import net.naonedbus.activity.impl.PlanActivity;
import net.naonedbus.activity.impl.StopDetailActivity;
import net.naonedbus.activity.map.overlay.TypeOverlayItem;
import net.naonedbus.bean.BookmarkGroup;
import net.naonedbus.bean.NextHoraireTask;
import net.naonedbus.bean.Stop;
import net.naonedbus.bean.StopBookmark;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.comparator.StopBookmarkComparator;
import net.naonedbus.comparator.StopBookmarkDistanceComparator;
import net.naonedbus.fragment.CustomListFragment;
import net.naonedbus.helper.FavorisHelper;
import net.naonedbus.helper.FavorisHelper.FavorisActionListener;
import net.naonedbus.helper.GroupesHelper;
import net.naonedbus.helper.StateHelper;
import net.naonedbus.manager.impl.BookmarkGroupManager;
import net.naonedbus.manager.impl.ScheduleManager;
import net.naonedbus.manager.impl.StopBookmarkManager;
import net.naonedbus.manager.impl.StopBookmarkManager.OnFavoriActionListener;
import net.naonedbus.manager.impl.StopBookmarkViewManager;
import net.naonedbus.provider.impl.MyLocationProvider;
import net.naonedbus.provider.impl.MyLocationProvider.MyLocationListener;
import net.naonedbus.provider.impl.StopBookmarkGroupLinkProvider;
import net.naonedbus.provider.impl.StopBookmarkGroupProvider;
import net.naonedbus.service.FavoriService;
import net.naonedbus.utils.FavorisUtil;
import net.naonedbus.widget.adapter.impl.StopBookmarkArrayAdapter;
import net.naonedbus.widget.indexer.impl.StopBookmarksArrayIndexer;

import org.joda.time.DateMidnight;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.Loader;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;

public class StopBookmarksFragment extends CustomListFragment implements OnItemLongClickListener, MyLocationListener,
		ActionMode.Callback {

	private static final String LOG_TAG = "StopBookmarksFragment";
	private static final boolean DBG = BuildConfig.DEBUG;

	private static final String ACTION_UPDATE_DELAYS = "net.naonedbus.action.UPDATE_DELAYS";
	private static final String PREF_GROUPES = "favoris.groupes.";

	private static final int MENU_GROUP_GROUPS = 1;

	private final static int SORT_NAME = 0;
	private final static int SORT_DISTANCE = 1;
	private final static SparseIntArray MENU_MAPPING = new SparseIntArray();
	static {
		MENU_MAPPING.append(SORT_NAME, R.id.menu_sort_name);
		MENU_MAPPING.append(SORT_DISTANCE, R.id.menu_sort_distance);
	}

	private final static IntentFilter intentFilter;
	static {
		intentFilter = new IntentFilter();
		intentFilter.addAction(FavoriService.ACTION_EXPORTED);
		intentFilter.addAction(StopBookmarksFragment.ACTION_UPDATE_DELAYS);
		intentFilter.addAction(Intent.ACTION_TIME_TICK);
		intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
	}

	private final static SparseArray<Comparator<StopBookmark>> comparators = new SparseArray<Comparator<StopBookmark>>();
	static {
		comparators.append(SORT_NAME, new StopBookmarkComparator());
		comparators.append(SORT_DISTANCE, new StopBookmarkDistanceComparator());
	}

	protected MyLocationProvider mLocationProvider;
	private ActionMode mActionMode;
	private ListView mListView;
	private StateHelper mStateHelper;
	private final BookmarkGroupManager mBookmarkGroupManager;
	private final StopBookmarkManager mStopBookmarkManager;
	private final StopBookmarkViewManager mFavorisViewManager;
	private final SharedPreferences mPreferences;
	private final List<Integer> mSelectedGroups;
	private List<BookmarkGroup> mBookmarkGroups;
	private LoadSchedules mLoadSchedules;
	private int mCurrentSort = SORT_NAME;
	private boolean mContentHasChanged = false;

	/**
	 * Action sur les favoris.
	 */
	private final OnFavoriActionListener mOnFavoriActionListener = new OnFavoriActionListener() {
		@Override
		public void onImport() {
			if (DBG)
				Log.d(LOG_TAG, "onImport");

			if (isVisible()) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						refreshContent();
					}
				});
			} else {
				mContentHasChanged = true;
			}
		}

		@Override
		@TargetApi(Build.VERSION_CODES.FROYO)
		public void onAdd(final Stop item) {
			if (DBG)
				Log.d(LOG_TAG, "onAdd : " + item);
			mContentHasChanged = true;
		};

		@Override
		@TargetApi(Build.VERSION_CODES.FROYO)
		public void onRemove(final int id) {
			if (DBG)
				Log.d(LOG_TAG, "onRemove : " + id);
			mContentHasChanged = true;
		};
	};

	/**
	 * Reçoit les intents de notre intentFilter
	 */
	private final BroadcastReceiver intentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (DBG)
				Log.d(LOG_TAG, "onReceive : " + intent);

			final String action = intent.getAction();

			if (FavoriService.ACTION_EXPORTED.equals(action)) {
				// Notifier l'export
				final String key = intent.getStringExtra(FavoriService.INTENT_PARAM_KEY);
				final FavorisHelper favorisHelper = new FavorisHelper(getActivity());
				favorisHelper.showExportKey(key);
			} else {
				// Mise à jour des horaires

				final int id = intent.getIntExtra("id", -1);
				final Throwable throwable = (Throwable) intent.getSerializableExtra("throwable");

				if (throwable != null) {
					markeFavoriHoraireError(id);
				} else if (id != -1) {
					loadHorairesFavoris(id);
				} else {
					loadHorairesFavoris();
				}
			}

		}
	};

	private final ContentObserver mGroupesContentObserver = new ContentObserver(new Handler()) {
		@Override
		@SuppressLint("NewApi")
		public void onChange(final boolean selfChange) {
			if (DBG)
				Log.d(LOG_TAG, "GroupesContentObserver onChange selfChange : " + selfChange);

			initGroupes();
			getSherlockActivity().invalidateOptionsMenu();

			mContentHasChanged = true;
		};
	};

	private final ContentObserver mFavorisGroupesContentObserver = new ContentObserver(new Handler()) {
		@Override
		@SuppressLint("NewApi")
		public void onChange(final boolean selfChange) {
			if (DBG)
				Log.d(LOG_TAG, "mFavorisGroupesContentObserver onChange selfChange : " + selfChange);

			if (isVisible()) {
				refreshContent();
			} else {
				mContentHasChanged = true;
			}

		};
	};

	public StopBookmarksFragment() {
		super(R.layout.fragment_listview_section);

		if (DBG)
			Log.i(LOG_TAG, "FavorisFragment()");

		mStopBookmarkManager = StopBookmarkManager.getInstance();
		mFavorisViewManager = StopBookmarkViewManager.getInstance();
		mLocationProvider = NBApplication.getLocationProvider();
		mBookmarkGroupManager = BookmarkGroupManager.getInstance();

		mPreferences = NBApplication.getPreferences();

		mSelectedGroups = new ArrayList<Integer>();
	}

	@TargetApi(Build.VERSION_CODES.FROYO)
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (DBG)
			Log.d(LOG_TAG, "onCreate");

		setHasOptionsMenu(true);

		setEmptyMessageValues(R.string.no_bookmark, R.string.slide_navigate_between_lines, R.drawable.favori);

		mStopBookmarkManager.addActionListener(mOnFavoriActionListener);
		mLocationProvider.addListener(this);
		// Initaliser le comparator avec la position actuelle.
		onLocationChanged(mLocationProvider.getLastKnownLocation());

		// Gestion du tri par défaut
		mStateHelper = new StateHelper(getActivity());
		mCurrentSort = mStateHelper.getSortType(this, SORT_NAME);

		initGroupes();
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (DBG)
			Log.d(LOG_TAG, "onActivityCreated");

		mListView = getListView();
		mListView.setOnItemLongClickListener(this);
		mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		final ContentResolver contentResolver = getActivity().getContentResolver();
		contentResolver.registerContentObserver(StopBookmarkGroupProvider.CONTENT_URI, true, mGroupesContentObserver);
		contentResolver.registerContentObserver(StopBookmarkGroupLinkProvider.CONTENT_URI, true,
				mFavorisGroupesContentObserver);

		loadContent();
	}

	@Override
	public void onResume() {
		if (DBG)
			Log.d(LOG_TAG, "onResume");
		super.onResume();

		getActivity().registerReceiver(intentReceiver, intentFilter);

		if (mContentHasChanged) {
			refreshContent();
		} else {
			loadHorairesFavoris();
		}

	}

	@Override
	public void onPause() {
		if (DBG)
			Log.d(LOG_TAG, "onPause");

		mContentHasChanged = false;
		getActivity().unregisterReceiver(intentReceiver);
		super.onPause();
	}

	@Override
	public void onStop() {
		if (DBG)
			Log.d(LOG_TAG, "onStop");

		mStateHelper.setSortType(this, mCurrentSort);

		mLocationProvider.removeListener(this);

		if (mLoadSchedules != null)
			mLoadSchedules.cancel(true);

		super.onStop();
	}

	@Override
	public void onDestroy() {
		if (DBG)
			Log.d(LOG_TAG, "onDestroy");

		mStopBookmarkManager.removeActionListener(mOnFavoriActionListener);
		final ContentResolver contentResolver = getActivity().getContentResolver();
		contentResolver.unregisterContentObserver(mGroupesContentObserver);
		contentResolver.unregisterContentObserver(mFavorisGroupesContentObserver);
		super.onDestroy();
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_stop_bookmarks, menu);
		menu.findItem(MENU_MAPPING.get(mCurrentSort)).setChecked(true);

		final SubMenu groupesSubMenu = menu.findItem(R.id.menu_group).getSubMenu();
		fillGroupesMenu(groupesSubMenu);

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onPrepareOptionsMenu(final Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menu.findItem(R.id.menu_export).setVisible(mListView.getCount() > 0);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		if (item.getGroupId() == MENU_GROUP_GROUPS) {
			item.setChecked(!item.isChecked());
			mPreferences.edit().putBoolean(PREF_GROUPES + item.getItemId(), item.isChecked()).commit();
			if (item.isChecked()) {
				mSelectedGroups.add(item.getItemId());
			} else {
				mSelectedGroups.remove((Object) item.getItemId());
			}

			if (mSelectedGroups.isEmpty()) {
				setEmptyMessageValues(R.string.no_group, R.string.no_groups_selected,
						R.drawable.favori);
			} else {
				setEmptyMessageValues(R.string.no_bookmark, R.string.slide_navigate_between_lines,
						R.drawable.favori);
			}

			refreshContent();
			return true;
		}

		switch (item.getItemId()) {
		case R.id.menu_import:
			final FavorisHelper importHelper = new FavorisHelper(getActivity());
			importHelper.importFavoris();
			break;
		case R.id.menu_export:
			final FavorisHelper exportHelper = new FavorisHelper(getActivity());
			exportHelper.exportFavoris();
			break;
		case R.id.menu_sort_distance:
			item.setChecked(true);
			menuSort(SORT_DISTANCE);
			break;
		case R.id.menu_sort_name:
			item.setChecked(true);
			menuSort(SORT_NAME);
			break;
		case R.id.menu_group_manage:
			getActivity().startActivity(new Intent(getActivity(), BookmarkGroupsActivity.class));
			break;
		default:
			return false;
		}

		return true;
	}

	private void menuEdit() {
		final StopBookmark item = getFirstSelectedItem();
		if (item != null) {
			final FavorisHelper favorisUtils = new FavorisHelper(getActivity(), new FavorisActionListener() {
				@Override
				public void onFavoriRenamed(final StopBookmark newItem) {
					final StopBookmarkArrayAdapter adapter = (StopBookmarkArrayAdapter) getListAdapter();
					item.setBookmarkName(newItem.getBookmarkName());
					adapter.notifyDataSetChanged();
				}
			});
			favorisUtils.renameFavori(item.getId());
		}
	}

	@SuppressLint("NewApi")
	private void menuDelete() {
		StopBookmark item;
		final ContentResolver contentResolver = getActivity().getContentResolver();
		final StopBookmarkArrayAdapter adapter = (StopBookmarkArrayAdapter) getListAdapter();

		for (int i = mListView.getCount() - 1; i > -1; i--) {
			if (mListView.isItemChecked(i)) {
				item = adapter.getItem(i);
				adapter.remove(item);
				mStopBookmarkManager.removeFavori(contentResolver, item.getId());
			}
		}

		adapter.notifyDataSetChanged();
		getSherlockActivity().invalidateOptionsMenu();
	}

	private void menuPlace() {
		final StopBookmark item = getFirstSelectedItem();
		final Intent intent = new Intent(getActivity(), MapActivity.class);
		intent.putExtra(MapFragment.PARAM_ITEM_ID, item.getIdStation());
		intent.putExtra(MapFragment.PARAM_ITEM_TYPE, TypeOverlayItem.TYPE_STATION.getId());
		startActivity(intent);
	}

	private void menuShowPlan() {
		final StopBookmark item = getFirstSelectedItem();
		final Intent intent = new Intent(getActivity(), PlanActivity.class);
		intent.putExtra(PlanActivity.PARAM_CODE_LIGNE, item.getCodeLigne());
		startActivity(intent);
	}

	private void menuSort(final int sortOrder) {
		mCurrentSort = sortOrder;
		sort();
	}

	private StopBookmark getFirstSelectedItem() {
		final SparseBooleanArray checkedPositions = mListView.getCheckedItemPositions();
		for (int i = 0; i < checkedPositions.size(); i++) {
			if (checkedPositions.valueAt(i)) {
				return (StopBookmark) mListView.getItemAtPosition(checkedPositions.keyAt(i));
			}
		}
		return null;
	}

	private int getCheckedItemsCount() {
		final SparseBooleanArray checkedPositions = mListView.getCheckedItemPositions();
		int count = 0;
		for (int i = 0; i < checkedPositions.size(); i++) {
			if (checkedPositions.valueAt(i)) {
				count++;
			}
		}
		return count;
	}

	private boolean hasItemChecked() {
		final SparseBooleanArray checked = mListView.getCheckedItemPositions();
		for (int i = 0; i < checked.size(); i++) {
			if (checked.valueAt(i))
				return true;
		}
		return false;
	}

	private List<Integer> getCheckedItemsIds() {
		final List<Integer> ids = new ArrayList<Integer>();
		final SparseBooleanArray checked = mListView.getCheckedItemPositions();
		for (int i = 0; i < checked.size(); i++) {
			if (checked.valueAt(i)) {
				final StopBookmark item = (StopBookmark) mListView.getItemAtPosition(checked.keyAt(i));
				ids.add(item.getId());
			}
		}

		return ids;
	}

	private void fillGroupesMenu(final SubMenu filterSubMenu) {
		boolean checked;

		for (final BookmarkGroup groupe : mBookmarkGroups) {
			final MenuItem item = filterSubMenu.add(MENU_GROUP_GROUPS, groupe.getId(), 0, groupe.getName());
			checked = mSelectedGroups.contains(groupe.getId());

			item.setCheckable(true);
			item.setChecked(checked);
		}
	}

	private void initGroupes() {
		boolean checked;

		mBookmarkGroups = mBookmarkGroupManager.getAll(getActivity().getContentResolver());

		if (DBG)
			Log.d(LOG_TAG, "Groupes : " + Arrays.toString(mBookmarkGroups.toArray()));

		mSelectedGroups.clear();
		for (final BookmarkGroup groupe : mBookmarkGroups) {
			checked = mPreferences.getBoolean(PREF_GROUPES + groupe.getId(), true);
			if (checked) {
				mSelectedGroups.add(groupe.getId());
			}
		}

		if (mBookmarkGroups.isEmpty()) {
			setEmptyMessageValues(R.string.no_bookmark, R.string.slide_navigate_between_lines,
					R.drawable.favori);
		} else if (mSelectedGroups.isEmpty()) {
			setEmptyMessageValues(R.string.no_group, R.string.no_groups_selected,
					R.drawable.favori);
		}
	}

	/**
	 * Trier les favoris selon les préférences.
	 */
	private void sort() {
		final StopBookmarkArrayAdapter adapter = (StopBookmarkArrayAdapter) getListAdapter();
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
	private void sort(final StopBookmarkArrayAdapter adapter) {
		final Comparator<StopBookmark> comparator;

		if (mCurrentSort == SORT_DISTANCE && !mLocationProvider.isProviderEnabled()) {
			// Tri par défaut si pas le localisation
			comparator = comparators.get(SORT_NAME);
		} else {
			comparator = comparators.get(mCurrentSort);
		}

		adapter.sort(comparator);
	}

	public void onItemSelected() {
		final SparseBooleanArray checkedPositions = mListView.getCheckedItemPositions();
		final StopBookmarkArrayAdapter adapter = (StopBookmarkArrayAdapter) getListAdapter();
		adapter.setCheckedItemPositions(checkedPositions);

		if (mActionMode == null) {
			getSherlockActivity().startActionMode(this);
		} else if (hasItemChecked() == false) {
			mActionMode.finish();
		} else {
			mActionMode.invalidate();
		}
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
		if (mActionMode == null) {
			mListView.setItemChecked(position, false);
			final StopBookmark item = (StopBookmark) l.getItemAtPosition(position);

			final Intent intent = new Intent(getActivity(), StopDetailActivity.class);
			intent.putExtra(StopDetailActivity.PARAM_ARRET, item);

			startActivity(intent);
		} else {
			onItemSelected();
		}
	}

	@Override
	public boolean onItemLongClick(final AdapterView<?> adapter, final View view, final int position, final long id) {
		mListView.setItemChecked(position, !mListView.isItemChecked(position));
		onItemSelected();
		return true;
	}

	@Override
	protected AsyncResult<ListAdapter> loadContent(final Context context, final Bundle bundle) {
		if (DBG)
			Log.d(LOG_TAG, "loadContent");

		final ScheduleManager horaireManager = ScheduleManager.getInstance();

		final AsyncResult<ListAdapter> result = new AsyncResult<ListAdapter>();
		final List<StopBookmark> favoris = mFavorisViewManager.getAll(context.getContentResolver(), mSelectedGroups);
		Collections.sort(favoris, comparators.get(mCurrentSort));

		int position = 0;
		for (final StopBookmark favori : favoris) {
			if (getActivity() != null) {
				favori.setDelay(FavorisUtil.formatDelayLoading(getActivity(), favori.getNextSchedule()));
			}

			if (favori.getNextSchedule() == null) {
				final NextHoraireTask horaireTask = new NextHoraireTask();
				horaireTask.setContext(context);
				horaireTask.setArret(favori);
				horaireTask.setId(position);
				horaireTask.setLimit(1);
				horaireTask.setActionCallback(ACTION_UPDATE_DELAYS);

				horaireManager.schedule(horaireTask);
			}
			position++;
		}

		final StopBookmarkArrayAdapter adapter = new StopBookmarkArrayAdapter(context, favoris);

		if (mBookmarkGroups.isEmpty() == false) {
			final SparseArray<String> groupes = new SparseArray<String>();
			for (final BookmarkGroup groupe : mBookmarkGroups) {
				groupes.append(groupe.getId(), groupe.getName());
			}
			adapter.setIndexer(new StopBookmarksArrayIndexer(groupes));
		}

		result.setResult(adapter);

		return result;
	}

	@SuppressLint("NewApi")
	@Override
	public void onLoadFinished(final Loader<AsyncResult<ListAdapter>> loader, final AsyncResult<ListAdapter> result) {
		super.onLoadFinished(loader, result);
		getSherlockActivity().invalidateOptionsMenu();
	}

	/**
	 * Lancer le chargement des tous les horaires.
	 */
	private void loadHorairesFavoris(final Integer... params) {
		if (getListAdapter() != null) {
			mLoadSchedules = (LoadSchedules) new LoadSchedules().execute(params);
		}
	}

	/**
	 * Classe de chargement des horaires.
	 */
	private class LoadSchedules extends AsyncTask<Integer, Void, Boolean> {

		final ScheduleManager horaireManager = ScheduleManager.getInstance();
		final DateMidnight today = new DateMidnight();

		@Override
		protected void onPreExecute() {
			if (DBG)
				Log.d(LOG_TAG, "LoadHoraires start");
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(final Integer... params) {
			Boolean result = true;

			try {
				if (params.length == 0) {
					for (int i = 0; i < getListAdapter().getCount(); i++) {
						updateAdapter(i);
					}
				} else {
					for (int i = 0; i < params.length; i++) {
						updateAdapter(params[i]);
					}
				}
			} catch (final IOException e) {
				result = false;
			}

			return result;
		}

		/**
		 * Mettre à jour la route de l'adapter
		 * 
		 * @throws IOException
		 */
		private void updateAdapter(final int position) throws IOException {
			if (position >= getListAdapter().getCount() || !isAdded() || getActivity() == null)
				return;

			final StopBookmark favori = (StopBookmark) getListAdapter().getItem(position);
			if (horaireManager.isInDB(getActivity().getContentResolver(), favori, today)) {
				final Integer delay = horaireManager.getMinutesToNextSchedule(getActivity().getContentResolver(),
						favori);
				updateItemTime(favori, delay);
				publishProgress();
			}
		}

		/**
		 * Mettre à jour les informations de délais d'un favori
		 */
		private void updateItemTime(final StopBookmark favori, final Integer delay) {
			if (isDetached() || getActivity() == null)
				return;

			favori.setDelay(FavorisUtil.formatDelayNoDeparture(getActivity(), delay));
		}

		@Override
		protected void onProgressUpdate(final Void... values) {
			if (!isAdded() || getActivity() == null)
				return;

			((StopBookmarkArrayAdapter) getListAdapter()).notifyDataSetChanged();
		}

		@Override
		protected void onPostExecute(final Boolean result) {
		}

	}

	/**
	 * Indiquer que le chargement des horaires de ce favori à posé problème.
	 * 
	 * @param position
	 *            La position du favori.
	 */
	private void markeFavoriHoraireError(final int position) {
		StopBookmarkArrayAdapter adapter;
		if ((adapter = (StopBookmarkArrayAdapter) getListAdapter()) != null) {
			final StopBookmark favori = (StopBookmark) getListAdapter().getItem(position);
			favori.setDelay(getString(R.string.not_available));
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
		final MenuInflater menuInflater = getSherlockActivity().getSupportMenuInflater();
		menuInflater.inflate(R.menu.fragment_stop_bookmarks_contextual, menu);
		return true;
	}

	@Override
	public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
		mActionMode = mode;
		final MenuItem menuEdit = menu.findItem(R.id.menu_edit);
		final MenuItem menuPlace = menu.findItem(R.id.menu_place);
		final MenuItem menuShowPlan = menu.findItem(R.id.menu_show_plan);
		final MenuItem menuGroupes = menu.findItem(R.id.menu_group);

		final int checkedItems = getCheckedItemsCount();
		mActionMode.setTitle(getResources().getQuantityString(R.plurals.selected_items, checkedItems, checkedItems));

		menuGroupes.setVisible(mBookmarkGroups.isEmpty() == false);

		if (checkedItems < 2) {
			menuEdit.setVisible(true);
			menuPlace.setVisible(true);
			menuShowPlan.setVisible(true);
		} else {
			menuEdit.setVisible(false);
			menuPlace.setVisible(false);
			menuShowPlan.setVisible(false);
		}
		return false;
	}

	@Override
	public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_edit:
			menuEdit();
			mode.finish();
			break;
		case R.id.menu_delete:
			menuDelete();
			mode.finish();
			break;
		case R.id.menu_place:
			menuPlace();
			mode.finish();
			break;
		case R.id.menu_show_plan:
			menuShowPlan();
			mode.finish();
			break;
		case R.id.menu_group:
			final GroupesHelper helper = new GroupesHelper(getActivity());
			helper.linkFavori(getCheckedItemsIds(), new Runnable() {
				@Override
				public void run() {
					mode.finish();
				}
			});
			break;
		default:
			return false;
		}

		return true;
	}

	@Override
	public void onDestroyActionMode(final ActionMode mode) {
		mActionMode = null;
		mListView.clearChoices();

		final StopBookmarkArrayAdapter adapter = (StopBookmarkArrayAdapter) getListAdapter();
		adapter.clearCheckedItemPositions();
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onLocationChanged(final Location location) {
		final StopBookmarkDistanceComparator comparator = (StopBookmarkDistanceComparator) comparators
				.get(SORT_DISTANCE);
		comparator.setReferentiel(location);
		if (mCurrentSort == SORT_DISTANCE) {
			sort();
		}
	}

	@Override
	public void onLocationDisabled() {
		final StopBookmarkDistanceComparator comparator = (StopBookmarkDistanceComparator) comparators
				.get(SORT_DISTANCE);
		comparator.setReferentiel(null);
		if (mCurrentSort == SORT_DISTANCE) {
			mCurrentSort = SORT_NAME;
			sort();
		}
	}

}
