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

import net.naonedbus.bean.LiveNews;
import net.naonedbus.rest.UrlBuilder;
import net.naonedbus.rest.controller.RestConfiguration;
import net.naonedbus.rest.controller.RestController;

import org.apache.http.HttpException;
import org.json.JSONException;
import org.json.JSONObject;

public class LiveNewsController extends RestController<LiveNews> {

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

	public LiveNewsController() {
		super(TAG_COMMENTAIRE);
	}

	public void post(final String routeCode, final String directionCode, final String codeArret, final String message,
			final String hash) throws IOException, HttpException {
		final UrlBuilder urlBuilder = new UrlBuilder(RestConfiguration.PATH, PATH);

		urlBuilder.addQueryParameter("codeLigne", routeCode);
		urlBuilder.addQueryParameter("codeSens", directionCode);
		urlBuilder.addQueryParameter("codeArret", codeArret);
		urlBuilder.addQueryParameter("message", message);
		urlBuilder.addQueryParameter("hash", hash);
		urlBuilder.addQueryParameter("idClient", RestConfiguration.ID_CLIENT);

		post(urlBuilder);
	}

	public List<LiveNews> getAll(final String routeCode, final String directionCode, final String codeArret)
			throws IOException, JSONException {
		final UrlBuilder url = new UrlBuilder(RestConfiguration.PATH, PATH);

		url.addQueryParameter("codeLigne", routeCode);
		url.addQueryParameter("codeSens", directionCode);
		url.addQueryParameter("codeArret", codeArret);
		url.addQueryParameter("timestamp", 0);
		url.addQueryParameter("limit", String.valueOf(LIMIT));

		return parseJson(url.getUrl());
	}

	@Override
	protected LiveNews parseJsonObject(final JSONObject object) throws JSONException {
		final LiveNews liveNews = new LiveNews();

		liveNews.setId(object.getInt(TAG_ID));
		if (object.has(TAG_CODE_LIGNE))
			liveNews.setCodeLigne(object.getString(TAG_CODE_LIGNE));
		if (object.has(TAG_CODE_SENS))
			liveNews.setCodeSens(object.getString(TAG_CODE_SENS));
		if (object.has(TAG_CODE_ARRET))
			liveNews.setCodeArret(object.getString(TAG_CODE_ARRET));
		if (object.has(TAG_MESSAGE))
			liveNews.setMessage(object.getString(TAG_MESSAGE));
		if (object.has(TAG_SOURCE))
			liveNews.setSource(object.getString(TAG_SOURCE));
		if (object.has(TAG_TIMESTAMP))
			liveNews.setTimestamp(object.getLong(TAG_TIMESTAMP));
		return liveNews;
	}

	@Override
	protected JSONObject toJsonObject(final LiveNews item) throws JSONException {
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