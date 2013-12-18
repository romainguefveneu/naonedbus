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
import net.naonedbus.manager.SQLiteManager;
import net.naonedbus.provider.impl.StopViewProvider;
import net.naonedbus.provider.table.StopTable;
import net.naonedbus.provider.table.StopsViewTable;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;

public class StopViewManager extends SQLiteManager<Stop> {

	private static StopViewManager sInstance;

	public static synchronized StopViewManager getInstance() {
		if (sInstance == null) {
			sInstance = new StopViewManager();
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

	private StopViewManager() {
		super(StopViewProvider.CONTENT_URI);
		mBuilder = new Stop.Builder();
	}

	public Cursor findByName(final ContentResolver contentResolver, final String query) {
		return getCursor(contentResolver, StopsViewTable.NORMALIZED_NAME + " LIKE %?%", new String[] { query });
	}

	public List<Stop> getAll(final ContentResolver contentResolver, final String serviceId, final String routeCode,
			final String directionCode) {
		final Cursor c = getCursor(contentResolver, serviceId, routeCode, directionCode);
		return getFromCursor(c);
	}

	@Override
	public Stop getSingle(final ContentResolver contentResolver, final int id) {
		final Cursor c = getCursor(contentResolver, StopsViewTable.TABLE_NAME + "._id = ?",
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
		final Cursor c = getCursor(contentResolver, StopsViewTable.TABLE_NAME + "." + StopsViewTable.ROUTE_CODE
				+ "=? AND " + StopsViewTable.TABLE_NAME + "." + StopsViewTable.DIRECTION_CODE + "=? AND "
				+ StopsViewTable.TABLE_NAME + "." + StopsViewTable.NORMALIZED_NAME + "=?", new String[] { routeCode,
				directionCode, stopName });
		return getFirstFromCursor(c);
	}

	public Cursor getCursor(final ContentResolver contentResolver, final String serviceId, final String routeCode,
			final String directionCode) {
		final Uri.Builder builder = StopViewProvider.CONTENT_URI.buildUpon();
		builder.path(StopViewProvider.STOP_DIRECTION_ROUTE_URI_PATH_QUERY);
		builder.appendQueryParameter("serviceId", serviceId);
		builder.appendQueryParameter("routeCode", routeCode);
		builder.appendQueryParameter("directionCode", directionCode);
		return contentResolver.query(builder.build(), null, null, null, null);
	}

	@Override
	public void onIndexCursor(final Cursor c) {
		mColId = c.getColumnIndex(StopsViewTable._ID);
		mColStopCode = c.getColumnIndex(StopsViewTable.STOP_CODE);
		mColLetter = c.getColumnIndex(StopsViewTable.ROUTE_LETTER);
		mColEquipmentCode = c.getColumnIndex(StopsViewTable.EQUIPMENT_CODE);
		mColRouteCode = c.getColumnIndex(StopsViewTable.ROUTE_CODE);
		mColDirectionCode = c.getColumnIndex(StopsViewTable.DIRECTION_CODE);
		mColName = c.getColumnIndex(StopsViewTable.NAME);
		mColNormalizedName = c.getColumnIndex(StopsViewTable.NORMALIZED_NAME);
		mColLatitude = c.getColumnIndex(StopsViewTable.LATITUDE);
		mColLongitude = c.getColumnIndex(StopsViewTable.LONGITUDE);
		mColEquipmentId = c.getColumnIndex(StopsViewTable.EQUIPMENT_ID);
		mColOrder = c.getColumnIndex(StopsViewTable.STOP_ORDER);
		mColStepType = c.getColumnIndex(StopsViewTable.STEP_TYPE);
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
		mBuilder.setId(c.getInt(c.getColumnIndex(StopsViewTable._ID)));
		mBuilder.setCodeArret(c.getString(c.getColumnIndex(StopsViewTable.STOP_CODE)));
		mBuilder.setLettre(c.getString(c.getColumnIndex(StopsViewTable.ROUTE_LETTER)));
		mBuilder.setCodeEquipement(c.getString(c.getColumnIndex(StopsViewTable.EQUIPMENT_CODE)));
		mBuilder.setCodeLigne(c.getString(c.getColumnIndex(StopsViewTable.ROUTE_CODE)));
		mBuilder.setCodeSens(c.getString(c.getColumnIndex(StopsViewTable.DIRECTION_CODE)));
		mBuilder.setNomArret(c.getString(c.getColumnIndex(StopsViewTable.NAME)));
		mBuilder.setLatitude(c.getFloat(c.getColumnIndex(StopsViewTable.LATITUDE)));
		mBuilder.setLongitude(c.getFloat(c.getColumnIndex(StopsViewTable.LONGITUDE)));
		mBuilder.setIdStation(c.getInt(c.getColumnIndex(StopsViewTable.EQUIPMENT_ID)));
		mBuilder.setOrdre(c.getInt(c.getColumnIndex(StopsViewTable.STOP_ORDER)));
		mBuilder.setStepType(c.getLong(c.getColumnIndex(StopsViewTable.STEP_TYPE)));
		return mBuilder.build();
	}

	public Stop getSingle(final ContentResolver contentResolver, final String code) {
		final Cursor c = getCursor(contentResolver, StopTable.STOP_CODE + " = ?", new String[] { code });
		return getFirstFromCursor(c);
	}

	@Override
	public ContentValues getContentValues(final Stop item) {
		return null;
	}

}
