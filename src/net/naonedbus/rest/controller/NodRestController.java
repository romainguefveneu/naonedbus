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

import net.naonedbus.R;

import org.json.JSONException;

import android.content.res.Resources;

public abstract class NodRestController<T> extends RestController<T> {

	private static final String PATH = "http://data.nantes.fr/api/%s/1.0/%s/?output=json";

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
	protected List<T> getAll(final Resources res, final String apiSection) throws IOException, JSONException {
		final URL url = new URL(String.format(PATH, apiSection, res.getString(R.string.nod_key)));
		return parseJson(url);
	}

}
