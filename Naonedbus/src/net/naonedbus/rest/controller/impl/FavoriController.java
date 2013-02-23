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
import java.util.List;

import net.naonedbus.bean.Favori;
import net.naonedbus.rest.UrlBuilder;
import net.naonedbus.rest.controller.RestConfiguration;
import net.naonedbus.rest.controller.RestController;

import org.apache.http.HttpException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Classe d'envoi des commentaires au WebService
 * 
 * @author romain.guefveneu
 * 
 */
public class FavoriController extends RestController<Favori> {

	public static final String PATH = "favoris";

	private static final String TAG_CODE_LIGNE = "codeLigne";
	private static final String TAG_CODE_SENS = "codeSens";
	private static final String TAG_CODE_ARRET = "codeArret";
	private static final String TAG_NOM_FAVORI = "nomFavori";

	public FavoriController() {
		super();
	}

	public String post(final String content) throws IOException, HttpException {
		final UrlBuilder urlBuilder = new UrlBuilder(RestConfiguration.PATH, PATH);
		urlBuilder.addQueryParameter("contenu", content);
		return post(urlBuilder);
	}

	public List<Favori> get(final String cle) throws IOException {
		final UrlBuilder url = new UrlBuilder(RestConfiguration.PATH, PATH);
		url.addQueryParameter("identifiant", cle);

		return parseJson(url.getUrl());
	}

	@Override
	protected Favori parseJsonObject(final JSONObject object) throws JSONException {
		final Favori favori = new Favori();

		if (object.has(TAG_CODE_LIGNE))
			favori.codeLigne = object.getString(TAG_CODE_LIGNE);
		if (object.has(TAG_CODE_SENS))
			favori.codeSens = object.getString(TAG_CODE_SENS);
		if (object.has(TAG_CODE_ARRET))
			favori.codeArret = object.getString(TAG_CODE_ARRET);
		if (object.has(TAG_NOM_FAVORI))
			favori.nomFavori = object.getString(TAG_NOM_FAVORI);

		return favori;
	}

	@Override
	protected JSONObject toJson(final Favori item) throws JSONException {
		final JSONObject object = new JSONObject();
		object.put(TAG_CODE_LIGNE, item.codeLigne);
		object.put(TAG_CODE_SENS, item.codeSens);
		object.put(TAG_CODE_ARRET, item.codeArret);
		object.put(TAG_NOM_FAVORI, item.nomArret);
		return object;
	}

}
