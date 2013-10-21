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

public interface StopBookmarkTable extends BaseColumns {
	public static final String TABLE_NAME = "stopBookmarks";

	public static final String STOP_CODE = "stopCode";
	public static final String DIRECTION_CODE = "directionCode";
	public static final String ROUTE_CODE = "routeCode";
	public static final String BOOKMARK_NAME = "bookmarkName";

	//@formatter:off
	public static final String JOIN = TABLE_NAME + " f " + " LEFT JOIN " + StopTable.TABLE_NAME
			+ " a ON f._id = a._id" + 
			" LEFT JOIN " + EquipmentTable.TABLE_NAME + " st ON st.idType = 0 AND st._id = a.idStation " +
			" LEFT JOIN " + RouteTable.TABLE_NAME+ " l ON l.code = f.routeCode" + 
			" LEFT JOIN " + DirectionTable.TABLE_NAME + " s ON s.routeCode = f.routeCode AND s.code = f.directionCode"; 

	public static final String[] PROJECTION = new String[] { 
		StopBookmarkTable._ID, 
		StopBookmarkTable.BOOKMARK_NAME, 
		StopBookmarkTable.STOP_CODE,
		StopBookmarkTable.DIRECTION_CODE,
		StopBookmarkTable.ROUTE_CODE };

	public static final String[] FULL_PROJECTION = new String[] { 
			StopBookmarkTable.TABLE_NAME + "." + StopBookmarkTable._ID,
			StopBookmarkTable.TABLE_NAME + "." + StopBookmarkTable.ROUTE_CODE, 
			StopBookmarkTable.TABLE_NAME + "." + StopBookmarkTable.DIRECTION_CODE, 
			StopBookmarkTable.TABLE_NAME + "." + StopBookmarkTable.STOP_CODE,
			StopBookmarkTable.TABLE_NAME + "." + StopBookmarkTable.BOOKMARK_NAME, 
			EquipmentTable.TABLE_NAME + "." + EquipmentTable.EQUIPMENT_NAME, 
			DirectionTable.TABLE_NAME + "." + DirectionTable.DIRECTION_NAME,
			RouteTable.TABLE_NAME + "." + RouteTable.BACK_COLOR, 
			RouteTable.TABLE_NAME +"." + RouteTable.FRONT_COLOR, 
			RouteTable.TABLE_NAME +"." + RouteTable.LETTER
			};

	public static final String FULL_ORDER =  
			RouteTable.TYPE_ID + ", CAST( " +StopBookmarkTable.TABLE_NAME +"." + StopBookmarkTable.ROUTE_CODE + " as numeric)," +
			StopBookmarkTable.BOOKMARK_NAME + ", " + EquipmentTable.EQUIPMENT_NAME;
	//@formatter:on
}
