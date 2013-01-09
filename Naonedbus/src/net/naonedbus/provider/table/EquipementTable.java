/**
 *  Copyright (C) 2011 Romain Guefveneu
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

/**
 * @author romain
 * 
 */
public interface EquipementTable extends BaseColumns {
	public static final String TABLE_NAME = "equipements";

	public static final String ID_TYPE = "idType";
	public static final String ID_SOUS_TYPE = "idSousType";
	public static final String CODE = "codeEquipement";
	public static final String NOM = "nom";
	public static final String NORMALIZED_NOM = "normalizedNom";
	public static final String ADRESSE = "adresse";
	public static final String DETAILS = "details";
	public static final String TELEPHONE = "telephone";
	public static final String URL = "url";
	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";

	public static final String TABLE_JOIN_TYPE_EQUIPEMENT = " left join " + TypeEquipementTable.TABLE_NAME + " on "
			+ TypeEquipementTable.TABLE_NAME + "._id = " + TABLE_NAME + "." + ID_TYPE;

	public static final String[] PROJECTION = new String[] { TABLE_NAME + "." + _ID + " as _id",
			TABLE_NAME + "." + ID_TYPE, TABLE_NAME + "." + ID_SOUS_TYPE, TABLE_NAME + "." + NOM,
			TABLE_NAME + "." + NORMALIZED_NOM, TABLE_NAME + "." + ADRESSE, TABLE_NAME + "." + DETAILS,
			TABLE_NAME + "." + TELEPHONE, TABLE_NAME + "." + URL, TABLE_NAME + "." + LATITUDE,
			TABLE_NAME + "." + LONGITUDE };
}
