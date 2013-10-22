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
import net.naonedbus.provider.table.DirectionTable;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class DirectionProvider extends ReadOnlyContentProvider {

	/**
	 * Tous les direction
	 */
	public static final int direction = 100;
	private static final String SENS_BASE_PATH = "direction";

	/**
	 * direction par son ID
	 */
	public static final int SENS_ID = 110;

	/**
	 * direction par directionCode
	 */
	public static final int SENS_LIGNE_CODE = 200;
	public static final String SENS_LIGNE_CODE_URI_PATH_QUERY = "routeCode";
	/**
	 * direction par directionCode et routeCode
	 */
	public static final int SENS_CODE_LIGNE_CODE = 300;
	public static final String SENS_CODE_LIGNE_CODE_URI_PATH_QUERY = "directionCodeCodeLigne";

	private static final String AUTHORITY = "net.naonedbus.provider.DirectionProvider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + SENS_BASE_PATH);

	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		URI_MATCHER.addURI(AUTHORITY, SENS_BASE_PATH, direction);
		URI_MATCHER.addURI(AUTHORITY, SENS_BASE_PATH + "/#", SENS_ID);
		URI_MATCHER.addURI(AUTHORITY, SENS_LIGNE_CODE_URI_PATH_QUERY, SENS_LIGNE_CODE);
		URI_MATCHER.addURI(AUTHORITY, SENS_CODE_LIGNE_CODE_URI_PATH_QUERY, SENS_CODE_LIGNE_CODE);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(DirectionTable.TABLE_NAME);
		if (sortOrder == null)
			sortOrder = DirectionTable.DIRECTION_CODE;

		int uriType = URI_MATCHER.match(uri);
		switch (uriType) {
		case SENS_ID:
			queryBuilder.appendWhere(DirectionTable._ID + "=" + uri.getLastPathSegment());
			break;
		case SENS_LIGNE_CODE:
			queryBuilder.appendWhere(DirectionTable.ROUTE_CODE + "=");
			queryBuilder.appendWhereEscapeString(uri.getQueryParameter("routeCode"));

			break;
		case SENS_CODE_LIGNE_CODE:
			queryBuilder.setTables(DirectionTable.TABLE_NAME);
			queryBuilder.appendWhere(DirectionTable.DIRECTION_CODE + "=");
			queryBuilder.appendWhereEscapeString(uri.getQueryParameter("directionCode"));
			queryBuilder.appendWhere(" AND ");
			queryBuilder.appendWhere(DirectionTable.ROUTE_CODE + "=");
			queryBuilder.appendWhereEscapeString(uri.getQueryParameter("routeCode"));

			break;
		case direction:
			// no filter
			break;
		default:
			throw new IllegalArgumentException("Unknown URI (" + uri + ")");
		}

		Cursor cursor = queryBuilder.query(getReadableDatabase(), projection, selection, selectionArgs, null, null,
				sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

}
