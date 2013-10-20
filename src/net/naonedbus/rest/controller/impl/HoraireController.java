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
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import net.naonedbus.bean.Stop;
import net.naonedbus.bean.horaire.Horaire;
import net.naonedbus.rest.UrlBuilder;
import net.naonedbus.rest.container.HoraireContainer;
import net.naonedbus.rest.container.HoraireContainer.HoraireNode;
import net.naonedbus.rest.controller.RestController;

import org.joda.time.DateMidnight;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

@SuppressLint("SimpleDateFormat")
public class HoraireController extends RestController<HoraireContainer> {

	private static final String LOG_TAG = HoraireController.class.getSimpleName();

	private static final String TAG_CODE_COULEUR = "codeCouleur";
	private static final String TAG_PLAGE_SERVICE = "plageDeService";
	private static final String TAG_NOTES = "notes";
	private static final String TAG_NOTES_CODE = "code";
	private static final String TAG_NOTES_LIBELLE = "libelle";
	private static final String TAG_HORAIRES = "horaires";
	private static final String TAG_HORAIRES_HEURE = "heure";
	private static final String TAG_HORAIRES_PASSAGE = "passages";

	private static final String PATH = "https://open.tan.fr/ewp/horairesarret.json";

	private final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private final SimpleDateFormat mDateDecode = new SimpleDateFormat("H'h'mm");

	public HoraireController() {
		super("horaires");
		mDateDecode.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	/**
	 * Récupérer les horaires depuis le WebService.
	 * 
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	public synchronized List<Horaire> getAllFromWeb(final Stop arret, final DateMidnight date) throws IOException {
		final UrlBuilder url = new UrlBuilder(PATH);
		long timeOffset = date.getMillis();
		final List<HoraireNode> horaires;
		List<Horaire> result = null;

		url.addSegment(arret.getCodeArret());
		url.addSegment(arret.getCodeLigne());
		url.addSegment(arret.getCodeSens());
		url.addSegment(mDateFormat.format(date.toDate()));
		final HoraireContainer content = parseJsonObject(url.getUrl());

		if (content != null) {
			horaires = content.horaires;
			result = new ArrayList<Horaire>();
			// Transformation des horaires TAN en horaire naonedbus.
			for (final HoraireNode horaireTan : horaires) {
				final String heure = horaireTan.heure;

				// Changement de jour
				if (heure.equals("0h")) {
					timeOffset = date.plusDays(1).getMillis();
				}
				for (final String minute : horaireTan.passages) {
					final Horaire horaire = new Horaire();
					horaire.setDayTrip(date.getMillis());
					horaire.setTimestamp(parseTimestamp(heure, minute, timeOffset));
					horaire.setTerminus(parseTerminus(minute, content.notes));
					horaire.setSection(new DateMidnight(horaire.getTimestamp()));
					result.add(horaire);
				}
			}
		}

		return result;
	}

	private long parseTimestamp(final String heure, final String minute, final long timeOffset) {
		long timestamp = 0;
		try {
			timestamp = timeOffset + mDateDecode.parse(heure + minute).getTime();
		} catch (final ParseException e) {
			Log.e(LOG_TAG, "Erreur de convertion.", e);
		}
		return timestamp;
	}

	private String parseTerminus(final String minute, final Map<String, String> notes) {
		if (TextUtils.isDigitsOnly(minute)) {
			return null;
		}
		final String code = minute.replaceAll("[0-9]", "");
		return notes.get(code);
	}

	@Override
	protected HoraireContainer parseJsonObject(final JSONObject object) throws JSONException {
		final HoraireContainer container = new HoraireContainer();

		if (object.has(TAG_PLAGE_SERVICE))
			container.plageDeService = object.getString(TAG_PLAGE_SERVICE);
		if (object.has(TAG_CODE_COULEUR))
			container.codeCouleur = object.getString(TAG_CODE_COULEUR);
		if (object.has(TAG_NOTES))
			container.notes = parseNotes(object.getJSONArray(TAG_NOTES));
		if (object.has(TAG_HORAIRES))
			container.horaires = parseHoraires(object.getJSONArray(TAG_HORAIRES));

		return container;
	}

	private Map<String, String> parseNotes(final JSONArray array) throws JSONException {
		final Map<String, String> notes = new HashMap<String, String>();
		JSONObject object;

		for (int i = 0; i < array.length(); i++) {
			object = array.getJSONObject(i);

			String code = null;
			String libelle = null;
			if (object.has(TAG_NOTES_CODE))
				code = object.getString(TAG_NOTES_CODE);
			if (object.has(TAG_NOTES_LIBELLE))
				libelle = object.getString(TAG_NOTES_LIBELLE);

			notes.put(code, libelle);
		}

		return notes;
	}

	private List<HoraireNode> parseHoraires(final JSONArray array) throws JSONException {
		final List<HoraireNode> horaires = new ArrayList<HoraireNode>();
		JSONObject object;

		for (int i = 0; i < array.length(); i++) {
			object = array.getJSONObject(i);

			final HoraireNode horaire = new HoraireNode();
			if (object.has(TAG_HORAIRES_HEURE))
				horaire.heure = object.getString(TAG_HORAIRES_HEURE);
			if (object.has(TAG_HORAIRES_PASSAGE))
				horaire.passages = parsePassages(object.getJSONArray(TAG_HORAIRES_PASSAGE));

			horaires.add(horaire);
		}

		return horaires;
	}

	private List<String> parsePassages(final JSONArray passagesArray) throws JSONException {
		final List<String> passages = new ArrayList<String>();
		for (int i = 0; i < passagesArray.length(); i++) {
			passages.add(passagesArray.getString(i));
		}

		return passages;
	}

	@Override
	protected JSONObject toJsonObject(final HoraireContainer item) throws JSONException {
		return null;
	}

}
