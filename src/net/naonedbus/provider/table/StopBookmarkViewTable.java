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

public interface StopBookmarkViewTable extends BaseColumns {
	public static final String TABLE_NAME = "stopBookmarksView";

	public static final String EQUIPMENT_ID = "equipmentId";
	public static final String BOOKMARKGROUP_ID = "stopBookmarkGroupId";

	public static final String ROUTE_CODE = "routeCode";
	public static final String DIRECTION_CODE = "directionCode";
	public static final String STOP_CODE = "stopCode";
	public static final String EQUIPMENT_CODE = "equipmentCode";

	public static final String BOOKMARK_NAME = "bookmarkName";
	public static final String EQUIPMENT_NAME = "equipmentName";
	public static final String NORMALIZED_NAME = "normalizedName";
	public static final String DIRECTION_NAME = "directionName";
	public static final String GROUP_NAME = "groupName";

	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";
	public static final String ROUTE_BACK_COLOR = "backColor";
	public static final String ROUTE_FRONT_COLOR = "frontColor";
	public static final String ROUTE_LETTER = "letter";
	public static final String ROUTE_TYPE = "typeId";

	public static final String GROUP_ORDER = "groupOrder";

	public static final String NEXT_SCHEDULE = "nextSchedule";

	//@formatter:off
	public static final String ORDER = 
			GROUP_ORDER + "," + 
			ROUTE_TYPE + 
			",CAST(" + ROUTE_CODE + " as numeric)," + 
			BOOKMARK_NAME	+ "," +
			EQUIPMENT_NAME;
	//@formatter:on

	public static final String WHERE = BOOKMARKGROUP_ID + " IN (%s) OR NOT EXISTS (SELECT 1 FROM "
			+ StopBookmarkGroupLinkTable.TABLE_NAME + " WHERE " + StopBookmarkGroupLinkTable.STOP_BOOKMARK_ID + " = " + TABLE_NAME + "."
			+ _ID + ")";

}
