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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import net.naonedbus.bean.Arret;
import net.naonedbus.bean.horaire.Horaire;
import net.naonedbus.rest.UrlBuilder;
import net.naonedbus.rest.container.HoraireContainer;
import net.naonedbus.rest.container.HoraireContainer.HoraireNode;
import net.naonedbus.rest.controller.RestController;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.MutableDateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.text.TextUtils;

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
	private final SimpleDateFormat mTimeFormat = new SimpleDateFormat("H'h'mm");

	public HoraireController() {
		super("horaires");
		mTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	/**
	 * Récupérer les horaires depuis le WebService.
	 * 
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	public synchronized List<Horaire> getAllFromWeb(final Arret arret, final DateMidnight date) throws IOException {
		final UrlBuilder url = new UrlBuilder(PATH);
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
			MutableDateTime dateTime = new MutableDateTime(date);
			dateTime.setZone(DateTimeZone.forID("GMT"));
			
			int lastHour = Integer.MIN_VALUE;
			
			for (final HoraireNode horaireTan : horaires) {
				int hours = Integer.parseInt(horaireTan.heure.replaceAll("[^\\d.]", ""));
				dateTime.setHourOfDay(hours);

				if (hours < lastHour) { // Changement de jour
					dateTime.addDays(1);
				}
				lastHour = hours;

				for (final String passage : horaireTan.passages) {
					final Horaire horaire = new Horaire();

					int minutes = Integer.parseInt(passage.replaceAll("[^\\d.]", ""));

					dateTime.setMinuteOfHour(minutes);

					horaire.setYear(dateTime.getYear());
					horaire.setDayOfYear(dateTime.getDayOfYear());

					horaire.setMinutes(hours * 60 + minutes * 60);
					horaire.setTerminus(parseTerminus(passage, content.notes));

					horaire.setDateTime(new DateTime(dateTime));
					horaire.setSection(new DateMidnight(dateTime));
					result.add(horaire);
				}
			}
		}

		return result;
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
