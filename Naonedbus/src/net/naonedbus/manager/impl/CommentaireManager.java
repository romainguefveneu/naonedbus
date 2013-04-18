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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.naonedbus.bean.Commentaire;
import net.naonedbus.rest.controller.impl.CommentaireController;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.joda.time.base.BaseDateTime;
import org.json.JSONException;

import android.content.Context;

import com.bugsense.trace.BugSenseHandler;

public class CommentaireManager {

	private static CommentaireManager instance;

	private static final long UP_TO_DATE_DELAY = 300000; // 5 min

	public static CommentaireManager getInstance() {
		if (instance == null) {
			instance = new CommentaireManager();
		}

		return instance;
	}

	public List<Commentaire> getAll(final Context context, final String codeLigne, final String codeSens,
			final String codeArret, final BaseDateTime date) throws IOException, JSONException {

		List<Commentaire> data;
		boolean isUpToDate = false;

		final long maxUpdateTime = System.currentTimeMillis() - UP_TO_DATE_DELAY;
		final File cacheFile = new File(context.getCacheDir(), genKey(codeLigne, codeSens, codeArret) + ".timeline");
		if (cacheFile.exists()) {
			isUpToDate = cacheFile.lastModified() > maxUpdateTime;
		}

		if (isUpToDate) {
			data = getFromCache(context, codeLigne, codeSens, codeArret);
		} else {
			data = getFromWeb(context, codeLigne, codeSens, codeArret, date);
		}

		return data;
	}

	public List<Commentaire> getFromCache(final Context context, final String codeLigne, final String codeSens,
			final String codeArret) {
		final File cacheFile = new File(context.getCacheDir(), genKey(codeLigne, codeSens, codeArret) + ".timeline");
		List<Commentaire> data = new ArrayList<Commentaire>();

		if (cacheFile.exists()) {
			final BufferedReader br = null;
			final CommentaireController controller = new CommentaireController();

			try {
				final String json = IOUtils.toString(new FileReader(cacheFile));
				data = controller.parseJsonArray(json);
			} catch (final JSONException e) {
				BugSenseHandler.sendExceptionMessage("Erreur lors de la lecture du cache timeline.", null, e);
				cacheFile.delete();
			} catch (final IOException e) {
				BugSenseHandler.sendExceptionMessage("Erreur lors de la lecture du cache timeline.", null, e);
				cacheFile.delete();
			} finally {
				IOUtils.closeQuietly(br);
			}
		}

		return data;
	}

	public List<Commentaire> getFromWeb(final Context context, final String codeLigne, final String codeSens,
			final String codeArret, final BaseDateTime date) throws IOException, JSONException {
		final CommentaireController commentaireController = new CommentaireController();
		final List<Commentaire> data = commentaireController.getAll(codeLigne, codeSens, codeArret, date);

		if (data != null && data.size() > 0) {
			final String key = genKey(codeLigne, codeSens, codeArret);
			saveToCache(context, key, data);
		}

		return data;
	}

	private void saveToCache(final Context context, final String key, final List<Commentaire> data) {
		final File cacheFile = new File(context.getCacheDir(), key + ".timeline");
		final CommentaireController controller = new CommentaireController();
		try {
			final String json = controller.toJson(data);
			FileUtils.writeStringToFile(cacheFile, json, "UTF-8");
		} catch (final IOException e) {
			BugSenseHandler.sendExceptionMessage("Erreur lors de l'écriture du cache timeline.", null, e);
		}
	}

	private String genKey(final String codeLigne, final String codeSens, final String codeArret) {
		return codeLigne + codeSens + codeArret;
	}
}
