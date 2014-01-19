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
package net.naonedbus.card.impl;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.naonedbus.BuildConfig;
import net.naonedbus.R;
import net.naonedbus.activity.impl.HorairesActivity;
import net.naonedbus.bean.Arret;
import net.naonedbus.bean.horaire.Horaire;
import net.naonedbus.card.Card;
import net.naonedbus.fragment.impl.ArretDetailFragment.OnArretChangeListener;
import net.naonedbus.manager.impl.HoraireManager;
import net.naonedbus.utils.FormatUtils;
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
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TableRow;
import android.widget.TextView;

public class HoraireCard extends Card<List<Horaire>> implements OnArretChangeListener {

	public static interface OnHoraireClickListener {
		void onHoraireClick(Horaire horaire);
	}

	private static final String LOG_TAG = "HoraireCard";
	private static final boolean DBG = BuildConfig.DEBUG;

	private final static IntentFilter intentFilter;
	static {
		intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_TIME_TICK);
		intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
	}

	/** Reçoit les intents de notre intentFilter */
	private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			restartLoader(null, HoraireCard.this).onContentChanged();
		}
	};

	private Arret mArret;
	private final DateFormat mTimeFormat;

	private final List<TextView> mHoraireViews;
	private final List<TextView> mDelaiViews;

	private final TerminusManager mTerminusManager;
	private final LayoutInflater mLayoutInflater;
	private ViewGroup mTerminusView;

	public HoraireCard(final Context context, final LoaderManager loaderManager, final FragmentManager fragmentManager,
			final Arret arret) {
		super(context, loaderManager, fragmentManager, R.string.card_horaires_title, R.layout.card_horaire);

		mLayoutInflater = LayoutInflater.from(context);

		mArret = arret;
		mHoraireViews = new ArrayList<TextView>();
		mDelaiViews = new ArrayList<TextView>();
		mTimeFormat = android.text.format.DateFormat.getTimeFormat(context);
		mTerminusManager = new TerminusManager();
	}

	@Override
	public void onResume() {
		super.onResume();
		getContext().registerReceiver(mIntentReceiver, intentFilter);
		if (mDelaiViews.size() > 2) {
			restartLoader(null, this);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		getContext().unregisterReceiver(mIntentReceiver);
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
	protected void bindView(final Context context, final View base, final View view) {

		mTerminusView = (ViewGroup) view.findViewById(R.id.terminus);

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
			}
		});

		final ViewTreeObserver obs = base.getViewTreeObserver();
		obs.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				if (base.getMeasuredWidth() != 0) {
					base.getViewTreeObserver().removeOnPreDrawListener(this);
					fillView((ViewGroup) base, (ViewGroup) view);
				}
				return true;
			}
		});

	}

	private void fillView(final ViewGroup base, final ViewGroup parent) {
		final int viewsCount = getTextViewCount(base);

		final TableRow rowTop = (TableRow) parent.findViewById(R.id.rowTop);
		final TableRow rowBottom = (TableRow) parent.findViewById(R.id.rowBottom);

		for (int i = 0; i < viewsCount; i++) {
			int layoutHoraire;
			if (i == 0)
				layoutHoraire = R.layout.card_horaire_text_first;
			else if (i < viewsCount - 1)
				layoutHoraire = R.layout.card_horaire_text;
			else
				layoutHoraire = R.layout.card_horaire_text_last;

			final TextView horaireView = (TextView) mLayoutInflater.inflate(layoutHoraire, null);
			final TextView delayView = (TextView) mLayoutInflater.inflate(R.layout.card_horaire_delay, null);

			setTypefaceRobotoLight(horaireView);

			rowTop.addView(horaireView);
			rowBottom.addView(delayView);

			mHoraireViews.add(horaireView);
			mDelaiViews.add(delayView);
		}

		initLoader(null, this).forceLoad();
	}

	private int getTextViewCount(final ViewGroup parent) {
		final TextView textView = (TextView) mLayoutInflater.inflate(R.layout.card_horaire_text, parent, false);
		final DateTime noon = new DateTime().withHourOfDay(12).withMinuteOfHour(00);
		final int padding = getContext().getResources().getDimensionPixelSize(R.dimen.padding_medium);
		textView.setText(FormatUtils.formatTimeAmPm(getContext(), mTimeFormat.format(noon.toDate())));

		final int specY = MeasureSpec.makeMeasureSpec(parent.getHeight(), MeasureSpec.UNSPECIFIED);
		final int specX = MeasureSpec.makeMeasureSpec(parent.getWidth(), MeasureSpec.UNSPECIFIED);
		textView.measure(specX, specY);

		return (parent.getWidth() - padding) / textView.getMeasuredWidth();
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

			int minutes;
			int indexView = 0;
			int indexHoraire = -1;
			int firstNextHoraireIndex = -1;

			// Gérer le précédent passage si présent
			DateTime date = new DateTime();
			for (int i = 0; i < horaires.size(); i++) {
				date = date.withMillis(horaires.get(i).getTimestamp());
				if (date.isBefore(now)) {
					indexHoraire = i;
				} else {
					break;
				}
			}

			if (indexHoraire == -1) {
				// Pas de précédent
				indexView = 1;
				indexHoraire = 0;
				firstNextHoraireIndex = 0;
				mHoraireViews.get(0).setVisibility(View.GONE);
				mDelaiViews.get(0).setVisibility(View.GONE);
			} else {
				// Afficher le précédent
				firstNextHoraireIndex = 1;
				mHoraireViews.get(0).setVisibility(View.VISIBLE);
				mDelaiViews.get(0).setVisibility(View.VISIBLE);
			}

			while (indexHoraire < horaires.size() && indexView < mHoraireViews.size()) {
				String delai = "";
				final Horaire horaire = horaires.get(indexHoraire);
				final TextView horaireView = mHoraireViews.get(indexView);

				CharSequence formattedTime = FormatUtils.formatTimeAmPm(getContext(),
						mTimeFormat.format(horaire.getDate()));

				if (horaire.getTerminus() != null) {
					CharSequence terminusLetter = mTerminusManager.getTerminusLetter(horaire.getTerminus());
					terminusLetter = FormatUtils.formatTerminusLetter(getContext(), terminusLetter);
					if (indexHoraire > firstNextHoraireIndex) {
						formattedTime = TextUtils.concat(formattedTime, "\n", terminusLetter);
					} else {
						formattedTime = TextUtils.concat(formattedTime, terminusLetter);
					}
				}

				horaireView.setText(formattedTime);

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

			// Add terminus information
			mTerminusView.removeAllViews();
			final Map<String, String> terminus = mTerminusManager.getTerminus();
			for (final Entry<String, String> item : terminus.entrySet()) {
				final TextView textView = (TextView) mLayoutInflater.inflate(R.layout.card_horaire_terminus,
						mTerminusView, false);
				textView.setText(item.getValue() + " " + item.getKey());
				mTerminusView.addView(textView);
			}

			showContent();
		} else {
			showMessage(R.string.msg_nothing_horaires);
		}
	}

	private class TerminusManager {
		private final Map<String, String> mLetterMap;
		private char mCurrentLetter = 0x278A;

		public TerminusManager() {
			mLetterMap = new LinkedHashMap<String, String>();
		}

		public String getTerminusLetter(final String terminus) {
			if (!mLetterMap.containsKey(terminus)) {
				mLetterMap.put(terminus, String.valueOf(mCurrentLetter));
				mCurrentLetter++;
			}
			return mLetterMap.get(terminus);
		}

		public Map<String, String> getTerminus() {
			return mLetterMap;
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
				horaires = mHoraireManager.getNextSchedules(getContext().getContentResolver(), mArret,
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
