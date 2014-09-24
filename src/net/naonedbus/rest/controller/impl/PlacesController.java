package net.naonedbus.rest.controller.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import net.naonedbus.BuildConfig;
import net.naonedbus.R;
import net.naonedbus.bean.AddressResult;
import net.naonedbus.rest.UrlBuilder;
import net.naonedbus.rest.controller.RestController;

import org.apache.http.HttpException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class PlacesController extends RestController<List<AddressResult>> {

	private static final String TAG = "PlacesController";
	private static final boolean DBG = BuildConfig.DEBUG;

	private static final String DEFAULT_LOCATION = "47.21806,-1.55278";
	private static final String DEFAULT_RADIUS = "16000"; // Km

	// JSON Node names
	private static final String TAG_ERROR = "error_message";

	private static final String TAG_RESULTS = "results";
	private static final String TAG_GEOMETRY = "geometry";
	private static final String TAG_GEOMETRY_LOCATION = "location";
	private static final String TAG_GEOMETRY_LOCATION_LAT = "lat";
	private static final String TAG_GEOMETRY_LOCATION_LNG = "lng";
	private static final String TAG_NAME = "name";
	private static final String TAG_VICINITY = "vicinity";

	private static final String PATH = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";

	public List<AddressResult> getPlaces(final Resources res, final String keyword) throws MalformedURLException,
			IOException, JSONException, HttpException {
		if (DBG)
			Log.d(TAG, "getPlaces " + keyword);

		final UrlBuilder url = new UrlBuilder(PATH);
		url.addQueryParameter("location", DEFAULT_LOCATION);
		url.addQueryParameter("radius", DEFAULT_RADIUS);
		url.addQueryParameter("keyword", keyword);
		url.addQueryParameter("key", res.getString(R.string.google_api_key));

		return parseJsonObject(url.getUrl());
	}

	@Override
	protected List<AddressResult> parseJsonObject(final JSONObject object) throws JSONException {
		final List<AddressResult> result = new ArrayList<AddressResult>();

		if (object.has(TAG_ERROR)) {
			throw new JSONException(object.getString(TAG_ERROR));
		} else if (object.has(TAG_RESULTS)) {
			final JSONArray array = object.getJSONArray(TAG_RESULTS);
			for (int i = 0; i < array.length(); i++) {
				final JSONObject placeObject = array.getJSONObject(i);
				final AddressResult place = parsePlace(placeObject);
				result.add(place);
			}
		}

		return result;
	}

	private AddressResult parsePlace(final JSONObject placeObject) throws JSONException {

		String title = null;
		String description = null;
		LatLng position = new LatLng(0d, 0d);

		if (placeObject.has(TAG_NAME))
			title = placeObject.getString(TAG_NAME);
		if (placeObject.has(TAG_VICINITY))
			description = placeObject.getString(TAG_VICINITY);
		if (placeObject.has(TAG_GEOMETRY))
			position = parseGeometry(placeObject.getJSONObject(TAG_GEOMETRY));

		AddressResult result = new AddressResult(title, description, null, R.drawable.ic_action_location_selector,
				Color.TRANSPARENT, position.latitude, position.longitude);
		result.setSection(0);

		return result;
	}

	private LatLng parseGeometry(final JSONObject placeObject) throws JSONException {
		LatLng result = null;
		if (placeObject.has(TAG_GEOMETRY_LOCATION)) {
			final JSONObject location = placeObject.getJSONObject(TAG_GEOMETRY_LOCATION);
			if (location.has(TAG_GEOMETRY_LOCATION_LAT) && location.has(TAG_GEOMETRY_LOCATION_LNG)) {
				final double lat = Double.parseDouble(location.getString(TAG_GEOMETRY_LOCATION_LAT));
				final double lng = Double.parseDouble(location.getString(TAG_GEOMETRY_LOCATION_LNG));
				result = new LatLng(lat, lng);
			}
		}

		return result;
	}

	@Override
	protected JSONObject toJsonObject(final List<AddressResult> item) throws JSONException {
		return null;
	}

}
