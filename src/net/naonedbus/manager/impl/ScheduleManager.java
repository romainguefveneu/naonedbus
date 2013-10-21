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
package net.naonedbus.manager.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.naonedbus.BuildConfig;
import net.naonedbus.bean.NextHoraireTask;
import net.naonedbus.bean.Stop;
import net.naonedbus.bean.horaire.Horaire;
import net.naonedbus.bean.horaire.ScheduleToken;
import net.naonedbus.manager.SQLiteManager;
import net.naonedbus.provider.impl.ScheduleProvider;
import net.naonedbus.provider.table.ScheduleTable;
import net.naonedbus.rest.controller.impl.HoraireController;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.Minutes;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.bugsense.trace.BugSenseHandler;

public class ScheduleManager extends SQLiteManager<Horaire> {

	private static final String LOG_TAG = "ScheduleManager";
	private static final boolean DBG = BuildConfig.DEBUG;

	private static final int DAYS_IN_CACHE = 3;
	private static final int END_OF_TRIP_HOURS = 4;

	private static ScheduleManager sInstance;

	private final HoraireController mController;
	private final ConcurrentLinkedQueue<NextHoraireTask> mSchedulesTasksQueue;
	private final Set<ScheduleToken> mEmptySchedules;
	private Thread mLoadThread;
	private final Object mDatabaseLock;

	private int mColId;
	private int mColTimestamp;
	private int mColTerminus;
	private int mColDayTrip;

	/**
	 * Make it singleton !
	 */
	public static synchronized ScheduleManager getInstance() {
		if (sInstance == null) {
			sInstance = new ScheduleManager();
		}
		return sInstance;
	}

	private ScheduleManager() {
		super(ScheduleProvider.CONTENT_URI);
		mController = new HoraireController();
		mSchedulesTasksQueue = new ConcurrentLinkedQueue<NextHoraireTask>();
		mEmptySchedules = new HashSet<ScheduleToken>();
		mDatabaseLock = new Object();
	}

	@Override
	public void onIndexCursor(final Cursor c) {
		mColId = c.getColumnIndex(ScheduleTable._ID);
		mColTimestamp = c.getColumnIndex(ScheduleTable.TIMESTAMP);
		mColTerminus = c.getColumnIndex(ScheduleTable.HEADSIGN);
		mColDayTrip = c.getColumnIndex(ScheduleTable.DAY_TRIP);
	}

	@Override
	public Horaire getSingleFromCursor(final Cursor c) {
		final Horaire item = new Horaire();
		item.setId(c.getInt(mColId));
		item.setTimestamp(c.getLong(mColTimestamp));
		item.setTerminus(c.getString(mColTerminus));
		item.setDayTrip(c.getLong(mColDayTrip));
		item.setSection(new DateMidnight(item.getTimestamp()));
		return item;
	}

	/**
	 * Indique si le cache contient les horaires de l'arrêt pour la date donnée.
	 * 
	 * @return {@code true} si le cache contient tous les horaires pour l'arret
	 *         et la date demandée {@code false} si les données ne sont pas en
	 *         cache.
	 */
	public boolean isInDB(final ContentResolver contentResolver, final Stop arret, final DateMidnight date) {
		return isInDB(contentResolver, arret, date, 1);
	}

	/**
	 * Indique si le cache contient les horaires de l'arrêt pour la date donnée.
	 * 
	 * @return {@code true} si le cache contient tous les horaires pour l'arret
	 *         et la date demandée {@code false} si les données ne sont pas en
	 *         cache.
	 */
	public boolean isInDB(final ContentResolver contentResolver, final Stop arret, final DateMidnight date,
			final int count) {

		final ScheduleToken token = new ScheduleToken(date.getMillis(), arret.getId());
		return isInDB(contentResolver, token, count);
	}

	/**
	 * Indique si le cache contient les horaires de l'arrêt pour la date donnée.
	 * 
	 * @return {@code true} si le cache contient tous les horaires pour l'arret
	 *         et la date demandée {@code false} si les données ne sont pas en
	 *         cache.
	 */
	public boolean isInDB(final ContentResolver contentResolver, final ScheduleToken token) {
		return isInDB(contentResolver, token, 1);
	}

	/**
	 * Indique si le cache contient les horaires du token donné.
	 * 
	 * @return {@code true} si le cache contient tous les horaires pour l'arret
	 *         et la date demandée {@code false} si les données ne sont pas en
	 *         cache.
	 */
	public boolean isInDB(final ContentResolver contentResolver, final ScheduleToken token, final int count) {
		if (mEmptySchedules.contains(token))
			return true;

		final Uri.Builder builder = ScheduleProvider.CONTENT_URI.buildUpon();
		builder.path(ScheduleProvider.HORAIRE_JOUR_URI_PATH_QUERY);
		builder.appendQueryParameter(ScheduleProvider.PARAM_ARRET_ID, String.valueOf(token.getArretId()));
		builder.appendQueryParameter(ScheduleProvider.PARAM_DAY_TRIP, String.valueOf(token.getDate()));

		synchronized (mDatabaseLock) {
			final Cursor c = contentResolver.query(builder.build(), null, null, null, null);
			final int cursorCount = c.getCount();
			c.close();
			return cursorCount >= count;
		}
	}

	/**
	 * Supprimer les horaires de plus d'1 jour.
	 */
	public void clearOldSchedules(final ContentResolver contentResolver) {
		if (DBG)
			Log.i(LOG_TAG, "Nettoyage du cache horaires");

		synchronized (mDatabaseLock) {
			contentResolver.delete(ScheduleProvider.CONTENT_URI, ScheduleTable.DAY_TRIP + " < ?",
					new String[] { String.valueOf(new DateMidnight().minusDays(1).getMillis()) });
		}
	}

	/**
	 * Supprimer tous les horaires.
	 */
	public void clearSchedules(final ContentResolver contentResolver) {
		if (DBG)
			Log.i(LOG_TAG, "Suppression du cache horaires");
		synchronized (mDatabaseLock) {
			contentResolver.delete(ScheduleProvider.CONTENT_URI, null, null);
		}
	}

	private ContentValues getContentValues(final int arretId, final Horaire horaire) {
		final ContentValues values = new ContentValues();
		values.put(ScheduleTable.STOP_ID, arretId);
		values.put(ScheduleTable.HEADSIGN, horaire.getTerminus());
		values.put(ScheduleTable.DAY_TRIP, horaire.getDayTrip());
		values.put(ScheduleTable.TIMESTAMP, horaire.getTimestamp());

		return values;
	}

	private void fillDB(final ContentResolver contentResolver, final ScheduleToken token, final List<Horaire> schedule) {
		if (DBG)
			Log.i(LOG_TAG, "Sauvegarde des horaires : " + token.getArretId() + " \t " + new Date(token.getDate())
					+ " \t " + (schedule == null ? null : schedule.size()));

		if (schedule != null) {
			if (schedule.size() == 0) {
				// Ajouter un marqueur pour indiquer que l'on a déjà cherché
				// les horaires

				mEmptySchedules.add(token);
			} else {
				// Ajouter les horaires dans la db
				final ContentValues[] values = new ContentValues[schedule.size()];
				for (int i = 0; i < schedule.size(); i++) {
					values[i] = getContentValues(token.getArretId(), schedule.get(i));
				}

				synchronized (mDatabaseLock) {
					contentResolver.bulkInsert(ScheduleProvider.CONTENT_URI, values);
				}
			}
		}
	}

	/**
	 * Récupérer les horaires d'un arrêt.
	 * 
	 * @throws IOException
	 */
	public List<Horaire> getSchedules(final ContentResolver contentResolver, final Stop arret, final DateMidnight date)
			throws IOException {
		return getSchedules(contentResolver, arret, date, null);
	}

	/**
	 * Récupérer les horaires d'un arrêt.
	 * 
	 * @throws IOException
	 */
	public List<Horaire> getSchedules(final ContentResolver contentResolver, final Stop stop, final DateMidnight date,
			final DateTime after) throws IOException {

		// Le cache ne doit stocker que les horaires du jour et du lendemain.
		final DateMidnight cacheLimit = new DateMidnight().plusDays(DAYS_IN_CACHE);
		final DateMidnight today = new DateMidnight();
		final DateTime now = new DateTime();
		List<Horaire> horaires;

		if (date.isBefore(cacheLimit)) {

			final ScheduleToken todayToken = new ScheduleToken(date.getMillis(), stop.getId());

			// Partie atomique
			synchronized (mDatabaseLock) {
				if (!isInDB(contentResolver, todayToken)) {
					// Charger les horaires depuis le web et les stocker en base

					if (date.isEqual(today) && now.getHourOfDay() < END_OF_TRIP_HOURS) {
						// Charger la veille si besoin (pour les horaires après
						// minuit)
						final ScheduleToken yesterdayToken = new ScheduleToken(date.minusDays(1).getMillis(),
								stop.getId());

						if (isInDB(contentResolver, yesterdayToken)) {
							horaires = mController.getAllFromWeb(stop, date.minusDays(1));
							fillDB(contentResolver, yesterdayToken, horaires);
						}
					}

					horaires = mController.getAllFromWeb(stop, date);
					fillDB(contentResolver, todayToken, horaires);
				}

				// Charger les horaires depuis la base
				final Uri.Builder builder = ScheduleProvider.CONTENT_URI.buildUpon();
				builder.path(ScheduleProvider.HORAIRE_JOUR_URI_PATH_QUERY);
				builder.appendQueryParameter(ScheduleProvider.PARAM_ARRET_ID, String.valueOf(stop.getId()));
				builder.appendQueryParameter(ScheduleProvider.PARAM_DAY_TRIP, String.valueOf(date.getMillis()));
				builder.appendQueryParameter(ScheduleProvider.PARAM_INCLUDE_LAST_DAY_TRIP, Boolean.TRUE.toString());

				// Eviter l'affichage de doublons
				if (after != null) {
					builder.appendQueryParameter(ScheduleProvider.PARAM_AFTER_TIME, String.valueOf(after.getMillis()));
				}

				final Cursor cursor = contentResolver.query(builder.build(), null, null, null, null);
				horaires = getFromCursor(cursor);
				cursor.close();
			}

		} else {
			horaires = mController.getAllFromWeb(stop, date);
		}

		if (DBG)
			Log.v(LOG_TAG, "getSchedules [" + stop + ";" + date + ";" + after + "] : " + horaires.size());

		return horaires;
	}

	/**
	 * Récupérer les prochains horaires d'un arrêt.
	 * 
	 * @throws IOException
	 */
	public List<Horaire> getNextSchedules(final ContentResolver contentResolver, final Stop arret,
			final DateMidnight date, final int limit) throws IOException {
		return getNextSchedules(contentResolver, arret, date, limit, 0);
	}

	/**
	 * Récupérer les prochains horaires d'un arrêt
	 * 
	 * @throws IOException
	 */
	public List<Horaire> getNextSchedules(final ContentResolver contentResolver, final Stop arret, DateMidnight date,
			final int limit, final int minuteDelay) throws IOException {
		if (DBG)
			Log.d(LOG_TAG, "getNextHoraires " + arret + " : " + date + "\t" + limit);

		List<Horaire> schedules;
		final long now = new DateTime().minusMinutes(minuteDelay).withSecondOfMinute(0).withMillisOfSecond(0)
				.getMillis();
		final List<Horaire> nextSchedules = new ArrayList<Horaire>();
		int schedulesCount = 0; // Juste renvoyer le bon nombre d'horaires
		int loopCount = 0; // Limiter le nombre d'itérations
		DateTime after = null; // Dernier horaire chargé

		do {
			schedules = getSchedules(contentResolver, arret, date, after);
			for (final Horaire schedule : schedules) {
				if (schedule.getTimestamp() >= now) {
					nextSchedules.add(schedule);
					if (++schedulesCount >= limit) {
						break;
					}
				}
			}

			if (schedules.size() > 0)
				after = new DateTime(schedules.get(schedules.size() - 1).getTimestamp());
			else
				after = null;

			date = date.plusDays(1);
			loopCount++;
		} while ((loopCount < 2) && (nextSchedules.size() < limit));

		return nextSchedules;
	}

	/**
	 * Récupérer le nombre de minutes jusqu'au prochain horaire.
	 * 
	 * @throws IOException
	 */
	public Integer getMinutesToNextSchedule(final ContentResolver contentResolver, final Stop arret) throws IOException {

		final List<Horaire> nextSchedules = getNextSchedules(contentResolver, arret, new DateMidnight(), 1);
		Integer result = null;

		if (nextSchedules.size() > 0) {
			final Horaire next = nextSchedules.get(0);

			final DateTime itemDateTime = new DateTime(next.getTimestamp()).withSecondOfMinute(0).withMillisOfSecond(0);
			final DateTime now = new DateTime().withSecondOfMinute(0).withMillisOfSecond(0);

			result = Minutes.minutesBetween(now, itemDateTime).getMinutes();
		}

		return result;
	}

	/**
	 * Ajouter une demande de recherche d'horaire A la fin du chargement, le
	 * signal TimeService.ACTION_APPWIDGET_UPDATE est envoyé.
	 */
	public synchronized void schedule(final NextHoraireTask task) {
		if (DBG)
			Log.i(LOG_TAG, "Planification de la tâche " + task);

		mSchedulesTasksQueue.add(task);
		if (mLoadThread == null || !mLoadThread.isAlive()) {
			mLoadThread = new Thread(loadHoraireTask);
			mLoadThread.setPriority(Thread.MIN_PRIORITY);
			mLoadThread.start();
		}
	}

	/**
	 * Thread de chargement des horaires selon la file
	 */
	private final Runnable loadHoraireTask = new Runnable() {

		private final String LOG_TAG = "HoraireManager$loadHoraireTask";

		@Override
		public void run() {
			if (DBG)
				Log.i(LOG_TAG, "Démarrage du thread de chargement des horaires");

			final DateMidnight today = new DateMidnight();
			NextHoraireTask task;

			while ((task = mSchedulesTasksQueue.poll()) != null) {
				if (isInDB(task.getContext().getContentResolver(), task.getArret(), today)) {
					onPostLoad(task);
				} else {
					load(task, today);
				}
			}

			if (DBG)
				Log.i(LOG_TAG, "Fin du thread de chargement des horaires");
		}

		private void load(final NextHoraireTask task, final DateMidnight today) {
			if (DBG)
				Log.d(LOG_TAG, "Récupération des horaires de l'arrêt " + task.getArret().getCodeArret());

			try {

				getNextSchedules(task.getContext().getContentResolver(), task.getArret(), today, task.getLimit());

			} catch (final IOException e) {
				if (DBG)
					Log.e(LOG_TAG, "Erreur de récupération des horaires de l'arrêt " + task.getArret().getCodeArret(),
							e);
				task.setThrowable(e);
			} catch (final Exception e) {
				if (DBG)
					Log.e(LOG_TAG, "Erreur de récupération des horaires de l'arrêt " + task.getArret().getCodeArret(),
							e);
				task.setThrowable(e);
				BugSenseHandler.sendExceptionMessage("Erreur lors du chargement des horaires", null, e);
			}

			onPostLoad(task);
		}

		private void onPostLoad(final NextHoraireTask task) {
			final Intent intent = new Intent(task.getActionCallback());
			intent.putExtra("id", task.getId());

			if (task.getThrowable() != null) {
				intent.putExtra("throwable", task.getThrowable());
			}

			task.getContext().sendBroadcast(intent);
		}

	};

	@Override
	protected ContentValues getContentValues(final Horaire item) {
		return null;
	}

}
