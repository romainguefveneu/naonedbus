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

import net.naonedbus.bean.StopBookmark;
import net.naonedbus.bean.Stop;
import net.naonedbus.manager.SQLiteManager;
import net.naonedbus.provider.impl.StopProvider;
import net.naonedbus.provider.table.EquipmentTable;
import net.naonedbus.provider.table.RouteTable;
import net.naonedbus.provider.table.StopBookmarkTable;
import net.naonedbus.provider.table.StopTable;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class StopManager extends SQLiteManager<Stop> {

	private static StopManager sInstance;

	public static synchronized StopManager getInstance() {
		if (sInstance == null) {
			sInstance = new StopManager();
		}
		return sInstance;
	}

	private final Stop.Builder mBuilder;
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

	private StopManager() {
		super(StopProvider.CONTENT_URI);
		mBuilder = new Stop.Builder();
	}

	/***
	 * Récéruper les arrêt selon un nom.
	 * 
	 * @param contentResolver
	 * @param query
	 * @return
	 */
	public Cursor findByName(final ContentResolver contentResolver, final String query) {
		return getCursor(contentResolver, EquipmentTable.NORMALIZED_NAME + " LIKE %?%", new String[] { query });
	}

	/**
	 * Récupérer une liste contenant les arrets de la ligne et du sens sépcifiée
	 * 
	 * @param contentResolver
	 * @param codeLigne
	 */
	public List<Stop> getAll(final ContentResolver contentResolver, final String codeLigne, final String codeSens) {
		final Cursor c = getCursor(contentResolver, codeLigne, codeSens);
		return getFromCursor(c);
	}

	@Override
	public Stop getSingle(final ContentResolver contentResolver, final int id) {
		final Cursor c = getCursor(contentResolver, StopTable.TABLE_NAME + "._id = ?",
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
	public Stop getSingle(final ContentResolver contentResolver, final String codeLigne, final String codeSens,
			final String nomArret) {
		final Cursor c = getCursor(contentResolver, StopTable.ROUTE_CODE + "=? AND " + StopTable.DIRECTION_CODE
				+ "=? AND " + EquipmentTable.NORMALIZED_NAME + "=?", new String[] { codeLigne, codeSens, nomArret });
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
	public List<Stop> getArretsFavoris(final ContentResolver contentResolver, final String codeLigne,
			final String codeSens) {
		final Cursor c = getCursor(contentResolver, StopTable.ROUTE_CODE + "=? AND " + StopTable.DIRECTION_CODE
				+ "=? AND EXISTS (SELECT 1 FROM " + StopBookmarkTable.TABLE_NAME + " WHERE "
				+ StopBookmarkTable.TABLE_NAME + "." + StopBookmarkTable._ID + "=" + StopTable.TABLE_NAME + "."
				+ StopTable._ID + ")", new String[] { codeLigne, codeSens });
		return getFromCursor(c);
	}

	public Cursor getCursor(final ContentResolver contentResolver, final String codeLigne, final String codeSens) {
		final Uri.Builder builder = StopProvider.CONTENT_URI.buildUpon();
		builder.path(StopProvider.ARRET_CODESENS_CODELIGNE_URI_PATH_QUERY);
		builder.appendQueryParameter("codeLigne", codeLigne);
		builder.appendQueryParameter("codeSens", codeSens);
		return contentResolver.query(builder.build(), null, null, null, null);
	}

	@Override
	public void onIndexCursor(final Cursor c) {
		mColId = c.getColumnIndex(StopTable._ID);
		mColCodeArret = c.getColumnIndex(StopTable.STOP_CODE);
		mColLettre = c.getColumnIndex(RouteTable.LETTER);
		mColCodeEquipement = c.getColumnIndex(EquipmentTable.EQUIPMENT_CODE);
		mColCodeLigne = c.getColumnIndex(StopTable.ROUTE_CODE);
		mColCodeSens = c.getColumnIndex(StopTable.DIRECTION_CODE);
		mColNomArret = c.getColumnIndex(EquipmentTable.EQUIPMENT_NAME);
		mColNormalizedNom = c.getColumnIndex(EquipmentTable.NORMALIZED_NAME);
		mColLatitude = c.getColumnIndex(EquipmentTable.LATITUDE);
		mColLongitude = c.getColumnIndex(EquipmentTable.LONGITUDE);
		mColIdStation = c.getColumnIndex(StopTable.EQUIPMENT_ID);
		mColOrdre = c.getColumnIndex(StopTable.STOP_ORDER);
	}

	@Override
	public Stop getSingleFromCursor(final Cursor c) {
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

	public Stop getSingleFromCursorWrapper(final CursorWrapper c) {
		mBuilder.setId(c.getInt(c.getColumnIndex(StopTable._ID)));
		mBuilder.setCodeArret(c.getString(c.getColumnIndex(StopTable.STOP_CODE)));
		mBuilder.setLettre(c.getString(c.getColumnIndex(RouteTable.LETTER)));
		mBuilder.setCodeEquipement(c.getString(c.getColumnIndex(EquipmentTable.EQUIPMENT_CODE)));
		mBuilder.setCodeLigne(c.getString(c.getColumnIndex(StopTable.ROUTE_CODE)));
		mBuilder.setCodeSens(c.getString(c.getColumnIndex(StopTable.DIRECTION_CODE)));
		mBuilder.setNomArret(c.getString(c.getColumnIndex(EquipmentTable.EQUIPMENT_NAME)));
		mBuilder.setLatitude(c.getFloat(c.getColumnIndex(EquipmentTable.LATITUDE)));
		mBuilder.setLongitude(c.getFloat(c.getColumnIndex(EquipmentTable.LONGITUDE)));
		mBuilder.setIdStation(c.getInt(c.getColumnIndex(StopTable.EQUIPMENT_ID)));
		mBuilder.setOrdre(c.getInt(c.getColumnIndex(StopTable.STOP_ORDER)));
		return mBuilder.build();
	}

	public Integer getIdByFavori(final ContentResolver contentResolver, final StopBookmark favori) {
		Integer id = null;

		final Uri.Builder builder = StopProvider.CONTENT_URI.buildUpon();
		builder.path(StopProvider.ARRET_CODEARRET_CODESENS_CODELIGNE_URI_PATH_QUERY);
		builder.appendQueryParameter("codeArret", favori.getCodeArret());
		builder.appendQueryParameter("codeSens", favori.getCodeSens());
		builder.appendQueryParameter("codeLigne", favori.getCodeLigne());

		final Cursor c = contentResolver.query(builder.build(), null, null, null, null);
		final Stop arretItem = getFirstFromCursor(c);
		if (arretItem != null) {
			id = arretItem.getId();
		}

		return id;
	}

	public Integer getIdByFavori(final SQLiteDatabase db, final StopBookmark favori) {
		Integer id = null;

		final Cursor c = db.query(StopTable.TABLE_NAME, new String[] { StopTable._ID }, StopTable.STOP_CODE + "=? AND "
				+ StopTable.DIRECTION_CODE + "=? AND " + StopTable.ROUTE_CODE + "=?",
				new String[] { favori.getCodeArret(), favori.getCodeSens(), favori.getCodeLigne() }, null, null, null);

		if (c.getCount() > 0) {
			c.moveToFirst();
			id = c.getInt(c.getColumnIndex(StopTable._ID));
		}
		c.close();

		return id;
	}

	public Stop getSingle(final ContentResolver contentResolver, final String code) {
		final Cursor c = getCursor(contentResolver, StopTable.STOP_CODE + " = ?", new String[] { code });
		return getFirstFromCursor(c);
	}

	@Override
	public ContentValues getContentValues(final Stop item) {
		final ContentValues values = new ContentValues();
		values.put(StopTable._ID, item.getId());
		values.put(StopTable.ROUTE_CODE, item.getCodeLigne());
		values.put(StopTable.DIRECTION_CODE, item.getCodeSens());
		values.put(StopTable.STOP_CODE, item.getCodeArret());
		values.put(StopTable.EQUIPMENT_ID, item.getIdStation());
		values.put(StopTable.STOP_ORDER, item.getOrdre());
		values.put(EquipmentTable.EQUIPMENT_NAME, item.getNomArret());
		values.put(EquipmentTable.NORMALIZED_NAME, item.getNormalizedNom());
		return values;
	}

}
