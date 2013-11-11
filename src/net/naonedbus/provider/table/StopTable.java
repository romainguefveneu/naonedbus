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

public interface StopTable extends BaseColumns {
	public static final String TABLE_NAME = "stops";

	public static final String STOP_CODE = "stopCode";
	public static final String DIRECTION_CODE = "directionCode";
	public static final String ROUTE_CODE = "routeCode";
	public static final String EQUIPMENT_ID = "equipmentId";
	public static final String STOP_ORDER = "stopOrder";
	public static final String STEP_TYPE = "stepType";

	//@formatter:off
	public static String TABLE_JOIN_STATIONS = 
			" LEFT JOIN " + EquipmentTable.TABLE_NAME + 
				" ON " + EquipmentTable.TABLE_NAME + "." + EquipmentTable.TYPE_ID +" = 0 AND " + 
						 TABLE_NAME + "." + StopTable.EQUIPMENT_ID +  " = " + EquipmentTable.TABLE_NAME + "._id AND " + 
						 EquipmentTable.EQUIPMENT_CODE + " IS NOT NULL " + 
			" LEFT JOIN " + RouteTable.TABLE_NAME + 
				" ON " + StopTable.TABLE_NAME + "." + StopTable.ROUTE_CODE + " = " + RouteTable.TABLE_NAME + "." + RouteTable.ROUTE_CODE;

	public static final String[] PROJECTION = new String[] { 
			TABLE_NAME + "." + _ID + " as _id",
			TABLE_NAME + "." + STOP_CODE, 
			TABLE_NAME + "." + DIRECTION_CODE, 
			TABLE_NAME + "." + ROUTE_CODE,
			TABLE_NAME + "." + EQUIPMENT_ID, 
			TABLE_NAME + "." + STOP_ORDER, 
			TABLE_NAME + "." + STEP_TYPE, 
			RouteTable.TABLE_NAME + "." + RouteTable.LETTER,
			EquipmentTable.TABLE_NAME + "." + EquipmentTable.EQUIPMENT_CODE,
			EquipmentTable.TABLE_NAME + "." + EquipmentTable.EQUIPMENT_NAME,
			EquipmentTable.TABLE_NAME + "." + EquipmentTable.NORMALIZED_NAME,
			EquipmentTable.TABLE_NAME + "." + EquipmentTable.LATITUDE,
			EquipmentTable.TABLE_NAME + "." + EquipmentTable.LONGITUDE };
	//@formatter:on
}
