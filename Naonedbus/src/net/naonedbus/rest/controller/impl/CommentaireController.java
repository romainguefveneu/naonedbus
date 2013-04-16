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
import java.util.List;

import net.naonedbus.bean.Commentaire;
import net.naonedbus.rest.UrlBuilder;
import net.naonedbus.rest.controller.RestConfiguration;
import net.naonedbus.rest.controller.RestController;

import org.apache.http.HttpException;
import org.joda.time.base.BaseDateTime;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Classe d'envoi des commentaires au WebService
 * 
 * @author romain.guefveneu
 * 
 */
public class CommentaireController extends RestController<Commentaire> {

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

	public CommentaireController() {
		super(TAG_COMMENTAIRE);
	}

	public void post(final String codeLigne, final String codeSens, final String codeArret, final String message, final String hash)
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

	public List<Commentaire> getAll(final String codeLigne, final String codeSens, final String codeArret, final BaseDateTime date)
			throws IOException, JSONException {
		final UrlBuilder url = new UrlBuilder(RestConfiguration.PATH, PATH);

		url.addQueryParameter("codeLigne", codeLigne);
		url.addQueryParameter("codeSens", codeSens);
		url.addQueryParameter("codeArret", codeArret);
		url.addQueryParameter("timestamp", String.valueOf(date.getMillis()));
		url.addQueryParameter("limit", String.valueOf(LIMIT));

		return parseJson(url.getUrl());
	}

	@Override
	protected Commentaire parseJsonObject(final JSONObject object) throws JSONException {
		final Commentaire commentaire = new Commentaire();

		commentaire.setId(object.getInt(TAG_ID));
		if (object.has(TAG_CODE_LIGNE))
			commentaire.setCodeLigne(object.getString(TAG_CODE_LIGNE));
		if (object.has(TAG_CODE_SENS))
			commentaire.setCodeSens(object.getString(TAG_CODE_SENS));
		if (object.has(TAG_CODE_ARRET))
			commentaire.setCodeArret(object.getString(TAG_CODE_ARRET));
		if (object.has(TAG_MESSAGE))
			commentaire.setMessage(object.getString(TAG_MESSAGE));
		if (object.has(TAG_SOURCE))
			commentaire.setSource(object.getString(TAG_SOURCE));
		if (object.has(TAG_TIMESTAMP))
			commentaire.setTimestamp(object.getLong(TAG_TIMESTAMP));
		return commentaire;
	}

	@Override
	protected JSONObject toJsonObject(final Commentaire item) throws JSONException {
		final JSONObject object = new JSONObject();
		object.put(TAG_ID, item.getId());
		object.put(TAG_CODE_LIGNE, item.getCodeLigne());
		object.put(TAG_CODE_SENS, item.getCodeSens());
		object.put(TAG_CODE_ARRET, item.getCodeArret());
		object.put(TAG_MESSAGE, item.getMessage());
		object.put(TAG_SOURCE, item.getSource());
		object.put(TAG_TIMESTAMP, item.getTimestamp());
		return object;
	}
}
