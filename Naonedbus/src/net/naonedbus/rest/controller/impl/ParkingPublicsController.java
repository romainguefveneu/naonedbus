package net.naonedbus.rest.controller.impl;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import net.naonedbus.bean.parking.pub.ParkingPublic;
import net.naonedbus.bean.parking.pub.ParkingPublicStatut;
import net.naonedbus.rest.controller.NodRestController;
import android.util.SparseArray;

import com.google.gson.reflect.TypeToken;

public class ParkingPublicsController extends NodRestController<ParkingPublic> {
	private static final String API = "getDisponibiliteParkingsPublics";
	private SparseArray<ParkingPublicStatut> statuts;

	public ParkingPublicsController() {
		super("Groupe_Parking");
		statuts = new SparseArray<ParkingPublicStatut>();
		for (ParkingPublicStatut statut : ParkingPublicStatut.values()) {
			statuts.put(statut.getValue(), statut);
		}
	}

	public List<ParkingPublic> getAll() throws IOException {
		final List<ParkingPublic> parkings = super.getAll(API, "Groupes_Parking", "Groupe_Parking");
		final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

		for (ParkingPublic parkingPublic : parkings) {
			parkingPublic.setStatut(statuts.get(parkingPublic.getStatutValue()));
			try {
				parkingPublic.setUpdateDate(dateFormat.parse(parkingPublic.getHorodatage()));
			} catch (ParseException e) {
				// Tant pis
			}
		}

		return parkings;
	}

	@Override
	protected ParkingPublic parseJsonObject(JSONObject object) throws JSONException {
		return null;
	}

}
