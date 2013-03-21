package net.naonedbus.card.impl;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import net.naonedbus.BuildConfig;
import net.naonedbus.R;
import net.naonedbus.activity.impl.HorairesActivity;
import net.naonedbus.bean.Arret;
import net.naonedbus.bean.horaire.Horaire;
import net.naonedbus.card.Card;
import net.naonedbus.fragment.impl.ArretDetailFragment.OnArretChangeListener;
import net.naonedbus.manager.impl.HoraireManager;
import net.naonedbus.utils.ViewHelper;
import net.naonedbus.utils.ViewHelper.OnTagFoundHandler;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.Minutes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class HoraireCard extends Card<List<Horaire>> implements OnArretChangeListener {

	private static final String LOG_TAG = "HoraireCard";
	private static final boolean DBG = BuildConfig.DEBUG;

	private Arret mArret;
	private final DateFormat mTimeFormat;

	private final List<TextView> mHoraireViews;
	private final List<TextView> mDelaiViews;

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
	private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			Log.d(getClass().getSimpleName(), "onReceive " + intent.getAction());
			restartLoader(null, HoraireCard.this).onContentChanged();
		}
	};

	public HoraireCard(final Context context, final LoaderManager loaderManager, final Arret arret) {
		super(context, loaderManager, R.string.card_horaires_title, R.layout.card_horaire);

		mArret = arret;
		mHoraireViews = new ArrayList<TextView>();
		mDelaiViews = new ArrayList<TextView>();
		mTimeFormat = android.text.format.DateFormat.getTimeFormat(context);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		getContext().registerReceiver(mIntentReceiver, intentFilter);
	}

	@Override
	public void onResume() {
		super.onResume();
		initLoader(null, this);
	}

	@Override
	public void onDestroy() {
		getContext().unregisterReceiver(mIntentReceiver);
		super.onDestroy();
	}

	@Override
	protected Intent getMoreIntent() {
		final Intent intent = new Intent(getContext(), HorairesActivity.class);
		intent.putExtra(HorairesActivity.PARAM_ARRET, mArret);
		intent.putExtra(Intent.EXTRA_TITLE, R.string.card_more_horaires);
		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, R.drawable.ic_card_list);
		return intent;
	}

	@Override
	protected void bindView(final Context context, final View view) {
		mHoraireViews.clear();
		mDelaiViews.clear();

		ViewHelper.findViewsByTag(view, context.getString(R.string.cardHoraireTag), new OnTagFoundHandler() {
			@Override
			public void onTagFound(final View v) {
				mHoraireViews.add((TextView) v);
				setTypefaceRobotoLight((TextView) v);
			}
		});

		ViewHelper.findViewsByTag(view, context.getString(R.string.cardDelaiTag), new OnTagFoundHandler() {
			@Override
			public void onTagFound(final View v) {
				mDelaiViews.add((TextView) v);
				setTypefaceRobotoBold((TextView) v);
			}
		});
	}

	@Override
	public void onArretChange(final Arret newArret) {
		mArret = newArret;
		showLoader();
		restartLoader(null, this);
	}

	@Override
	public Loader<List<Horaire>> onCreateLoader(final int id, final Bundle bundle) {
		return new LoaderTask(getContext(), mArret, mHoraireViews.size() + 1);
	}

	@Override
	public void onLoadFinished(final Loader<List<Horaire>> loader, final List<Horaire> horaires) {
		if (horaires != null && !horaires.isEmpty()) {

			final DateTime now = new DateTime().withSecondOfMinute(0).withMillisOfSecond(0);
			final Horaire first = horaires.get(0);

			int minutes;
			int indexView = 0;
			int indexHoraire = 0;

			// Gérer le précédent passage si présent
			final DateTime firstDate = new DateTime(first.getDate());
			if (firstDate.isAfterNow() || firstDate.isEqual(now)) {
				indexView = 1;
				mHoraireViews.get(0).setVisibility(View.GONE);
				mDelaiViews.get(0).setVisibility(View.GONE);
			} else {
				mHoraireViews.get(0).setVisibility(View.VISIBLE);
				mDelaiViews.get(0).setVisibility(View.VISIBLE);
			}

			Horaire horaire;
			String delai = "";
			while (indexHoraire < horaires.size() && indexView < mHoraireViews.size()) {
				horaire = horaires.get(indexHoraire);

				mHoraireViews.get(indexView).setText(mTimeFormat.format(horaire.getDate()));

				if (indexView > 0) {
					minutes = Minutes.minutesBetween(now,
							new DateTime(horaire.getDate()).withSecondOfMinute(0).withMillisOfSecond(0)).getMinutes();

					if (minutes > 60) {
						delai = getString(R.string.msg_depart_heure_short, minutes / 60);
					} else if (minutes > 0) {
						delai = getString(R.string.msg_depart_min_short, minutes);
					} else if (minutes == 0) {
						delai = getString(R.string.msg_depart_proche);
					}

					mDelaiViews.get(indexView).setText(delai);
				}

				indexHoraire++;
				indexView++;
			}

			showContent();
		} else {
			showMessage(R.string.msg_nothing_horaires);
		}
	}

	private static class LoaderTask extends AsyncTaskLoader<List<Horaire>> {

		private final HoraireManager mHoraireManager;
		private final Arret mArret;
		private final int mHorairesCount;
		private List<Horaire> mHoraires;

		public LoaderTask(final Context context, final Arret arret, final int horairesCount) {
			super(context);
			mArret = arret;
			mHorairesCount = horairesCount;

			mHoraireManager = HoraireManager.getInstance();
		}

		@Override
		public List<Horaire> loadInBackground() {
			List<Horaire> horaires = null;

			try {
				horaires = mHoraireManager.getNextHoraires(getContext().getContentResolver(), mArret,
						new DateMidnight(), mHorairesCount, 5);
			} catch (final IOException e) {
				if (DBG)
					Log.e(LOG_TAG, "Erreur de chargement des horaires.", e);
			}

			mHoraires = horaires;

			return horaires;
		}

		/**
		 * Called when there is new data to deliver to the client. The super
		 * class will take care of delivering it; the implementation here just
		 * adds a little more logic.
		 */
		@Override
		public void deliverResult(final List<Horaire> horaires) {
			mHoraires = horaires;

			if (isStarted()) {
				// If the Loader is currently started, we can immediately
				// deliver its results.
				super.deliverResult(horaires);
			}
		}

		/**
		 * Handles a request to start the Loader.
		 */
		@Override
		protected void onStartLoading() {
			if (mHoraires != null) {
				// If we currently have a result available, deliver it
				// immediately.
				deliverResult(mHoraires);
			}

			if (takeContentChanged() || mHoraires == null) {
				// If the data has changed since the last time it was loaded
				// or is not currently available, start a load.
				forceLoad();
			}
		}

	}

}
