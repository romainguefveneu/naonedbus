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

import java.util.List;

import net.naonedbus.bean.Arret;
import net.naonedbus.bean.Favori;
import net.naonedbus.manager.SQLiteManager;
import net.naonedbus.provider.impl.ArretProvider;
import net.naonedbus.provider.table.ArretTable;
import net.naonedbus.provider.table.EquipementTable;
import net.naonedbus.provider.table.FavoriTable;
import net.naonedbus.provider.table.LigneTable;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class ArretManager extends SQLiteManager<Arret> {

	private static ArretManager sInstance;

	public static synchronized ArretManager getInstance() {
		if (sInstance == null) {
			sInstance = new ArretManager();
		}
		return sInstance;
	}

	private final Arret.Builder mBuilder;
	private int mColId;
	private int mColCodeLigne;
	private int mColLettre;
	private int mColCodeSens;
	private int mColCodeArret;
	private int mColCodeEquipement;
	private int mColNormalizedNom;
	private int mColLatitude;
	private int mColLongitude;
	private int mColIdStation;
	private int mColOrdre;
	private int mColNomArret;

	private ArretManager() {
		super(ArretProvider.CONTENT_URI);
		mBuilder = new Arret.Builder();
	}

	/**
	 * Récupérer une liste contenant les arrets de la ligne et du sens sépcifiée
	 * 
	 * @param contentResolver
	 * @param codeLigne
	 */
	public List<Arret> getAll(final ContentResolver contentResolver, final String codeLigne, final String codeSens) {
		final Cursor c = getCursor(contentResolver, codeLigne, codeSens);
		return getFromCursor(c);
	}

	@Override
	public Arret getSingle(final ContentResolver contentResolver, final int id) {
		final Cursor c = getCursor(contentResolver, ArretTable.TABLE_NAME + "._id = ?",
				new String[] { String.valueOf(id) });
		return getFirstFromCursor(c);
	}

	/**
	 * Récupérer un arrêt selon son code ligne et sens et du nom de l'arrêt.
	 * 
	 * @param contentResolver
	 * @param codeLigne
	 *            le code de la ligne
	 * @param codeSens
	 *            le code du sens
	 * @param nomArret
	 *            le code de l'arrêt
	 * @return L'arrêt cherche, ou {@code null} si non trouvé.
	 */
	public Arret getSingle(final ContentResolver contentResolver, final String codeLigne, final String codeSens,
			final String nomArret) {
		final Cursor c = getCursor(contentResolver, ArretTable.CODE_LIGNE + "=? AND " + ArretTable.CODE_SENS
				+ "=? AND " + EquipementTable.NORMALIZED_NOM + "=?", new String[] { codeLigne, codeSens, nomArret });
		return getFirstFromCursor(c);
	}

	/**
	 * Récupérer les arrêts favoris selon son code ligne et sens.
	 * 
	 * @param contentResolver
	 * @param codeLigne
	 *            le code de la ligne
	 * @param codeSens
	 *            le code du sens
	 * @return La liste des arrêts favoris de la ligne et du sens donné
	 */
	public List<Arret> getArretsFavoris(final ContentResolver contentResolver, final String codeLigne,
			final String codeSens) {
		final Cursor c = getCursor(contentResolver, ArretTable.CODE_LIGNE + "=? AND " + ArretTable.CODE_SENS
				+ "=? AND EXISTS (SELECT 1 FROM " + FavoriTable.TABLE_NAME + " WHERE " + FavoriTable.TABLE_NAME + "."
				+ FavoriTable._ID + "=" + ArretTable.TABLE_NAME + "." + ArretTable._ID + ")", new String[] { codeLigne,
				codeSens });
		return getFromCursor(c);
	}

	public Cursor getCursor(final ContentResolver contentResolver, final String codeLigne, final String codeSens) {
		final Uri.Builder builder = ArretProvider.CONTENT_URI.buildUpon();
		builder.path(ArretProvider.ARRET_CODESENS_CODELIGNE_URI_PATH_QUERY);
		builder.appendQueryParameter("codeLigne", codeLigne);
		builder.appendQueryParameter("codeSens", codeSens);
		return contentResolver.query(builder.build(), null, null, null, null);
	}

	@Override
	public void onIndexCursor(final Cursor c) {
		mColId = c.getColumnIndex(ArretTable._ID);
		mColCodeArret = c.getColumnIndex(ArretTable.CODE);
		mColLettre = c.getColumnIndex(LigneTable.LETTRE);
		mColCodeEquipement = c.getColumnIndex(EquipementTable.CODE);
		mColCodeLigne = c.getColumnIndex(ArretTable.CODE_LIGNE);
		mColCodeSens = c.getColumnIndex(ArretTable.CODE_SENS);
		mColNomArret = c.getColumnIndex(EquipementTable.NOM);
		mColNormalizedNom = c.getColumnIndex(EquipementTable.NORMALIZED_NOM);
		mColLatitude = c.getColumnIndex(EquipementTable.LATITUDE);
		mColLongitude = c.getColumnIndex(EquipementTable.LONGITUDE);
		mColIdStation = c.getColumnIndex(ArretTable.ID_STATION);
		mColOrdre = c.getColumnIndex(ArretTable.ORDRE);
	}

	@Override
	public Arret getSingleFromCursor(final Cursor c) {
		mBuilder.setId(c.getInt(mColId));
		mBuilder.setCodeArret(c.getString(mColCodeArret));
		mBuilder.setLettre(c.getString(mColLettre));
		mBuilder.setCodeEquipement(c.getString(mColCodeEquipement));
		mBuilder.setCodeLigne(c.getString(mColCodeLigne));
		mBuilder.setCodeSens(c.getString(mColCodeSens));
		mBuilder.setNomArret(c.getString(mColNomArret));
		mBuilder.setNormalizedNom(c.getString(mColNormalizedNom));
		mBuilder.setLatitude(c.getFloat(mColLatitude));
		mBuilder.setLongitude(c.getFloat(mColLongitude));
		mBuilder.setIdStation(c.getInt(mColIdStation));
		mBuilder.setOrdre(c.getInt(mColOrdre));
		return mBuilder.build();
	}

	public Arret getSingleFromCursorWrapper(final CursorWrapper c) {
		mBuilder.setId(c.getInt(c.getColumnIndex(ArretTable._ID)));
		mBuilder.setCodeArret(c.getString(c.getColumnIndex(ArretTable.CODE)));
		mBuilder.setLettre(c.getString(c.getColumnIndex(LigneTable.LETTRE)));
		mBuilder.setCodeEquipement(c.getString(c.getColumnIndex(EquipementTable.CODE)));
		mBuilder.setCodeLigne(c.getString(c.getColumnIndex(ArretTable.CODE_LIGNE)));
		mBuilder.setCodeSens(c.getString(c.getColumnIndex(ArretTable.CODE_SENS)));
		mBuilder.setNomArret(c.getString(c.getColumnIndex(EquipementTable.NOM)));
		mBuilder.setLatitude(c.getFloat(c.getColumnIndex(EquipementTable.LATITUDE)));
		mBuilder.setLongitude(c.getFloat(c.getColumnIndex(EquipementTable.LONGITUDE)));
		mBuilder.setIdStation(c.getInt(c.getColumnIndex(ArretTable.ID_STATION)));
		mBuilder.setOrdre(c.getInt(c.getColumnIndex(ArretTable.ORDRE)));
		return mBuilder.build();
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
			id = arretItem.getId();
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
	public Arret getSingle(final ContentResolver contentResolver, final String code) {
		final Cursor c = getCursor(contentResolver, ArretTable.TABLE_NAME + ".code = ?", new String[] { code });
		return getFirstFromCursor(c);
	}

	@Override
	public ContentValues getContentValues(final Arret item) {
		final ContentValues values = new ContentValues();
		values.put(ArretTable._ID, item.getId());
		values.put(ArretTable.CODE_LIGNE, item.getCodeLigne());
		values.put(ArretTable.CODE_SENS, item.getCodeSens());
		values.put(ArretTable.CODE, item.getCodeArret());
		values.put(ArretTable.ID_STATION, item.getIdStation());
		values.put(ArretTable.ORDRE, item.getOrdre());
		values.put(EquipementTable.NOM, item.getNomArret());
		values.put(EquipementTable.NORMALIZED_NOM, item.getNormalizedNom());
		return values;
	}

}
