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
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

import net.naonedbus.rest.UrlBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * @author romain.guefveneu
 * 
 */
public abstract class RestController<T> {

	/**
	 * Timeout en millisecondes
	 */
	private static int TIMEOUT = 15000;

	/**
	 * Début de la plage de code d'erreur de HTTP
	 */
	private static int HTTP_ERROR_CODE_START = 400;

	/**
	 * {@link RestController#parseJson(URL, Class)}
	 * 
	 * @see RestController#parseJson(URL, Class)
	 */
	protected T parseJson(UrlBuilder url, Class<T> clazz) throws IOException {
		return parseJson(url.getUrl(), clazz);
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
	protected T parseJson(URL url, Class<T> clazz) throws IOException {
		final Gson gson = new Gson();
		Reader comReader;
		T result;

		final URLConnection conn = url.openConnection();
		conn.setConnectTimeout(TIMEOUT);
		conn.setReadTimeout(TIMEOUT);
		conn.setRequestProperty("Accept-Language", Locale.getDefault().getISO3Language());

		comReader = new InputStreamReader(conn.getInputStream());
		result = gson.fromJson(comReader, clazz);
		comReader.close();

		return result;
	}

	/**
	 * {@link RestController#parseJson(URL, Type)}
	 * 
	 * @see RestController#parseJson(URL, Type)
	 */
	protected T parseJson(UrlBuilder url, Type type) throws IOException {
		return parseJson(url.getUrl(), type);
	}

	/**
	 * Parser la réponse d'un webservice Rest json et récupérer une instance du
	 * type passé en paramètre
	 * 
	 * @param url
	 * @param type
	 * @return
	 * @throws IOException
	 */
	protected T parseJson(URL url, Type type) throws IOException {
		final Gson gson = new Gson();
		Reader comReader;
		T result;

		final URLConnection conn = url.openConnection();
		conn.setConnectTimeout(TIMEOUT);
		conn.setReadTimeout(TIMEOUT);
		conn.setRequestProperty("Accept-Language", Locale.getDefault().getISO3Language());

		comReader = new InputStreamReader(conn.getInputStream());
		result = gson.fromJson(comReader, type);
		comReader.close();

		return result;
	}

	/**
	 * Parser un élément JsonElement.F
	 * 
	 * @param element
	 * @param type
	 * @return
	 */
	protected T parseJson(JsonElement element, Type type) {
		final Gson gson = new Gson();
		T result;
		result = gson.fromJson(element, type);
		return result;
	}

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