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
import net.naonedbus.provider.table.FavorisGroupesTable;
import net.naonedbus.provider.table.GroupeTable;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class FavoriGroupeProvider extends CustomContentProvider {

	private static final int GROUPES = 100;
	private static final int GROUPE_ID = 110;
	private static final int LINK = 200;
	private static final int FAVORI_ID = 300;

	public static final String QUERY_PARAMETER_IDS = "ids";

	private static final String AUTHORITY = "net.naonedbus.provider.FavoriGroupeProvider";
	public static final String LINK_BASE_PATH = "link";
	public static final String FAVORI_ID_BASE_PATH = "favori";

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		URI_MATCHER.addURI(AUTHORITY, null, GROUPES);
		URI_MATCHER.addURI(AUTHORITY, "#/#", GROUPES);
		URI_MATCHER.addURI(AUTHORITY, "#", GROUPE_ID);
		URI_MATCHER.addURI(AUTHORITY, FAVORI_ID_BASE_PATH, FAVORI_ID);
		URI_MATCHER.addURI(AUTHORITY, LINK_BASE_PATH, LINK);
	}

	@Override
	public int delete(final Uri uri, final String selection, final String[] selectionArgs) {
		final SQLiteDatabase db = getWritableDatabase();

		int count;
		switch (URI_MATCHER.match(uri)) {
		case GROUPES:
			final String idGroupe = uri.getPathSegments().get(0);
			final String idFavori = uri.getLastPathSegment();
			count = db.delete(FavorisGroupesTable.TABLE_NAME, FavorisGroupesTable.ID_GROUPE + "=" + idGroupe + " AND "
					+ FavorisGroupesTable.ID_FAVORI + "=" + idFavori, null);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI (" + URI_MATCHER.match(uri) + ") " + uri);
		}

		if (count > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}

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
			rowId = db.insertWithOnConflict(FavorisGroupesTable.TABLE_NAME, null, values,
					SQLiteDatabase.CONFLICT_IGNORE);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri + " (" + URI_MATCHER.match(uri) + ")");
		}

		if (rowId > 0) {
			final Uri insertUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(uri, null);
			return insertUri;
		}

		return null;
	}

	@Override
	public Cursor query(final Uri uri, final String[] projection, final String selection, final String[] selectionArgs,
			final String sortOrder) {
		final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(FavorisGroupesTable.TABLE_NAME);

		final String query;
		final int uriType = URI_MATCHER.match(uri);
		switch (uriType) {
		case LINK:
			final String favoriIds = uri.getQueryParameter(FavoriGroupeProvider.QUERY_PARAMETER_IDS);
			query = String.format(LinkQuery.SELECT, favoriIds);
			return getReadableDatabase().rawQuery(query, null);

		case FAVORI_ID:
			final String favoriId = uri.getQueryParameter(FavoriGroupeProvider.QUERY_PARAMETER_IDS);
			query = String.format(FavoriGroupesQuery.SELECT, favoriId);
			return getReadableDatabase().rawQuery(query, null);

		case GROUPE_ID:
			queryBuilder.appendWhere(FavorisGroupesTable.ID_GROUPE + "=" + uri.getLastPathSegment());
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
		final int rowCount = db.update(FavorisGroupesTable.TABLE_NAME, values, selection, selectionArgs);
		if (rowCount > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return rowCount;
	}

	/**
	 * Composants de la requête de sélection des groupes avec l'association à un
	 * favori.
	 * 
	 * @author romain.guefveneu
	 * 
	 */
	private static interface LinkQuery {
		final String GROUPE_COUNT = "SELECT MIN(COUNT(1),1) FROM " + FavorisGroupesTable.TABLE_NAME + " fgt WHERE fgt."
				+ FavorisGroupesTable.ID_GROUPE + "=" + GroupeTable.TABLE_NAME + "." + GroupeTable._ID + " AND fgt."
				+ FavorisGroupesTable.ID_FAVORI + " IN (%s)";

		final String ORDER = " ORDER BY " + GroupeTable.ORDRE;

		public static final String SELECT = "SELECT " + GroupeTable._ID + ", " + GroupeTable.NOM + ", "
				+ GroupeTable.VISIBILITE + ", " + GroupeTable.ORDRE + ", (" + GROUPE_COUNT + ") as "
				+ FavorisGroupesTable.LINKED + " FROM " + GroupeTable.TABLE_NAME + ORDER;

	}

	/**
	 * Composants de la requête de sélection des groupes associés à un favori.
	 * 
	 * @author romain.guefveneu
	 * 
	 */
	private static interface FavoriGroupesQuery {

		public static final String SELECT = "SELECT " + GroupeTable._ID + ", " + GroupeTable.NOM + ", "
				+ GroupeTable.VISIBILITE + ", " + GroupeTable.ORDRE + " FROM " + GroupeTable.TABLE_NAME
				+ " INNER JOIN " + FavorisGroupesTable.TABLE_NAME + " fgt ON fgt." + FavorisGroupesTable.ID_GROUPE
				+ " = " + GroupeTable._ID + " AND fgt." + FavorisGroupesTable.ID_FAVORI + "= %s";;
	}

}
