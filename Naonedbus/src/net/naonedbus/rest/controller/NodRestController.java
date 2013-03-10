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
