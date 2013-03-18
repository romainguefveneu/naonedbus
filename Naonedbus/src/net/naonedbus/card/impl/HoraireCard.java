package net.naonedbus.card.impl;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import net.naonedbus.R;
import net.naonedbus.bean.Arret;
import net.naonedbus.bean.horaire.Horaire;
import net.naonedbus.card.Card;
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
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.view.View;
import android.widget.TextView;

public class HoraireCard extends Card implements LoaderCallbacks<List<Horaire>> {

	private final Arret mArret;
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
	private final BroadcastReceiver intentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			getLoaderManager().initLoader(0, null, HoraireCard.this).forceLoad();
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
	public void onStart() {
		getContext().registerReceiver(intentReceiver, intentFilter);
	}

	@Override
	public void onStop() {
		getContext().unregisterReceiver(intentReceiver);
	}

	@Override
	protected void bindView(final Context context, final View view) {
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

		getLoaderManager().initLoader(0, null, this).forceLoad();
	}

	@Override
	public android.support.v4.content.Loader<List<Horaire>> onCreateLoader(final int arg0, final Bundle arg1) {
		return new Loader(getContext(), mArret, mHoraireViews.size() + 1);
	}

	@Override
	public void onLoadFinished(final android.support.v4.content.Loader<List<Horaire>> loader,
			final List<Horaire> horaires) {

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
		}
	}

	@Override
	public void onLoaderReset(final android.support.v4.content.Loader<List<Horaire>> arg0) {

	}

	private static class Loader extends AsyncTaskLoader<List<Horaire>> {
		private final HoraireManager mHoraireManager;
		private final Arret mArret;
		private final int mHorairesCount;

		public Loader(final Context context, final Arret arret, final int horairesCount) {
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
				e.printStackTrace();
			}
			return horaires;
		}

	}

}
