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
package net.naonedbus.provider.impl;

import net.naonedbus.provider.CustomContentProvider;
import net.naonedbus.provider.table.ScheduleTable;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class ScheduleProvider extends CustomContentProvider {

	public static final String PARAM_STOP_ID = "stopId";
	public static final String PARAM_DAY_TRIP = "dayTrip";
	public static final String PARAM_INCLUDE_LAST_DAY_TRIP = "includeLastDayTrip";
	public static final String PARAM_AFTER_TIME = "afterTime";

	public static final int SCHEDULE = 100;
	public static final int SCHEDULE_ID = 110;

	/**
	 * Les horaires d'un arrêt et d'un jour
	 */
	public static final int SCHEDULE_DAY = 200;
	public static final String SCHEDULE_DAY_URI_PATH_QUERY = "stop";

	private static final String AUTHORITY = "net.naonedbus.provider.ScheduleProvider";
	private static final String SCHEDULE_BASE_PATH = "schedule";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + SCHEDULE_BASE_PATH);

	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		URI_MATCHER.addURI(AUTHORITY, SCHEDULE_BASE_PATH, SCHEDULE);
		URI_MATCHER.addURI(AUTHORITY, SCHEDULE_BASE_PATH + "/#", SCHEDULE_ID);

		URI_MATCHER.addURI(AUTHORITY, SCHEDULE_DAY_URI_PATH_QUERY, SCHEDULE_DAY);

	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(ScheduleTable.TABLE_NAME);

		if (sortOrder == null) {
			sortOrder = "_id";
		}

		int uriType = URI_MATCHER.match(uri);
		switch (uriType) {
		case SCHEDULE_ID:
			queryBuilder.appendWhere(ScheduleTable._ID + "=" + uri.getLastPathSegment());
			break;
		case SCHEDULE_DAY:
			final String stopId = uri.getQueryParameter(PARAM_STOP_ID);
			final String dayTrip = uri.getQueryParameter(PARAM_DAY_TRIP);
			final String afterTime = uri.getQueryParameter(PARAM_AFTER_TIME);

			queryBuilder.appendWhere(ScheduleTable.STOP_ID + " = " + stopId);
			queryBuilder.appendWhere(" AND (");
			queryBuilder.appendWhere(ScheduleTable.DAY_TRIP + " = " + dayTrip);

			// Fin du trip passé pour les horaires d'après minuit
			if (uri.getQueryParameter(PARAM_INCLUDE_LAST_DAY_TRIP) != null) {
				queryBuilder.appendWhere(" OR (");
				queryBuilder.appendWhere(ScheduleTable.DAY_TRIP + " < " + dayTrip);
				queryBuilder.appendWhere(" AND ");
				queryBuilder.appendWhere(ScheduleTable.TIMESTAMP + " >= " + dayTrip);
				queryBuilder.appendWhere(")");
			}

			queryBuilder.appendWhere(")");

			// Eviter l'affichage de doublons
			if (afterTime != null) {
				queryBuilder.appendWhere(" AND ");
				queryBuilder.appendWhere(ScheduleTable.TIMESTAMP + " > " + afterTime);
			}

			break;
		case SCHEDULE:
			// no filter
			break;
		default:
			throw new IllegalArgumentException("Unknown URI");
		}

		final Cursor cursor = queryBuilder.query(getReadableDatabase(), projection, selection, selectionArgs, null,
				null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = getWritableDatabase();

		int count;
		switch (URI_MATCHER.match(uri)) {
		case SCHEDULE:
			count = db.delete(ScheduleTable.TABLE_NAME, selection, selectionArgs);
			break;
		case SCHEDULE_ID:
			String segment = uri.getPathSegments().get(1);
			count = db.delete(ScheduleTable.TABLE_NAME,
					ScheduleTable._ID + "=" + segment
							+ (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI (" + URI_MATCHER.match(uri) + ") " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);

		return count;
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		ContentValues values;

		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}

		if (URI_MATCHER.match(uri) != SCHEDULE) {
			throw new IllegalArgumentException("Unknown URI " + uri + " (" + URI_MATCHER.match(uri) + ")");
		}

		final SQLiteDatabase db = getWritableDatabase();
		final long rowId = db.insert(ScheduleTable.TABLE_NAME, null, values);
		if (rowId > 0) {
			Uri insertUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(uri, null);
			return insertUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		final SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		try {
			for (ContentValues contentValues : values) {
				db.insert(ScheduleTable.TABLE_NAME, null, contentValues);
			}
			db.setTransactionSuccessful();
			getContext().getContentResolver().notifyChange(uri, null);
		} finally {
			db.endTransaction();
		}
		return values.length;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return 0;
	}

}
