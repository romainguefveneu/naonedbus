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
import java.lang.reflect.Type;
import java.util.List;

import net.naonedbus.bean.Favori;
import net.naonedbus.rest.UrlBuilder;
import net.naonedbus.rest.controller.RestConfiguration;
import net.naonedbus.rest.controller.RestController;

import org.apache.http.HttpException;

import com.google.gson.reflect.TypeToken;

/**
 * Classe d'envoi des commentaires au WebService
 * 
 * @author romain.guefveneu
 * 
 */
public class FavoriController extends RestController<List<Favori>> {

	public static final String PATH = "favoris";

	public String post(String content) throws IOException, HttpException {
		final UrlBuilder urlBuilder = new UrlBuilder(RestConfiguration.PATH, PATH);
		urlBuilder.addQueryParameter("contenu", content);
		return post(urlBuilder);
	}

	public List<Favori> get(String cle) throws IOException {
		final UrlBuilder url = new UrlBuilder(RestConfiguration.PATH, PATH);
		final Type collectionType = new TypeToken<List<Favori>>() {
		}.getType();
		url.addQueryParameter("identifiant", cle);

		return parseJson(url, collectionType);
	}

}
