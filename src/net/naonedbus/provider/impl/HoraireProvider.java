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
import net.naonedbus.provider.table.HoraireTable;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

/**
 * @author romain.guefveneu
 * 
 */
public class HoraireProvider extends CustomContentProvider {

	public static final String PARAM_ARRET_ID = "arretId";
	public static final String PARAM_YEAR = "year";
	public static final String PARAM_DAY_OF_YEAR = "dayOfYear";
	public static final String PARAM_INCLUDE_LAST_DAY_TRIP = "includeLastDayTrip";
	public static final String PARAM_AFTER_TIME = "afterTime";

	public static final int HORAIRE = 100;
	public static final int HORAIRE_ID = 110;

	/**
	 * Les horaires d'un arrêt et d'un jour
	 */
	public static final int HORAIRE_JOUR = 200;
	public static final String HORAIRE_JOUR_URI_PATH_QUERY = "arret";

	private static final String AUTHORITY = "net.naonedbus.provider.HoraireProvider";
	private static final String HORAIRE_BASE_PATH = "horaire";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + HORAIRE_BASE_PATH);

	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		URI_MATCHER.addURI(AUTHORITY, HORAIRE_BASE_PATH, HORAIRE);
		URI_MATCHER.addURI(AUTHORITY, HORAIRE_BASE_PATH + "/#", HORAIRE_ID);

		URI_MATCHER.addURI(AUTHORITY, HORAIRE_JOUR_URI_PATH_QUERY, HORAIRE_JOUR);

	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(HoraireTable.TABLE_NAME);

		if (sortOrder == null) {
			sortOrder = "_id";
		}

		int uriType = URI_MATCHER.match(uri);
		switch (uriType) {
		case HORAIRE_ID:
			queryBuilder.appendWhere(HoraireTable._ID + "=" + uri.getLastPathSegment());
			break;
		case HORAIRE_JOUR:
			final String idArret = uri.getQueryParameter(PARAM_ARRET_ID);
			final int dayOfYear = Integer.parseInt(uri.getQueryParameter(PARAM_DAY_OF_YEAR));
			final String year = uri.getQueryParameter(PARAM_YEAR);
			final String afterTime = uri.getQueryParameter(PARAM_AFTER_TIME);

			queryBuilder.appendWhere(HoraireTable.ID_ARRET + " = " + idArret);
			queryBuilder.appendWhere(" AND (");
			queryBuilder.appendWhere(HoraireTable.YEAR + " = " + year);
			queryBuilder.appendWhere(" AND ");
			queryBuilder.appendWhere(HoraireTable.DAY_OF_YEAR + " = " + dayOfYear);

			// Fin du trip passé pour les horaires d'après minuit
			if (uri.getQueryParameter(PARAM_INCLUDE_LAST_DAY_TRIP) != null) {
				queryBuilder.appendWhere(" OR (");
				queryBuilder.appendWhere(HoraireTable.DAY_OF_YEAR + " = " + (dayOfYear-1));
//				queryBuilder.appendWhere(" AND ");
//				queryBuilder.appendWhere(HoraireTable.MINUTES + " >= " + dayTrip);
				queryBuilder.appendWhere(")");
			}

			queryBuilder.appendWhere(")");

			// Eviter l'affichage de doublons
			if (afterTime != null) {
				queryBuilder.appendWhere(" AND ");
				queryBuilder.appendWhere(HoraireTable.MINUTES + " > " + afterTime);
			}

			break;
		case HORAIRE:
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
		case HORAIRE:
			count = db.delete(HoraireTable.TABLE_NAME, selection, selectionArgs);
			break;
		case HORAIRE_ID:
			String segment = uri.getPathSegments().get(1);
			count = db.delete(HoraireTable.TABLE_NAME,
					HoraireTable._ID + "=" + segment
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

		if (URI_MATCHER.match(uri) != HORAIRE) {
			throw new IllegalArgumentException("Unknown URI " + uri + " (" + URI_MATCHER.match(uri) + ")");
		}

		final SQLiteDatabase db = getWritableDatabase();
		final long rowId = db.insert(HoraireTable.TABLE_NAME, null, values);
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
				db.insert(HoraireTable.TABLE_NAME, null, contentValues);
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
