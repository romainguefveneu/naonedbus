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
package net.naonedbus.manager;

import java.util.ArrayList;
import java.util.List;

import net.naonedbus.provider.table.ArretTable;
import net.naonedbus.provider.table.EquipementTable;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

public abstract class SQLiteManager<T> {

	private Uri contentUri;

	protected SQLiteManager(Uri contentUri) {
		this.contentUri = contentUri;
	}

	/**
	 * Récupérer un cursor contenant tous les éléments
	 * 
	 * @param contentResolver
	 * @return un cursor
	 */
	public Cursor getCursor(ContentResolver contentResolver) {
		return contentResolver.query(contentUri, null, null, null, null);
	}

	/**
	 * Récupérer un cursor de la sélection donnée
	 * 
	 * @param contentResolver
	 * @param selection
	 * @param selectionArg
	 * @return un cursor
	 */
	public Cursor getCursor(ContentResolver contentResolver, String selection, String[] selectionArg) {
		return contentResolver.query(contentUri, null, selection, selectionArg, null);
	}

	/**
	 * Récupérer tous les éléments
	 * 
	 * @param contentResolver
	 * @param selection
	 * @param selectionArg
	 * @return une liste des éléments
	 */
	public List<T> getAll(ContentResolver contentResolver, String selection, String[] selectionArg) {
		Cursor c = getCursor(contentResolver, selection, selectionArg);
		return getFromCursor(c);
	}

	/**
	 * Récupérer tous les éléments, sans filtre possible.
	 * 
	 * @param contentResolver
	 * @return la liste de tous les éléments de la table
	 */
	public List<T> getAll(ContentResolver contentResolver) {
		Cursor c = getCursor(contentResolver, null, null);
		return getFromCursor(c);
	}

	/**
	 * Récupérer un élément selon la sélection
	 * 
	 * @param contentResolver
	 * @param selection
	 * @param selectionArg
	 * @return un élément
	 */
	public T getSingle(ContentResolver contentResolver, String selection, String[] selectionArg) {
		Cursor c = getCursor(contentResolver, selection, selectionArg);
		return getFirstFromCursor(c);
	}

	/**
	 * Récupérer un élément selon son id
	 * 
	 * @param contentResolver
	 * @param id
	 * @return un élément
	 */
	public T getSingle(ContentResolver contentResolver, int id) {
		final Cursor c = getCursor(contentResolver, "_id = ?", new String[] { String.valueOf(id) });
		return getFirstFromCursor(c);
	}

	/**
	 * Récupérer un élément selon son id
	 * 
	 * @param contentResolver
	 * @param code
	 * @return un élément
	 */
	public T getSingle(ContentResolver contentResolver, String code) {
		final Cursor c = getCursor(contentResolver, "code = ?", new String[] { code });
		return getFirstFromCursor(c);
	}

	/**
	 * Transformer un curseur en liste d'éléments
	 * 
	 * @param c
	 *            un Curseur
	 * @return une liste d'éléments
	 */
	protected List<T> getFromCursor(Cursor c) {
		List<T> items = new ArrayList<T>();
		if (c.getCount() > 0) {
			c.moveToFirst();
			while (!c.isAfterLast()) {
				items.add(getSingleFromCursor(c));
				c.moveToNext();
			}
		}
		c.close();
		return items;
	}

	/**
	 * Retourner le premier élément d'un curseur
	 * 
	 * @param c
	 * @return le premier élément du curseur
	 */
	protected T getFirstFromCursor(Cursor c) {
		T result = null;
		if (c.getCount() > 0) {
			c.moveToFirst();
			result = getSingleFromCursor(c);
		}
		c.close();
		return result;
	}

	/**
	 * Transformer la position courante d'un curseur en élément
	 * 
	 * @param c
	 *            le curseur
	 * @return un élément
	 */
	public abstract T getSingleFromCursor(Cursor c);

}
