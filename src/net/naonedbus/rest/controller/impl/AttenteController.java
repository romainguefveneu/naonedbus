package net.naonedbus.rest.controller.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import net.naonedbus.bean.horaire.Attente;
import net.naonedbus.rest.UrlBuilder;
import net.naonedbus.rest.controller.RestController;

import org.json.JSONException;
import org.json.JSONObject;

public class AttenteController extends RestController<Attente> {

	private static final String TAG_SENS = "sens";
	private static final String TAG_TEMPS = "temps";
	private static final String TAG_LIGNE = "ligne";
	private static final String TAG_LIGNE_NUM = "numLigne";
	private static final String TAG_ARRET = "arret";
	private static final String TAG_ARRET_CODE = "codeArret";

	private static final String PATH = "https://open.tan.fr/ewp/tempsattente.json/";

	public List<Attente> getAll(final String codeEquipement) throws MalformedURLException, IOException, JSONException {
		final UrlBuilder url = new UrlBuilder(PATH);
		url.addSegment(codeEquipement);

		return parseJson(url.getUrl());
	}

	@Override
	protected Attente parseJsonObject(JSONObject object) throws JSONException {
		Attente result = new Attente();

		if (object.has(TAG_SENS))
			result.setCodeSens(object.getString(TAG_SENS));
		if (object.has(TAG_TEMPS))
			result.setTemps(object.getString(TAG_TEMPS));
		if (object.has(TAG_LIGNE))
			result.setCodeLigne(parseCodeLigne(object.getJSONObject(TAG_LIGNE)));
		if (object.has(TAG_ARRET))
			result.setCodeArret(parseCodeArret(object.getJSONObject(TAG_ARRET)));

		return result;
	}

	private String parseCodeLigne(JSONObject object) throws JSONException {
		if (object.has(TAG_LIGNE_NUM))
			return object.getString(TAG_LIGNE_NUM);
		else
			return null;
	}

	private String parseCodeArret(JSONObject object) throws JSONException {
		if (object.has(TAG_ARRET_CODE))
			return object.getString(TAG_ARRET_CODE);
		else
			return null;
	}

	@Override
	protected JSONObject toJsonObject(Attente item) throws JSONException {
		return null;
	}

}
