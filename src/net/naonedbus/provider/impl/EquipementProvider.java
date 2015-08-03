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

import java.text.Normalizer;
import java.util.HashMap;
import java.util.regex.Pattern;

import net.naonedbus.provider.CustomContentProvider;
import net.naonedbus.provider.table.EquipementTable;
import net.naonedbus.provider.table.TypeEquipementTable;
import android.app.SearchManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class EquipementProvider extends CustomContentProvider {

	/**
	 * Recherche Android.
	 */
	private static final int SEARCH = 10;
	/**
	 * Touss les equipements
	 */
	private static final int EQUIPEMENTS = 100;
	public static final String EQUIPEMENTS_URI_PATH_QUERY = "equipements";

	/**
	 * Equipement par son ID
	 */
	private static final int EQUIPEMENT_ID = 110;

	/**
	 * Equipement par son Type
	 */
	private static final int EQUIPEMENTS_TYPE = 200;
	public static final String EQUIPEMENTS_TYPE_URI_PATH_QUERY = "type";

	/**
	 * Equipement par localisation
	 */

	private static final int EQUIPEMENTS_LOCATION = 300;
	public static final String EQUIPEMENTS_LOCATION_URI_PATH_QUERY = "location";

	/**
	 * Equipement par son Nom
	 */
	private static final int EQUIPEMENTS_NAME = 400;

	private static final String AUTHORITY = "net.naonedbus.provider.EquipementProvider";
	private static final String EQUIPEMENTS_BASE_PATH = "equipements";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + EQUIPEMENTS_BASE_PATH);

	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		URI_MATCHER.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH);
		URI_MATCHER.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH);

		URI_MATCHER.addURI(AUTHORITY, EQUIPEMENTS_URI_PATH_QUERY, EQUIPEMENTS);
		URI_MATCHER.addURI(AUTHORITY, EQUIPEMENTS_URI_PATH_QUERY + "/#", EQUIPEMENT_ID);
		URI_MATCHER.addURI(AUTHORITY, EQUIPEMENTS_URI_PATH_QUERY + "/*", EQUIPEMENTS_NAME);
		URI_MATCHER.addURI(AUTHORITY, EQUIPEMENTS_TYPE_URI_PATH_QUERY + "/#", EQUIPEMENTS_TYPE);
		URI_MATCHER.addURI(AUTHORITY, EQUIPEMENTS_LOCATION_URI_PATH_QUERY, EQUIPEMENTS_LOCATION);
	}

	private static final HashMap<String, String> SUGGESTION_PROJECTION_MAP;
	static {
		SUGGESTION_PROJECTION_MAP = new HashMap<String, String>();
		SUGGESTION_PROJECTION_MAP.put(SearchManager.SUGGEST_COLUMN_TEXT_1, EquipementTable.TABLE_NAME + "."
				+ EquipementTable.NOM + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_1);
		SUGGESTION_PROJECTION_MAP.put(SearchManager.SUGGEST_COLUMN_TEXT_2, TypeEquipementTable.TABLE_NAME + "."
				+ TypeEquipementTable.NOM + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_2);
		SUGGESTION_PROJECTION_MAP.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, EquipementTable.TABLE_NAME + "."
				+ EquipementTable._ID + " AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
		SUGGESTION_PROJECTION_MAP.put(SearchManager.SUGGEST_COLUMN_QUERY, EquipementTable.TABLE_NAME + "."
				+ EquipementTable._ID + " AS " + SearchManager.SUGGEST_COLUMN_QUERY);
		SUGGESTION_PROJECTION_MAP.put(SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA, EquipementTable.TABLE_NAME + "."
				+ EquipementTable.ID_TYPE + " AS " + SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA);
		SUGGESTION_PROJECTION_MAP.put(EquipementTable.TABLE_NAME + "." + EquipementTable._ID,
				EquipementTable.TABLE_NAME + "." + EquipementTable._ID);
	}

	@Override
	public Cursor query(final Uri uri, String[] projection, final String selection, final String[] selectionArgs,
			String sortOrder) {
		final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(EquipementTable.TABLE_NAME);

		final int uriType = URI_MATCHER.match(uri);
		switch (uriType) {

		case SEARCH:
			queryBuilder.setTables(EquipementTable.TABLE_NAME + EquipementTable.TABLE_JOIN_TYPE_EQUIPEMENT);
			queryBuilder.setProjectionMap(SUGGESTION_PROJECTION_MAP);
			queryBuilder.appendWhere(EquipementTable.TABLE_NAME + "." + EquipementTable.NORMALIZED_NOM + " LIKE ");
			queryBuilder.appendWhereEscapeString('%' + uri.getLastPathSegment() + '%');

			sortOrder = TypeEquipementTable.TABLE_NAME + "." + TypeEquipementTable._ID + ","
					+ EquipementTable.TABLE_NAME + "." + EquipementTable.NORMALIZED_NOM;
			break;
		case EQUIPEMENT_ID:
			queryBuilder.appendWhere(EquipementTable._ID + "=" + uri.getLastPathSegment());
			break;

		case EQUIPEMENTS_TYPE:
			queryBuilder.appendWhere(EquipementTable.ID_TYPE + "=" + uri.getLastPathSegment());
			sortOrder = EquipementTable.TABLE_NAME + "." + EquipementTable.NORMALIZED_NOM;
			break;

		case EQUIPEMENTS_NAME:
			queryBuilder.setTables(EquipementTable.TABLE_NAME + EquipementTable.TABLE_JOIN_TYPE_EQUIPEMENT);
			queryBuilder.appendWhere(EquipementTable.TABLE_NAME + "." + EquipementTable.NORMALIZED_NOM + " LIKE ");
			queryBuilder.appendWhereEscapeString('%' + removeDiacritics(uri.getLastPathSegment()) + '%');

			projection = EquipementTable.PROJECTION;
			sortOrder = TypeEquipementTable.TABLE_NAME + "." + TypeEquipementTable._ID + ","
					+ EquipementTable.TABLE_NAME + "." + EquipementTable.NORMALIZED_NOM;
			break;

		case EQUIPEMENTS_LOCATION:
			final String latitude = uri.getQueryParameter("latitude");
			final String longitude = uri.getQueryParameter("longitude");
			final String limit = uri.getQueryParameter("limit");
			sortOrder = String.format(LocationQuery.ORDER_BY, latitude, longitude);
			if (limit != null) {
				sortOrder += String.format(LocationQuery.LIMIT, limit);
			}

			queryBuilder.appendWhere(LocationQuery.WHERE);
			break;

		case EQUIPEMENTS:
			sortOrder = EquipementTable.TABLE_NAME + "." + EquipementTable.ID_TYPE + "," + EquipementTable.TABLE_NAME
					+ "." + EquipementTable.NORMALIZED_NOM;
			break;
		default:
			throw new IllegalArgumentException("Unknown URI : " + uri);
		}

		final Cursor cursor = queryBuilder.query(getReadableDatabase(), projection, selection, selectionArgs, null,
				null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public Uri insert(final Uri uri, final ContentValues initialValues) {
		ContentValues values;

		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}

		if (URI_MATCHER.match(uri) != EQUIPEMENTS) {
			throw new IllegalArgumentException("Unknown URI " + uri + " (" + URI_MATCHER.match(uri) + ")");
		}

		final SQLiteDatabase db = getWritableDatabase();
		final long rowId = db.insert(EquipementTable.TABLE_NAME, null, values);
		if (rowId > 0) {
			final Uri insertUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(uri, null);
			return insertUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public int delete(final Uri uri, final String selection, final String[] selectionArgs) {
		final SQLiteDatabase db = getWritableDatabase();

		int count;
		switch (URI_MATCHER.match(uri)) {
		case EQUIPEMENTS_TYPE:
			final String segment = uri.getPathSegments().get(1);
			count = db.delete(EquipementTable.TABLE_NAME,
					EquipementTable.ID_TYPE + "=" + segment
							+ (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
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
	public int update(final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs) {
		return 0;
	}

	private String removeDiacritics(final String s) {
		String result = Normalizer.normalize(s, Normalizer.Form.NFD);
		result = result.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
		return result;
	}

	/**
	 * Composants de la requête de sélection des equipements à proximité.
	 */
	private static interface LocationQuery {
		public static final String WHERE = EquipementTable.LATITUDE + "  IS NOT NULL AND " + EquipementTable.LONGITUDE
				+ " IS NOT NULL ";
		public static final String ORDER_BY = " ( abs(" + EquipementTable.LATITUDE + " - (%s)) + abs("
				+ EquipementTable.LONGITUDE + " - (%s) )) ";
		public static final String LIMIT = " LIMIT %s ";
	}

}
