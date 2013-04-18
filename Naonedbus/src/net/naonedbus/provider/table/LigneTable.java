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

public interface LigneTable extends BaseColumns {
	public static final String TABLE_NAME = "lignes";

	public static final String CODE = "code";
	public static final String LETTRE = "lettre";
	public static final String COULEUR = "couleur";
	public static final String DEPUIS = "depuis";
	public static final String VERS = "vers";
	public static final String TYPE = "type";
}
