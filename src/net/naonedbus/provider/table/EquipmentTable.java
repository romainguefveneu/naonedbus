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

public interface EquipmentTable extends BaseColumns {
	public static final String TABLE_NAME = "equipments";

	public static final String TYPE_ID = "typeId";
	public static final String SUBTYPE_ID = "subtypeId";
	public static final String EQUIPMENT_CODE = "equipmentCode";
	public static final String EQUIPMENT_NAME = "equipmentName";
	public static final String NORMALIZED_NAME = "normalizedName";
	public static final String ADDRESS = "address";
	public static final String DETAILS = "details";
	public static final String PHONE = "phone";
	public static final String URL = "url";
	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";

	public static final String TABLE_JOIN_TYPE_EQUIPMENT = " left join " + EquipmentTypeTable.TABLE_NAME + " on "
			+ EquipmentTypeTable.TABLE_NAME + "._id = " + TABLE_NAME + "." + TYPE_ID;

	public static final String[] PROJECTION = new String[] { TABLE_NAME + "." + _ID + " as _id",
			TABLE_NAME + "." + TYPE_ID, TABLE_NAME + "." + SUBTYPE_ID, TABLE_NAME + "." + EQUIPMENT_NAME,
			TABLE_NAME + "." + NORMALIZED_NAME, TABLE_NAME + "." + ADDRESS, TABLE_NAME + "." + DETAILS,
			TABLE_NAME + "." + PHONE, TABLE_NAME + "." + URL, TABLE_NAME + "." + LATITUDE, TABLE_NAME + "." + LONGITUDE };
}
