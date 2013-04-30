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
import java.util.List;

import net.naonedbus.bean.Commentaire;
import net.naonedbus.manager.SQLiteManager;
import net.naonedbus.provider.impl.CommentaireProvider;
import net.naonedbus.provider.table.CommentaireTable;
import net.naonedbus.rest.controller.impl.CommentaireController;

import org.joda.time.base.BaseDateTime;
import org.json.JSONException;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class CommentaireManager extends SQLiteManager<Commentaire> {

	private static CommentaireManager sInstance;

	private static final long UP_TO_DATE_DELAY = 300000; // 5 min de cache

	private long mLastUpdateTimestamp = -1;

	private int mColId;
	private int mColCodeLigne;
	private int mColCodeSens;
	private int mColCodeArret;
	private int mColMessage;
	private int mColSource;
	private int mColTimestamp;

	public static synchronized CommentaireManager getInstance() {
		if (sInstance == null) {
			sInstance = new CommentaireManager();
		}

		return sInstance;
	}

	private CommentaireManager() {
		super(CommentaireProvider.CONTENT_URI);
	}

	public boolean isUpToDate() {
		return mLastUpdateTimestamp + UP_TO_DATE_DELAY >= System.currentTimeMillis();
	}

	public List<Commentaire> getAll(final ContentResolver contentResolver, final String codeLigne,
			final String codeSens, final String codeArret, final BaseDateTime date) throws IOException, JSONException {

		List<Commentaire> commentaires = null;

		if (isUpToDate()) {
			commentaires = getFromCache(contentResolver, codeLigne, codeSens, codeArret);
		}

		if (commentaires == null || commentaires.isEmpty()) {
			commentaires = getFromWeb(contentResolver, codeLigne, codeSens, codeArret, date);
		}

		return commentaires;
	}

	public List<Commentaire> getFromCache(final ContentResolver contentResolver, final String codeLigne,
			final String codeSens, final String codeArret) {

		final Uri.Builder builder = CommentaireProvider.CONTENT_URI.buildUpon();
		if (codeLigne != null) {
			builder.appendPath(codeLigne);
			if (codeSens != null) {
				builder.appendPath(codeSens);
				if (codeArret != null) {
					builder.appendPath(codeArret);
				}
			}
		}

		final Cursor c = contentResolver.query(builder.build(), null, null, null, null);
		return getFromCursor(c);
	}

	public List<Commentaire> getFromWeb(final ContentResolver contentResolver, final String codeLigne,
			final String codeSens, final String codeArret, final BaseDateTime date) throws IOException, JSONException {

		final CommentaireController commentaireController = new CommentaireController();
		final List<Commentaire> data = commentaireController.getAll(codeLigne, codeSens, codeArret, date);

		clear(contentResolver, codeLigne, codeSens, codeArret);
		fillDB(contentResolver, data);
		mLastUpdateTimestamp = System.currentTimeMillis();

		return data;
	}

	public void clear(final ContentResolver contentResolver) {
		contentResolver.delete(CommentaireProvider.CONTENT_URI, null, null);
	}

	public void clear(final ContentResolver contentResolver, final String codeLigne, final String codeSens,
			final String codeArret) {

		final Uri.Builder builder = CommentaireProvider.CONTENT_URI.buildUpon();
		if (codeLigne != null) {
			builder.appendPath(codeLigne);
			if (codeSens != null) {
				builder.appendPath(codeSens);
				if (codeArret != null) {
					builder.appendPath(codeArret);
				}
			}
		}

		contentResolver.delete(builder.build(), null, null);
	}

	@Override
	public void onIndexCursor(final Cursor c) {
		mColId = c.getColumnIndex(CommentaireTable._ID);
		mColCodeLigne = c.getColumnIndex(CommentaireTable.CODE_LIGNE);
		mColCodeSens = c.getColumnIndex(CommentaireTable.CODE_SENS);
		mColCodeArret = c.getColumnIndex(CommentaireTable.CODE_ARRET);
		mColMessage = c.getColumnIndex(CommentaireTable.MESSAGE);
		mColSource = c.getColumnIndex(CommentaireTable.SOURCE);
		mColTimestamp = c.getColumnIndex(CommentaireTable.TIMESTAMP);
	}

	@Override
	public Commentaire getSingleFromCursor(final Cursor c) {
		final Commentaire commentaire = new Commentaire();
		commentaire.setId(c.getInt(mColId));
		commentaire.setCodeLigne(c.getString(mColCodeLigne));
		commentaire.setCodeSens(c.getString(mColCodeSens));
		commentaire.setCodeArret(c.getString(mColCodeArret));
		commentaire.setMessage(c.getString(mColMessage));
		commentaire.setSource(c.getString(mColSource));
		commentaire.setTimestamp(c.getLong(mColTimestamp));
		return commentaire;
	}

	private ContentValues getContentValues(final Commentaire commentaire) {
		final ContentValues values = new ContentValues();
		values.put(CommentaireTable.CODE_LIGNE, commentaire.getCodeLigne());
		values.put(CommentaireTable.CODE_SENS, commentaire.getCodeSens());
		values.put(CommentaireTable.CODE_ARRET, commentaire.getCodeArret());
		values.put(CommentaireTable.MESSAGE, commentaire.getMessage());
		values.put(CommentaireTable.SOURCE, commentaire.getSource());
		values.put(CommentaireTable.TIMESTAMP, commentaire.getTimestamp());
		return values;
	}

	private void fillDB(final ContentResolver contentResolver, final List<Commentaire> commentaires) {
		final ContentValues[] values = new ContentValues[commentaires.size()];
		for (int i = 0; i < commentaires.size(); i++) {
			values[i] = getContentValues(commentaires.get(i));
		}

		contentResolver.bulkInsert(CommentaireProvider.CONTENT_URI, values);
	}
}
