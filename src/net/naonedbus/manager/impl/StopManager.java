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

import net.naonedbus.bean.Stop;
import net.naonedbus.bean.StopBookmark;
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
	private int mColRouteCode;
	private int mColLetter;
	private int mColDirectionCode;
	private int mColStopCode;
	private int mColEquipmentCode;
	private int mColNormalizedName;
	private int mColLatitude;
	private int mColLongitude;
	private int mColEquipmentId;
	private int mColOrder;
	private int mColStepType;
	private int mColName;

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

	public List<Stop> getAll(final ContentResolver contentResolver, final String routeCode, final String directionCode) {
		final Cursor c = getCursor(contentResolver, routeCode, directionCode);
		return getFromCursor(c);
	}

	public List<Stop> getAll(final ContentResolver contentResolver, final String serviceId, final String routeCode,
			final String directionCode) {
		final Cursor c = getCursor(contentResolver, serviceId, routeCode, directionCode);
		return getFromCursor(c);
	}

	@Override
	public Stop getSingle(final ContentResolver contentResolver, final int id) {
		final Cursor c = getCursor(contentResolver, StopTable.TABLE_NAME + "._id = ?",
				new String[] { String.valueOf(id) });
		return getFirstFromCursor(c);
	}

	/**
	 * Récupérer un arrêt selon son code route et direction et du nom de
	 * l'arrêt.
	 * 
	 * @param contentResolver
	 * @param routeCode
	 *            le code de la route
	 * @param directionCode
	 *            le code du direction
	 * @param stopName
	 *            le code de l'arrêt
	 * @return L'arrêt cherche, ou {@code null} si non trouvé.
	 */
	public Stop getSingle(final ContentResolver contentResolver, final String routeCode, final String directionCode,
			final String stopName) {
		final Cursor c = getCursor(contentResolver, StopTable.TABLE_NAME + "." + StopTable.ROUTE_CODE + "=? AND "
				+ StopTable.TABLE_NAME + "." + StopTable.DIRECTION_CODE + "=? AND " + EquipmentTable.TABLE_NAME + "."
				+ EquipmentTable.NORMALIZED_NAME + "=?", new String[] { routeCode, directionCode, stopName });
		return getFirstFromCursor(c);
	}

	/**
	 * Récupérer les arrêts favoris selon son code route et direction.
	 * 
	 * @param contentResolver
	 * @param routeCode
	 *            le code de la route
	 * @param directionCode
	 *            le code du direction
	 * @return La liste des arrêts favoris de la route et du direction donné
	 */
	public List<Stop> getBookmarks(final ContentResolver contentResolver, final String routeCode,
			final String directionCode) {
		final Cursor c = getCursor(contentResolver, StopTable.TABLE_NAME + "." + StopTable.ROUTE_CODE + "=? AND "
				+ StopTable.DIRECTION_CODE + "=? AND EXISTS (SELECT 1 FROM " + StopBookmarkTable.TABLE_NAME + " WHERE "
				+ StopBookmarkTable.TABLE_NAME + "." + StopBookmarkTable._ID + "=" + StopTable.TABLE_NAME + "."
				+ StopTable._ID + ")", new String[] { routeCode, directionCode });
		return getFromCursor(c);
	}

	public Cursor getCursor(final ContentResolver contentResolver, final String routeCode, final String directionCode) {
		return getCursor(contentResolver, null, routeCode, directionCode);
	}

	public Cursor getCursor(final ContentResolver contentResolver, final String serviceId, final String routeCode,
			final String directionCode) {
		final Uri.Builder builder = StopProvider.CONTENT_URI.buildUpon();
		builder.path(StopProvider.STOP_DIRECTION_ROUTE_URI_PATH_QUERY);
		if (serviceId != null) {
			builder.appendQueryParameter("serviceId", serviceId);
		}
		builder.appendQueryParameter("routeCode", routeCode);
		builder.appendQueryParameter("directionCode", directionCode);
		return contentResolver.query(builder.build(), null, null, null, null);
	}

	@Override
	public void onIndexCursor(final Cursor c) {
		mColId = c.getColumnIndex(StopTable._ID);
		mColStopCode = c.getColumnIndex(StopTable.STOP_CODE);
		mColLetter = c.getColumnIndex(RouteTable.LETTER);
		mColEquipmentCode = c.getColumnIndex(EquipmentTable.EQUIPMENT_CODE);
		mColRouteCode = c.getColumnIndex(StopTable.ROUTE_CODE);
		mColDirectionCode = c.getColumnIndex(StopTable.DIRECTION_CODE);
		mColName = c.getColumnIndex(EquipmentTable.EQUIPMENT_NAME);
		mColNormalizedName = c.getColumnIndex(EquipmentTable.NORMALIZED_NAME);
		mColLatitude = c.getColumnIndex(EquipmentTable.LATITUDE);
		mColLongitude = c.getColumnIndex(EquipmentTable.LONGITUDE);
		mColEquipmentId = c.getColumnIndex(StopTable.EQUIPMENT_ID);
		mColOrder = c.getColumnIndex(StopTable.STOP_ORDER);
		mColStepType = c.getColumnIndex(StopTable.STEP_TYPE);
	}

	@Override
	public Stop getSingleFromCursor(final Cursor c) {
		mBuilder.setId(c.getInt(mColId));
		mBuilder.setCodeArret(c.getString(mColStopCode));
		mBuilder.setLettre(c.getString(mColLetter));
		mBuilder.setCodeEquipement(c.getString(mColEquipmentCode));
		mBuilder.setCodeLigne(c.getString(mColRouteCode));
		mBuilder.setCodeSens(c.getString(mColDirectionCode));
		mBuilder.setNomArret(c.getString(mColName));
		mBuilder.setNormalizedNom(c.getString(mColNormalizedName));
		mBuilder.setLatitude(c.getFloat(mColLatitude));
		mBuilder.setLongitude(c.getFloat(mColLongitude));
		mBuilder.setIdStation(c.getInt(mColEquipmentId));
		mBuilder.setOrdre(c.getInt(mColOrder));
		mBuilder.setStepType(c.getLong(mColStepType));
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
		mBuilder.setStepType(c.getLong(c.getColumnIndex(StopTable.STEP_TYPE)));
		return mBuilder.build();
	}

	public Integer getIdByFavori(final ContentResolver contentResolver, final StopBookmark favori) {
		Integer id = null;

		final Uri.Builder builder = StopProvider.CONTENT_URI.buildUpon();
		builder.path(StopProvider.STOP_CODES_URI_PATH_QUERY);
		builder.appendQueryParameter("codeArret", favori.getCodeArret());
		builder.appendQueryParameter("directionCode", favori.getCodeSens());
		builder.appendQueryParameter("routeCode", favori.getCodeLigne());

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
		values.put(EquipmentTable.EQUIPMENT_NAME, item.getName());
		values.put(EquipmentTable.NORMALIZED_NAME, item.getNormalizedNom());
		return values;
	}

}
