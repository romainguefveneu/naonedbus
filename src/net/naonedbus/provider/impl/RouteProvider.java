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
import net.naonedbus.provider.table.RouteTable;
import net.naonedbus.provider.table.StopTable;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class RouteProvider extends ReadOnlyContentProvider {

	/**
	 * Search.
	 */
	public static final int SEARCH = 10;

	/**
	 * All routes.
	 */
	public static final int ROUTES = 100;
	public static final String ROUTE_URI_PATH_QUERY = "routes";

	/**
	 * Route by id.
	 */
	public static final int ROUTE_ID = 110;

	/**
	 * Route by code.
	 */
	public static final int ROUTE_CODE = 120;
	public static final String ROUTE_CODE_URI_PATH_QUERY = "code";

	/**
	 * Routes by stop.
	 */
	public static final int ROUTE_STOP = 200;
	public static final String ROUTE_STOP_URI_PATH_QUERY = "stop";

	/**
	 * Routes by type.
	 */
	public static final int ROUTE_TYPE = 300;
	public static final String ROUTE_TYPE_URI_PATH_QUERY = "type";

	private static final String AUTHORITY = "net.naonedbus.provider.RouteProvider";
	private static final String ROUTE_BASE_PATH = "routes";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + ROUTE_BASE_PATH);

	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		URI_MATCHER.addURI(AUTHORITY, ROUTE_URI_PATH_QUERY, ROUTES);
		URI_MATCHER.addURI(AUTHORITY, ROUTE_URI_PATH_QUERY + "/*", SEARCH);
		URI_MATCHER.addURI(AUTHORITY, ROUTE_URI_PATH_QUERY + "/#", ROUTE_ID);
		URI_MATCHER.addURI(AUTHORITY, ROUTE_CODE_URI_PATH_QUERY + "/*", ROUTE_CODE);
		URI_MATCHER.addURI(AUTHORITY, ROUTE_STOP_URI_PATH_QUERY + "/*", ROUTE_STOP);
		URI_MATCHER.addURI(AUTHORITY, ROUTE_TYPE_URI_PATH_QUERY + "/#", ROUTE_TYPE);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(RouteTable.TABLE_NAME);

		if (sortOrder == null) {
			sortOrder = RouteTable.TYPE_ID + ", CAST(" + RouteTable.LETTER + " as numeric)";
		}

		int uriType = URI_MATCHER.match(uri);
		switch (uriType) {
		case SEARCH:
			final String keyword = uri.getLastPathSegment();
			queryBuilder.appendWhere(RouteTable.LETTER + " LIKE ");
			queryBuilder.appendWhereEscapeString("%" + keyword + "%");
			queryBuilder.appendWhere(" OR ");
			queryBuilder.appendWhere(RouteTable.HEADSIGN_FROM + " LIKE ");
			queryBuilder.appendWhereEscapeString("%" + keyword + "%");
			queryBuilder.appendWhere(" OR ");
			queryBuilder.appendWhere(RouteTable.HEADSIGN_TO + " LIKE ");
			queryBuilder.appendWhereEscapeString("%" + keyword + "%");
			break;

		case ROUTE_ID:
			queryBuilder.appendWhere(RouteTable._ID + "=" + uri.getLastPathSegment());
			break;

		case ROUTE_CODE:
			queryBuilder.appendWhere(RouteTable.ROUTE_CODE + "=");
			queryBuilder.appendWhereEscapeString(uri.getLastPathSegment());
			break;

		case ROUTE_STOP:
			queryBuilder.appendWhere(String.format(RouteStopQuery.WHERE, uri.getLastPathSegment()));
			sortOrder = RouteStopQuery.ORDER_BY;
			break;

		case ROUTE_TYPE:
			queryBuilder.appendWhere(RouteTable.TYPE_ID + "=" + uri.getLastPathSegment());
			break;

		case ROUTES:
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

	/**
	 * Routes by stop query.
	 */
	private static interface RouteStopQuery {
		public static final String WHERE = RouteTable.ROUTE_CODE + "  IN (SELECT distinct(" + StopTable.ROUTE_CODE
				+ " ) FROM " + StopTable.TABLE_NAME + " WHERE " + StopTable.EQUIPMENT_ID + "= %s)";
		public static final String ORDER_BY = RouteTable.TYPE_ID + ", CAST(" + RouteTable.LETTER + " as numeric) ";

	}

}
