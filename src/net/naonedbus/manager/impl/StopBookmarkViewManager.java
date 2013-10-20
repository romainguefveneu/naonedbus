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
import net.naonedbus.manager.SQLiteManager;
import net.naonedbus.provider.impl.StopBookmarkViewProvider;
import net.naonedbus.provider.table.StopBookmarkViewTable;
import net.naonedbus.utils.QueryUtils;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class StopBookmarkViewManager extends SQLiteManager<StopBookmark> {

	private static StopBookmarkViewManager instance;

	private final StopBookmark.Builder mBuilder;

	public static synchronized StopBookmarkViewManager getInstance() {
		if (instance == null) {
			instance = new StopBookmarkViewManager();
		}
		return instance;
	}

	protected StopBookmarkViewManager() {
		super(StopBookmarkViewProvider.CONTENT_URI);
		mBuilder = new StopBookmark.Builder();
	}

	/**
	 * Récupérer tous les favoris d'un ensemble de groupe.
	 * 
	 * @param contentResolver
	 * @return la liste de tous les favoris appartenant à un des groupes
	 */
	public List<StopBookmark> getAll(final ContentResolver contentResolver, final List<Integer> idGroupes) {
		final Uri.Builder builder = StopBookmarkViewProvider.CONTENT_URI.buildUpon();
		builder.path(StopBookmarkViewProvider.FAVORIS_GROUPES_URI_PATH_QUERY);
		builder.appendQueryParameter(StopBookmarkViewProvider.QUERY_PARAMETER_GROUPES_IDS,
				QueryUtils.listToInStatement(idGroupes));

		return getFromCursor(contentResolver.query(builder.build(), null, null, null, null));
	}

	public List<StopBookmark> getUnique(final ContentResolver contentResolver) {
		final Uri.Builder builder = StopBookmarkViewProvider.CONTENT_URI.buildUpon();
		return getFromCursor(contentResolver.query(builder.build(), null, null, null, null));
	}

	@Override
	public StopBookmark getSingleFromCursor(final Cursor c) {

		mBuilder.setId(c.getInt(c.getColumnIndex(StopBookmarkViewTable._ID)));
		mBuilder.setCodeLigne(c.getString(c.getColumnIndex(StopBookmarkViewTable.ROUTE_CODE)));
		mBuilder.setCodeEquipement(c.getString(c.getColumnIndex(StopBookmarkViewTable.EQUIPMENT_CODE)));
		mBuilder.setCodeSens(c.getString(c.getColumnIndex(StopBookmarkViewTable.DIRECTION_CODE)));
		mBuilder.setCodeArret(c.getString(c.getColumnIndex(StopBookmarkViewTable.STOP_CODE)));
		mBuilder.setBookmarkName(c.getString(c.getColumnIndex(StopBookmarkViewTable.BOOKMARK_NAME)));
		mBuilder.setDirectionName(c.getString(c.getColumnIndex(StopBookmarkViewTable.DIRECTION_NAME)));
		mBuilder.setIdStation(c.getInt(c.getColumnIndex(StopBookmarkViewTable.EQUIPMENT_ID)));
		mBuilder.setLettre(c.getString(c.getColumnIndex(StopBookmarkViewTable.ROUTE_LETTER)));

		final int couleurBackground = c.getInt(c.getColumnIndex(StopBookmarkViewTable.ROUTE_BACK_COLOR));
		final int couleurFront = c.getInt(c.getColumnIndex(StopBookmarkViewTable.ROUTE_FRONT_COLOR));
		mBuilder.setBackColor(couleurBackground);
		mBuilder.setFrontColor(couleurFront);

		mBuilder.setNomArret(c.getString(c.getColumnIndex(StopBookmarkViewTable.EQUIPMENT_NAME)));
		mBuilder.setNormalizedNom(c.getString(c.getColumnIndex(StopBookmarkViewTable.NORMALIZED_NAME)));
		mBuilder.setLatitude(c.getFloat(c.getColumnIndex(StopBookmarkViewTable.LATITUDE)));
		mBuilder.setLongitude(c.getFloat(c.getColumnIndex(StopBookmarkViewTable.LONGITUDE)));
		mBuilder.setGroupName(c.getString(c.getColumnIndex(StopBookmarkViewTable.GROUP_NAME)));

		int index = c.getColumnIndex(StopBookmarkViewTable.NEXT_SCHEDULE);
		if (c.isNull(index)) {
			mBuilder.setNextSchedule(null);
		} else {
			mBuilder.setNextSchedule(c.getInt(index));
		}

		index = c.getColumnIndex(StopBookmarkViewTable.BOOKMARKGROUP_ID);
		if (c.isNull(index)) {
			mBuilder.setGroupId(-1);
			mBuilder.setSection(-1);
		} else {
			mBuilder.setGroupId(c.getInt(index));
			mBuilder.setSection(c.getInt(index));
		}

		return mBuilder.build();
	}

	@Override
	protected ContentValues getContentValues(final StopBookmark item) {
		return null;
	}
}
