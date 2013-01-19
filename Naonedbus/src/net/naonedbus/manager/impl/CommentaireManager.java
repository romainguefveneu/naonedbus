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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import net.naonedbus.bean.Commentaire;
import net.naonedbus.rest.controller.impl.CommentaireController;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.joda.time.base.BaseDateTime;

import android.content.Context;

import com.bugsense.trace.BugSenseHandler;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class CommentaireManager {
	private static CommentaireManager instance;

	public static CommentaireManager getInstance() {
		if (instance == null) {
			instance = new CommentaireManager();
		}

		return instance;
	}

	public List<Commentaire> getFromCache(Context context, String codeLigne, String codeSens, String codeArret) {
		final File cacheFile = new File(context.getCacheDir(), genKey(codeLigne, codeSens, codeArret) + ".timeline");
		List<Commentaire> data = new ArrayList<Commentaire>();

		if (cacheFile.exists()) {
			BufferedReader br = null;
			final Gson gson = new Gson();

			try {
				br = new BufferedReader(new FileReader(cacheFile));

				final Type type = new TypeToken<List<Commentaire>>() {
				}.getType();

				data = gson.fromJson(br, type);
			} catch (IOException e) {
				BugSenseHandler.sendExceptionMessage("Erreur lors de la lecture du cache timeline.", null, e);
				cacheFile.delete();
			} catch (JsonSyntaxException e) {
				cacheFile.delete();
			} finally {
				IOUtils.closeQuietly(br);
			}
		}

		return data;
	}

	public List<Commentaire> getFromWeb(Context context, String codeLigne, String codeSens, String codeArret,
			BaseDateTime date) throws IOException {
		final CommentaireController commentaireController = new CommentaireController();
		final List<Commentaire> data = commentaireController.getAll(codeLigne, codeSens, codeArret, date);

		if (data != null && data.size() > 0) {
			final String key = genKey(codeLigne, codeSens, codeArret);
			saveToCache(context, key, data);
		}

		return data;
	}

	private void saveToCache(Context context, String key, List<Commentaire> data) {
		final File cacheFile = new File(context.getCacheDir(), key + ".timeline");
		final Gson gson = new Gson();
		try {
			final String json = gson.toJson(data);
			FileUtils.writeStringToFile(cacheFile, json, "UTF-8");
		} catch (IOException e) {
			BugSenseHandler.sendExceptionMessage("Erreur lors de l'écriture du cache timeline.", null, e);
		}
	}

	private String genKey(String codeLigne, String codeSens, String codeArret) {
		return codeLigne + codeSens + codeArret;
	}
}
