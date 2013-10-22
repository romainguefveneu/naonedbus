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

import net.naonedbus.bean.Equipment.Type;
import net.naonedbus.bean.parking.IncentivePark;
import net.naonedbus.manager.SQLiteManager;
import net.naonedbus.provider.impl.EquipmentProvider;
import net.naonedbus.provider.table.EquipmentTable;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class ParkingRelaiManager extends SQLiteManager<IncentivePark> {

	private static ParkingRelaiManager instance;

	public static ParkingRelaiManager getInstance() {
		if (instance == null) {
			instance = new ParkingRelaiManager();
		}
		return instance;
	}

	private ParkingRelaiManager() {
		super(EquipmentProvider.CONTENT_URI);
	}

	@Override
	public List<IncentivePark> getAll(final ContentResolver contentResolver) {
		final Uri.Builder builder = EquipmentProvider.CONTENT_URI.buildUpon();
		builder.path(EquipmentProvider.EQUIPEMENTS_TYPE_URI_PATH_QUERY);
		builder.appendPath(Integer.toString(Type.TYPE_PARK.getId()));

		final Cursor c = contentResolver.query(builder.build(), null, EquipmentTable.SUBTYPE_ID + "=?",
				new String[] { String.valueOf(EquipmentManager.SubType.INCENTIVE_PARK.getValue()) }, null);
		return getFromCursor(c);
	}

	@Override
	public IncentivePark getSingleFromCursor(final Cursor c) {
		final IncentivePark parking = new IncentivePark();
		parking.setId(c.getInt(c.getColumnIndex(EquipmentTable._ID)));
		parking.setName(c.getString(c.getColumnIndex(EquipmentTable.EQUIPMENT_NAME)));
		parking.setAdress(c.getString(c.getColumnIndex(EquipmentTable.ADDRESS)));
		parking.setPhone(c.getString(c.getColumnIndex(EquipmentTable.PHONE)));
		parking.setUrl(c.getString(c.getColumnIndex(EquipmentTable.URL)));
		parking.setLatitude(c.getDouble(c.getColumnIndex(EquipmentTable.LATITUDE)));
		parking.setLongitude(c.getDouble(c.getColumnIndex(EquipmentTable.LONGITUDE)));
		return parking;
	}

	@Override
	protected ContentValues getContentValues(final IncentivePark item) {
		return null;
	}

}
