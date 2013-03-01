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
package net.naonedbus.provider.impl;

import net.naonedbus.provider.CustomContentProvider;
import net.naonedbus.provider.table.FavoriTable;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class FavoriProvider extends CustomContentProvider {

	public static final int FAVORIS = 100;
	public static final int FAVORI_ID = 110;

	/**
	 * Récupérer les favoris selon une liste de groupes.
	 */
	public static final int FAVORI_GROUPES = 200;
	public static final String FAVORIS_GROUPES_URI_PATH_QUERY = "groupes";
	public static final String QUERY_PARAMETER_GROUPES_IDS = "groupes";

	private static final String AUTHORITY = "net.naonedbus.provider.FavoriProvider";
	private static final String ARRETS_BASE_PATH = "favoris";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + ARRETS_BASE_PATH);

	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		URI_MATCHER.addURI(AUTHORITY, ARRETS_BASE_PATH, FAVORIS);
		URI_MATCHER.addURI(AUTHORITY, ARRETS_BASE_PATH + "/#", FAVORI_ID);
		URI_MATCHER.addURI(AUTHORITY, FAVORIS_GROUPES_URI_PATH_QUERY, FAVORI_GROUPES);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		final SQLiteDatabase db = getWritableDatabase();

		int count;
		switch (URI_MATCHER.match(uri)) {
		case FAVORIS:
			count = db.delete(FavoriTable.TABLE_NAME, selection, selectionArgs);
			break;
		case FAVORI_ID:
			String segment = uri.getPathSegments().get(1);
			count = db.delete(FavoriTable.TABLE_NAME, FavoriTable._ID + "=" + segment
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI (" + URI_MATCHER.match(uri) + ") " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);

		return count;
	}

	@Override
	public String getType(Uri arg0) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		ContentValues values;

		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}

		if (URI_MATCHER.match(uri) != FAVORIS) {
			throw new IllegalArgumentException("Unknown URI " + uri + " (" + URI_MATCHER.match(uri) + ")");
		}

		final SQLiteDatabase db = getWritableDatabase();
		final long rowId = db.insert(FavoriTable.TABLE_NAME, null, values);
		if (rowId > 0) {
			final Uri insertUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(uri, null);
			return insertUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		if (projection == null) {
			queryBuilder.setTables(FavoriTable.JOIN);
			projection = FavoriTable.FULL_PROJECTION;
			if (sortOrder == null) {
				sortOrder = FavoriTable.FULL_ORDER;
			}
		} else {
			queryBuilder.setTables(FavoriTable.TABLE_NAME);
		}

		int uriType = URI_MATCHER.match(uri);
		switch (uriType) {
		case FAVORI_ID:
			queryBuilder.appendWhere(FavoriTable._ID + "=" + uri.getLastPathSegment());
			break;
		case FAVORI_GROUPES:
			final String where = String.format(FavoriTable.WHERE, uri.getQueryParameter(QUERY_PARAMETER_GROUPES_IDS));
			queryBuilder.appendWhere(where);

			break;
		case FAVORIS:

			break;
		default:
			throw new IllegalArgumentException("Unknown URI (" + uri + ")");
		}

		final Cursor cursor = queryBuilder.query(getReadableDatabase(), projection, selection, selectionArgs, null,
				null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = getWritableDatabase();
		final int rowCount = db.update(FavoriTable.TABLE_NAME, values, selection, selectionArgs);
		if (rowCount > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return rowCount;
	}

}
