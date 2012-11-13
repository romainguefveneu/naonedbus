package net.naonedbus.fragment.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.activity.impl.FavorisImportActivity;
import net.naonedbus.activity.impl.HoraireActivity;
import net.naonedbus.activity.impl.MapActivity;
import net.naonedbus.activity.impl.PlanActivity;
import net.naonedbus.activity.map.overlay.TypeOverlayItem;
import net.naonedbus.bean.Arret;
import net.naonedbus.bean.Favori;
import net.naonedbus.bean.NextHoraireTask;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.comparator.FavoriComparator;
import net.naonedbus.comparator.FavoriDistanceComparator;
import net.naonedbus.fragment.CustomFragmentActions;
import net.naonedbus.fragment.CustomListFragment;
import net.naonedbus.helper.FavorisHelper;
import net.naonedbus.helper.FavorisHelper.FavorisActionListener;
import net.naonedbus.intent.ParamIntent;
import net.naonedbus.manager.impl.FavoriManager;
import net.naonedbus.manager.impl.FavoriManager.OnActionListener;
import net.naonedbus.manager.impl.HoraireManager;
import net.naonedbus.provider.impl.MyLocationProvider;
import net.naonedbus.provider.impl.MyLocationProvider.MyLocationListener;
import net.naonedbus.widget.adapter.impl.FavoriArrayAdapter;

import org.joda.time.DateMidnight;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.bugsense.trace.BugSenseHandler;
import com.google.gson.JsonSyntaxException;

public class FavorisFragment extends CustomListFragment implements CustomFragmentActions, OnItemLongClickListener,
		MyLocationListener, ActionMode.Callback {

	private static final String LOG_TAG = FavorisFragment.class.getSimpleName();

	private static final String FORMAT_DELAY_MIN = "dans %d min";
	private static final String FORMAT_DELAY_HOUR = "dans %d h";
	private static final String ACTION_UPDATE_DELAYS = "net.naonedbus.action.UPDATE_DELAYS";
	private static final Integer MIN_HOUR = 60;
	private static final Integer MIN_DURATION = 0;

	private final static int SORT_NOM = R.id.menu_sort_name;
	private final static int SORT_DISTANCE = R.id.menu_sort_distance;

	private final static IntentFilter intentFilter;
	static {
		intentFilter = new IntentFilter();
		intentFilter.addAction(FavorisFragment.ACTION_UPDATE_DELAYS);
		intentFilter.addAction(Intent.ACTION_TIME_TICK);
		intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
	}

	private final static SparseArray<Comparator<Favori>> comparators = new SparseArray<Comparator<Favori>>();
	static {
		comparators.append(SORT_NOM, new FavoriComparator());
		comparators.append(SORT_DISTANCE, new FavoriDistanceComparator());
	}

	protected MyLocationProvider mLocationProvider;
	private ActionMode mActionMode;
	private ListView mListView;
	private SharedPreferences mSharedPreferences;
	private int mCurrentSort = SORT_NOM;

	/**
	 * Action sur les favoris.
	 */
	private OnActionListener onImportListener = new OnActionListener() {
		@Override
		public void onImport() {
			refreshContent();
		}

		public void onAdd(Arret item) {
			refreshContent();
		};

		public void onRemove(int id) {
			final FavoriArrayAdapter adapter;
			if ((adapter = (FavoriArrayAdapter) getListAdapter()) != null)
				adapter.notifyDataSetChanged();
		};
	};

	/**
	 * Reçoit les intents de notre intentFilter
	 */
	private final BroadcastReceiver intentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(LOG_TAG, "onReceive : " + intent);
			final int id = intent.getIntExtra("id", -1);
			if (id != -1) {
				forceLoadHorairesFavoris(id);
			} else {
				loadHorairesFavoris();
			}
		}
	};

	private FavoriManager mFavoriManager;

	public FavorisFragment() {
		super(R.string.title_fragment_favoris, R.layout.fragment_favoris);
		mFavoriManager = FavoriManager.getInstance();
		mLocationProvider = NBApplication.getLocationProvider();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_import:
			startActivity(new Intent(getActivity(), FavorisImportActivity.class));
			break;
		case R.id.menu_sort_distance:
			item.setChecked(true);
			menuSort(SORT_DISTANCE);
			break;
		case R.id.menu_sort_name:
			item.setChecked(true);
			menuSort(SORT_NOM);
			break;
		default:
			return false;
		}

		return true;
	}

	private void updateSortPref() {
		final Editor editor = mSharedPreferences.edit();
		editor.putInt(NBApplication.PREF_FAVORIS_SORT, mCurrentSort);
		editor.commit();
	}

	private int getSortPref() {
		return mSharedPreferences.getInt(NBApplication.PREF_FAVORIS_SORT, SORT_NOM);
	}

	private void menuEdit() {
		final Favori item = getFirstSelectedItem();
		if (item != null) {
			final FavorisHelper favorisUtils = new FavorisHelper(getActivity(), new FavorisActionListener() {
				@Override
				public void onFavoriRenamed(Favori newItem) {
					final FavoriArrayAdapter adapter = (FavoriArrayAdapter) getListAdapter();
					item.nomFavori = newItem.nomFavori;
					adapter.notifyDataSetChanged();
				}
			});
			favorisUtils.renameFavori(item._id);
		}
	}

	private void menuDelete() {
		Favori item;
		final ContentResolver contentResolver = getActivity().getContentResolver();
		final FavoriArrayAdapter adapter = (FavoriArrayAdapter) getListAdapter();

		for (int i = mListView.getCount() - 1; i > -1; i--) {
			if (mListView.isItemChecked(i)) {
				item = adapter.getItem(i);
				adapter.remove(item);
				mFavoriManager.removeFavori(contentResolver, item._id);
			}
		}

		adapter.notifyDataSetChanged();
	}

	private void menuPlace() {
		final Favori item = getFirstSelectedItem();
		final ParamIntent intent = new ParamIntent(getActivity(), MapActivity.class);
		intent.putExtra(MapActivity.Param.itemId, item.idStation);
		intent.putExtra(MapActivity.Param.itemType, TypeOverlayItem.TYPE_STATION.getId());
		startActivity(intent);
	}

	private void menuShowPlan() {
		final Favori item = getFirstSelectedItem();
		final ParamIntent intent = new ParamIntent(getActivity(), PlanActivity.class);
		intent.putExtra(PlanActivity.Param.codeLigne, item.codeLigne);
		startActivity(intent);
	}

	private void menuSort(int sortOrder) {
		mCurrentSort = sortOrder;
		updateSortPref();
		sort();
	}

	private Favori getFirstSelectedItem() {
		final SparseBooleanArray checkedPositions = mListView.getCheckedItemPositions();
		for (int i = 0; i < checkedPositions.size(); i++) {
			if (checkedPositions.valueAt(i)) {
				return (Favori) mListView.getItemAtPosition(checkedPositions.keyAt(i));
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
		boolean hasCheckedElement = false;
		for (int i = 0; i < checked.size() && !hasCheckedElement; i++) {
			hasCheckedElement = checked.valueAt(i);
		}
		return hasCheckedElement;
	}

	/**
	 * Trier les parkings selon les préférences.
	 */
	private void sort() {
		final FavoriArrayAdapter adapter = (FavoriArrayAdapter) getListAdapter();
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
	private void sort(FavoriArrayAdapter adapter) {
		final Comparator<Favori> comparator;
		// final CustomSectionIndexer<Equipement> indexer;

		if (mCurrentSort == SORT_DISTANCE && !mLocationProvider.isProviderEnabled()) {
			// Tri par défaut si pas le localisation
			comparator = comparators.get(SORT_NOM);
			// indexer = indexers.get(SORT_NOM);
		} else {
			comparator = comparators.get(mCurrentSort);
			// indexer = indexers.get(currentSortPreference);
		}

		adapter.sort(comparator);
		// adapter.setIndexer(indexer);
	}

	public void onItemSelected() {
		if (mActionMode == null) {
			getSherlockActivity().startActionMode(this);
		} else if (hasItemChecked() == false) {
			mActionMode.finish();
		} else {
			mActionMode.invalidate();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setEmptyMessageValues(R.string.error_title_empty_favori, R.string.error_summary_empty_favori, R.drawable.favori);

		mFavoriManager.setActionListener(onImportListener);
		mLocationProvider.addListener(this);
		// Initaliser le comparator avec la position actuelle.
		onLocationChanged(mLocationProvider.getLastKnownLocation());

		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		mCurrentSort = getSortPref();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.d(LOG_TAG, "onActivityCreated");

		super.onActivityCreated(savedInstanceState);
		mListView = getListView();
		mListView.setOnItemLongClickListener(this);
	}

	@Override
	public void onDestroy() {
		mFavoriManager.unsetActionListener();
		super.onDestroy();
	}

	@Override
	public void onStop() {
		mLocationProvider.removeListener(this);
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
		getActivity().registerReceiver(intentReceiver, intentFilter);
		loadHorairesFavoris();
	}

	@Override
	public void onPause() {
		getActivity().unregisterReceiver(intentReceiver);
		super.onPause();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu) {
		final MenuInflater menuInflater = getSherlockActivity().getSupportMenuInflater();
		menuInflater.inflate(R.menu.fragment_favoris, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.findItem(mCurrentSort).setChecked(true);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if (mActionMode == null) {
			final Favori item = (Favori) l.getItemAtPosition(position);
			final ParamIntent intent = new ParamIntent(getActivity(), HoraireActivity.class);
			intent.putExtra(HoraireActivity.Param.idArret, item._id);
			startActivity(intent);
		} else {
			mListView.setItemChecked(position, !mListView.isItemChecked(position));
			mListView.invalidate();
			onItemSelected();
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long id) {
		mListView.setItemChecked(position, !mListView.isItemChecked(position));
		mListView.invalidate();
		onItemSelected();
		return true;
	}

	@Override
	protected AsyncResult<ListAdapter> loadContent(final Context context) {
		final HoraireManager horaireManager = HoraireManager.getInstance();
		final DateMidnight today = new DateMidnight();

		final AsyncResult<ListAdapter> result = new AsyncResult<ListAdapter>();
		final List<Favori> favoris = mFavoriManager.getAll(context.getContentResolver());
		Collections.sort(favoris, comparators.get(mCurrentSort));

		int position = 0;
		for (Favori favori : favoris) {
			if (!horaireManager.isInDB(context.getContentResolver(), favori, today)) {
				final NextHoraireTask horaireTask = new NextHoraireTask();
				horaireTask.setContext(context).setArret(favori).setId(position).setLimit(1)
						.setActionCallback(ACTION_UPDATE_DELAYS);

				horaireManager.schedule(horaireTask);
			} else {
				loadHorairesFavoris(position);
			}
			position++;
		}

		final FavoriArrayAdapter adapter = new FavoriArrayAdapter(context, favoris);
		result.setResult(adapter);

		return result;
	}

	@Override
	protected void onPostExecute() {
		loadHorairesFavoris();
	}

	/**
	 * Lancer le chargement des tous les horaires
	 */
	private void loadHorairesFavoris(Integer... params) {
		if (getListAdapter() != null) {
			new LoadHoraires().execute(params);
		}
	}

	/**
	 * Lancer le chargement des tous les horaires, même si un thread est déjà en
	 * cours
	 */
	private void forceLoadHorairesFavoris(Integer... params) {
		if (getListAdapter() != null) {
			new LoadHoraires().execute(params);
		}
	}

	/**
	 * Classe de chargement des horaires
	 * 
	 * @author romain.guefveneu
	 * 
	 */
	private class LoadHoraires extends AsyncTask<Integer, Void, Boolean> {

		final HoraireManager horaireManager = HoraireManager.getInstance();
		final DateMidnight today = new DateMidnight();

		@Override
		protected void onPreExecute() {
			Log.d(LOG_TAG, "LoadHoraires start");
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(Integer... params) {
			Thread.currentThread().setName("LoadHoraires");
			Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
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
			} catch (JsonSyntaxException e) {
				BugSenseHandler.sendExceptionMessage("Erreur lors du chargement des horaires", null, e);
				result = false;
			} catch (IOException e) {
				BugSenseHandler.sendExceptionMessage("Erreur lors du chargement des horaires", null, e);
				result = false;
			}

			return result;
		}

		/**
		 * Mettre à jour la ligne de l'adapter
		 * 
		 * @throws IOException
		 */
		private void updateAdapter(int position) throws IOException {
			if (position >= getListAdapter().getCount())
				return;

			final Favori favori = (Favori) getListAdapter().getItem(position);
			if (horaireManager.isInDB(getActivity().getContentResolver(), favori, today)) {
				final Integer delay = horaireManager
						.getMinutesToNextHoraire(getActivity().getContentResolver(), favori);
				updateItemTime(favori, delay);
				publishProgress();
			}
		}

		/**
		 * Mettre à jour les informations de délais d'un favori
		 */
		private void updateItemTime(Favori favori, Integer delay) {
			if (delay != null) {
				if (delay >= MIN_DURATION) {
					if (delay == MIN_DURATION) {
						favori.delay = getString(R.string.msg_depart_proche);
					} else if (delay <= MIN_HOUR) {
						favori.delay = String.format(FORMAT_DELAY_MIN, delay);
					} else {
						favori.delay = String.format(FORMAT_DELAY_HOUR, (delay / MIN_HOUR));
					}
				}
			} else {
				favori.delay = getString(R.string.msg_aucun_depart_24h);
			}
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			((FavoriArrayAdapter) getListAdapter()).notifyDataSetChanged();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			Log.d(LOG_TAG, "LoadHoraires end");
		}

	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		final MenuInflater menuInflater = getSherlockActivity().getSupportMenuInflater();
		menuInflater.inflate(R.menu.fragment_favoris_contextuel, menu);
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		mActionMode = mode;
		final MenuItem menuEdit = menu.findItem(R.id.menu_edit);
		final MenuItem menuPlace = menu.findItem(R.id.menu_place);
		final MenuItem menuShowPlan = menu.findItem(R.id.menu_show_plan);

		final int checkedItems = getCheckedItemsCount();
		mActionMode.setTitle(getResources().getQuantityString(R.plurals.selected_items, checkedItems, checkedItems));

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
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_edit:
			menuEdit();
			break;
		case R.id.menu_delete:
			menuDelete();
			break;
		case R.id.menu_place:
			menuPlace();
			break;
		case R.id.menu_show_plan:
			menuShowPlan();
			break;
		default:
			return false;
		}
		mode.finish();
		return true;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		mActionMode = null;
		mListView.clearChoices();
	}

	@Override
	public void onLocationChanged(Location location) {
		final FavoriDistanceComparator comparator = (FavoriDistanceComparator) comparators.get(SORT_DISTANCE);
		comparator.setReferentiel(location);
		if (mCurrentSort == SORT_DISTANCE) {
			sort();
		}
	}

	@Override
	public void onLocationDisabled() {
		final FavoriDistanceComparator comparator = (FavoriDistanceComparator) comparators.get(SORT_DISTANCE);
		comparator.setReferentiel(null);
		if (mCurrentSort == SORT_DISTANCE) {
			mCurrentSort = SORT_NOM;
			sort();
		}
	}

}
