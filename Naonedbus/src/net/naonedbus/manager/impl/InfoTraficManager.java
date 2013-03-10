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
package net.naonedbus.manager.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.naonedbus.bean.InfoTrafic;
import net.naonedbus.comparator.InfoTraficComparator;
import net.naonedbus.helper.DateTimeFormatHelper;
import net.naonedbus.rest.controller.impl.InfoTraficController;

import org.joda.time.DateTime;
import org.json.JSONException;

import android.content.Context;
import android.util.SparseArray;

/**
 * @author romain
 * 
 */
public class InfoTraficManager {

	private static final int CACHE_LIMITE_MINUTES = 15;
	private static InfoTraficManager instance;
	private static Map<String, ArrayList<InfoTrafic>> cache = Collections
			.synchronizedMap(new HashMap<String, ArrayList<InfoTrafic>>());
	private static SparseArray<InfoTrafic> cacheById = new SparseArray<InfoTrafic>();

	private static DateTime dateLimit;

	public static InfoTraficManager getInstance() {
		if (instance == null) {
			instance = new InfoTraficManager();
		}

		return instance;
	}

	public synchronized List<InfoTrafic> getByLigneCode(final Context context, final String code) throws IOException, JSONException {
		List<InfoTrafic> result = new ArrayList<InfoTrafic>();

		init(context);

		if (cache.containsKey(code)) {
			result = cache.get(code);
		}

		return result;
	}

	public synchronized List<InfoTrafic> getAll(final Context context) throws IOException, JSONException {
		final List<InfoTrafic> result = new ArrayList<InfoTrafic>();
		init(context);

		for (final Entry<String, ArrayList<InfoTrafic>> item : cache.entrySet()) {
			result.addAll(item.getValue());
		}

		Collections.sort(result, new InfoTraficComparator());

		return result;
	}

	public synchronized InfoTrafic getById(final Context context, final int id) throws IOException, JSONException {
		init(context);
		return cacheById.get(id);
	}

	/**
	 * Gérer le remplissage et la péremption du cache
	 * 
	 * @throws IOException
	 * @throws JSONException 
	 */
	public void init(final Context context) throws IOException, JSONException {
		final DateTime now = new DateTime();

		if (cache.isEmpty() || now.isAfter(dateLimit)) {
			cache.clear();
			final InfoTraficController infoTraficController = new InfoTraficController();
			populateCache(context, infoTraficController.getAll());
			dateLimit = now.plusMinutes(CACHE_LIMITE_MINUTES);
		}

	}

	/**
	 * Ajouter les données au cache, selon les troncons indiqués
	 */
	private void populateCache(final Context context, final List<InfoTrafic> infoTrafics) {
		final Pattern pattern = Pattern.compile("\\[([0-9A-Z]{1,2})/");
		final DateTimeFormatHelper dateTimeFormatHelper = new DateTimeFormatHelper(context);

		for (final InfoTrafic infoTrafic : infoTrafics) {

			infoTrafic.setDateFormated(dateTimeFormatHelper.formatDuree(infoTrafic.getDateDebut(),
					infoTrafic.getDateFin()));

			if (infoTrafic.getDateFin() == null || infoTrafic.getDateFin().isAfterNow()) {
				final String troncons = infoTrafic.getTroncons();
				if (troncons != null) {
					final Matcher matcher = pattern.matcher(troncons);
					while (matcher.find()) {
						final String key = matcher.group(1);
						if (!cache.containsKey(key)) {
							cache.put(key, new ArrayList<InfoTrafic>());
						}
						if (!cache.get(key).contains(infoTrafic)) {
							infoTrafic.addLignes(key);
							cache.get(key).add(infoTrafic);
						}
					}
				}
			}

			cacheById.put(Integer.valueOf(infoTrafic.getCode()), infoTrafic);
		}

	}
}
