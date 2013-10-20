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
import net.naonedbus.provider.table.StopBookmarkGroupLinkTable;
import net.naonedbus.provider.table.StopBookmarkGroupTable;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class StopBookmarkGroupProvider extends CustomContentProvider {

	public static final int GROUPES = 100;
	public static final int GROUPE_ID = 110;

	public static final int FAVORIS_GROUPES = 200;
	public static final String FAVORIS_GROUPES_URI_PATH_QUERY = "favorisGroupes";

	private static final String AUTHORITY = "net.naonedbus.provider.StopBookmarkGroupProvider";
	private static final String GROUPES_BASE_PATH = "groupes";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + GROUPES_BASE_PATH);

	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		URI_MATCHER.addURI(AUTHORITY, GROUPES_BASE_PATH, GROUPES);
		URI_MATCHER.addURI(AUTHORITY, GROUPES_BASE_PATH + "/#", GROUPE_ID);
		URI_MATCHER.addURI(AUTHORITY, GROUPES_BASE_PATH + "/#/#", FAVORIS_GROUPES);
		URI_MATCHER.addURI(AUTHORITY, FAVORIS_GROUPES_URI_PATH_QUERY, FAVORIS_GROUPES);
	}

	@Override
	public int delete(final Uri uri, final String selection, final String[] selectionArgs) {
		final SQLiteDatabase db = getWritableDatabase();
		final String segment;

		int count;
		switch (URI_MATCHER.match(uri)) {
		case GROUPES:
			count = db.delete(StopBookmarkGroupTable.TABLE_NAME, selection, selectionArgs);
			break;
		case GROUPE_ID:
			segment = uri.getLastPathSegment();
			count = db.delete(StopBookmarkGroupTable.TABLE_NAME, StopBookmarkGroupTable._ID + "=" + segment
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
			break;

		case FAVORIS_GROUPES:
			segment = uri.getPathSegments().get(1);
			final String idFavori = uri.getLastPathSegment();
			count = db.delete(StopBookmarkGroupLinkTable.TABLE_NAME, StopBookmarkGroupLinkTable.GROUP_ID + "=" + segment + " AND "
					+ StopBookmarkGroupLinkTable.STOP_BOOKMARK_ID + "=" + idFavori, null);

			break;
		default:
			throw new IllegalArgumentException("Unknown URI (" + URI_MATCHER.match(uri) + ") " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);

		return count;
	}

	@Override
	public String getType(final Uri uri) {
		return null;
	}

	@Override
	public Uri insert(final Uri uri, final ContentValues initialValues) {
		final SQLiteDatabase db = getWritableDatabase();
		final long rowId;
		ContentValues values;

		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}

		switch (URI_MATCHER.match(uri)) {
		case GROUPES:
			rowId = db.insert(StopBookmarkGroupTable.TABLE_NAME, null, values);
			break;
		case FAVORIS_GROUPES:
			rowId = db.insert(StopBookmarkGroupLinkTable.TABLE_NAME, null, values);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri + " (" + URI_MATCHER.match(uri) + ")");
		}

		if (rowId > 0) {
			final Uri insertUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(uri, null);
			return insertUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public Cursor query(final Uri uri, final String[] projection, final String selection, final String[] selectionArgs, String sortOrder) {
		final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(StopBookmarkGroupTable.TABLE_NAME);
		if (sortOrder == null) {
			sortOrder = StopBookmarkGroupTable.GROUP_ORDER;
		}

		final int uriType = URI_MATCHER.match(uri);
		switch (uriType) {
		case GROUPE_ID:
			queryBuilder.appendWhere(StopBookmarkGroupTable._ID + "=" + uri.getLastPathSegment());
			break;

		case FAVORIS_GROUPES:
			queryBuilder.setTables(StopBookmarkGroupLinkTable.TABLE_NAME);
			sortOrder = null;
			break;

		case GROUPES:

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
	public int update(final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs) {
		final SQLiteDatabase db = getWritableDatabase();
		final int rowCount = db.update(StopBookmarkGroupTable.TABLE_NAME, values, selection, selectionArgs);
		if (rowCount > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return rowCount;
	}

}
