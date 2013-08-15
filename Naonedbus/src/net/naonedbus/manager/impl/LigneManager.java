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
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class LigneManager extends SQLiteManager<Ligne> implements Unschedulable<LignesTaskInfo> {

	private static final String LOG_TAG = "LigneManager";
	private static final boolean DBG = BuildConfig.DEBUG;

	private static LigneManager sInstance;

	private int mColId;
	private int mColCode;
	private int mColDepuis;
	private int mColLettre;
	private int mColVers;
	private int mColCouleurBack;
	private int mColCouleurFront;
	private int mColType;

	private Thread mLignesLoader;
	private final ConcurrentLinkedQueue<LignesTaskInfo> mLignesTasks;
	private final Object mLock = new Object();
	private final Ligne.Builder mBuilder;

	public static synchronized LigneManager getInstance() {
		if (sInstance == null) {
			sInstance = new LigneManager();
		}
		return sInstance;
	}

	protected LigneManager() {
		super(LigneProvider.CONTENT_URI);
		mLignesTasks = new ConcurrentLinkedQueue<LignesTaskInfo>();
		mBuilder = new Ligne.Builder();
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
	public void onIndexCursor(final Cursor c) {
		mColId = c.getColumnIndex(LigneTable._ID);
		mColCode = c.getColumnIndex(LigneTable.CODE);
		mColDepuis = c.getColumnIndex(LigneTable.DEPUIS);
		mColLettre = c.getColumnIndex(LigneTable.LETTRE);
		mColVers = c.getColumnIndex(LigneTable.VERS);
		mColCouleurBack = c.getColumnIndex(LigneTable.COULEUR_BACK);
		mColCouleurFront = c.getColumnIndex(LigneTable.COULEUR_FRONT);
		mColType = c.getColumnIndex(LigneTable.TYPE);
	}

	@Override
	public Ligne getSingleFromCursor(final Cursor c) {
		mBuilder.setId(c.getInt(mColId));
		mBuilder.setCode(c.getString(mColCode));
		mBuilder.setDepuis(c.getString(mColDepuis));
		mBuilder.setLettre(c.getString(mColLettre));
		mBuilder.setVers(c.getString(mColVers));
		mBuilder.setCouleurBack(c.getInt(mColCouleurBack));
		mBuilder.setCouleurFront(c.getInt(mColCouleurFront));
		mBuilder.setSection(c.getInt(mColType));
		return mBuilder.build();
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

		mLignesTasks.add(task);

		if (mLignesLoader == null || !mLignesLoader.isAlive()) {
			mLignesLoader = new Thread(lignesFromStationLoader);
			mLignesLoader.start();
		} else if (mLignesLoader.getState().equals(Thread.State.TIMED_WAITING)) {
			synchronized (mLock) {
				mLock.notify();
			}
		}

		return task;
	}

	@Override
	public void unschedule(final LignesTaskInfo task) {
		if (DBG)
			Log.d(LOG_TAG, "unschedule :\t" + task);
		mLignesTasks.remove(task);
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

			while ((task = mLignesTasks.poll()) != null) {
				lignes = getLignesFromStation(task.getContext().getContentResolver(), task.getTag());

				handler = task.getHandler();
				message = new Message();
				message.obj = lignes;
				handler.sendMessage(message);

				if (mLignesTasks.isEmpty()) {
					synchronized (mLock) {
						try {
							mLock.wait(2000);
						} catch (final InterruptedException e) {
						}
					}
				}

			}
		}
	};

	@Override
	protected ContentValues getContentValues(final Ligne item) {
		return null;
	}

}
