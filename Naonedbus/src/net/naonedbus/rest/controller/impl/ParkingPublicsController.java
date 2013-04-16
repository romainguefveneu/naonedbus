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

import net.naonedbus.bean.parking.pub.ParkingPublic;
import net.naonedbus.bean.parking.pub.ParkingPublicStatut;
import net.naonedbus.rest.controller.NodRestController;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.SparseArray;

public class ParkingPublicsController extends NodRestController<ParkingPublic> {
	private static final String API = "getDisponibiliteParkingsPublics";
	private final SparseArray<ParkingPublicStatut> statuts;

	private static final String TAG_ID = "IdObj";
	private static final String TAG_NOM = "Grp_nom";
	private static final String TAG_STATUT = "Grp_statut";
	private static final String TAG_DISPONIBILITE = "Grp_disponible";
	private static final String TAG_COMPLET = "Grp_complet";
	private static final String TAG_EXPLOITATION = "Grp_exploitation";
	private static final String TAG_HORODATAGE = "Grp_horodatage";

	public ParkingPublicsController() {
		super("opendata", "answer", "data", "Groupes_Parking", "Groupe_Parking");

		statuts = new SparseArray<ParkingPublicStatut>();
		for (final ParkingPublicStatut statut : ParkingPublicStatut.values()) {
			statuts.put(statut.getValue(), statut);
		}
	}

	public List<ParkingPublic> getAll() throws IOException, JSONException {
		final List<ParkingPublic> parkings = super.getAll(API);
		final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

		for (final ParkingPublic parkingPublic : parkings) {
			parkingPublic.setStatut(statuts.get(parkingPublic.getStatutValue()));
			try {
				parkingPublic.setUpdateDate(dateFormat.parse(parkingPublic.getHorodatage()));
			} catch (final ParseException e) {
				// Tant pis
			}
		}

		return parkings;
	}

	@Override
	protected ParkingPublic parseJsonObject(final JSONObject object) throws JSONException {
		final ParkingPublic parking = new ParkingPublic();
		parking.setId(object.getInt(TAG_ID));
		parking.setNom(object.getString(TAG_NOM));
		parking.setStatutValue(object.getInt(TAG_STATUT));
		parking.setPlacesDisponibles(object.getInt(TAG_DISPONIBILITE));
		parking.setPlacesTotales(object.getInt(TAG_EXPLOITATION));
		parking.setSeuilComplet(object.getInt(TAG_COMPLET));
		parking.setHorodatage(object.getString(TAG_HORODATAGE));
		return parking;
	}

	@Override
	protected JSONObject toJsonObject(final ParkingPublic item) throws JSONException {
		return null;
	}

}
