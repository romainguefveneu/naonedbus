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

import net.naonedbus.provider.ReadOnlyContentProvider;
import net.naonedbus.provider.table.StopsViewTable;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class StopViewProvider extends ReadOnlyContentProvider {

	/**
	 * Récupérer tous les arrêts
	 */
	private static final int STOPS = 20;
	/**
	 * Récupérer un arrêt par son id
	 */
	private static final int STOP_ID = 30;

	/**
	 * Récupérer un arrêt par son code route et code direction.
	 */
	private static final int STOP_DIRECTION_ROUTE = 40;
	public static final String STOP_DIRECTION_ROUTE_URI_PATH_QUERY = "directionRoute";

	/**
	 * Récupérer un arrêt par son code stop, code route et code direction
	 */
	private static final int STOP_CODES = 41;
	public static final String STOP_CODES_URI_PATH_QUERY = "all";

	private static final String AUTHORITY = "net.naonedbus.provider.StopViewProvider";
	private static final String BASE_PATH = "stops";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	static {

		URI_MATCHER.addURI(AUTHORITY, BASE_PATH, STOPS);
		URI_MATCHER.addURI(AUTHORITY, BASE_PATH + "/#", STOP_ID);

		URI_MATCHER.addURI(AUTHORITY, STOP_DIRECTION_ROUTE_URI_PATH_QUERY, STOP_DIRECTION_ROUTE);
		URI_MATCHER.addURI(AUTHORITY, STOP_CODES_URI_PATH_QUERY, STOP_CODES);
	}

	@Override
	public Cursor query(final Uri uri, final String[] projection, final String selection, final String[] selectionArgs,
			final String sortOrder) {
		Cursor cursor;
		final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(StopsViewTable.TABLE_NAME);

		final int uriType = URI_MATCHER.match(uri);
		switch (uriType) {

		case STOP_ID:
			queryBuilder.appendWhere(StopsViewTable.TABLE_NAME + "." + StopsViewTable._ID + "=");
			queryBuilder.appendWhereEscapeString(uri.getLastPathSegment());
			break;
		case STOP_DIRECTION_ROUTE:
			final String serviceId = uri.getQueryParameter("serviceId");
			if (serviceId != null) {
				queryBuilder.appendWhere(StopsViewTable.TABLE_NAME + "." + StopsViewTable.SERVICE_ID + " = ");
				queryBuilder.appendWhereEscapeString(uri.getQueryParameter("serviceId"));
				queryBuilder.appendWhere(" AND ");
			}
			queryBuilder.appendWhere(StopsViewTable.TABLE_NAME + "." + StopsViewTable.DIRECTION_CODE + " = ");
			queryBuilder.appendWhereEscapeString(uri.getQueryParameter("directionCode"));
			queryBuilder.appendWhere(" AND ");
			queryBuilder.appendWhere(StopsViewTable.TABLE_NAME + "." + StopsViewTable.ROUTE_CODE + " = ");
			queryBuilder.appendWhereEscapeString(uri.getQueryParameter("routeCode"));
			break;
		case STOP_CODES:
			queryBuilder.appendWhere(StopsViewTable.TABLE_NAME + "." + StopsViewTable.STOP_CODE + " = ");
			queryBuilder.appendWhereEscapeString(uri.getQueryParameter("codeArret"));
			queryBuilder.appendWhere(" AND ");
			queryBuilder.appendWhere(StopsViewTable.TABLE_NAME + "." + StopsViewTable.DIRECTION_CODE + " = ");
			queryBuilder.appendWhereEscapeString(uri.getQueryParameter("directionCode"));
			queryBuilder.appendWhere(" AND ");
			queryBuilder.appendWhere(StopsViewTable.TABLE_NAME + "." + StopsViewTable.ROUTE_CODE + " = ");
			queryBuilder.appendWhereEscapeString(uri.getQueryParameter("routeCode"));
			break;

		case STOPS:
			// no filter
			break;

		default:
			throw new IllegalArgumentException("Unknown URI (" + uri + ")");
		}

		cursor = queryBuilder.query(getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

}
