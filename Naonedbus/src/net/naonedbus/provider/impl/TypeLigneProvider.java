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
import net.naonedbus.provider.table.TypeLigneTable;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class TypeLigneProvider extends ReadOnlyContentProvider {

	public static final int TYPESLIGNES = 100;
	public static final int TYPE_ID = 110;

	private static final String AUTHORITY = "net.naonedbus.provider.TypeLigneProvider";
	private static final String TYPESLIGNES_BASE_PATH = "typeslignes";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TYPESLIGNES_BASE_PATH);

	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		URI_MATCHER.addURI(AUTHORITY, TYPESLIGNES_BASE_PATH, TYPESLIGNES);
		URI_MATCHER.addURI(AUTHORITY, TYPESLIGNES_BASE_PATH + "/#", TYPE_ID);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(TypeLigneTable.TABLE_NAME);

		if (sortOrder == null) {
			sortOrder = "_id";
		}

		int uriType = URI_MATCHER.match(uri);
		switch (uriType) {
		case TYPE_ID:
			queryBuilder.appendWhere(TypeLigneTable._ID + "=" + uri.getLastPathSegment());
			break;
		case TYPESLIGNES:
			// no filter
			break;
		default:
			throw new IllegalArgumentException("Unknown URI");
		}

		Cursor cursor = queryBuilder.query(getReadableDatabase(), projection, selection, selectionArgs, null, null,
				sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

}
