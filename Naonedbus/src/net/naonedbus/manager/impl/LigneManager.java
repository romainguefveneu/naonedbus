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

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.naonedbus.BuildConfig;
import net.naonedbus.bean.Ligne;
import net.naonedbus.bean.async.AsyncTaskInfo;
import net.naonedbus.bean.async.LignesTaskInfo;
import net.naonedbus.manager.SQLiteManager;
import net.naonedbus.manager.Unschedulable;
import net.naonedbus.provider.impl.LigneProvider;
import net.naonedbus.provider.table.LigneTable;
import net.naonedbus.utils.ColorUtils;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class LigneManager extends SQLiteManager<Ligne> implements Unschedulable<LignesTaskInfo> {

	private static final String LOG_TAG = "LigneManager";
	private static final boolean DBG = BuildConfig.DEBUG;

	private static LigneManager instance;

	private Thread lignesLoader;
	private final ConcurrentLinkedQueue<LignesTaskInfo> lignesTasks;
	private final Object lock = new Object();

	public static synchronized LigneManager getInstance() {
		if (instance == null) {
			instance = new LigneManager();
		}
		return instance;
	}

	protected LigneManager() {
		super(LigneProvider.CONTENT_URI);
		lignesTasks = new ConcurrentLinkedQueue<LignesTaskInfo>();
	}

	/**
	 * Récupérer les lignes selon un type donné
	 * 
	 * @param contentResolver
	 * @param idType
	 * @return Les lignes correspondants au type
	 */
	public List<Ligne> getLignesByType(final ContentResolver contentResolver, final Integer idType) {
		final Uri.Builder builder = LigneProvider.CONTENT_URI.buildUpon();
		builder.path(LigneProvider.LIGNE_TYPE_URI_PATH_QUERY);
		builder.appendPath(Integer.toString(idType));

		final Cursor c = contentResolver.query(builder.build(), null, null, null, null);
		return getFromCursor(c);
	}

	/**
	 * Récupérer les lignes selon un mot clé.
	 * 
	 * @param contentResolver
	 * @param keyword
	 * @return Les lignes correspondants au mot clé
	 */
	public Cursor getLignesSearch(final ContentResolver contentResolver, final String keyword) {
		final Uri.Builder builder = LigneProvider.CONTENT_URI.buildUpon();
		builder.appendPath(keyword);

		return contentResolver.query(builder.build(), null, null, null, null);
	}

	@Override
	public Ligne getSingleFromCursor(final Cursor c) {
		final Ligne ligneItem = new Ligne();
		ligneItem._id = c.getInt(c.getColumnIndex(LigneTable._ID));
		ligneItem.code = c.getString(c.getColumnIndex(LigneTable.CODE));
		ligneItem.depuis = c.getString(c.getColumnIndex(LigneTable.DEPUIS));
		ligneItem.lettre = c.getString(c.getColumnIndex(LigneTable.LETTRE));
		ligneItem.vers = c.getString(c.getColumnIndex(LigneTable.VERS));
		ligneItem.nom = ligneItem.depuis + " \u2194 " + ligneItem.vers;
		ligneItem.couleurBackground = c.getInt(c.getColumnIndex(LigneTable.COULEUR));
		ligneItem.couleurTexte = ColorUtils.isLightColor(ligneItem.couleurBackground) ? Color.BLACK : Color.WHITE;
		ligneItem.section = c.getInt(c.getColumnIndex(LigneTable.TYPE));
		return ligneItem;
	}

	public List<Ligne> getLignesFromStation(final ContentResolver contentResolver, final int idStation) {
		final Uri.Builder builder = LigneProvider.CONTENT_URI.buildUpon();
		builder.path(LigneProvider.LIGNE_STATION_URI_PATH_QUERY);
		builder.appendQueryParameter("idStation", String.valueOf(idStation));
		final Cursor cursor = contentResolver.query(builder.build(), null, null, null, null);

		return getFromCursor(cursor);
	}

	/**
	 * Programmer la récupération des lignes de manière asynchrone.
	 * 
	 * @param contentResolver
	 * @param idStation
	 *            L'id de la station dont les lignes sont à charger.
	 * @param callback
	 *            Un {@code Handler} receptionnant le resultat dans {@code obj}
	 *            sous forme de {@code List<Ligne>} .
	 */
	public LignesTaskInfo scheduleGetLignesFromStation(final Context context, final int idStation,
			final Handler callback) {
		final LignesTaskInfo task = new LignesTaskInfo(context, idStation, callback);
		if (DBG)
			Log.d(LOG_TAG, "schedule :\t" + task);

		lignesTasks.add(task);

		if (lignesLoader == null || !lignesLoader.isAlive()) {
			lignesLoader = new Thread(lignesFromStationLoader);
			lignesLoader.start();
		} else if (lignesLoader.getState().equals(Thread.State.TIMED_WAITING)) {
			synchronized (lock) {
				lock.notify();
			}
		}

		return task;
	}

	@Override
	public void unschedule(final LignesTaskInfo task) {
		if (DBG)
			Log.d(LOG_TAG, "unschedule :\t" + task);
		lignesTasks.remove(task);
	}

	/**
	 * Loader pour le chargement des lignes asynchrone.
	 */
	private final Runnable lignesFromStationLoader = new Runnable() {
		@Override
		public void run() {
			AsyncTaskInfo<Integer> task;
			List<Ligne> lignes;
			Handler handler;
			Message message;

			while ((task = lignesTasks.poll()) != null) {
				lignes = getLignesFromStation(task.getContext().getContentResolver(), task.getTag());

				handler = task.getHandler();
				message = new Message();
				message.obj = lignes;
				handler.sendMessage(message);

				if (lignesTasks.isEmpty()) {
					synchronized (lock) {
						try {
							lock.wait(2000);
						} catch (final InterruptedException e) {
						}
					}
				}

			}
		}
	};

}
