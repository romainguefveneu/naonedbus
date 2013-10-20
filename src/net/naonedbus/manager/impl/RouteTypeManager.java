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

import net.naonedbus.bean.RouteType;
import net.naonedbus.manager.SQLiteManager;
import net.naonedbus.provider.impl.RouteTypeProvider;
import net.naonedbus.provider.table.RouteTypeTable;
import android.content.ContentValues;
import android.database.Cursor;

public class RouteTypeManager extends SQLiteManager<RouteType> {

	private static RouteTypeManager instance;

	public static RouteTypeManager getInstance() {
		if (instance == null) {
			instance = new RouteTypeManager();
		}

		return instance;
	}

	private RouteTypeManager() {
		super(RouteTypeProvider.CONTENT_URI);
	}

	@Override
	public RouteType getSingleFromCursor(final Cursor c) {
		final RouteType item = new RouteType();
		item.setId(c.getInt(c.getColumnIndex(RouteTypeTable._ID)));
		item.setTypeName(c.getString(c.getColumnIndex(RouteTypeTable.TYPE_NAME)));
		return item;
	}

	@Override
	protected ContentValues getContentValues(final RouteType item) {
		return null;
	}

}
