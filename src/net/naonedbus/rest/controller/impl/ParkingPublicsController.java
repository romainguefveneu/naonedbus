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
package net.naonedbus.rest.controller.impl;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import net.naonedbus.bean.parking.PublicPark;
import net.naonedbus.bean.parking.PublicParkStatus;
import net.naonedbus.rest.controller.NodRestController;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Resources;
import android.util.SparseArray;

public class ParkingPublicsController extends NodRestController<PublicPark> {
	private static final String API = "getDisponibiliteParkingsPublics";
	private final SparseArray<PublicParkStatus> statuts;

	private static final String TAG_ID = "IdObj";
	private static final String TAG_NOM = "Grp_nom";
	private static final String TAG_STATUT = "Grp_statut";
	private static final String TAG_DISPONIBILITE = "Grp_disponible";
	private static final String TAG_COMPLET = "Grp_complet";
	private static final String TAG_EXPLOITATION = "Grp_exploitation";
	private static final String TAG_HORODATAGE = "Grp_horodatage";

	public ParkingPublicsController() {
		super("opendata", "answer", "data", "Groupes_Parking", "Groupe_Parking");

		statuts = new SparseArray<PublicParkStatus>();
		for (final PublicParkStatus statut : PublicParkStatus.values()) {
			statuts.put(statut.getValue(), statut);
		}
	}

	public List<PublicPark> getAll(final Resources res) throws IOException, JSONException {
		final List<PublicPark> parkings = super.getAll(res, API);
		final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

		for (final PublicPark parkingPublic : parkings) {
			parkingPublic.setStatus(statuts.get(parkingPublic.getStatusValue()));
			try {
				parkingPublic.setUpdateDate(dateFormat.parse(parkingPublic.getTimestamp()));
			} catch (final ParseException e) {
				// Tant pis
			}
		}

		return parkings;
	}

	@Override
	protected PublicPark parseJsonObject(final JSONObject object) throws JSONException {
		final PublicPark parking = new PublicPark();
		parking.setId(object.getInt(TAG_ID));
		parking.setName(object.getString(TAG_NOM));
		parking.setStatusValue(object.getInt(TAG_STATUT));
		parking.setAvailableSpaces(object.getInt(TAG_DISPONIBILITE));
		parking.setTotalSpaces(object.getInt(TAG_EXPLOITATION));
		parking.setFullLimit(object.getInt(TAG_COMPLET));
		parking.setTimestamp(object.getString(TAG_HORODATAGE));
		return parking;
	}

	@Override
	protected JSONObject toJsonObject(final PublicPark item) throws JSONException {
		return null;
	}

}
