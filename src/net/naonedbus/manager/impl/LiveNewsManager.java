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
import java.util.List;

import net.naonedbus.bean.LiveNews;
import net.naonedbus.manager.SQLiteManager;
import net.naonedbus.provider.impl.LiveNewsProvider;
import net.naonedbus.provider.table.LiveNewsTable;
import net.naonedbus.rest.controller.impl.LiveNewsController;

import org.json.JSONException;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class LiveNewsManager extends SQLiteManager<LiveNews> {

	private static LiveNewsManager sInstance;

	private static final long UP_TO_DATE_DELAY = 300000; // 5 min de cache

	private long mLastUpdateTimestamp = -1;

	private int mColId;
	private int mColRouteCode;
	private int mColDirectionCode;
	private int mColStopCode;
	private int mColMessage;
	private int mColSource;
	private int mColTimestamp;

	public static synchronized LiveNewsManager getInstance() {
		if (sInstance == null) {
			sInstance = new LiveNewsManager();
		}

		return sInstance;
	}

	private LiveNewsManager() {
		super(LiveNewsProvider.CONTENT_URI);
	}

	public boolean isUpToDate() {
		return mLastUpdateTimestamp + UP_TO_DATE_DELAY >= System.currentTimeMillis();
	}

	public List<LiveNews> getAll(final ContentResolver contentResolver, final String routeCode,
			final String directionCode, final String stopCode) {

		final Uri.Builder builder = LiveNewsProvider.CONTENT_URI.buildUpon();
		if (routeCode != null) {
			builder.appendPath(routeCode);
			if (directionCode != null) {
				builder.appendPath(directionCode);
				if (stopCode != null) {
					builder.appendPath(stopCode);
				}
			}
		}

		final Cursor c = contentResolver.query(builder.build(), null, null, null, null);
		return getFromCursor(c);
	}

	public void updateCache(final ContentResolver contentResolver) throws IOException, JSONException {
		final LiveNewsController naoNewController = new LiveNewsController();

		try {
			final List<LiveNews> data = naoNewController.getAll(null, null, null);
			clear(contentResolver);
			fillDB(contentResolver, data);
		} finally {
			mLastUpdateTimestamp = System.currentTimeMillis();
		}

	}

	public void clear(final ContentResolver contentResolver) {
		contentResolver.delete(LiveNewsProvider.CONTENT_URI, null, null);
	}

	@Override
	public void onIndexCursor(final Cursor c) {
		mColId = c.getColumnIndex(LiveNewsTable._ID);
		mColRouteCode = c.getColumnIndex(LiveNewsTable.ROUTE_CODE);
		mColDirectionCode = c.getColumnIndex(LiveNewsTable.DIRECTION_CODE);
		mColStopCode = c.getColumnIndex(LiveNewsTable.STOP_CODE);
		mColMessage = c.getColumnIndex(LiveNewsTable.MESSAGE);
		mColSource = c.getColumnIndex(LiveNewsTable.SOURCE);
		mColTimestamp = c.getColumnIndex(LiveNewsTable.TIMESTAMP);
	}

	@Override
	public LiveNews getSingleFromCursor(final Cursor c) {
		final LiveNews liveNews = new LiveNews();
		liveNews.setId(c.getInt(mColId));
		liveNews.setCodeLigne(c.getString(mColRouteCode));
		liveNews.setCodeSens(c.getString(mColDirectionCode));
		liveNews.setCodeArret(c.getString(mColStopCode));
		liveNews.setMessage(c.getString(mColMessage));
		liveNews.setSource(c.getString(mColSource));
		liveNews.setTimestamp(c.getLong(mColTimestamp));
		return liveNews;
	}

	@Override
	protected ContentValues getContentValues(final LiveNews liveNews) {
		final ContentValues values = new ContentValues();
		values.put(LiveNewsTable.ROUTE_CODE, liveNews.getCodeLigne());
		values.put(LiveNewsTable.DIRECTION_CODE, liveNews.getCodeSens());
		values.put(LiveNewsTable.STOP_CODE, liveNews.getCodeArret());
		values.put(LiveNewsTable.MESSAGE, liveNews.getMessage());
		values.put(LiveNewsTable.SOURCE, liveNews.getSource());
		values.put(LiveNewsTable.TIMESTAMP, liveNews.getTimestamp());
		return values;
	}

	private void fillDB(final ContentResolver contentResolver, final List<LiveNews> commentaires) {
		final ContentValues[] values = new ContentValues[commentaires.size()];
		for (int i = 0; i < commentaires.size(); i++) {
			values[i] = getContentValues(commentaires.get(i));
		}

		contentResolver.bulkInsert(LiveNewsProvider.CONTENT_URI, values);
	}
}
