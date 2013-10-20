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

import net.naonedbus.bean.Comment;
import net.naonedbus.manager.SQLiteManager;
import net.naonedbus.provider.impl.CommentProvider;
import net.naonedbus.provider.table.CommentTable;
import net.naonedbus.rest.controller.impl.CommentaireController;

import org.json.JSONException;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class CommentManager extends SQLiteManager<Comment> {

	private static CommentManager sInstance;

	private static final long UP_TO_DATE_DELAY = 300000; // 5 min de cache

	private long mLastUpdateTimestamp = -1;

	private int mColId;
	private int mColCodeLigne;
	private int mColCodeSens;
	private int mColCodeArret;
	private int mColMessage;
	private int mColSource;
	private int mColTimestamp;

	public static synchronized CommentManager getInstance() {
		if (sInstance == null) {
			sInstance = new CommentManager();
		}

		return sInstance;
	}

	private CommentManager() {
		super(CommentProvider.CONTENT_URI);
	}

	public boolean isUpToDate() {
		return mLastUpdateTimestamp + UP_TO_DATE_DELAY >= System.currentTimeMillis();
	}

	public List<Comment> getAll(final ContentResolver contentResolver, final String codeLigne,
			final String codeSens, final String codeArret) {

		final Uri.Builder builder = CommentProvider.CONTENT_URI.buildUpon();
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

	public void updateCache(final ContentResolver contentResolver) throws IOException, JSONException {
		final CommentaireController commentaireController = new CommentaireController();

		try {
			final List<Comment> data = commentaireController.getAll(null, null, null);
			clear(contentResolver);
			fillDB(contentResolver, data);
		} finally {
			mLastUpdateTimestamp = System.currentTimeMillis();
		}

	}

	public void clear(final ContentResolver contentResolver) {
		contentResolver.delete(CommentProvider.CONTENT_URI, null, null);
	}

	@Override
	public void onIndexCursor(final Cursor c) {
		mColId = c.getColumnIndex(CommentTable._ID);
		mColCodeLigne = c.getColumnIndex(CommentTable.ROUTE_CODE);
		mColCodeSens = c.getColumnIndex(CommentTable.DIRECTION_CODE);
		mColCodeArret = c.getColumnIndex(CommentTable.STOP_CODE);
		mColMessage = c.getColumnIndex(CommentTable.MESSAGE);
		mColSource = c.getColumnIndex(CommentTable.SOURCE);
		mColTimestamp = c.getColumnIndex(CommentTable.TIMESTAMP);
	}

	@Override
	public Comment getSingleFromCursor(final Cursor c) {
		final Comment commentaire = new Comment();
		commentaire.setId(c.getInt(mColId));
		commentaire.setCodeLigne(c.getString(mColCodeLigne));
		commentaire.setCodeSens(c.getString(mColCodeSens));
		commentaire.setCodeArret(c.getString(mColCodeArret));
		commentaire.setMessage(c.getString(mColMessage));
		commentaire.setSource(c.getString(mColSource));
		commentaire.setTimestamp(c.getLong(mColTimestamp));
		return commentaire;
	}

	@Override
	protected ContentValues getContentValues(final Comment commentaire) {
		final ContentValues values = new ContentValues();
		values.put(CommentTable.ROUTE_CODE, commentaire.getCodeLigne());
		values.put(CommentTable.DIRECTION_CODE, commentaire.getCodeSens());
		values.put(CommentTable.STOP_CODE, commentaire.getCodeArret());
		values.put(CommentTable.MESSAGE, commentaire.getMessage());
		values.put(CommentTable.SOURCE, commentaire.getSource());
		values.put(CommentTable.TIMESTAMP, commentaire.getTimestamp());
		return values;
	}

	private void fillDB(final ContentResolver contentResolver, final List<Comment> commentaires) {
		final ContentValues[] values = new ContentValues[commentaires.size()];
		for (int i = 0; i < commentaires.size(); i++) {
			values[i] = getContentValues(commentaires.get(i));
		}

		contentResolver.bulkInsert(CommentProvider.CONTENT_URI, values);
	}
}
