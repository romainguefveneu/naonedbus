package net.naonedbus.rest.controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public abstract class NodRestController<T> extends RestController<T> {

	private static final String PATH = "http://data.nantes.fr/api/%s/1.0/%s/?output=json";
	private static final String API_KEY = "UWW4DQIDC1OQ8XK";

	public NodRestController(String rootNode) {
		super(rootNode);
	}

	/**
	 * Récupérer tous les éléments sous forme de liste d'object définie.
	 * 
	 * @param apiSection
	 * @param jsonPath
	 * @return La liste des éléments.
	 * @throws IOException
	 */
	protected List<T> getAll(String apiSection, String... jsonPath) throws IOException {
		final URL url = new URL(String.format(PATH, apiSection, API_KEY));
		return parseJson(url);
	}
}
