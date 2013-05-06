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
package net.naonedbus.manager.impl;

import net.naonedbus.bean.TypeLigne;
import net.naonedbus.manager.SQLiteManager;
import net.naonedbus.provider.impl.TypeLigneProvider;
import net.naonedbus.provider.table.TypeLigneTable;
import android.content.ContentValues;
import android.database.Cursor;

public class TypeLigneManager extends SQLiteManager<TypeLigne> {

	private static TypeLigneManager instance;

	public static TypeLigneManager getInstance() {
		if (instance == null) {
			instance = new TypeLigneManager();
		}

		return instance;
	}

	private TypeLigneManager() {
		super(TypeLigneProvider.CONTENT_URI);
	}

	@Override
	public TypeLigne getSingleFromCursor(final Cursor c) {
		final TypeLigne item = new TypeLigne();
		item._id = c.getInt(c.getColumnIndex(TypeLigneTable._ID));
		item.nom = c.getString(c.getColumnIndex(TypeLigneTable.NOM));
		return item;
	}

	@Override
	protected ContentValues getContentValues(final TypeLigne item) {
		return null;
	}

}
