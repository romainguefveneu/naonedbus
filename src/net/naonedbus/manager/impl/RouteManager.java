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
import net.naonedbus.bean.async.RouteTaskInfo;
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

public class RouteManager extends SQLiteManager<Route> implements Unschedulable<RouteTaskInfo> {

	private static final String LOG_TAG = "RouteManager";
	private static final boolean DBG = BuildConfig.DEBUG;

	private static RouteManager sInstance;

	private int mColId;
	private int mColCode;
	private int mColLetter;
	private int mColHeadsignFrom;
	private int mColHeadsignTo;
	private int mColBackColor;
	private int mColFrontColor;
	private int mColTypeId;

	private Thread mRoutesLoader;
	private final ConcurrentLinkedQueue<RouteTaskInfo> mRoutesTasks;
	private final Object mLock = new Object();
	private final Route.Builder mBuilder;
	private boolean mIsIndexed;

	public static synchronized RouteManager getInstance() {
		if (sInstance == null) {
			sInstance = new RouteManager();
		}
		return sInstance;
	}

	protected RouteManager() {
		super(RouteProvider.CONTENT_URI);
		mRoutesTasks = new ConcurrentLinkedQueue<RouteTaskInfo>();
		mBuilder = new Route.Builder();
	}

	/**
	 * Get routes by type.
	 * 
	 * @param contentResolver
	 * @param typeId
	 *            the id of the type
	 * @return Route corresponding to the type
	 */
	public List<Route> getRouteByType(final ContentResolver contentResolver, final Integer typeId) {
		final Uri.Builder builder = RouteProvider.CONTENT_URI.buildUpon();
		builder.path(RouteProvider.ROUTE_TYPE_URI_PATH_QUERY);
		builder.appendPath(Integer.toString(typeId));

		final Cursor c = contentResolver.query(builder.build(), null, null, null, null);
		return getFromCursor(c);
	}

	public Route getSingle(final ContentResolver contentResolver, final String code) {
		final Uri.Builder builder = RouteProvider.CONTENT_URI.buildUpon();
		builder.path(RouteProvider.ROUTE_CODE_URI_PATH_QUERY);
		builder.appendPath(code);

		final Cursor cursor = contentResolver.query(builder.build(), null, null, null, null);
		return getFirstFromCursor(cursor);
	}

	/**
	 * Get routes by keyword.
	 */
	public Cursor getRoutes(final ContentResolver contentResolver, final String keyword) {
		final Uri.Builder builder = RouteProvider.CONTENT_URI.buildUpon();
		builder.appendPath(keyword);

		return contentResolver.query(builder.build(), null, null, null, null);
	}

	@Override
	public void onIndexCursor(final Cursor c) {
		mColId = c.getColumnIndex(RouteTable._ID);
		mColCode = c.getColumnIndex(RouteTable.ROUTE_CODE);
		mColHeadsignFrom = c.getColumnIndex(RouteTable.HEADSIGN_FROM);
		mColLetter = c.getColumnIndex(RouteTable.LETTER);
		mColHeadsignTo = c.getColumnIndex(RouteTable.HEADSIGN_TO);
		mColBackColor = c.getColumnIndex(RouteTable.BACK_COLOR);
		mColFrontColor = c.getColumnIndex(RouteTable.FRONT_COLOR);
		mColTypeId = c.getColumnIndex(RouteTable.TYPE_ID);

		mIsIndexed = true;
	}

	@Override
	public Route getSingleFromCursor(final Cursor c) {
		if (!mIsIndexed)
			onIndexCursor(c);
		mBuilder.setId(c.getInt(mColId));
		mBuilder.setCode(c.getString(mColCode));
		mBuilder.setHeadsignFrom(c.getString(mColHeadsignFrom));
		mBuilder.setLetter(c.getString(mColLetter));
		mBuilder.setHeadsignTo(c.getString(mColHeadsignTo));
		mBuilder.setBackColor(c.getInt(mColBackColor));
		mBuilder.setFrontColor(c.getInt(mColFrontColor));
		mBuilder.setSection(c.getInt(mColTypeId));
		return mBuilder.build();
	}

	/**
	 * Get all routes passing by a stop area (stops in equipments table).
	 */
	public List<Route> getRoutesByStopArea(final ContentResolver contentResolver, final int equipmentId) {
		final Uri.Builder builder = RouteProvider.CONTENT_URI.buildUpon();
		builder.path(RouteProvider.ROUTE_STOP_URI_PATH_QUERY);
		builder.appendPath(String.valueOf(equipmentId));

		final Cursor cursor = contentResolver.query(builder.build(), null, null, null, null);
		return getFromCursor(cursor);
	}

	/**
	 * Programmer la récupération des lignes de manière asynchrone.
	 * 
	 * @param contentResolver
	 * @param equipmentId
	 *            L'id de la station dont les lignes sont à charger.
	 * @param callback
	 *            Un {@code Handler} receptionnant le resultat dans {@code obj}
	 *            sous forme de {@code List<route>} .
	 */
	public RouteTaskInfo scheduleGetRoutesByStopArea(final Context context, final int equipmentId, final Handler callback) {
		final RouteTaskInfo task = new RouteTaskInfo(context, equipmentId, callback);
		if (DBG)
			Log.d(LOG_TAG, "schedule :\t" + task);

		mRoutesTasks.add(task);

		if (mRoutesLoader == null || !mRoutesLoader.isAlive()) {
			mRoutesLoader = new Thread(routesLoader);
			mRoutesLoader.start();
		} else if (mRoutesLoader.getState().equals(Thread.State.TIMED_WAITING)) {
			synchronized (mLock) {
				mLock.notify();
			}
		}

		return task;
	}

	@Override
	public void unschedule(final RouteTaskInfo task) {
		if (DBG)
			Log.d(LOG_TAG, "unschedule :\t" + task);
		mRoutesTasks.remove(task);
	}

	/**
	 * Loader pour le chargement des lignes asynchrone.
	 */
	private final Runnable routesLoader = new Runnable() {
		@Override
		public void run() {
			AsyncTaskInfo<Integer> task;
			List<Route> routes;
			Handler handler;
			Message message;

			while ((task = mRoutesTasks.poll()) != null) {
				routes = getRoutesByStopArea(task.getContext().getContentResolver(), task.getTag());

				handler = task.getHandler();
				message = new Message();
				message.obj = routes;
				handler.sendMessage(message);

				if (mRoutesTasks.isEmpty()) {
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
