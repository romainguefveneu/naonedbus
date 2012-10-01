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
import net.naonedbus.provider.table.EquipementTable;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class ArretProvider extends ReadOnlyContentProvider {

	/**
	 * Récupérer tous les arrêts
	 */
	private static final int ARRETS = 20;
	/**
	 * Récupérer un arrêt par son id
	 */
	private static final int ARRET_ID = 30;

	/**
	 * Récupérer un arrêt par son code ligne et code sens.
	 */
	private static final int ARRET_CODESENS_CODELIGNE = 40;
	public static final String ARRET_CODESENS_CODELIGNE_URI_PATH_QUERY = "codeSensLigne";

	/**
	 * Récupérer un arrêt par son code arret, code ligne et code sens
	 */
	private static final int ARRET_CODEARRET_CODESENS_CODELIGNE = 41;
	public static final String ARRET_CODEARRET_CODESENS_CODELIGNE_URI_PATH_QUERY = "arretSensLigne";

	private static final String AUTHORITY = "net.naonedbus.provider.ArretProvider";
	private static final String ARRETS_BASE_PATH = "arrets";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + ARRETS_BASE_PATH);

	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	static {

		URI_MATCHER.addURI(AUTHORITY, ARRETS_BASE_PATH, ARRETS);
		URI_MATCHER.addURI(AUTHORITY, ARRETS_BASE_PATH + "/#", ARRET_ID);

		URI_MATCHER.addURI(AUTHORITY, ARRET_CODESENS_CODELIGNE_URI_PATH_QUERY, ARRET_CODESENS_CODELIGNE);
		URI_MATCHER.addURI(AUTHORITY, ARRET_CODEARRET_CODESENS_CODELIGNE_URI_PATH_QUERY,
				ARRET_CODEARRET_CODESENS_CODELIGNE);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		Cursor cursor;
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(ArretTable.TABLE_NAME + ArretTable.TABLE_JOIN_STATIONS);
		String groupBy = EquipementTable.NORMALIZED_NOM;
		projection = ArretTable.PROJECTION;

		int uriType = URI_MATCHER.match(uri);
		switch (uriType) {

		case ARRET_ID:
			queryBuilder.appendWhere(ArretTable.TABLE_NAME + "." + ArretTable._ID + "=");
			queryBuilder.appendWhereEscapeString(uri.getLastPathSegment());
			break;
		case ARRET_CODESENS_CODELIGNE:
			queryBuilder.appendWhere(ArretTable.TABLE_NAME + "." + ArretTable.CODE_SENS + " = ");
			queryBuilder.appendWhereEscapeString(uri.getQueryParameter("codeSens"));
			queryBuilder.appendWhere(" AND ");
			queryBuilder.appendWhere(ArretTable.TABLE_NAME + "." + ArretTable.CODE_LIGNE + " = ");
			queryBuilder.appendWhereEscapeString(uri.getQueryParameter("codeLigne"));
			queryBuilder.appendWhere(" AND ");
			queryBuilder.appendWhere(EquipementTable.TABLE_NAME + "." + EquipementTable.CODE + " is not null");
			break;
		case ARRET_CODEARRET_CODESENS_CODELIGNE:
			queryBuilder.appendWhere(ArretTable.TABLE_NAME + "." + ArretTable.CODE + " = ");
			queryBuilder.appendWhereEscapeString(uri.getQueryParameter("codeArret"));
			queryBuilder.appendWhere(" AND ");
			queryBuilder.appendWhere(ArretTable.TABLE_NAME + "." + ArretTable.CODE_SENS + " = ");
			queryBuilder.appendWhereEscapeString(uri.getQueryParameter("codeSens"));
			queryBuilder.appendWhere(" AND ");
			queryBuilder.appendWhere(ArretTable.TABLE_NAME + "." + ArretTable.CODE_LIGNE + " = ");
			queryBuilder.appendWhereEscapeString(uri.getQueryParameter("codeLigne"));
			queryBuilder.appendWhere(" AND ");
			queryBuilder.appendWhere(EquipementTable.TABLE_NAME + "." + EquipementTable.CODE + " is not null");
			break;

		case ARRETS:
			// no filter
			break;

		default:
			throw new IllegalArgumentException("Unknown URI (" + uri + ")");
		}

		cursor = queryBuilder.query(getReadableDatabase(), projection, selection, selectionArgs, groupBy, null,
				sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

}
