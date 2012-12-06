/**
 *  Copyright (C) 2011 Romain Guefveneu
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.naonedbus.bean.Arret;
import net.naonedbus.bean.NextHoraireTask;
import net.naonedbus.bean.horaire.Horaire;
import net.naonedbus.bean.horaire.HoraireToken;
import net.naonedbus.manager.SQLiteManager;
import net.naonedbus.provider.impl.HoraireProvider;
import net.naonedbus.provider.table.HoraireTable;
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
import com.google.gson.JsonSyntaxException;

/**
 * @author romain
 * 
 */
public class HoraireManager extends SQLiteManager<Horaire> {

	private static final String LOG_TAG = HoraireManager.class.getSimpleName();

	private static final int DAYS_IN_CACHE = 3;

	private static final int END_OF_TRIP_HOURS = 4;

	private static Set<HoraireToken> emptyHoraires = new HashSet<HoraireToken>();

	private static HoraireManager instance;
	private static ConcurrentLinkedQueue<NextHoraireTask> horairesTasksQueue = new ConcurrentLinkedQueue<NextHoraireTask>();
	private static Thread loadThread;
	private static HoraireController controller;

	/**
	 * Make it singleton !
	 */
	public static synchronized HoraireManager getInstance() {
		if (instance == null) {
			instance = new HoraireManager();
		}
		return instance;
	}

	private HoraireManager() {
		super(HoraireProvider.CONTENT_URI);
		controller = new HoraireController();
	}

	@Override
	protected Horaire getSingleFromCursor(Cursor c) {
		final Horaire item = new Horaire();
		item.setId(c.getInt(c.getColumnIndex(HoraireTable._ID)));
		item.setTimestamp(c.getLong(c.getColumnIndex(HoraireTable.TIMESTAMP)));
		item.setTerminus(c.getString(c.getColumnIndex(HoraireTable.TERMINUS)));
		item.setDayTrip(c.getLong(c.getColumnIndex(HoraireTable.DAY_TRIP)));
		item.setSection(new DateMidnight(item.getTimestamp()));
		return item;
	}

	/**
	 * Indique si une date est postérieure à la date limite du cache.
	 * 
	 * @param date
	 *            La date à tester.
	 * @return {@code true} si la date est postérieure à la date limite du
	 *         cache, {@code false} sinon.
	 */
	public boolean isDateAfterCacheLimit(DateMidnight date) {
		final DateMidnight dateMax = new DateMidnight().plusDays(DAYS_IN_CACHE);
		return date.isAfter(dateMax);
	}

	/**
	 * Indique si le cache contient les horaires de l'arrêt pour la date donnée
	 * 
	 * @return {@code true} si le cache contient tous les horaires pour l'arret
	 *         et la date demandée {@code false} si les données ne sont pas en
	 *         cache.
	 */
	public boolean isInDB(final ContentResolver contentResolver, final Arret arret, final DateMidnight date) {
		final HoraireToken flag = new HoraireToken(date.getMillis(), arret._id);
		if (emptyHoraires.contains(flag))
			return true;

		final Uri.Builder builder = HoraireProvider.CONTENT_URI.buildUpon();
		builder.path(HoraireProvider.HORAIRE_JOUR_URI_PATH_QUERY);
		builder.appendQueryParameter(HoraireProvider.PARAM_ARRET_ID, String.valueOf(arret._id));
		builder.appendQueryParameter(HoraireProvider.PARAM_DAY_TRIP, String.valueOf(date.getMillis()));

		final Cursor c = contentResolver.query(builder.build(), null, null, null, null);
		return c.getCount() > 0;
	}

	/**
	 * Supprimer les anciens horaires
	 */
	public void clearOldHoraires(final ContentResolver contentResolver) {
		Log.i(LOG_TAG, "Nettoyage du cache horaires");

		contentResolver.delete(HoraireProvider.CONTENT_URI, HoraireTable.DAY_TRIP + " < ?",
				new String[] { String.valueOf(new DateMidnight().getMillis()) });
	}

	/**
	 * Supprimer tous les horaires
	 */
	public void clearAllHoraires(final ContentResolver contentResolver) {
		Log.i(LOG_TAG, "Suppression du cache horaires");

		contentResolver.delete(HoraireProvider.CONTENT_URI, null, null);
	}

	private ContentValues getContentValues(final Arret arret, final Horaire horaire) {
		final ContentValues values = new ContentValues();
		values.put(HoraireTable.ID_ARRET, arret._id);
		values.put(HoraireTable.TERMINUS, horaire.getTerminus());
		values.put(HoraireTable.DAY_TRIP, horaire.getDayTrip());
		values.put(HoraireTable.TIMESTAMP, horaire.getTimestamp());

		return values;
	}

	private void fillDB(final ContentResolver contentResolver, final Arret arret, final HoraireToken flag,
			final List<Horaire> horaires) {
		if (horaires.size() == 0) {
			// Ajouter un marqueur pour indiquer que l'on a déjà cherché
			// les horaires

			emptyHoraires.add(flag);
		} else {
			// Ajouter les horaires dans la db
			final ContentValues[] values = new ContentValues[horaires.size()];
			for (int i = 0; i < horaires.size(); i++) {
				values[i] = getContentValues(arret, horaires.get(i));
			}

			contentResolver.bulkInsert(HoraireProvider.CONTENT_URI, values);
		}
	}

	/**
	 * Récupérer les horaires d'un arrêt
	 * 
	 * @throws IOException
	 */
	public synchronized List<Horaire> getHoraires(final ContentResolver contentResolver, final Arret arret,
			final DateMidnight date) throws IOException, JsonSyntaxException {
		return getHoraires(contentResolver, arret, date, null);
	}

	/**
	 * Récupérer les horaires d'un arrêt
	 * 
	 * @throws IOException
	 */
	public synchronized List<Horaire> getHoraires(final ContentResolver contentResolver, final Arret arret,
			final DateMidnight date, final DateTime after) throws IOException, JsonSyntaxException {
		// Le cache ne doit stocker que les horaires du jour et du lendemain.
		final DateMidnight dateMax = new DateMidnight().plusDays(DAYS_IN_CACHE);
		final DateMidnight currentDay = new DateMidnight();
		final DateTime now = new DateTime();

		if (date.isBefore(dateMax)) {

			final HoraireToken flag = new HoraireToken(date.getMillis(), arret._id);
			List<Horaire> horaires;

			if (!isInDB(contentResolver, arret, date) && (!emptyHoraires.contains(flag))) {
				//
				// Charger les horaires depuis le web et les stocker en base

				// Charger la veille si besoin (pour les horaires passé minuit)
				if (date.isEqual(currentDay) && now.getHourOfDay() < END_OF_TRIP_HOURS) {
					final HoraireToken previousFlag = new HoraireToken(date.minusDays(1).getMillis(), arret._id);
					if (!isInDB(contentResolver, arret, date.minusDays(1)) && (!emptyHoraires.contains(previousFlag))) {
						horaires = controller.getAllFromWeb(arret, date.minusDays(1));
						fillDB(contentResolver, arret, previousFlag, horaires);
					}
				}

				horaires = controller.getAllFromWeb(arret, date);
				fillDB(contentResolver, arret, flag, horaires);

			}
			//
			// Charger les horaires depuis la base

			final Uri.Builder builder = HoraireProvider.CONTENT_URI.buildUpon();
			builder.path(HoraireProvider.HORAIRE_JOUR_URI_PATH_QUERY);
			builder.appendQueryParameter(HoraireProvider.PARAM_ARRET_ID, String.valueOf(arret._id));
			builder.appendQueryParameter(HoraireProvider.PARAM_DAY_TRIP, String.valueOf(date.getMillis()));
			builder.appendQueryParameter(HoraireProvider.PARAM_INCLUDE_LAST_DAY_TRIP, "true");

			// Eviter l'affichage de doublons
			if (after != null) {
				builder.appendQueryParameter(HoraireProvider.PARAM_AFTER_TIME, String.valueOf(after.getMillis()));
			}

			horaires = getFromCursor(contentResolver.query(builder.build(), null, null, null, null));

			return horaires;

		} else {
			return controller.getAllFromWeb(arret, date);
		}
	}

	/**
	 * Récupérer les prochains horaires d'un arrêt
	 * 
	 * @throws IOException
	 */
	public List<Horaire> getNextHoraires(ContentResolver contentResolver, Arret arret, DateMidnight date, int limit)
			throws IOException, JsonSyntaxException {
		List<Horaire> horaires;
		final long now = new DateTime().withSecondOfMinute(0).withMillisOfSecond(0).getMillis();
		final List<Horaire> nextHoraires = new ArrayList<Horaire>();
		int horairesCount = 0; // Juste renvoyer le bon nombre d'horaires
		int loopCount = 0; // Limiter le nombre d'itérations

		do {
			horaires = getHoraires(contentResolver, arret, date);
			for (Horaire horaire : horaires) {
				if (horaire.getTimestamp() >= now) {
					nextHoraires.add(horaire);
					if (++horairesCount >= limit) {
						break;
					}
				}
			}
			date = date.plusDays(1);
			loopCount++;
		} while ((loopCount < 2) && (nextHoraires.size() < limit));

		return nextHoraires;
	}

	/**
	 * Récupérer le nombre de minutes jusqu'au prochain horaire
	 * 
	 * @throws IOException
	 */
	public Integer getMinutesToNextHoraire(final ContentResolver contentResolver, final Arret arret)
			throws IOException, JsonSyntaxException {

		final List<Horaire> nextHoraires = getNextHoraires(contentResolver, arret, new DateMidnight(), 1);
		Integer result = null;

		if (nextHoraires.size() > 0) {
			final Horaire next = nextHoraires.get(0);

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
		Log.i(LOG_TAG, "Plannification de la tâche " + task);
		horairesTasksQueue.add(task);
		if (loadThread == null || !loadThread.isAlive()) {
			loadThread = new Thread(loadHoraireTask);
			loadThread.setPriority(Thread.MIN_PRIORITY);
			loadThread.start();
		}
	}

	/**
	 * Thread de chargement des horaires selon la file
	 */
	private Runnable loadHoraireTask = new Runnable() {

		private String LOG_TAG = HoraireManager.class.getSimpleName() + "$loadHoraireTask";

		@Override
		public void run() {
			Log.i(LOG_TAG, "Démarrage du thread de chargement des horaires");
			NextHoraireTask task;
			final DateMidnight today = new DateMidnight();

			while ((task = horairesTasksQueue.poll()) != null) {
				if (!isInDB(task.getContext().getContentResolver(), task.getArret(), today)) {
					load(task);
				}
			}
			Log.i(LOG_TAG, "Fin du thread de chargement des horaires");
		}

		private void load(final NextHoraireTask task) {
			Log.d(LOG_TAG, "Récupération des horaires de l'arrêt " + task.getArret().codeArret);

			try {
				getNextHoraires(task.getContext().getContentResolver(), task.getArret(), new DateMidnight(),
						task.getLimit());

				onPostLoad(task);
			} catch (IOException e) {
				BugSenseHandler.sendExceptionMessage("Erreur lors du chargement des horaires", null, e);
			}

		}

		private void onPostLoad(final NextHoraireTask task) {
			final Intent intent = new Intent(task.getActionCallback());
			intent.putExtra("id", task.getId());
			task.getContext().sendBroadcast(intent);
		}

	};

}