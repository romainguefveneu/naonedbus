package net.naonedbus.fragment.impl;

import java.util.ArrayList;
import java.util.List;

import net.naonedbus.R;
import net.naonedbus.bean.Arret;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.bean.horaire.Horaire;
import net.naonedbus.fragment.CustomInfiniteListFragement;
import net.naonedbus.manager.impl.ArretManager;
import net.naonedbus.manager.impl.HoraireManager;
import net.naonedbus.widget.adapter.impl.HoraireArrayAdapter;
import net.naonedbus.widget.indexer.impl.HoraireIndexer;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.Minutes;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListAdapter;

import com.actionbarsherlock.view.Menu;

public class HorairesFragment extends CustomInfiniteListFragement {

	public static final String PARAM_ID_ARRET = "idArret";

	private static final String formatDelay = "dans %d min";

	private final HoraireManager mHoraireManager;
	private final ArretManager mArretManager;
	private HoraireArrayAdapter mAdapter;
	private List<Horaire> mHoraires;

	private boolean mIsFirstLoad = true;

	private DateMidnight mLastDayLoaded;
	private DateTime mLastDateTimeLoaded;

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
		mAdapter = new HoraireArrayAdapter(getActivity(), mHoraires);
		mAdapter.setIndexer(new HoraireIndexer());
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
			for (Horaire horaire : data) {
				Log.d("Horaire", String.valueOf(horaire.getTimestamp()));
			}
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
