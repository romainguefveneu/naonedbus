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

import net.naonedbus.bean.Equipement.Type;
import net.naonedbus.bean.parking.relai.ParkingRelai;
import net.naonedbus.manager.SQLiteManager;
import net.naonedbus.provider.impl.EquipementProvider;
import net.naonedbus.provider.table.EquipementTable;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

/**
 * @author romain.guefveneu
 * 
 */
public class ParkingRelaiManager extends SQLiteManager<ParkingRelai> {

	private static ParkingRelaiManager instance;

	public static ParkingRelaiManager getInstance() {
		if (instance == null) {
			instance = new ParkingRelaiManager();
		}
		return instance;
	}

	private ParkingRelaiManager() {
		super(EquipementProvider.CONTENT_URI);
	}

	@Override
	public List<ParkingRelai> getAll(ContentResolver contentResolver) {
		final Uri.Builder builder = EquipementProvider.CONTENT_URI.buildUpon();
		builder.path(EquipementProvider.EQUIPEMENTS_TYPE_URI_PATH_QUERY);
		builder.appendPath(Integer.toString(Type.TYPE_PARKING.getId()));

		final Cursor c = contentResolver.query(builder.build(), null, EquipementTable.ID_SOUS_TYPE + "=?",
				new String[] { String.valueOf(EquipementManager.SousType.PARKING_RELAI.getValue()) }, null);
		return getFromCursor(c);
	}

	@Override
	public ParkingRelai getSingleFromCursor(Cursor c) {
		final ParkingRelai parking = new ParkingRelai();
		parking.setId(c.getInt(c.getColumnIndex(EquipementTable._ID)));
		parking.setNom(c.getString(c.getColumnIndex(EquipementTable.NOM)));
		parking.setAdresse(c.getString(c.getColumnIndex(EquipementTable.ADRESSE)));
		parking.setTelephone(c.getString(c.getColumnIndex(EquipementTable.TELEPHONE)));
		parking.setUrl(c.getString(c.getColumnIndex(EquipementTable.URL)));
		parking.setLatitude(c.getDouble(c.getColumnIndex(EquipementTable.LATITUDE)));
		parking.setLongitude(c.getDouble(c.getColumnIndex(EquipementTable.LONGITUDE)));
		return parking;
	}

}
