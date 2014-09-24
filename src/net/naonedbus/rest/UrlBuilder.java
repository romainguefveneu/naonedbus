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
package net.naonedbus.rest;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Classe servant à construire une url rest
 * 
 * @author romain.guefveneu
 * 
 */
public class UrlBuilder {

	private final StringBuilder stringBuilder;
	private boolean isFirstParameter = true;

	public UrlBuilder(final String... params) {
		stringBuilder = new StringBuilder();
		for (final String string : params) {
			stringBuilder.append(string);
		}
	}

	/**
	 * Ajouter un paramètre à l'url
	 * 
	 * @param parameter
	 * @param value
	 */
	public void addQueryParameter(final String parameter, final Object value) {
		if (isFirstParameter) {
			stringBuilder.append("?");
			isFirstParameter = false;
		} else {
			stringBuilder.append("&");
		}
		stringBuilder.append(URLEncoder.encode(parameter)).append("=");
		if (value != null) {
			stringBuilder.append(URLEncoder.encode(value.toString()));
		}

	}

	/**
	 * Ajouter un segment à l'url
	 * <p>
	 * Exemple : /url/segment/
	 * 
	 * @param value
	 */
	public void addSegment(final String value) {
		if (!stringBuilder.toString().endsWith("/")) {
			stringBuilder.append("/");
		}
		stringBuilder.append(value);
	}

	public URL getUrl() throws MalformedURLException {
		return new URL(stringBuilder.toString());
	}

	public InputStream openStream() throws MalformedURLException, IOException {
		return new URL(stringBuilder.toString()).openStream();
	}

	@Override
	public String toString() {
		return stringBuilder.toString();
	}

}
