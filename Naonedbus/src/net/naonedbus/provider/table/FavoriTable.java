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

public interface FavoriTable extends BaseColumns {
	public static final String TABLE_NAME = "favoris";

	public static final String CODE_ARRET = "codeArret";
	public static final String CODE_SENS = "codeSens";
	public static final String CODE_LIGNE = "codeLigne";
	public static final String NOM = "nomFavori";

	//@formatter:off
	public static final String JOIN = TABLE_NAME + " f " + " LEFT JOIN " + ArretTable.TABLE_NAME
			+ " a ON f._id = a._id" + 
			" LEFT JOIN " + EquipementTable.TABLE_NAME + " st ON st.idType = 0 AND st._id = a.idStation " +
			" LEFT JOIN " + LigneTable.TABLE_NAME+ " l ON l.code = f.codeLigne" + 
			" LEFT JOIN " + SensTable.TABLE_NAME + " s ON s.codeLigne = f.codeLigne AND s.code = f.codeSens"; 

	public static final String[] PROJECTION = new String[] { 
		FavoriTable._ID, 
		FavoriTable.NOM, 
		FavoriTable.CODE_ARRET,
		FavoriTable.CODE_SENS,
		FavoriTable.CODE_LIGNE };

	public static final String[] FULL_PROJECTION = new String[] { 
			"f." + FavoriTable._ID,
			"f." + FavoriTable.CODE_LIGNE, 
			"f." + FavoriTable.CODE_SENS, 
			"f." + FavoriTable.CODE_ARRET,
			"f." + FavoriTable.NOM, 
			"st." + EquipementTable.NOM, 
			"s." + SensTable.NOM,
			"l." + LigneTable.COULEUR, 
			"l." + LigneTable.LETTRE
			};

	public static final String FULL_ORDER = "l." + LigneTable.TYPE + ", CAST( f." + FavoriTable.CODE_LIGNE
			+ " as numeric), f." + FavoriTable.NOM+ ", st." + EquipementTable.NOM;
	//@formatter:on
}