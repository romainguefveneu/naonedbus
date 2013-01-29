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

import net.naonedbus.provider.ReadOnlyContentProvider;
import net.naonedbus.provider.table.ArretTable;
import net.naonedbus.provider.table.FavoriTable;
import net.naonedbus.provider.table.LigneTable;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class LigneProvider extends ReadOnlyContentProvider {

	/**
	 * Recherche.
	 */
	public static final int SEARCH = 10;

	/**
	 * Toutes les lignes.
	 */
	public static final int LIGNES = 100;
	public static final String LIGNE_URI_PATH_QUERY = "lignes";

	/**
	 * Ligne par son ID
	 */
	public static final int LIGNE_ID = 110;

	/**
	 * Ligne par son code.
	 */
	public static final int LIGNE_CODE = 120;
	public static final String LIGNE_CODE_URI_PATH_QUERY = "code";

	/**
	 * Lignes passant par une station donnée.
	 */
	public static final int LIGNE_STATION = 200;
	public static final String LIGNE_STATION_URI_PATH_QUERY = "arret";

	/**
	 * Lignes par type.
	 */
	public static final int LIGNE_TYPE = 300;
	public static final String LIGNE_TYPE_URI_PATH_QUERY = "type";

	/**
	 * Lignes ayant des favoris.
	 */
	public static final int LIGNE_FAVORIS = 400;
	public static final String LIGNE_FAVORIS_URI_PATH_QUERY = "favoris";

	private static final String AUTHORITY = "net.naonedbus.provider.LigneProvider";
	private static final String LIGNES_BASE_PATH = "lignes";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + LIGNES_BASE_PATH);

	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		URI_MATCHER.addURI(AUTHORITY, LIGNE_URI_PATH_QUERY, LIGNES);
		URI_MATCHER.addURI(AUTHORITY, LIGNE_URI_PATH_QUERY + "/*", SEARCH);
		URI_MATCHER.addURI(AUTHORITY, LIGNE_URI_PATH_QUERY + "/#", LIGNE_ID);
		URI_MATCHER.addURI(AUTHORITY, LIGNE_CODE_URI_PATH_QUERY, LIGNE_CODE);
		URI_MATCHER.addURI(AUTHORITY, LIGNE_STATION_URI_PATH_QUERY, LIGNE_STATION);
		URI_MATCHER.addURI(AUTHORITY, LIGNE_TYPE_URI_PATH_QUERY + "/#", LIGNE_TYPE);
		URI_MATCHER.addURI(AUTHORITY, LIGNE_FAVORIS_URI_PATH_QUERY, LIGNE_FAVORIS);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(LigneTable.TABLE_NAME);

		if (sortOrder == null) {
			sortOrder = "type, CAST(" + LigneTable.LETTRE + " as numeric)";
		}

		int uriType = URI_MATCHER.match(uri);
		switch (uriType) {
		case SEARCH:
			final String keyword = uri.getLastPathSegment();
			queryBuilder.appendWhere(LigneTable.LETTRE + " LIKE ");
			queryBuilder.appendWhereEscapeString("%" + keyword + "%");
			queryBuilder.appendWhere(" OR ");
			queryBuilder.appendWhere(LigneTable.DEPUIS + " LIKE ");
			queryBuilder.appendWhereEscapeString("%" + keyword + "%");
			queryBuilder.appendWhere(" OR ");
			queryBuilder.appendWhere(LigneTable.VERS + " LIKE ");
			queryBuilder.appendWhereEscapeString("%" + keyword + "%");
			break;

		case LIGNE_ID:
			queryBuilder.appendWhere(LigneTable._ID + "=" + uri.getLastPathSegment());
			break;

		case LIGNE_CODE:
			queryBuilder.appendWhere(LigneTable.CODE + "=");
			queryBuilder.appendWhereEscapeString(uri.getQueryParameter("code"));
			break;

		case LIGNE_STATION:
			queryBuilder.appendWhere(String.format(LigneArretQuery.WHERE, uri.getQueryParameter("idStation")));
			sortOrder = LigneArretQuery.ORDER_BY;
			break;

		case LIGNE_TYPE:
			queryBuilder.appendWhere(LigneTable.TYPE + "=" + uri.getLastPathSegment());
			break;

		case LIGNE_FAVORIS:
			queryBuilder.appendWhere(LigneFavorisQuery.WHERE);
			break;

		case LIGNES:
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
	 * Composants de la requête de sélection des lignes passant par un arrêt.
	 */
	private static interface LigneArretQuery {
		public static final String WHERE = LigneTable.CODE + "  IN (SELECT distinct(" + ArretTable.CODE_LIGNE
				+ " ) FROM " + ArretTable.TABLE_NAME + " WHERE " + ArretTable.ID_STATION + "= %s)";
		public static final String ORDER_BY = LigneTable.TYPE + ", CAST(" + LigneTable.LETTRE + " as numeric) ";

	}

	/**
	 * Composants de la requête de sélection des lignes ayant au moins un
	 * favori.
	 */
	private static interface LigneFavorisQuery {
		public static final String WHERE = LigneTable.CODE + "  IN (SELECT distinct(" + FavoriTable.CODE_LIGNE
				+ " ) FROM " + FavoriTable.TABLE_NAME + ")";

	}

}
