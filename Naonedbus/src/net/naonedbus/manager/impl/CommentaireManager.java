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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import net.naonedbus.bean.Commentaire;
import net.naonedbus.rest.controller.impl.CommentaireController;

import org.joda.time.base.BaseDateTime;

import android.content.Context;

import com.bugsense.trace.BugSenseHandler;

public class CommentaireManager {
	private static CommentaireManager instance;

	public static CommentaireManager getInstance() {
		if (instance == null) {
			instance = new CommentaireManager();
		}

		return instance;
	}

	@SuppressWarnings("unchecked")
	public List<Commentaire> getFromCache(Context context, String codeLigne, String codeSens, String codeArret) {
		List<Commentaire> data = new ArrayList<Commentaire>();
		File cacheFile = new File(context.getCacheDir(), genKey(codeLigne, codeSens, codeArret) + ".timeline");

		if (cacheFile.exists()) {

			ObjectInputStream oos = null;
			try {

				final FileInputStream fis = new FileInputStream(cacheFile);
				oos = new ObjectInputStream(fis);
				data = (List<Commentaire>) oos.readObject();

			} catch (FileNotFoundException e) {
				BugSenseHandler.sendException(e);
			} catch (IOException e) {
				BugSenseHandler.sendException(e);
			} catch (ClassNotFoundException e) {
				BugSenseHandler.sendException(e);
			} finally {
				if (oos != null) {
					try {
						oos.close();
					} catch (IOException e) {
					}
				}
			}

		}

		return data;
	}

	public List<Commentaire> getFromWeb(Context context, String codeLigne, String codeSens, String codeArret,
			BaseDateTime date) throws IOException {
		CommentaireController commentaireController = new CommentaireController();
		List<Commentaire> data = commentaireController.getAll(codeLigne, codeSens, codeArret, date);

		if (data.size() > 0) {
			final String key = genKey(codeLigne, codeSens, codeArret);
			saveToCache(context, key, data);
		}
		return data;
	}

	private void saveToCache(Context context, String key, List<Commentaire> data) {
		ObjectOutputStream oos = null;
		try {

			final FileOutputStream fos = new FileOutputStream(new File(context.getCacheDir(), key + ".timeline"));
			oos = new ObjectOutputStream(fos);
			oos.writeObject(data);
			oos.flush();

		} catch (FileNotFoundException e) {
			BugSenseHandler.sendException(e);
		} catch (IOException e) {
			BugSenseHandler.sendException(e);
		}
		if (oos != null) {
			try {
				oos.close();
			} catch (IOException e) {
			}
		}
	}

	private String genKey(String codeLigne, String codeSens, String codeArret) {
		return codeLigne + codeSens + codeArret;
	}
}
