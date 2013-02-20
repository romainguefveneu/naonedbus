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
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.naonedbus.bean.Commentaire;
import net.naonedbus.rest.UrlBuilder;
import net.naonedbus.rest.container.CommentaireContainer;
import net.naonedbus.rest.controller.RestConfiguration;
import net.naonedbus.rest.controller.RestController;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpException;
import org.joda.time.base.BaseDateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Classe d'envoi des commentaires au WebService
 * 
 * @author romain.guefveneu
 * 
 */
public class CommentaireController extends RestController<CommentaireContainer> {

	private static final int LIMIT = 25;
	private static final String PATH = "commentaire";

	// JSON Node names
	private static final String TAG_COMMENTAIRE = "commentaire";
	private static final String TAG_ID = "id";
	private static final String TAG_CODE_ARRET = "codeArret";
	private static final String TAG_CODE_LIGNE = "codeLigne";
	private static final String TAG_CODE_SENS = "codeSens";
	private static final String TAG_MESSAGE = "message";
	private static final String TAG_SOURCE = "source";
	private static final String TAG_TIMESTAMP = "timestamp";

	public void post(String codeLigne, String codeSens, String codeArret, String message, String hash)
			throws IOException, HttpException {
		final UrlBuilder urlBuilder = new UrlBuilder(RestConfiguration.PATH, PATH);

		urlBuilder.addQueryParameter("codeLigne", codeLigne);
		urlBuilder.addQueryParameter("codeSens", codeSens);
		urlBuilder.addQueryParameter("codeArret", codeArret);
		urlBuilder.addQueryParameter("message", message);
		urlBuilder.addQueryParameter("hash", hash);
		urlBuilder.addQueryParameter("idClient", RestConfiguration.ID_CLIENT);

		post(urlBuilder);
	}

	public List<Commentaire> getAll(String codeLigne, String codeSens, String codeArret, BaseDateTime date)
			throws IOException {
		final UrlBuilder url = new UrlBuilder(RestConfiguration.PATH, PATH);

		CommentaireContainer result;

		url.addQueryParameter("codeLigne", codeLigne);
		url.addQueryParameter("codeSens", codeSens);
		url.addQueryParameter("codeArret", codeArret);
		url.addQueryParameter("timestamp", String.valueOf(date.getMillis()));
		url.addQueryParameter("limit", String.valueOf(LIMIT));

		result = parseJson(url.getUrl());

		return (result == null) ? null : result.commentaire;
	}

	/**
	 * Parser la réponse d'un webservice Rest json et récupérer une instance de
	 * la classe passée en paramètre
	 * 
	 * @param url
	 * @param clazz
	 * @return
	 * @throws IOException
	 */
	protected CommentaireContainer parseJson(URL url) throws IOException {
		CommentaireContainer result = null;
		final List<Commentaire> commentaires = new ArrayList<Commentaire>();

		final URLConnection conn = url.openConnection();
		conn.setRequestProperty("Accept-Language", Locale.getDefault().getISO3Language());

		final InputStreamReader comReader = new InputStreamReader(conn.getInputStream());
		final String source = IOUtils.toString(comReader);
		IOUtils.closeQuietly(comReader);

		try {
			final JSONObject json = new JSONObject(source);
			final JSONArray jsonArray = json.getJSONArray(TAG_COMMENTAIRE);

			Commentaire commentaire;
			JSONObject c;
			// looping through All Contacts
			for (int i = 0; i < jsonArray.length(); i++) {
				c = jsonArray.getJSONObject(i);
				commentaire = new Commentaire();

				commentaire.setId(c.getInt(TAG_ID));
				if (c.has(TAG_CODE_LIGNE))
					commentaire.setCodeLigne(c.getString(TAG_CODE_LIGNE));
				if (c.has(TAG_CODE_SENS))
					commentaire.setCodeSens(c.getString(TAG_CODE_SENS));
				if (c.has(TAG_CODE_ARRET))
					commentaire.setCodeArret(c.getString(TAG_CODE_ARRET));
				if (c.has(TAG_MESSAGE))
					commentaire.setMessage(c.getString(TAG_MESSAGE));
				if (c.has(TAG_SOURCE))
					commentaire.setSource(c.getString(TAG_SOURCE));
				if (c.has(TAG_TIMESTAMP))
					commentaire.setTimestamp(c.getLong(TAG_TIMESTAMP));

				commentaires.add(commentaire);
			}

			result = new CommentaireContainer();
			result.commentaire = commentaires;
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return result;
	}
}
