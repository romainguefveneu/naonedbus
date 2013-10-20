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
package net.naonedbus.provider.table;

import android.provider.BaseColumns;

public interface RouteTable extends BaseColumns {
	public static final String TABLE_NAME = "routes";

	public static final String ROUTE_CODE = "routeCode";
	public static final String LETTER = "letter";
	public static final String BACK_COLOR = "backColor";
	public static final String FRONT_COLOR = "frontColor";
	public static final String HEADSIGN_FROM = "headsignFrom";
	public static final String HEADSIGN_TO = "headsignTo";
	public static final String TYPE_ID = "typeId";
}
