package net.naonedbus.fragment.impl;

import java.util.ArrayList;
import java.util.List;

import net.naonedbus.R;
import net.naonedbus.activity.impl.MapActivity;
import net.naonedbus.activity.map.overlay.TypeOverlayItem;
import net.naonedbus.bean.Arret;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.bean.horaire.Horaire;
import net.naonedbus.fragment.CustomInfiniteListFragement;
import net.naonedbus.intent.ParamIntent;
import net.naonedbus.manager.impl.ArretManager;
import net.naonedbus.manager.impl.HoraireManager;
import net.naonedbus.widget.adapter.impl.HoraireArrayAdapter;
import net.naonedbus.widget.indexer.impl.HoraireIndexer;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.Minutes;

import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.ListAdapter;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class HorairesFragment extends CustomInfiniteListFragement {

	private static final String ACTION_UPDATE_DELAYS = "net.naonedbus.action.UPDATE_DELAYS";
	public static final String PARAM_ID_ARRET = "idArret";

	private static final String formatDelay = "dans %d min";

	private final static IntentFilter intentFilter;
	static {
		intentFilter = new IntentFilter();
		intentFilter.addAction(HorairesFragment.ACTION_UPDATE_DELAYS);
		intentFilter.addAction(Intent.ACTION_TIME_TICK);
		intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
	}

	/**
	 * Reçoit les intents de notre intentFilter
	 */
	private final BroadcastReceiver intentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateItemsTime();
		}
	};

	private final HoraireManager mHoraireManager;
	private final ArretManager mArretManager;
	private HoraireArrayAdapter mAdapter;
	private List<Horaire> mHoraires;

	private Arret mArret;
	private boolean mIsFirstLoad = true;

	private DateMidnight mLastDayLoaded;
	private DateTime mLastDateTimeLoaded;

	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			changeDate(new DateMidnight(year, monthOfYear + 1, dayOfMonth));
		}
	};

	public HorairesFragment() {
		super(R.string.title_activity_horaires, R.layout.fragment_listview_section);
		mHoraireManager = HoraireManager.getInstance();
		mArretManager = ArretManager.getInstance();
		mHoraires = new ArrayList<Horaire>();

		mLastDayLoaded = new DateMidnight();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getActivity().registerReceiver(intentReceiver, intentFilter);

		mAdapter = new HoraireArrayAdapter(getActivity(), mHoraires);
		mAdapter.setIndexer(new HoraireIndexer());

		final int idArret = getArguments().getInt(PARAM_ID_ARRET);
		mArret = mArretManager.getSingle(getActivity().getContentResolver(), idArret);
	}

	@Override
	public void onDestroy() {
		getActivity().unregisterReceiver(intentReceiver);
		super.onDestroy();
	}

	@Override
	protected void onLoadMoreItems() {
		if (mLastDayLoaded != null) {
			loadHoraires(mLastDayLoaded.plusDays(1));
		}
	}

	private void loadHoraires(DateMidnight date) {
		if (!getLoaderManager().hasRunningLoaders()) {
			mLastDayLoaded = date;
			refreshContent();
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu) {
		final MenuInflater menuInflater = getSherlockActivity().getSupportMenuInflater();
		menuInflater.inflate(R.menu.fragment_horaires, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_place:
			showArretPlan();
			break;
		case R.id.menu_date:
			changeDate();
			break;
		case R.id.menu_date_maintenant:
			changeDateToNow();
			break;
		default:
			break;
		}
		return false;
	}

	private void changeDate() {
		final DatePickerDialog dateDialog = new DatePickerDialog(getActivity(), mDateSetListener,
				mLastDayLoaded.getYear(), mLastDayLoaded.getMonthOfYear() - 1, mLastDayLoaded.getDayOfMonth());
		dateDialog.show();
	}

	protected void changeDateToNow() {
		changeDate(new DateMidnight());
	}

	protected void showArretPlan() {
		ParamIntent intent = new ParamIntent(getActivity(), MapActivity.class);
		intent.putExtra(MapActivity.Param.itemId, mArret.idStation);
		intent.putExtra(MapActivity.Param.itemType, TypeOverlayItem.TYPE_STATION.getId());
		startActivity(intent);
	}

	/**
	 * Clear data and reload with a new date
	 * 
	 * @param date
	 */
	public void changeDate(DateMidnight date) {
		mAdapter.clear();
		mIsFirstLoad = true;
		mLastDateTimeLoaded = null;

		setListAdapter(null);
		loadHoraires(date);
	}

	@Override
	protected AsyncResult<ListAdapter> loadContent(Context context) {
		final AsyncResult<ListAdapter> result = new AsyncResult<ListAdapter>();
		List<Horaire> data = new ArrayList<Horaire>();

		final int idArret = getArguments().getInt(PARAM_ID_ARRET);
		final Arret arret = mArretManager.getSingle(context.getContentResolver(), idArret);

		try {

			data = mHoraireManager
					.getHoraires(context.getContentResolver(), arret, mLastDayLoaded, mLastDateTimeLoaded);
			mHoraires.addAll(data);
			result.setResult(mAdapter);

		} catch (Exception e) {
			result.setException(e);
		}
		return result;
	}

	@Override
	protected void onPostExecute() {
		super.onPostExecute();
		if (mAdapter.getCount() > 0) {
			final Horaire lastHoraire = mAdapter.getItem(mAdapter.getCount() - 1);
			mLastDateTimeLoaded = new DateTime(lastHoraire.getTimestamp());
		}

		mAdapter.notifyDataSetChanged();
		updateItemsTime();
	}

	/**
	 * Mettre à jour les informations de délais
	 */
	private void updateItemsTime() {

		final Long currentTime = new DateTime().minusMinutes(5).withSecondOfMinute(0).withMillisOfSecond(0).getMillis();
		int nextHorairePosition = -1;

		for (int i = 0; i < mAdapter.getCount(); i++) {
			Horaire horaire = mAdapter.getItem(i);

			DateTime itemDateTime = new DateTime(horaire.getDate()).withSecondOfMinute(0).withMillisOfSecond(0);
			DateTime now = new DateTime().withSecondOfMinute(0).withMillisOfSecond(0);
			int delay = Minutes.minutesBetween(now, itemDateTime).getMinutes();

			if (delay > 0 && delay < 60) {
				horaire.setDelai(String.format(formatDelay, delay));
			} else if (delay == 0) {
				horaire.setDelai("départ proche");
			} else {
				horaire.setDelai(null);
			}
			horaire.setBeforeNow(itemDateTime.isBefore(now));

			// Recherche le prochain horaire
			if (nextHorairePosition == -1 && horaire.getTimestamp() >= currentTime) {
				nextHorairePosition = mAdapter.getPosition(horaire);
			}
		}

		mAdapter.notifyDataSetChanged();

		if (nextHorairePosition != -1 && mIsFirstLoad) {
			getListView().setSelection(nextHorairePosition);
			mIsFirstLoad = false;
		}
	}

}
