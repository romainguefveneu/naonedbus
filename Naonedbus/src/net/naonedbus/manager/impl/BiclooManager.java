package net.naonedbus.manager.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.naonedbus.bean.Bicloo;
import net.naonedbus.rest.controller.impl.BiclooController;

import org.joda.time.DateTime;
import org.json.JSONException;

import android.content.Context;

public class BiclooManager {
	private static final String LOG_TAG = BiclooManager.class.getSimpleName();

	private static final int CACHE_LIMITE_MINUTES = 15;
	private static BiclooManager sInstance;

	private List<Bicloo> mCache;
	private DateTime mDateLimit;

	public static synchronized BiclooManager getInstance() {
		if (sInstance == null) {
			sInstance = new BiclooManager();
		}

		return sInstance;
	}

	private BiclooManager() {
		mCache = new ArrayList<Bicloo>();
	}

	/**
	 * Charger les données et gérer le cache.
	 * 
	 * @throws IOException
	 * @throws JSONException
	 */
	private void init(final Context context) throws IOException, JSONException {
		final DateTime now = new DateTime();

		if (mCache.isEmpty() || now.isAfter(mDateLimit)) {
			final BiclooController controller = new BiclooController();
			mCache.clear();
			mCache = controller.getAll(context.getResources());
			mDateLimit = now.plusMinutes(CACHE_LIMITE_MINUTES);
		}
	}

	/**
	 * Récupérer les bicloos.
	 * 
	 * @return La liste des bicloos
	 * @throws IOException
	 * @throws JSONException
	 */
	public List<Bicloo> getAll(final Context context) throws IOException, JSONException {
		init(context);
		return mCache;
	}

}
