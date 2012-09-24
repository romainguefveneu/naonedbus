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
package net.naonedbus.manager.impl;

import net.naonedbus.bean.TypeEquipement;
import net.naonedbus.manager.SQLiteManager;
import net.naonedbus.provider.impl.TypeEquipementProvider;
import net.naonedbus.provider.table.TypeEquipementTable;
import android.database.Cursor;

/**
 * @author romain
 * 
 */
public class TypeEquipementManager extends SQLiteManager<TypeEquipement> {

	private static TypeEquipementManager instance;

	public static TypeEquipementManager getInstance() {
		if (instance == null) {
			instance = new TypeEquipementManager();
		}

		return instance;
	}

	private TypeEquipementManager() {
		super(TypeEquipementProvider.CONTENT_URI);
	}

	@Override
	protected TypeEquipement getSingleFromCursor(Cursor c) {
		TypeEquipement item = new TypeEquipement();
		item._id = c.getInt(c.getColumnIndex(TypeEquipementTable._ID));
		item.nom = c.getString(c.getColumnIndex(TypeEquipementTable.NOM));
		return item;
	}

}
