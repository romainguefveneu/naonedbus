package net.naonedbus.fragment.impl;

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
import net.naonedbus.intent.ParamIntent;
import net.naonedbus.manager.impl.ArretManager;
import net.naonedbus.provider.impl.MyLocationProvider;
import net.naonedbus.provider.impl.MyLocationProvider.MyLocationListener;
import net.naonedbus.widget.adapter.impl.ArretArrayAdapter;
import net.naonedbus.widget.adapter.impl.ArretArrayAdapter.ViewType;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class ArretsFragment extends CustomListFragment implements CustomFragmentActions, OnChangeSens,
		MyLocationListener {

	private final static int SORT_NOM = R.id.menu_sort_name;
	private final static int SORT_ORDRE = R.id.menu_sort_ordre;
	private final static float SMOOTH_SCROLL_MIN_DISTANCE = 500f;

	private interface NearestStationCallback {
		void onNearestStationFound(Integer position);
	}

	protected final SparseArray<Comparator<Arret>> mComparators;

	protected MyLocationProvider mLocationProvider;
	protected int mCurrentSortPreference = SORT_ORDRE;

	private Sens mSens;

	private FindNearestStationTask mFindNearestStationTask;
	private NearestStationCallback mNearestStationCallback;

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
		mNearestStationCallback = new NearestStationCallback() {
			@SuppressLint("NewApi")
			@Override
			public void onNearestStationFound(Integer position) {
				if (position != null) {
					// if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
					getListView().setSelection(position);
					// } else {
					// getListView().smoothScrollToPosition(position);
					// }
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
		mLocationProvider.stop();
		if (mFindNearestStationTask != null) {
			mFindNearestStationTask.cancel(true);
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
		final ArretArrayAdapter adapter = (ArretArrayAdapter) getListAdapter();

		switch (item.getItemId()) {
		case R.id.menu_sort_name:
			mCurrentSortPreference = SORT_NOM;
			adapter.setViewType(ViewType.TYPE_STANDARD);
			sort();
			break;
		case R.id.menu_sort_ordre:
			mCurrentSortPreference = SORT_ORDRE;
			adapter.setViewType(ViewType.TYPE_METRO);
			sort();
			break;
		case R.id.menu_show_plan:
			menuShowPlan();
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

	/**
	 * Trier les parkings selon les préférences.
	 */
	private void sort() {
		final ArretArrayAdapter adapter = (ArretArrayAdapter) getListAdapter();
		sort(adapter);
		adapter.notifyDataSetChanged();
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
		if (mSens == null)
			cancelLoading();

		final AsyncResult<ListAdapter> result = new AsyncResult<ListAdapter>();
		try {
			final ArretManager arretManager = ArretManager.getInstance();
			final List<Arret> arrets = arretManager.getAll(context.getContentResolver(), mSens.codeLigne, mSens.code);
			final ArretArrayAdapter adapter = new ArretArrayAdapter(context, arrets);

			result.setResult(adapter);
		} catch (Exception e) {
			result.setException(e);
		}
		return result;
	}

	@Override
	protected void onPostExecute() {
		sort();

		if (mFindNearestStationTask != null) {
			mFindNearestStationTask.cancel(true);
		}
		mFindNearestStationTask = (FindNearestStationTask) new FindNearestStationTask(mNearestStationCallback,
				mLocationProvider.getLastKnownLocation(), getListAdapter()).execute();
	}

	@Override
	public void onChangeSens(Sens sens) {
		mSens = sens;
		refreshContent();
	}

	private class FindNearestStationTask extends AsyncTask<Void, Void, Integer> {

		private NearestStationCallback mNearestStationCallback;
		private ListAdapter mAdapter;
		private Location mCurrentLocation;

		public FindNearestStationTask(final NearestStationCallback callback, final Location currentLocation,
				final ListAdapter adapter) {
			mNearestStationCallback = callback;
			mCurrentLocation = currentLocation;
			mAdapter = adapter;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			mNearestStationCallback = null;
		}

		@Override
		protected Integer doInBackground(Void... params) {
			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

			final int count = mAdapter.getCount();
			Arret arret;
			Integer nearestPosition = null;
			Float nearestDistance = Float.MAX_VALUE;
			Float distance = null;

			final Location arretLocation = new Location(LocationManager.GPS_PROVIDER);

			for (int i = 0; i < count - 1; i++) {
				arret = (Arret) mAdapter.getItem(i);

				arretLocation.setLatitude(arret.getLatitude());
				arretLocation.setLongitude(arret.getLongitude());

				distance = arretLocation.distanceTo(mCurrentLocation);
				if (distance < nearestDistance) {
					nearestDistance = distance;
					nearestPosition = i;
				}

				if (isCancelled()) {
					break;
				}
			}

			if (nearestDistance < SMOOTH_SCROLL_MIN_DISTANCE) {
				return nearestPosition;
			} else {
				return null;
			}
		}

		@Override
		protected void onPostExecute(Integer result) {
			if (!isCancelled() && mNearestStationCallback != null) {
				mNearestStationCallback.onNearestStationFound(result);
			}
		}

	}

	@Override
	public void onLocationChanged(Location location) {
	}

	@Override
	public void onLocationDisabled() {

	}

}
