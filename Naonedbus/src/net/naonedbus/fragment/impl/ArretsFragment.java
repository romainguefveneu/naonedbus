package net.naonedbus.fragment.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.activity.impl.ArretsActivity.OnChangeSens;
import net.naonedbus.activity.impl.HoraireActivity;
import net.naonedbus.activity.impl.PlanActivity;
import net.naonedbus.bean.Arret;
import net.naonedbus.bean.Sens;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.comparator.ArretComparator;
import net.naonedbus.comparator.ArretOrdreComparator;
import net.naonedbus.fragment.CustomFragmentActions;
import net.naonedbus.fragment.CustomListFragment;
import net.naonedbus.helper.StateHelper;
import net.naonedbus.intent.ParamIntent;
import net.naonedbus.manager.impl.ArretManager;
import net.naonedbus.provider.impl.MyLocationProvider;
import net.naonedbus.provider.impl.MyLocationProvider.MyLocationListener;
import net.naonedbus.task.AddressResolverTask;
import net.naonedbus.task.AddressResolverTask.AddressTaskListener;
import net.naonedbus.widget.adapter.impl.ArretArrayAdapter;
import net.naonedbus.widget.adapter.impl.ArretArrayAdapter.ViewType;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class ArretsFragment extends CustomListFragment implements CustomFragmentActions, OnChangeSens,
		MyLocationListener, AddressTaskListener {

	private final static int SORT_NOM = R.id.menu_sort_name;
	private final static int SORT_ORDRE = R.id.menu_sort_ordre;

	private interface DistanceTaskCallback {
		void onNearestStationFound(Integer position);

		void onPostExecute();
	}

	protected final SparseArray<Comparator<Arret>> mComparators;
	protected int mCurrentSortPreference;

	private StateHelper mStateHelper;
	private MyLocationProvider mLocationProvider;
	private DistanceTask mDistanceTask;
	private DistanceTaskCallback mDistanceTaskCallback;
	private AddressResolverTask mAddressResolverTask;
	private Integer mNearestArretPosition;
	private ArretArrayAdapter mAdapter;

	private List<Arret> mArrets;

	private Sens mSens;

	public ArretsFragment() {
		super(R.string.title_fragment_arrets, R.layout.fragment_listview);
		mLocationProvider = NBApplication.getLocationProvider();
		mLocationProvider.addListener(this);

		mComparators = new SparseArray<Comparator<Arret>>();
		mComparators.append(SORT_NOM, new ArretComparator());
		mComparators.append(SORT_ORDRE, new ArretOrdreComparator());
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mStateHelper = new StateHelper(getActivity());
		mCurrentSortPreference = mStateHelper.getSortType(this, SORT_NOM);

		mArrets = new ArrayList<Arret>();
		mAdapter = new ArretArrayAdapter(getActivity(), mArrets);
		if (mCurrentSortPreference == SORT_ORDRE) {
			mAdapter.setViewType(ViewType.TYPE_METRO);
		}

		mDistanceTaskCallback = new DistanceTaskCallback() {
			@SuppressLint("NewApi")
			@Override
			public void onNearestStationFound(Integer position) {
				mNearestArretPosition = position;
				final ArretArrayAdapter adapter = (ArretArrayAdapter) getListAdapter();
				if (adapter != null && mNearestArretPosition != null) {
					adapter.setNearestPosition(mNearestArretPosition);
					adapter.notifyDataSetChanged();
				}
			}

			@Override
			public void onPostExecute() {
				final ArretArrayAdapter adapter = (ArretArrayAdapter) getListAdapter();
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
		mStateHelper.setSortType(this, mCurrentSortPreference);

		mLocationProvider.stop();
		if (mDistanceTask != null) {
			mDistanceTask.cancel(true);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu) {
		final SherlockFragmentActivity activity = getSherlockActivity();
		if (activity != null) {
			final MenuInflater menuInflater = getSherlockActivity().getSupportMenuInflater();
			menuInflater.inflate(R.menu.fragment_arrets, menu);
			menu.findItem(mCurrentSortPreference).setChecked(true);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		item.setChecked(true);

		switch (item.getItemId()) {
		case R.id.menu_sort_name:
			changeSortOrder(SORT_NOM, ViewType.TYPE_STANDARD);
			break;
		case R.id.menu_sort_ordre:
			changeSortOrder(SORT_ORDRE, ViewType.TYPE_METRO);
			break;
		case R.id.menu_show_plan:
			menuShowPlan();
			break;
		case R.id.menu_location:
			menuLocation();
			break;
		}
		return false;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		final Arret item = (Arret) l.getItemAtPosition(position);
		final ParamIntent intent = new ParamIntent(getActivity(), HoraireActivity.class);
		intent.putExtra(HoraireActivity.Param.idArret, item._id);
		startActivity(intent);
	}

	private void menuShowPlan() {
		final ParamIntent intent = new ParamIntent(getActivity(), PlanActivity.class);
		intent.putExtra(PlanActivity.Param.codeLigne, mSens.codeLigne);
		startActivity(intent);
	}

	@TargetApi(11)
	private void menuLocation() {
		if (mNearestArretPosition != null) {
			final ListView listView = getListView();
			final int listViewHeight = listView.getHeight();
			final int itemHeight = listView.getChildAt(0).getHeight();

			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
				listView.setSelectionFromTop(mNearestArretPosition, (listViewHeight - itemHeight) / 2);
			} else {
				listView.smoothScrollToPositionFromTop(mNearestArretPosition, (listViewHeight - itemHeight) / 2);
			}

			loadAddress();
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
	private void changeSortOrder(int sortOrder, ViewType viewType) {
		final ArretArrayAdapter adapter = (ArretArrayAdapter) getListAdapter();
		mCurrentSortPreference = sortOrder;

		adapter.setViewType(viewType);
		getListView().setSelection(0);

		sort();
		loadDistances();
	}

	/**
	 * Trier les parkings selon les préférences.
	 */
	private void sort() {
		final ArretArrayAdapter adapter = (ArretArrayAdapter) getListAdapter();
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
	private void sort(ArretArrayAdapter adapter) {
		final Comparator<Arret> comparator = mComparators.get(mCurrentSortPreference);
		adapter.sort(comparator);
	}

	@Override
	protected AsyncResult<ListAdapter> loadContent(final Context context) {
		final AsyncResult<ListAdapter> result = new AsyncResult<ListAdapter>();
		try {
			final ArretManager arretManager = ArretManager.getInstance();
			final List<Arret> arrets = arretManager.getAll(context.getContentResolver(), mSens.codeLigne, mSens.code);

			mArrets.clear();
			mArrets.addAll(arrets);

			result.setResult(mAdapter);
		} catch (Exception e) {
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
	public void onChangeSens(Sens sens) {
		mSens = sens;
		refreshContent();
	}

	/**
	 * Lance la récupération de l'adresse courante.
	 * 
	 * @param location
	 */
	private void loadAddress() {
		if (mAddressResolverTask != null) {
			mAddressResolverTask.cancel(true);
		}
		mAddressResolverTask = (AddressResolverTask) new AddressResolverTask(this).execute();
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
		private ListAdapter mAdapter;
		private Location mCurrentLocation;

		public DistanceTask(final DistanceTaskCallback callback, final Location currentLocation,
				final ListAdapter adapter) {
			mCallback = callback;
			mCurrentLocation = currentLocation;
			mAdapter = adapter;
		}

		@Override
		protected Integer doInBackground(Void... params) {
			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

			Arret arret;
			Integer nearestPosition = null;
			Float nearestDistance = Float.MAX_VALUE;
			Float distance = null;
			final Location equipementLocation = new Location(LocationManager.GPS_PROVIDER);

			if (mCurrentLocation != null) {
				for (int i = 0; i < mAdapter.getCount(); i++) {
					arret = (Arret) mAdapter.getItem(i);
					equipementLocation.setLatitude(arret.getLatitude());
					equipementLocation.setLongitude(arret.getLongitude());

					distance = mCurrentLocation.distanceTo(equipementLocation);
					arret.distance = distance;

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
		protected void onPostExecute(Integer result) {
			if (!isCancelled() && mCallback != null) {
				mCallback.onPostExecute();
				mCallback.onNearestStationFound(result);
			}
		}

	}

	@Override
	public void onLocationChanged(Location location) {
		loadDistances();
	}

	@Override
	public void onLocationDisabled() {

	}

	@Override
	public void onAddressTaskPreExecute() {
	}

	@Override
	public void onAddressTaskResult(String address) {
		if (getActivity() != null && address != null && address.length() > 0) {
			Toast.makeText(getActivity(), address, Toast.LENGTH_LONG).show();
		}
	}

}
