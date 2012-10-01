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

import net.naonedbus.bean.Arret;
import net.naonedbus.bean.Favori;
import net.naonedbus.manager.SQLiteManager;
import net.naonedbus.provider.impl.ArretProvider;
import net.naonedbus.provider.table.ArretTable;
import net.naonedbus.provider.table.EquipementTable;
import net.naonedbus.provider.table.LigneTable;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class ArretManager extends SQLiteManager<Arret> {

	private static ArretManager instance;

	public static synchronized ArretManager getInstance() {
		if (instance == null) {
			instance = new ArretManager();
		}
		return instance;
	}

	private ArretManager() {
		super(ArretProvider.CONTENT_URI);
	}

	/**
	 * Récupérer une liste contenant les arrets de la ligne et du sens sépcifiée
	 * 
	 * @param contentResolver
	 * @param codeLigne
	 */
	public List<Arret> getAll(ContentResolver contentResolver, String codeLigne, String codeSens) {
		Cursor c = getCursor(contentResolver, codeLigne, codeSens);
		return getFromCursor(c);
	}

	@Override
	public Arret getSingle(ContentResolver contentResolver, int id) {
		Cursor c = getCursor(contentResolver, ArretTable.TABLE_NAME + "._id = ?", new String[] { String.valueOf(id) });
		return getFirstFromCursor(c);
	}

	public Cursor getCursor(ContentResolver contentResolver, String codeLigne, String codeSens) {
		final Uri.Builder builder = ArretProvider.CONTENT_URI.buildUpon();
		builder.path(ArretProvider.ARRET_CODESENS_CODELIGNE_URI_PATH_QUERY);
		builder.appendQueryParameter("codeLigne", codeLigne);
		builder.appendQueryParameter("codeSens", codeSens);
		return contentResolver.query(builder.build(), null, null, null, null);
	}

	public Arret getSingleFromCursor(Cursor c) {
		Arret item = new Arret();
		item._id = c.getInt(c.getColumnIndex(ArretTable._ID));
		item.code = c.getString(c.getColumnIndex(ArretTable.CODE));
		item.lettre = c.getString(c.getColumnIndex(LigneTable.LETTRE));
		item.codeEquipement = c.getString(c.getColumnIndex(EquipementTable.CODE));
		item.codeLigne = c.getString(c.getColumnIndex(ArretTable.CODE_LIGNE));
		item.codeSens = c.getString(c.getColumnIndex(ArretTable.CODE_SENS));
		item.text = c.getString(c.getColumnIndex(EquipementTable.NOM));
		item.normalizedNom = c.getString(c.getColumnIndex(EquipementTable.NORMALIZED_NOM));
		item.latitude = c.getFloat(c.getColumnIndex(EquipementTable.LATITUDE));
		item.longitude = c.getFloat(c.getColumnIndex(EquipementTable.LONGITUDE));
		item.idStation = c.getInt(c.getColumnIndex(ArretTable.ID_STATION));
		return item;
	}

	public Arret getSingleFromCursorWrapper(CursorWrapper c) {
		Arret item = new Arret();
		item._id = c.getInt(c.getColumnIndex(ArretTable._ID));
		item.code = c.getString(c.getColumnIndex(ArretTable.CODE));
		item.lettre = c.getString(c.getColumnIndex(LigneTable.LETTRE));
		item.codeEquipement = c.getString(c.getColumnIndex(EquipementTable.CODE));
		item.codeLigne = c.getString(c.getColumnIndex(ArretTable.CODE_LIGNE));
		item.codeSens = c.getString(c.getColumnIndex(ArretTable.CODE_SENS));
		item.text = c.getString(c.getColumnIndex(EquipementTable.NOM));
		item.latitude = c.getFloat(c.getColumnIndex(EquipementTable.LATITUDE));
		item.longitude = c.getFloat(c.getColumnIndex(EquipementTable.LONGITUDE));
		item.idStation = c.getInt(c.getColumnIndex(ArretTable.ID_STATION));
		return item;
	}

	public Integer getIdByFavori(final ContentResolver contentResolver, final Favori favori) {
		Integer id = null;

		final Uri.Builder builder = ArretProvider.CONTENT_URI.buildUpon();
		builder.path(ArretProvider.ARRET_CODEARRET_CODESENS_CODELIGNE_URI_PATH_QUERY);
		builder.appendQueryParameter("codeArret", favori.getCodeArret());
		builder.appendQueryParameter("codeSens", favori.getCodeSens());
		builder.appendQueryParameter("codeLigne", favori.getCodeLigne());

		final Cursor c = contentResolver.query(builder.build(), null, null, null, null);
		final Arret arretItem = getFirstFromCursor(c);
		if (arretItem != null) {
			id = arretItem._id;
		}

		return id;
	}

	public Integer getIdByFavori(final SQLiteDatabase db, final Favori favori) {
		Integer id = null;

		final Cursor c = db.query(ArretTable.TABLE_NAME, new String[] { ArretTable._ID }, ArretTable.CODE + "=? AND "
				+ ArretTable.CODE_SENS + "=? AND " + ArretTable.CODE_LIGNE + "=?", new String[] {
				favori.getCodeArret(), favori.getCodeSens(), favori.getCodeLigne() }, null, null, null);

		if (c.getCount() > 0) {
			c.moveToFirst();
			id = c.getInt(c.getColumnIndex(ArretTable._ID));
		}
		c.close();

		return id;
	}

	@Override
	public Arret getSingle(ContentResolver contentResolver, String code) {
		Cursor c = getCursor(contentResolver, ArretTable.TABLE_NAME + ".code = ?", new String[] { code });
		return getFirstFromCursor(c);
	}

	public ContentValues getContentValues(Arret item) {
		ContentValues values = new ContentValues();
		values.put(ArretTable._ID, item._id);
		values.put(ArretTable.CODE_LIGNE, item.codeLigne);
		values.put(ArretTable.CODE_SENS, item.codeSens);
		values.put(ArretTable.CODE, item.code);
		values.put(EquipementTable.NOM, item.text);
		values.put(EquipementTable.NORMALIZED_NOM, item.normalizedNom);

		return values;
	}

}
