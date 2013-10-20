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

import java.util.List;

import net.naonedbus.bean.BookmarkGroup;
import net.naonedbus.manager.SQLiteManager;
import net.naonedbus.provider.impl.StopBookmarkGroupLinkProvider;
import net.naonedbus.provider.impl.StopBookmarkGroupProvider;
import net.naonedbus.provider.table.StopBookmarkGroupLinkTable;
import net.naonedbus.provider.table.StopBookmarkGroupTable;
import net.naonedbus.utils.QueryUtils;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class BookmarkGroupManager extends SQLiteManager<BookmarkGroup> {

	private static BookmarkGroupManager instance;

	public static synchronized BookmarkGroupManager getInstance() {
		if (instance == null) {
			instance = new BookmarkGroupManager();
		}
		return instance;
	}

	private BookmarkGroupManager() {
		super(StopBookmarkGroupProvider.CONTENT_URI);
	}

	/**
	 * Récupérer un groupe selon son nom.
	 * 
	 * @param contentResolver
	 * @param nom
	 * @return le groupe
	 */
	public BookmarkGroup getSingle(final ContentResolver contentResolver, final String nom) {
		final Cursor c = getCursor(contentResolver, StopBookmarkGroupTable.GROUP_NAME + "=?", new String[] { nom });
		return getFirstFromCursor(c);
	}

	@Override
	public BookmarkGroup getSingleFromCursor(final Cursor c) {
		final BookmarkGroup groupe = new BookmarkGroup();
		groupe.setId(c.getInt(c.getColumnIndex(StopBookmarkGroupTable._ID)));
		groupe.setName(c.getString(c.getColumnIndex(StopBookmarkGroupTable.GROUP_NAME)));
		groupe.setOrder(c.getInt(c.getColumnIndex(StopBookmarkGroupTable.GROUP_ORDER)));
		return groupe;
	}

	public Cursor getCursor(final ContentResolver contentResolver, final List<Integer> idFavoris) {
		final Uri.Builder builder = StopBookmarkGroupLinkProvider.CONTENT_URI.buildUpon();
		builder.path(StopBookmarkGroupLinkProvider.LINK_BASE_PATH);
		builder.appendQueryParameter(StopBookmarkGroupLinkProvider.QUERY_PARAMETER_IDS, QueryUtils.listToInStatement(idFavoris));

		return contentResolver.query(builder.build(), null, null, null, null);
	}

	public void delete(final ContentResolver contentResolver, final int idGroupe) {
		final Uri.Builder builder = StopBookmarkGroupProvider.CONTENT_URI.buildUpon();
		builder.appendPath(String.valueOf(idGroupe));

		contentResolver.delete(builder.build(), null, null);
	}

	public void update(final ContentResolver contentResolver, final BookmarkGroup groupe) {
		final ContentValues contentValues = getContentValues(groupe);

		contentResolver.update(StopBookmarkGroupProvider.CONTENT_URI, contentValues, StopBookmarkGroupTable._ID + "=?",
				new String[] { String.valueOf(groupe.getId()) });
	}

	public boolean isFavoriAssociated(final ContentResolver contentResolver, final int idGroupe, final int idFavori) {
		boolean result;

		final Cursor c = contentResolver.query(StopBookmarkGroupLinkProvider.CONTENT_URI, null,
				StopBookmarkGroupLinkTable.GROUP_ID + "=? AND " + StopBookmarkGroupLinkTable.STOP_BOOKMARK_ID + "=?",
				new String[] { String.valueOf(idGroupe), String.valueOf(idFavori) }, null);
		result = c.getCount() > 0;
		c.close();

		return result;
	}

	public List<BookmarkGroup> getAll(final ContentResolver contentResolver, final int idFavori) {
		final Uri.Builder builder = StopBookmarkGroupLinkProvider.CONTENT_URI.buildUpon();
		builder.path(StopBookmarkGroupLinkProvider.FAVORI_ID_BASE_PATH);
		builder.appendQueryParameter(StopBookmarkGroupLinkProvider.QUERY_PARAMETER_IDS, String.valueOf(idFavori));

		return getFromCursor(contentResolver.query(builder.build(), null, null, null, null));
	}

	public void addFavoriToGroup(final ContentResolver contentResolver, final int idGroupe, final int idFavori) {
		final ContentValues contentValues = new ContentValues();
		contentValues.put(StopBookmarkGroupLinkTable.GROUP_ID, String.valueOf(idGroupe));
		contentValues.put(StopBookmarkGroupLinkTable.STOP_BOOKMARK_ID, String.valueOf(idFavori));

		contentResolver.insert(StopBookmarkGroupLinkProvider.CONTENT_URI, contentValues);
	}

	public void removeFavoriFromGroup(final ContentResolver contentResolver, final int idGroupe, final int idFavori) {
		final Uri.Builder builder = StopBookmarkGroupLinkProvider.CONTENT_URI.buildUpon();
		builder.appendPath(String.valueOf(idGroupe));
		builder.appendPath(String.valueOf(idFavori));
		contentResolver.delete(builder.build(), null, null);
	}

	public void move(final ContentResolver contentResolver, final Cursor cursor, final int from, final int to) {
		BookmarkGroup groupe;
		final int start = Math.min(from, to);
		final int stop = Math.max(from, to);
		int position = start;

		cursor.moveToPosition(start);
		while (cursor.getPosition() <= stop && !cursor.isAfterLast()) {
			groupe = getSingleFromCursor(cursor);
			groupe.setOrder(position);

			update(contentResolver, groupe);

			cursor.moveToNext();
			position++;
		}

	}

	@Override
	protected ContentValues getContentValues(final BookmarkGroup item) {
		final ContentValues contentValues = new ContentValues();
		contentValues.put(StopBookmarkGroupTable.GROUP_NAME, item.getName());
		contentValues.put(StopBookmarkGroupTable.GROUP_ORDER, item.getOrder());
		return contentValues;
	}

}
