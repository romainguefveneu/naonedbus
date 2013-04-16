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
package net.naonedbus.rest.controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.json.JSONException;

public abstract class NodRestController<T> extends RestController<T> {

	private static final String PATH = "http://data.nantes.fr/api/%s/1.0/%s/?output=json";
	private static final String API_KEY = "UWW4DQIDC1OQ8XK";

	public NodRestController(final String... rootNodes) {
		super(rootNodes);
	}

	/**
	 * Récupérer tous les éléments sous forme de liste d'object définie.
	 * 
	 * @param apiSection
	 * @return La liste des éléments.
	 * @throws IOException
	 * @throws JSONException 
	 */
	protected List<T> getAll(final String apiSection) throws IOException, JSONException {
		final URL url = new URL(String.format(PATH, apiSection, API_KEY));
		return parseJson(url);
	}

}
