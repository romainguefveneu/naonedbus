package net.naonedbus.rest.controller.impl;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import net.naonedbus.R;
import net.naonedbus.bean.Bicloo;
import net.naonedbus.rest.controller.RestController;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;

public class BiclooController extends RestController<Bicloo> {

	private static final String URL = "https://api.jcdecaux.com/vls/v1/stations?contract=Nantes&apiKey=%s";

	// JSON Node names
	private static final String TAG_NUMBER = "number";
	private static final String TAG_NAME = "name";
	private static final String TAG_ADDRESS = "address";
	private static final String TAG_POSITION = "position";
	private static final String TAG_POSITION_LAT = "lat";
	private static final String TAG_POSITION_LON = "lon";
	private static final String TAG_BANKING = "banking";
	private static final String TAG_BONUS = "bonus";
	private static final String TAG_STATUS = "status";
	private static final String TAG_BIKE_STANDS = "bike_stands";
	private static final String TAG_AVAILABLE_BIKE_STANDS = "available_bike_stands";
	private static final String TAG_AVAILABLE_BIKES = "available_bikes";
	private static final String TAG_LAST_UPDATE = "last_update";

	public List<Bicloo> getAll(final Resources res) throws IOException, JSONException {
		final String url = String.format(URL, res.getString(R.string.jcdecaux_key));

		return parseJson(new URL(url));
	}

	@Override
	protected Bicloo parseJsonObject(final JSONObject object) throws JSONException {
		final Bicloo bicloo = new Bicloo();
		bicloo.setNumber(object.getInt(TAG_NUMBER));
		bicloo.setName(object.getString(TAG_NAME));
		bicloo.setAddress(object.getString(TAG_ADDRESS));
		bicloo.setLocation(getLocation(object.getJSONObject(TAG_POSITION)));
		bicloo.setBanking(object.getBoolean(TAG_BANKING));
		bicloo.setBonus(object.getBoolean(TAG_BONUS));
		bicloo.setStatus(object.getString(TAG_STATUS));
		bicloo.setBikeStands(object.getInt(TAG_BIKE_STANDS));
		bicloo.setAvailableBikeStands(object.getInt(TAG_AVAILABLE_BIKE_STANDS));
		bicloo.setAvailableBike(object.getInt(TAG_AVAILABLE_BIKES));
		bicloo.setLastUpdate(object.getLong(TAG_LAST_UPDATE));
		return null;
	}

	private Location getLocation(final JSONObject object) throws JSONException {
		final Location location = new Location(LocationManager.GPS_PROVIDER);
		location.setLatitude(object.getDouble(TAG_POSITION_LAT));
		location.setLatitude(object.getDouble(TAG_POSITION_LON));
		return location;
	}

	@Override
	protected JSONObject toJsonObject(final Bicloo item) throws JSONException {
		return null;
	}

}
