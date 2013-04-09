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
import java.util.ArrayList;
import java.util.List;

import net.naonedbus.rest.UrlBuilder;
import net.naonedbus.rest.container.FavoriContainer;
import net.naonedbus.rest.container.FavoriContainer.Favori;
import net.naonedbus.rest.container.FavoriContainer.Groupe;
import net.naonedbus.rest.controller.RestConfiguration;
import net.naonedbus.rest.controller.RestController;

import org.apache.http.HttpException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.SparseArray;

/**
 * Classe d'envoi des favoris au WebService.
 * 
 * @author romain.guefveneu
 * 
 */
public class FavoriController extends RestController<FavoriContainer> {

	public static final String PATH = "favoris";

	private static final String TAG_META = "meta";
	private static final String TAG_META_VERSION = "version";

	private static final String TAG_FAVORIS = "favoris";
	private static final String TAG_FAVORI_CODE_LIGNE = "codeLigne";
	private static final String TAG_FAVORI_CODE_SENS = "codeSens";
	private static final String TAG_FAVORI_CODE_ARRET = "codeArret";
	private static final String TAG_FAVORI_NOM_FAVORI = "nomFavori";
	private static final String TAG_FAVORI_GROUPES = "groupes";

	private static final String TAG_GROUPES = "groupes";
	private static final String TAG_GROUPE_ID = "id";
	private static final String TAG_GROUPE_NOM = "nom";
	private static final String TAG_GROUPE_ORDRE = "ordre";

	private final SparseArray<ContainerReader> mContainerReader = new SparseArray<ContainerReader>();

	public FavoriController() {
		mContainerReader.put(2, new AcapulcoContainerReader());
	}

	public String post(final String content) throws IOException, HttpException {
		final UrlBuilder urlBuilder = new UrlBuilder(RestConfiguration.PATH, PATH);
		urlBuilder.addQueryParameter("contenu", content);
		return post(urlBuilder);
	}

	public FavoriContainer get(final String cle) throws IOException, JSONException {
		final UrlBuilder url = new UrlBuilder(RestConfiguration.PATH, PATH);
		url.addQueryParameter("identifiant", cle);

		return parseJson(readJsonFromUrl(url.getUrl()));
	}

	public FavoriContainer parseJson(final String source) throws JSONException {
		FavoriContainer result = null;

		final JSONTokener tokener = new JSONTokener(source);
		final Object object = tokener.nextValue();
		if (object instanceof JSONArray) {
			result = parseGreenDevil(new JSONArray(source));
		} else if (object instanceof JSONObject) {
			result = parseJsonObject(new JSONObject(source));
		} else {
			throw new JSONException("L'élément n'est pas flux JSON valide.");
		}

		return result;
	}

	@Override
	protected FavoriContainer parseJsonObject(final JSONObject object) throws JSONException {
		int version = -1;
		if (object.has(TAG_META)) {
			final JSONObject meta = object.getJSONObject(TAG_META);
			version = meta.getInt(TAG_META_VERSION);
		}

		if (version != -1) {
			final ContainerReader reader = mContainerReader.get(version);
			return reader.parseJsonObject(object);
		} else {
			return null;
		}
	}

	protected FavoriContainer parseGreenDevil(final JSONArray object) throws JSONException {
		final FavoriContainer container = new FavoriContainer();
		for (int i = 0; i < object.length(); i++) {
			final JSONObject favori = object.getJSONObject(i);
			container.addFavori(favori.getString(TAG_FAVORI_CODE_LIGNE), favori.getString(TAG_FAVORI_CODE_SENS),
					favori.getString(TAG_FAVORI_CODE_ARRET), favori.getString(TAG_FAVORI_NOM_FAVORI), null);
		}

		return container;
	}

	@Override
	public JSONObject toJsonObject(final FavoriContainer item) throws JSONException {
		final JSONObject object = new JSONObject();

		final JSONObject meta = new JSONObject();
		meta.put(TAG_META_VERSION, FavoriContainer.VERSION);
		object.put(TAG_META, meta);

		final JSONArray groupes = new JSONArray();
		for (final Groupe groupe : item.groupes) {
			final JSONObject groupeJson = new JSONObject();
			groupeJson.put(TAG_GROUPE_ID, groupe.id);
			groupeJson.put(TAG_GROUPE_NOM, groupe.nom);
			groupeJson.put(TAG_GROUPE_ORDRE, groupe.ordre);

			groupes.put(groupeJson);
		}
		object.put(TAG_GROUPES, groupes);

		final JSONArray favoris = new JSONArray();
		for (final Favori favori : item.favoris) {
			final JSONObject favoriJson = new JSONObject();
			favoriJson.put(TAG_FAVORI_CODE_LIGNE, favori.codeLigne);
			favoriJson.put(TAG_FAVORI_CODE_SENS, favori.codeSens);
			favoriJson.put(TAG_FAVORI_CODE_ARRET, favori.codeArret);
			favoriJson.put(TAG_FAVORI_NOM_FAVORI, favori.nomFavori);

			final JSONArray idGroupes = new JSONArray();
			for (final Integer id : favori.idGroupes) {
				idGroupes.put(id);
			}
			favoriJson.put(TAG_FAVORI_GROUPES, idGroupes);

			favoris.put(favoriJson);
		}
		object.put(TAG_FAVORIS, favoris);

		return object;
	}

	interface ContainerReader {
		FavoriContainer parseJsonObject(final JSONObject object) throws JSONException;
	}

	/**
	 * Import des favoris à partir de 3.0.
	 * 
	 * @author romain
	 * 
	 */
	class AcapulcoContainerReader implements ContainerReader {

		@Override
		public FavoriContainer parseJsonObject(final JSONObject object) throws JSONException {
			final FavoriContainer container = new FavoriContainer();

			final JSONArray groupes = object.getJSONArray(TAG_GROUPES);
			for (int i = 0; i < groupes.length(); i++) {
				final JSONObject groupe = groupes.getJSONObject(i);
				container.addGroupe(groupe.getInt(TAG_GROUPE_ID), groupe.getString(TAG_GROUPE_NOM),
						groupe.getInt(TAG_GROUPE_ORDRE));
			}

			final JSONArray favoris = object.getJSONArray(TAG_FAVORIS);
			for (int i = 0; i < favoris.length(); i++) {
				final JSONObject favori = favoris.getJSONObject(i);

				final List<Integer> idGroupes = new ArrayList<Integer>();
				final JSONArray favoriGroupeJson = favori.getJSONArray(TAG_FAVORI_GROUPES);
				for (int g = 0; g < favoriGroupeJson.length(); g++) {
					idGroupes.add(favoriGroupeJson.getInt(g));
				}

				final String nomFavori;
				if (favori.has(TAG_FAVORI_NOM_FAVORI)) {
					nomFavori = favori.getString(TAG_FAVORI_NOM_FAVORI);
				} else {
					nomFavori = null;
				}
				container.addFavori(favori.getString(TAG_FAVORI_CODE_LIGNE), favori.getString(TAG_FAVORI_CODE_SENS),
						favori.getString(TAG_FAVORI_CODE_ARRET), nomFavori, idGroupes);
			}

			return container;
		}
	}

}
