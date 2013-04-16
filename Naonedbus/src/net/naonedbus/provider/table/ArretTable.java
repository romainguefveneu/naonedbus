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

public interface ArretTable extends BaseColumns {
	public static final String TABLE_NAME = "arrets";

	public static final String CODE = "code";
	public static final String CODE_SENS = "codeSens";
	public static final String CODE_LIGNE = "codeLigne";
	public static final String ID_STATION = "idStation";
	public static final String ORDRE = "ordre";

	//@formatter:off
	public static String TABLE_JOIN_STATIONS = 
			" LEFT JOIN " + EquipementTable.TABLE_NAME + 
				" ON idType = 0 AND idStation = " + EquipementTable.TABLE_NAME + "._id AND " + EquipementTable.CODE + " IS NOT NULL " + 
			" LEFT JOIN " + LigneTable.TABLE_NAME + 
				" ON " + ArretTable.TABLE_NAME + "." + ArretTable.CODE_LIGNE + " = " + LigneTable.TABLE_NAME + "." + LigneTable.CODE;
	//@formatter:on

	public static final String[] PROJECTION = new String[] { TABLE_NAME + "." + _ID + " as _id",
			TABLE_NAME + "." + CODE, TABLE_NAME + "." + CODE_SENS, TABLE_NAME + "." + CODE_LIGNE,
			TABLE_NAME + "." + ID_STATION, TABLE_NAME + "." + ORDRE, LigneTable.TABLE_NAME + "." + LigneTable.LETTRE,
			EquipementTable.TABLE_NAME + "." + EquipementTable.CODE,
			EquipementTable.TABLE_NAME + "." + EquipementTable.NOM,
			EquipementTable.TABLE_NAME + "." + EquipementTable.NORMALIZED_NOM,
			EquipementTable.TABLE_NAME + "." + EquipementTable.LATITUDE,
			EquipementTable.TABLE_NAME + "." + EquipementTable.LONGITUDE };
}
