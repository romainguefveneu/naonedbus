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
import net.naonedbus.provider.table.FavoriViewTable;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class FavorisViewProvider extends ReadOnlyContentProvider {

	public static final int FAVORIS = 100;

	/**
	 * Récupérer les favoris selon une liste de groupes.
	 */
	public static final int FAVORI_GROUPES = 200;
	public static final String FAVORIS_GROUPES_URI_PATH_QUERY = "groupes";
	public static final String QUERY_PARAMETER_GROUPES_IDS = "groupes";

	/**
	 * Récupérer uniquement les favoris, sans groupe.
	 */
	public static final int FAVORI_UNIQUES = 300;
	public static final String FAVORIS_UNIQUES_URI_PATH_QUERY = "uniques";

	private static final String AUTHORITY = "net.naonedbus.provider.FavorisViewProvider";
	private static final String ARRETS_BASE_PATH = "favoris";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + ARRETS_BASE_PATH);

	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		URI_MATCHER.addURI(AUTHORITY, ARRETS_BASE_PATH, FAVORIS);
		URI_MATCHER.addURI(AUTHORITY, FAVORIS_GROUPES_URI_PATH_QUERY, FAVORI_GROUPES);
		URI_MATCHER.addURI(AUTHORITY, FAVORIS_UNIQUES_URI_PATH_QUERY, FAVORI_UNIQUES);
	}

	@Override
	public Cursor query(final Uri uri, final String[] projection, final String selection, final String[] selectionArgs,
			String sortOrder) {
		final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(FavoriViewTable.TABLE_NAME);

		if (sortOrder == null) {
			sortOrder = FavoriViewTable.ORDER;
		}

		final int uriType = URI_MATCHER.match(uri);
		switch (uriType) {
		case FAVORI_GROUPES:
			final String idGroupes = uri.getQueryParameter(QUERY_PARAMETER_GROUPES_IDS);
			final String where = String.format(FavoriViewTable.WHERE, idGroupes);
			queryBuilder.appendWhere(where);
			break;
		case FAVORI_UNIQUES:
			queryBuilder.setDistinct(true);
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

}
