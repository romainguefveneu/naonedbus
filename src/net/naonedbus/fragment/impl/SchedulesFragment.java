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
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import net.naonedbus.BuildConfig;
import net.naonedbus.R;
import net.naonedbus.activity.impl.AddEventActivity;
import net.naonedbus.activity.impl.SendNewsActivity;
import net.naonedbus.activity.impl.MapActivity;
import net.naonedbus.activity.impl.PlanActivity;
import net.naonedbus.activity.map.overlay.TypeOverlayItem;
import net.naonedbus.bean.Direction;
import net.naonedbus.bean.Route;
import net.naonedbus.bean.Stop;
import net.naonedbus.bean.StopBookmark;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.bean.schedule.EmptySchedule;
import net.naonedbus.bean.schedule.Schedule;
import net.naonedbus.fragment.CustomInfiniteListFragement;
import net.naonedbus.intent.ParamIntent;
import net.naonedbus.manager.impl.DirectionManager;
import net.naonedbus.manager.impl.ScheduleManager;
import net.naonedbus.manager.impl.StopBookmarkManager;
import net.naonedbus.manager.impl.StopManager;
import net.naonedbus.utils.FormatUtils;
import net.naonedbus.widget.adapter.impl.ScheduleArrayAdapter;
import net.naonedbus.widget.indexer.impl.ScheduleIndexer;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.Minutes;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.DatePicker;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class SchedulesFragment extends CustomInfiniteListFragement implements OnItemClickListener {

	private static final String LOG_TAG = "SchedulesFragment";
	private static final boolean DBG = BuildConfig.DEBUG;

	public static final String PARAM_ROUTE = "route";
	public static final String PARAM_DIRECTION = "direction";
	public static final String PARAM_STOP = "stop";

	public static interface OnDirectionChangedListener {
		void onDirectionChanged(Direction newDirection);
	}

	private final static IntentFilter intentFilter;
	static {
		intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_TIME_TICK);
		intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
	}

	/**
	 * Reçoit les intents de notre intentFilter
	 */
	private final BroadcastReceiver intentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			updateItemsTime();
		}
	};

	private final ScheduleManager mScheduleManager;
	private final StopManager mStopManager;
	private final DirectionManager mDirectionManager;
	private final StopBookmarkManager mStopBookmarkManager;
	private OnDirectionChangedListener mOnDirectionChangedListener;
	private ScheduleArrayAdapter mAdapter;
	private final List<Schedule> mSchedules;

	private Route mRoute;
	private Direction mDirection;
	private Stop mStop;
	private boolean mIsFirstLoad = true;
	private final AtomicBoolean mIsLoading = new AtomicBoolean(false);

	private DateMidnight mLastDayLoaded;
	private DateTime mLastDateTimeLoaded;

	private final DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
		@Override
		public void onDateSet(final DatePicker view, final int year, final int monthOfYear, final int dayOfMonth) {
			changeDate(new DateMidnight(year, monthOfYear + 1, dayOfMonth));
		}
	};

	public SchedulesFragment() {
		super(R.layout.fragment_listview_section);
		mScheduleManager = ScheduleManager.getInstance();
		mStopBookmarkManager = StopBookmarkManager.getInstance();
		mStopManager = StopManager.getInstance();
		mDirectionManager = DirectionManager.getInstance();

		mSchedules = new ArrayList<Schedule>();
		mLastDayLoaded = new DateMidnight();
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Activity activity = getActivity();
		if (activity instanceof OnDirectionChangedListener) {
			setOnChangeSensListener((OnDirectionChangedListener) activity);
		} else {
			throw new IllegalStateException();
		}

		mRoute = getArguments().getParcelable(PARAM_ROUTE);
		mDirection = getArguments().getParcelable(PARAM_DIRECTION);
		mStop = getArguments().getParcelable(PARAM_STOP);

		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAdapter = new ScheduleArrayAdapter(getActivity(), mSchedules);
		mAdapter.setIndexer(new ScheduleIndexer());

		getListView().setOnItemClickListener(this);

		loadContent();
	}

	@Override
	public void onStart() {
		getActivity().registerReceiver(intentReceiver, intentFilter);
		super.onStart();
	}

	@Override
	public void onStop() {
		getActivity().unregisterReceiver(intentReceiver);
		super.onDestroy();
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
		final Schedule schedule = (Schedule) getListView().getItemAtPosition(position);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			final String title = mStop.getName();
			final String description = FormatUtils.formatTitle(
					getString(R.string.format_route, mRoute.getCode()), mStop.getName(),
					mDirection.getName());

			final Intent calIntent = new Intent(Intent.ACTION_INSERT);
			calIntent.setType("vnd.android.cursor.item/event");
			calIntent.putExtra(Events.TITLE, title);
			calIntent.putExtra(Events.DESCRIPTION, description);
			calIntent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false);
			calIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, schedule.getTimestamp());
			calIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, schedule.getTimestamp());

			try {
				startActivity(calIntent);
				getActivity().overridePendingTransition(R.anim.slide_in_from_right, R.anim.half_fade_out);
			} catch (ActivityNotFoundException e) {
				addEventFallback(schedule);
			}

		} else {
			addEventFallback(schedule);
		}

	}

	private void addEventFallback(Schedule schedule) {
		final ParamIntent intent = new ParamIntent(getActivity(), AddEventActivity.class);
		intent.putExtra(AddEventActivity.PARAM_LIGNE, mRoute);
		intent.putExtra(AddEventActivity.PARAM_SENS, mDirection);
		intent.putExtra(AddEventActivity.PARAM_ARRET, mStop);
		intent.putExtra(AddEventActivity.PARAM_TIMESTAMP, schedule.getTimestamp());

		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.slide_in_from_right, R.anim.half_fade_out);
	}

	@Override
	protected void onLoadMoreItems() {
		if (mLastDayLoaded != null) {
			loadHoraires(mLastDayLoaded.plusDays(1));
		}
	}

	private void loadHoraires(final DateMidnight date) {
		if (mIsLoading.get() == false) {
			if (DBG)
				Log.d(LOG_TAG, "loadHoraires " + date.toString() + "\t" + mIsLoading.get());

			mLastDayLoaded = date;
			refreshContent();
		}
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_schedules, menu);
		final MenuItem menuFavori = menu.findItem(R.id.menu_favori);

		final int icon = isFavori() ? R.drawable.ic_action_important : R.drawable.ic_action_not_important;
		menuFavori.setIcon(icon);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onPrepareOptionsMenu(final Menu menu) {
		final MenuItem menuFavori = menu.findItem(R.id.menu_favori);

		final int icon = isFavori() ? R.drawable.ic_action_important : R.drawable.ic_action_not_important;
		menuFavori.setIcon(icon);
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_favori:
			onStarClick();
			break;
		case R.id.menu_place:
			showArretPlan();
			break;
		case R.id.menu_date:
			changeDate();
			break;
		case R.id.menu_date_maintenant:
			changeDateToNow();
			break;
		case R.id.menu_comment:
			menuComment();
			break;
		case R.id.menu_show_plan:
			menuShowPlan();
			break;
		case R.id.menu_sens:
			menuChangeSens();
			break;
		default:
			break;
		}
		return false;
	}

	private boolean isFavori() {
		final StopBookmark item = mStopBookmarkManager.getSingle(getActivity().getContentResolver(), mStop.getId());
		return (item != null);
	}

	@SuppressLint("NewApi")
	private void onStarClick() {
		if (isFavori()) {
			removeFromFavoris();
			Toast.makeText(getActivity(), R.string.bookmark_removed, Toast.LENGTH_SHORT).show();
		} else {
			addToFavoris();
			Toast.makeText(getActivity(), R.string.bookmark_added, Toast.LENGTH_SHORT).show();
		}

		getSherlockActivity().invalidateOptionsMenu();
	}

	private void changeDate() {
		final DatePickerDialog dateDialog = new DatePickerDialog(getActivity(), mDateSetListener,
				mLastDayLoaded.getYear(), mLastDayLoaded.getMonthOfYear() - 1, mLastDayLoaded.getDayOfMonth());
		dateDialog.show();
	}

	private void changeDateToNow() {
		changeDate(new DateMidnight());
	}

	private void addToFavoris() {
		mStopBookmarkManager.addFavori(getActivity().getContentResolver(), mStop);
	}

	private void removeFromFavoris() {
		mStopBookmarkManager.removeFavori(getActivity().getContentResolver(), mStop.getId());
	}

	protected void showArretPlan() {
		final Intent intent = new Intent(getActivity(), MapActivity.class);
		intent.putExtra(MapFragment.PARAM_ITEM_ID, mStop.getIdStation());
		intent.putExtra(MapFragment.PARAM_ITEM_TYPE, TypeOverlayItem.TYPE_STATION.getId());
		startActivity(intent);
	}

	private void menuComment() {
		final Intent intent = new Intent(getActivity(), SendNewsActivity.class);
		intent.putExtra(SendNewsActivity.PARAM_LIGNE, mRoute);
		intent.putExtra(SendNewsActivity.PARAM_SENS, mDirection);
		intent.putExtra(SendNewsActivity.PARAM_ARRET, mStop);
		startActivity(intent);
	}

	private void menuShowPlan() {
		final Intent intent = new Intent(getActivity(), PlanActivity.class);
		intent.putExtra(PlanActivity.PARAM_CODE_LIGNE, mRoute.getCode());
		startActivity(intent);
	}

	@SuppressLint("NewApi")
	private void menuChangeSens() {
		Direction autreSens = null;

		// Inverser le direction
		final List<Direction> direction = mDirectionManager
				.getAll(getActivity().getContentResolver(), mRoute.getCode());
		for (final Direction sensItem : direction) {
			if (sensItem.getId() != mDirection.getId()) {
				autreSens = sensItem;
				break;
			}
		}

		if (autreSens != null) {

			// Chercher l'arrêt dans le nouveau direction
			final Stop stop = mStopManager.getSingle(getActivity().getContentResolver(), mRoute.getCode(),
					autreSens.getCode(), mStop.getNormalizedNom());

			if (stop != null) {
				mDirection = autreSens;
				mStop = stop;

				mAdapter.clear();
				mAdapter.notifyDataSetChanged();

				changeDateToNow();

				if (mOnDirectionChangedListener != null) {
					mOnDirectionChangedListener.onDirectionChanged(mDirection);
				}

				getSherlockActivity().invalidateOptionsMenu();
				return;
			}
		}
		Toast.makeText(getActivity(), getString(R.string.no_stop_other_way), Toast.LENGTH_SHORT).show();
	}

	/**
	 * Clear data and reload with a new date
	 * 
	 * @param date
	 */
	public void changeDate(final DateMidnight date) {
		mAdapter.clear();
		mIsFirstLoad = true;
		mLastDateTimeLoaded = null;

		setListAdapter(null);
		loadHoraires(date);
	}

	@Override
	protected AsyncResult<ListAdapter> loadContent(final Context context, final Bundle bundle) {
		mIsLoading.set(true);

		final AsyncResult<ListAdapter> result = new AsyncResult<ListAdapter>();

		if (DBG)
			Log.d(LOG_TAG, "\tloadContent " + mLastDayLoaded.toString());

		try {

			final List<Schedule> data = mScheduleManager.getSchedules(context.getContentResolver(), mStop,
					mLastDayLoaded, mLastDateTimeLoaded);

			if (data.size() == 0) {
				// Si le précédent chargement à déjà charger la totalité du jour
				// actuel (route de nuit par exemple) ne pas réafficher le jour.
				if (((mLastDayLoaded == null || mLastDateTimeLoaded == null) || !mLastDayLoaded
						.equals(mLastDateTimeLoaded.toDateMidnight()))) {
					mSchedules.add(new EmptySchedule(R.string.no_schedules, mLastDayLoaded.toDate()));
				}
			} else {
				mSchedules.addAll(data);

				final Schedule lastHoraire = mSchedules.get(mSchedules.size() - 1);
				mLastDateTimeLoaded = new DateTime(lastHoraire.getTimestamp());
			}

			result.setResult(mAdapter);

		} catch (final Exception e) {
			result.setException(e);
		}
		return result;
	}

	@Override
	public void onLoadFinished(final Loader<AsyncResult<ListAdapter>> loader, final AsyncResult<ListAdapter> result) {

		if (result.getException() == null) {
			final ListAdapter adapter = result.getResult();
			if (!adapter.equals(getListAdapter())) {
				setListAdapter(adapter);
			}

			updateItemsTime();
			showContent();
		} else {
			final Exception exception = result.getException();
			if (exception instanceof IOException) {
				showError(R.string.network_fail, R.string.check_connection);
			} else {
				showError(R.string.tan_webservice_fail, R.string.try_gain_few_moments);
			}
			Log.w(LOG_TAG, "Erreur", exception);
		}

		resetNextUpdate();
		onPostExecute();

	}

	@Override
	protected void onPostExecute() {
		super.onPostExecute();

		mAdapter.notifyDataSetChanged();
		updateItemsTime();

		if (DBG)
			Log.d(LOG_TAG, "\tloadContent end " + mLastDayLoaded.toString());
		mIsLoading.set(false);
	}

	/**
	 * Mettre à jour les informations de délais
	 */
	private void updateItemsTime() {

		final Long currentTime = new DateTime().minusMinutes(5).withSecondOfMinute(0).withMillisOfSecond(0).getMillis();
		final DateTime now = new DateTime().withSecondOfMinute(0).withMillisOfSecond(0);

		int nextHorairePosition = -1;
		int delay;

		DateTime itemDateTime;
		Schedule schedule;

		for (int i = 0; i < mAdapter.getCount(); i++) {
			schedule = mAdapter.getItem(i);

			if (schedule instanceof EmptySchedule) {
				continue;
			}

			itemDateTime = new DateTime(schedule.getDate()).withSecondOfMinute(0).withMillisOfSecond(0);
			delay = Minutes.minutesBetween(now, itemDateTime).getMinutes();

			if (delay > 0 && delay < 60) {
				schedule.setDelai(getString(R.string.departure_min, delay));
			} else if (delay == 0) {
				schedule.setDelai(getString(R.string.departure_imminent));
			} else {
				schedule.setDelai(null);
			}
			schedule.setBeforeNow(itemDateTime.isBefore(now));

			// Recherche le prochain schedule
			if (nextHorairePosition == -1 && schedule.getTimestamp() >= currentTime) {
				nextHorairePosition = mAdapter.getPosition(schedule);
			}
		}

		// Si aucun prochain schedule n'est trouvé, sélectionner le dernier
		if (nextHorairePosition == -1) {
			nextHorairePosition = mAdapter.getCount() - 1;
		}

		mAdapter.notifyDataSetChanged();

		if (nextHorairePosition != -1 && mIsFirstLoad) {
			getListView().setSelection(nextHorairePosition);
			mIsFirstLoad = false;
		}
	}

	public void setOnChangeSensListener(final OnDirectionChangedListener l) {
		mOnDirectionChangedListener = l;
	}

}
