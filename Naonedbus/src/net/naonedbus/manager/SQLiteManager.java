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
package net.naonedbus.manager;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public abstract class SQLiteManager<T> {

	private final Uri mContentUri;

	protected SQLiteManager(final Uri contentUri) {
		mContentUri = contentUri;
	}

	public Long add(final ContentResolver contentResolver, final T item) {
		final Uri uri = contentResolver.insert(mContentUri, getContentValues(item));
		return Long.valueOf(uri.getLastPathSegment());
	}

	public void remove(final ContentResolver contentResolver, final long id) {
		contentResolver.delete(mContentUri, "_id = ?", new String[] { String.valueOf(id) });
	}

	/**
	 * Récupérer un cursor contenant tous les éléments
	 * 
	 * @param contentResolver
	 * @return un cursor
	 */
	public Cursor getCursor(final ContentResolver contentResolver) {
		return contentResolver.query(mContentUri, null, null, null, null);
	}

	/**
	 * Récupérer un cursor de la sélection donnée
	 * 
	 * @param contentResolver
	 * @param selection
	 * @param selectionArg
	 * @return un cursor
	 */
	public Cursor getCursor(final ContentResolver contentResolver, final String selection, final String[] selectionArg) {
		return contentResolver.query(mContentUri, null, selection, selectionArg, null);
	}

	/**
	 * Récupérer tous les éléments
	 * 
	 * @param contentResolver
	 * @param selection
	 * @param selectionArg
	 * @return une liste des éléments
	 */
	public List<T> getAll(final ContentResolver contentResolver, final String selection, final String[] selectionArg) {
		final Cursor c = getCursor(contentResolver, selection, selectionArg);
		return getFromCursor(c);
	}

	/**
	 * Récupérer tous les éléments, sans filtre possible.
	 * 
	 * @param contentResolver
	 * @return la liste de tous les éléments de la table
	 */
	public List<T> getAll(final ContentResolver contentResolver) {
		final Cursor c = getCursor(contentResolver, null, null);
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
	public T getSingle(final ContentResolver contentResolver, final String selection, final String[] selectionArg) {
		final Cursor c = getCursor(contentResolver, selection, selectionArg);
		return getFirstFromCursor(c);
	}

	/**
	 * Récupérer un élément selon son id
	 * 
	 * @param contentResolver
	 * @param id
	 * @return un élément
	 */
	public T getSingle(final ContentResolver contentResolver, final int id) {
		final Cursor c = getCursor(contentResolver, "_id = ?", new String[] { String.valueOf(id) });
		return getFirstFromCursor(c);
	}

	/**
	 * Récupérer un élément selon son code.
	 * 
	 * @param contentResolver
	 * @param code
	 * @return un élément
	 */
	public T getSingle(final ContentResolver contentResolver, final String code) {
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
	protected List<T> getFromCursor(final Cursor c) {
		final List<T> items = new ArrayList<T>();
		if (c.getCount() > 0) {
			onIndexCursor(c);
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
	 *            le curseur
	 * @return le premier élément du curseur
	 */
	protected T getFirstFromCursor(final Cursor c) {
		T result = null;
		if (c.getCount() > 0) {
			onIndexCursor(c);
			c.moveToFirst();
			result = getSingleFromCursor(c);
		}
		c.close();
		return result;
	}

	/**
	 * Indexer la position des colonnes pour préparer le
	 * {@link #getSingleFromCursor(Cursor)}.
	 * 
	 * @param c
	 *            le curseur à indexer.
	 */
	public void onIndexCursor(final Cursor c) {
	}

	/**
	 * Transformer la position courante d'un curseur en élément.
	 * 
	 * @param c
	 *            le curseur
	 * @return un élément
	 */
	public abstract T getSingleFromCursor(Cursor c);

	protected abstract ContentValues getContentValues(final T item);

}
