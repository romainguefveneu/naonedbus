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

import net.naonedbus.bean.TanNews;
import net.naonedbus.comparator.TanNewsComparator;
import net.naonedbus.helper.DateTimeFormatHelper;
import net.naonedbus.rest.controller.impl.TanNewsController;

import org.json.JSONException;

import android.content.Context;
import android.util.SparseArray;

public class TanNewsManager {

	private static final long CACHE_LIMITE_MILLI = 15l * 60l * 1000l; // 15
																		// minutes
	private static TanNewsManager sInstance;

	private final Map<String, ArrayList<TanNews>> mCache = new HashMap<String, ArrayList<TanNews>>();
	private final SparseArray<TanNews> mCacheById = new SparseArray<TanNews>();
	private long mDateLimit;

	public static synchronized TanNewsManager getInstance() {
		if (sInstance == null) {
			sInstance = new TanNewsManager();
		}
		return sInstance;
	}

	public synchronized List<TanNews> getByRouteCode(final Context context, final String code) throws IOException,
			JSONException {

		List<TanNews> result = null;
		init(context);
		if (mCache.containsKey(code)) {
			result = mCache.get(code);
		}
		return result;
	}

	public synchronized List<TanNews> getAll(final Context context) throws IOException, JSONException {
		final List<TanNews> result = new ArrayList<TanNews>();
		init(context);

		for (final Entry<String, ArrayList<TanNews>> item : mCache.entrySet()) {
			result.addAll(item.getValue());
		}

		Collections.sort(result, new TanNewsComparator());

		return result;
	}

	public synchronized TanNews getById(final Context context, final int id) throws IOException, JSONException {
		init(context);
		return mCacheById.get(id);
	}

	/**
	 * Gérer le remplissage et la péremption du cache
	 * 
	 * @throws IOException
	 * @throws JSONException
	 */
	public void init(final Context context) throws IOException, JSONException {
		final long now = System.currentTimeMillis();

		if (mCache.isEmpty() || now > mDateLimit) {
			mCache.clear();
			final TanNewsController tanNewsController = new TanNewsController();
			fillCache(context, tanNewsController.getAll(context.getResources()));
			mDateLimit = now + CACHE_LIMITE_MILLI;
		}

	}

	/**
	 * Ajouter les données au cache, selon les troncons indiqués.
	 */
	private void fillCache(final Context context, final List<TanNews> items) {
		final Pattern pattern = Pattern.compile("\\[([0-9A-Z]{1,2})/");
		final DateTimeFormatHelper dateTimeFormatHelper = new DateTimeFormatHelper(context);

		for (final TanNews item : items) {

			item.setDateFormated(dateTimeFormatHelper.formatDuree(item.getStartDate(),
					item.getEndDate()));

			if (item.getEndDate() != null && item.getEndDate().isAfterNow()) {
				final String section = item.getRoadSection();
				if (section != null) {
					final Matcher matcher = pattern.matcher(section);
					while (matcher.find()) {
						final String key = matcher.group(1);
						if (!mCache.containsKey(key)) {
							mCache.put(key, new ArrayList<TanNews>());
						}
						if (!mCache.get(key).contains(item)) {
							item.addRoute(key);
							mCache.get(key).add(item);
						}
					}
				}
			}

			mCacheById.put(Integer.valueOf(item.getCode()), item);
		}

	}
}
