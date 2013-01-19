package net.naonedbus.fragment.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import net.naonedbus.R;
import net.naonedbus.activity.impl.CommentaireActivity;
import net.naonedbus.activity.impl.MapActivity;
import net.naonedbus.activity.impl.PlanActivity;
import net.naonedbus.activity.map.overlay.TypeOverlayItem;
import net.naonedbus.bean.Arret;
import net.naonedbus.bean.Favori;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.bean.horaire.EmptyHoraire;
import net.naonedbus.bean.horaire.Horaire;
import net.naonedbus.fragment.CustomInfiniteListFragement;
import net.naonedbus.intent.ParamIntent;
import net.naonedbus.manager.impl.ArretManager;
import net.naonedbus.manager.impl.FavoriManager;
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
import android.support.v4.content.Loader;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class HorairesFragment extends CustomInfiniteListFragement {

	private static final String LOG_TAG = "HorairesFragment";

	private static final String ACTION_UPDATE_DELAYS = "net.naonedbus.action.UPDATE_DELAYS";
	public static final String PARAM_ID_ARRET = "idArret";

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
	private final FavoriManager mFavoriManager;
	private HoraireArrayAdapter mAdapter;
	private List<Horaire> mHoraires;

	private Arret mArret;
	private boolean mIsFirstLoad = true;
	private AtomicBoolean mIsLoading = new AtomicBoolean(false);

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
		mFavoriManager = FavoriManager.getInstance();
		mArretManager = ArretManager.getInstance();

		mHoraires = new ArrayList<Horaire>();
		mLastDayLoaded = new DateMidnight();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAdapter = new HoraireArrayAdapter(getActivity(), mHoraires);
		mAdapter.setIndexer(new HoraireIndexer());

		final int idArret = getArguments().getInt(PARAM_ID_ARRET);
		mArret = mArretManager.getSingle(getActivity().getContentResolver(), idArret);

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

	@Override
	protected void onLoadMoreItems() {
		if (mLastDayLoaded != null) {
			loadHoraires(mLastDayLoaded.plusDays(1));
		}
	}

	private void loadHoraires(DateMidnight date) {
		if (mIsLoading.get() == false) {
			Log.d(LOG_TAG, "loadHoraires " + date.toString() + "\t" + mIsLoading.get());
			mLastDayLoaded = date;
			refreshContent();
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu) {
		final MenuInflater menuInflater = getSherlockActivity().getSupportMenuInflater();
		menuInflater.inflate(R.menu.fragment_horaires, menu);

		final MenuItem menuFavori = menu.findItem(R.id.menu_favori);
		int icon = isFavori() ? R.drawable.ic_action_important : R.drawable.ic_action_not_important;
		menuFavori.setIcon(icon);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
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
		default:
			break;
		}
		return false;
	}

	private boolean isFavori() {
		final Favori item = mFavoriManager.getSingle(getActivity().getContentResolver(), mArret._id);
		return (item != null);
	}

	private void onStarClick() {
		if (isFavori()) {
			removeFromFavoris();
			Toast.makeText(getActivity(), R.string.toast_favori_retire, Toast.LENGTH_SHORT).show();
		} else {
			addToFavoris();
			Toast.makeText(getActivity(), R.string.toast_favori_ajout, Toast.LENGTH_SHORT).show();
		}

		final SherlockFragmentActivity activity = (SherlockFragmentActivity) getActivity();
		activity.invalidateOptionsMenu();
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
		mFavoriManager.addFavori(getActivity().getContentResolver(), mArret);
	}

	private void removeFromFavoris() {
		mFavoriManager.removeFavori(getActivity().getContentResolver(), mArret._id);
	}

	protected void showArretPlan() {
		final ParamIntent intent = new ParamIntent(getActivity(), MapActivity.class);
		intent.putExtra(MapActivity.Param.itemId, mArret.idStation);
		intent.putExtra(MapActivity.Param.itemType, TypeOverlayItem.TYPE_STATION.getId());
		startActivity(intent);
	}

	private void menuComment() {
		final ParamIntent intent = new ParamIntent(getActivity(), CommentaireActivity.class);
		intent.putExtra(CommentaireActivity.Param.idArret, mArret._id);
		startActivity(intent);
	}

	private void menuShowPlan() {
		final ParamIntent intent = new ParamIntent(getActivity(), PlanActivity.class);
		intent.putExtra(PlanActivity.Param.codeLigne, mArret.codeLigne);
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
		mIsLoading.set(true);

		final AsyncResult<ListAdapter> result = new AsyncResult<ListAdapter>();

		final int idArret = getArguments().getInt(PARAM_ID_ARRET);
		final Arret arret = mArretManager.getSingle(context.getContentResolver(), idArret);

		Log.d(LOG_TAG, "\tloadContent " + mLastDayLoaded.toString());

		try {

			final List<Horaire> data = mHoraireManager.getHoraires(context.getContentResolver(), arret, mLastDayLoaded,
					mLastDateTimeLoaded);

			for (Horaire horaire : data) {
				Log.d(LOG_TAG, new DateTime(horaire.getTimestamp()).toLocalDateTime().toString());
			}

			if (data.size() == 0) {
				// Si le précédent chargement à déjà charger la totalité du jour
				// actuel (ligne de nuit par exemple) ne pas réafficher le jour.
				if (((mLastDayLoaded == null || mLastDateTimeLoaded == null) || !mLastDayLoaded
						.equals(mLastDateTimeLoaded.toDateMidnight()))) {
					mHoraires.add(new EmptyHoraire(R.string.msg_nothing_horaires, mLastDayLoaded.toDate()));
				}
			} else {
				mHoraires.addAll(data);

				final Horaire lastHoraire = mHoraires.get(mHoraires.size() - 1);
				mLastDateTimeLoaded = new DateTime(lastHoraire.getTimestamp());
			}

			result.setResult(mAdapter);

		} catch (Exception e) {
			result.setException(e);
		}
		return result;
	}

	@Override
	public void onLoadFinished(Loader<AsyncResult<ListAdapter>> loader, AsyncResult<ListAdapter> result) {

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
				showError(R.string.error_title_network, R.string.error_summary_network);
			} else {
				showError(R.string.error_title_webservice, R.string.error_summary_webservice);
			}
			Log.e(LOG_TAG, "Erreur", exception);
		}

		resetNextUpdate();
		onPostExecute();

	}

	@Override
	protected void onPostExecute() {
		super.onPostExecute();

		mAdapter.notifyDataSetChanged();
		updateItemsTime();

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
		Horaire horaire;

		for (int i = 0; i < mAdapter.getCount(); i++) {
			horaire = mAdapter.getItem(i);

			if (horaire instanceof EmptyHoraire) {
				continue;
			}

			itemDateTime = new DateTime(horaire.getDate()).withSecondOfMinute(0).withMillisOfSecond(0);
			delay = Minutes.minutesBetween(now, itemDateTime).getMinutes();

			if (delay > 0 && delay < 60) {
				horaire.setDelai(getString(R.string.msg_depart_min, delay));
			} else if (delay == 0) {
				horaire.setDelai(getString(R.string.msg_depart_proche));
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
