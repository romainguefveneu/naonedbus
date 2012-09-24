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

import net.naonedbus.bean.Commentaire;
import net.naonedbus.rest.UrlBuilder;
import net.naonedbus.rest.container.CommentaireContainer;
import net.naonedbus.rest.controller.RestConfiguration;
import net.naonedbus.rest.controller.RestController;

import org.apache.http.HttpException;
import org.joda.time.base.BaseDateTime;

/**
 * Classe d'envoi des commentaires au WebService
 * 
 * @author romain.guefveneu
 * 
 */
public class CommentaireController extends RestController<CommentaireContainer> {

	private static final int LIMIT = 20;
	private static final String PATH = "commentaire";

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

		result = parseJson(url, CommentaireContainer.class);

		return (result != null) ? result.commentaire : null;
	}

}
