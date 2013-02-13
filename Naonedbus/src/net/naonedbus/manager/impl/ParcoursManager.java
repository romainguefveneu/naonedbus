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

import java.util.List;

import net.naonedbus.bean.Parcours;
import net.naonedbus.manager.SQLiteManager;
import net.naonedbus.provider.impl.ParcoursProvider;
import net.naonedbus.provider.impl.ParcoursProvider.ParcoursTable;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

/**
 * @author romain
 * 
 */
public class ParcoursManager extends SQLiteManager<Parcours> {

	private static ParcoursManager instance;

	public static synchronized ParcoursManager getInstance() {
		if (instance == null) {
			instance = new ParcoursManager();
		}
		return instance;
	}

	private ParcoursManager() {
		super(ParcoursProvider.CONTENT_URI);
	}

	public Cursor getParcours(ContentResolver contentResolver, String normalizedNom) {
		Uri.Builder builder = ParcoursProvider.CONTENT_URI.buildUpon();
		builder.appendQueryParameter("normalizedNom", normalizedNom);
		return contentResolver.query(builder.build(), null, null, null, null);
	}

	public List<Parcours> getParcoursList(ContentResolver contentResolver, String normalizedNom) {
		return getFromCursor(getParcours(contentResolver, normalizedNom));
	}

	public Parcours getSingleFromCursor(Cursor c) {
		Parcours item = new Parcours();
		item._id = c.getInt(c.getColumnIndex(ParcoursTable._ID));
		item.couleur = c.getInt(c.getColumnIndex(ParcoursTable.COULEUR));
		item.codeLigne = c.getString(c.getColumnIndex(ParcoursTable.CODE_LIGNE));
		item.lettre = c.getString(c.getColumnIndex(ParcoursTable.LETTRE));
		item.nomSens = c.getString(c.getColumnIndex(ParcoursTable.NOM_SENS));
		item.idLigne = c.getInt(c.getColumnIndex(ParcoursTable.ID_LIGNE));
		return item;
	}

}
