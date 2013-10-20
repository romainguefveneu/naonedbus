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
import net.naonedbus.bean.Route;
import net.naonedbus.bean.async.AsyncTaskInfo;
import net.naonedbus.bean.async.LignesTaskInfo;
import net.naonedbus.manager.SQLiteManager;
import net.naonedbus.manager.Unschedulable;
import net.naonedbus.provider.impl.RouteProvider;
import net.naonedbus.provider.table.RouteTable;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class LigneManager extends SQLiteManager<Route> implements Unschedulable<LignesTaskInfo> {

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
	private final Route.Builder mBuilder;
	private boolean mIsIndexed;

	public static synchronized LigneManager getInstance() {
		if (sInstance == null) {
			sInstance = new LigneManager();
		}
		return sInstance;
	}

	protected LigneManager() {
		super(RouteProvider.CONTENT_URI);
		mLignesTasks = new ConcurrentLinkedQueue<LignesTaskInfo>();
		mBuilder = new Route.Builder();
	}

	/**
	 * Récupérer les lignes selon un type donné
	 * 
	 * @param contentResolver
	 * @param idType
	 * @return Les lignes correspondants au type
	 */
	public List<Route> getLignesByType(final ContentResolver contentResolver, final Integer idType) {
		final Uri.Builder builder = RouteProvider.CONTENT_URI.buildUpon();
		builder.path(RouteProvider.LIGNE_TYPE_URI_PATH_QUERY);
		builder.appendPath(Integer.toString(idType));

		final Cursor c = contentResolver.query(builder.build(), null, null, null, null);
		return getFromCursor(c);
	}

	public Route getSingle(final ContentResolver contentResolver, final String code) {
		final Cursor c = getCursor(contentResolver, RouteTable.ROUTE_CODE + " = ?", new String[] { code });
		return getFirstFromCursor(c);
	}

	/**
	 * Récupérer les lignes selon un mot clé.
	 * 
	 * @param contentResolver
	 * @param keyword
	 * @return Les lignes correspondants au mot clé
	 */
	public Cursor getLignesSearch(final ContentResolver contentResolver, final String keyword) {
		final Uri.Builder builder = RouteProvider.CONTENT_URI.buildUpon();
		builder.appendPath(keyword);

		return contentResolver.query(builder.build(), null, null, null, null);
	}

	@Override
	public void onIndexCursor(final Cursor c) {
		mColId = c.getColumnIndex(RouteTable._ID);
		mColCode = c.getColumnIndex(RouteTable.ROUTE_CODE);
		mColDepuis = c.getColumnIndex(RouteTable.HEADSIGN_FROM);
		mColLettre = c.getColumnIndex(RouteTable.LETTER);
		mColVers = c.getColumnIndex(RouteTable.HEADSIGN_TO);
		mColCouleurBack = c.getColumnIndex(RouteTable.BACK_COLOR);
		mColCouleurFront = c.getColumnIndex(RouteTable.FRONT_COLOR);
		mColType = c.getColumnIndex(RouteTable.TYPE_ID);

		mIsIndexed = true;
	}

	@Override
	public Route getSingleFromCursor(final Cursor c) {
		if (!mIsIndexed)
			onIndexCursor(c);
		mBuilder.setId(c.getInt(mColId));
		mBuilder.setCode(c.getString(mColCode));
		mBuilder.setHeadsignFrom(c.getString(mColDepuis));
		mBuilder.setLetter(c.getString(mColLettre));
		mBuilder.setHeadsignTo(c.getString(mColVers));
		mBuilder.setBackColor(c.getInt(mColCouleurBack));
		mBuilder.setFrontColor(c.getInt(mColCouleurFront));
		mBuilder.setSection(c.getInt(mColType));
		return mBuilder.build();
	}

	public List<Route> getLignesFromStation(final ContentResolver contentResolver, final int idStation) {
		final Uri.Builder builder = RouteProvider.CONTENT_URI.buildUpon();
		builder.path(RouteProvider.LIGNE_STATION_URI_PATH_QUERY);
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
			List<Route> lignes;
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
	protected ContentValues getContentValues(final Route item) {
		return null;
	}

}
