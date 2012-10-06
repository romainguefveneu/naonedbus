/**
 *  Copyright (C) 2011 Romain Guefveneu
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
import java.util.List;
import java.util.TimeZone;

import net.naonedbus.bean.Arret;
import net.naonedbus.bean.horaire.Horaire;
import net.naonedbus.bean.horaire.HoraireTan;
import net.naonedbus.rest.UrlBuilder;
import net.naonedbus.rest.container.HoraireContainer;
import net.naonedbus.rest.controller.RestController;

import org.joda.time.DateMidnight;

import android.util.Log;

/**
 * @author romain.guefveneu
 * 
 */
public class HoraireController extends RestController<HoraireContainer> {

	private static final String LOG_TAG = HoraireController.class.getSimpleName();

	private static final String PATH = "https://open.tan.fr/ewp/horairesarret.json";
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private static final SimpleDateFormat dateDecode = new SimpleDateFormat("H'h'mm");
	static {
		dateDecode.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	/**
	 * Récupérer les horaires depuis le WebService
	 * 
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	public List<Horaire> getAllFromWeb(Arret arret, DateMidnight date) throws IOException {
		final UrlBuilder url = new UrlBuilder(PATH);
		long timeOffset = date.getMillis();
		final List<Horaire> result = new ArrayList<Horaire>();
		final List<HoraireTan> horaires;

		url.addSegment(arret.code);
		url.addSegment(arret.codeLigne);
		url.addSegment(arret.codeSens);
		url.addSegment(dateFormat.format(date.toDate()));
		HoraireContainer content = parseJson(url, HoraireContainer.class);

		if (content != null) {
			horaires = content.horaires;
			// Transformation des horaires TAN en horaire naonedbus.
			Horaire horaire;
			String heure;
			for (HoraireTan horaireTan : horaires) {
				heure = horaireTan.heure;
				// Changement de jour
				if (heure.equals("0h")) {
					timeOffset = date.plusDays(1).getMillis();
				}
				for (String minute : horaireTan.passages) {
					horaire = new Horaire();
					horaire.setDayTrip(date.getMillis());
					try {
						horaire.setTimestamp(timeOffset + dateDecode.parse(heure + minute).getTime());
					} catch (ParseException e) {
						Log.e(LOG_TAG, "Erreur de convertion.", e);
					}
					horaire.setSection(new DateMidnight(horaire.getTimestamp()));
					result.add(horaire);
				}
			}
		}

		return result;
	}

}
