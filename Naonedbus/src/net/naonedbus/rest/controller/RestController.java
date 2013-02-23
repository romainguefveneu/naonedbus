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
package net.naonedbus.rest.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.naonedbus.BuildConfig;
import net.naonedbus.rest.UrlBuilder;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;

/**
 * @author romain.guefveneu
 * 
 */
public abstract class RestController<T> {

	private static final boolean DBG = BuildConfig.DEBUG;

	/**
	 * Timeout en millisecondes
	 */
	private static int TIMEOUT = 15000;

	/**
	 * Début de la plage de code d'erreur de HTTP
	 */
	private static int HTTP_ERROR_CODE_START = 400;

	private String[] mRootNodes;

	public RestController(final String... rootNode) {
		mRootNodes = rootNode;
	}

	/**
	 * Parser une chaîne Json.
	 * 
	 * @param source
	 *            La chaîne Json
	 * @return L'objet correspondant
	 */
	public T parseJsonObject(final String source) {
		T result = null;
		try {
			result = parseJsonObject(new JSONObject(source));
		} catch (JSONException e) {
			if (DBG)
				Log.e(getClass().getSimpleName(), e.getMessage());
		}

		return result;
	}

	/**
	 * Parser une chaîne Json.
	 * 
	 * @param source
	 *            La chaîne Json
	 * @return L'objet correspondant
	 */
	public List<T> parseJsonArray(final String source) {
		List<T> result = null;

		try {
			final JSONTokener tokener = new JSONTokener(source);
			final Object object = tokener.nextValue();
			if (object instanceof JSONArray) {
				result = parseJsonArray(new JSONArray(source));
			} else if (object instanceof JSONObject) {
				result = parseJsonArray(new JSONObject(source));
			}
		} catch (JSONException e) {
			if (DBG)
				Log.e(getClass().getSimpleName(), e.getMessage(), e);
		}

		return result;
	}

	/**
	 * Parser la réponse d'un webservice Rest json et récupérer une instance du
	 * type passé en paramètre
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	protected T parseJsonObject(URL url) throws IOException {
		final URLConnection conn = url.openConnection();
		conn.setConnectTimeout(TIMEOUT);
		conn.setReadTimeout(TIMEOUT);
		conn.setRequestProperty("Accept-Language", Locale.getDefault().getISO3Language());

		final InputStreamReader comReader = new InputStreamReader(conn.getInputStream());
		final String source = IOUtils.toString(comReader);
		IOUtils.closeQuietly(comReader);

		return parseJsonObject(source);
	}

	/**
	 * Parser la réponse d'un webservice Rest json et récupérer une instance du
	 * type passé en paramètre
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	protected List<T> parseJson(URL url) throws IOException {
		final URLConnection conn = url.openConnection();
		conn.setConnectTimeout(TIMEOUT);
		conn.setReadTimeout(TIMEOUT);
		conn.setRequestProperty("Accept-Language", Locale.getDefault().getISO3Language());

		final InputStreamReader comReader = new InputStreamReader(conn.getInputStream());
		final String source = IOUtils.toString(comReader);
		IOUtils.closeQuietly(comReader);

		return parseJsonArray(source);
	}

	protected JSONArray getRootNode(final JSONObject json) throws JSONException {
		JSONObject object = json;
		for (String node : mRootNodes) {
			if (object.get(node) instanceof JSONObject)
				object = object.getJSONObject(node);
		}
		return (JSONArray) object.getJSONArray(mRootNodes[mRootNodes.length - 1]);
	}

	protected List<T> parseJsonArray(JSONObject json) throws JSONException {
		final JSONArray jsonArray = getRootNode(json);
		return parseJsonArray(jsonArray);
	}

	protected List<T> parseJsonArray(JSONArray jsonArray) throws JSONException {
		final List<T> result = new ArrayList<T>();
		JSONObject c;

		for (int i = 0; i < jsonArray.length(); i++) {
			c = jsonArray.getJSONObject(i);
			result.add(parseJsonObject(c));
		}

		return result;
	}

	public String toJson(List<T> items) {
		final JSONArray list = new JSONArray();
		JSONObject object;
		for (final T item : items) {
			try {
				object = toJson(item);
				list.put(object);
			} catch (JSONException e) {
				if (DBG)
					Log.e(getClass().getSimpleName(), e.getMessage());
			}
		}
		return list.toString();
	}

	/**
	 * Parser un object JsonElement.F
	 * 
	 * @param object
	 *            L'objet à parser
	 * @return L'objet reconstruit
	 */
	protected abstract T parseJsonObject(JSONObject object) throws JSONException;

	/**
	 * Convertir un élément en élément json.
	 * 
	 * @param item
	 *            L'élément à convertir
	 * @return L'object Json
	 */
	protected abstract JSONObject toJson(T item) throws JSONException;

	/**
	 * @see RestController#post(URL)
	 */
	protected String post(UrlBuilder url) throws IOException, HttpException {
		return post(url.getUrl());
	}

	/**
	 * Soumettre le formulaire au webservice
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 * @throws HttpException
	 */
	protected String post(URL url) throws IOException, HttpException {

		final HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT);

		final HttpClient httpClient = new DefaultHttpClient(httpParams);
		final HttpPost httppost = new HttpPost(url.toString());
		final HttpResponse response = httpClient.execute(httppost);

		if (response.getStatusLine().getStatusCode() >= HTTP_ERROR_CODE_START) {
			throw new HttpException(response.getStatusLine().toString());
		}

		final HttpEntity entity = response.getEntity();

		return readResponse(entity);
	}

	/**
	 * Lire la réponse du WebService
	 * 
	 * @param entity
	 * @return
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	private String readResponse(HttpEntity entity) throws IllegalStateException, IOException {
		final StringBuilder builder = new StringBuilder();
		String line;

		if (entity != null) {
			final BufferedReader buffer = new BufferedReader(new InputStreamReader(entity.getContent()));
			while ((line = buffer.readLine()) != null) {
				builder.append(line);
			}
			buffer.close();
		}

		return builder.toString();
	}
}
