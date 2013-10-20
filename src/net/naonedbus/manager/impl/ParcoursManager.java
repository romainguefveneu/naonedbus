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
package net.naonedbus.manager.impl;

import java.util.List;

import net.naonedbus.bean.StopPath;
import net.naonedbus.manager.SQLiteManager;
import net.naonedbus.provider.impl.StopPathProvider;
import net.naonedbus.provider.table.StopDirectionTable;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class ParcoursManager extends SQLiteManager<StopPath> {

	private static ParcoursManager instance;

	public static synchronized ParcoursManager getInstance() {
		if (instance == null) {
			instance = new ParcoursManager();
		}
		return instance;
	}

	private ParcoursManager() {
		super(StopPathProvider.CONTENT_URI);
	}

	public Cursor getParcours(final ContentResolver contentResolver, final String normalizedNom) {
		final Uri.Builder builder = StopPathProvider.CONTENT_URI.buildUpon();
		builder.appendQueryParameter("normalizedNom", normalizedNom);
		return contentResolver.query(builder.build(), null, null, null, null);
	}

	public List<StopPath> getParcoursList(final ContentResolver contentResolver, final String normalizedNom) {
		return getFromCursor(getParcours(contentResolver, normalizedNom));
	}

	@Override
	public StopPath getSingleFromCursor(final Cursor c) {
		final StopPath item = new StopPath();
		item.setId(c.getInt(c.getColumnIndex(StopDirectionTable.STOP_ID)));
		item.setBackColor(c.getInt(c.getColumnIndex(StopDirectionTable.ROUTE_BACK_COLOR)));
		item.setFrontColor(c.getInt(c.getColumnIndex(StopDirectionTable.ROUTE_FRONT_COLOR)));
		item.setRouteCode(c.getString(c.getColumnIndex(StopDirectionTable.ROUTE_CODE)));
		item.setRouteLetter(c.getString(c.getColumnIndex(StopDirectionTable.ROUTE_LETTER)));
		item.setDirectionName(c.getString(c.getColumnIndex(StopDirectionTable.DIRECTION_NAME)));
		item.setRouteId(c.getInt(c.getColumnIndex(StopDirectionTable.ROUTE_ID)));
		return item;
	}

	@Override
	protected ContentValues getContentValues(final StopPath item) {
		return null;
	}

}
