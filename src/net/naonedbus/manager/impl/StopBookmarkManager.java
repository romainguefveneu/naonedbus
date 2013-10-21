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
import java.util.List;

import net.naonedbus.bean.Stop;
import net.naonedbus.bean.StopBookmark;
import net.naonedbus.bean.BookmarkGroup;
import net.naonedbus.manager.SQLiteManager;
import net.naonedbus.provider.impl.StopBookmarkProvider;
import net.naonedbus.provider.impl.StopBookmarkGroupProvider;
import net.naonedbus.provider.table.EquipmentTable;
import net.naonedbus.provider.table.StopBookmarkTable;
import net.naonedbus.provider.table.RouteTable;
import net.naonedbus.provider.table.DirectionTable;
import net.naonedbus.rest.container.FavoriContainer;
import net.naonedbus.rest.controller.impl.FavoriController;

import org.json.JSONException;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.SparseIntArray;

import com.google.gson.JsonParseException;

public class StopBookmarkManager extends SQLiteManager<StopBookmark> {

	private final List<OnFavoriActionListener> mListeners = new ArrayList<OnFavoriActionListener>();
	private boolean mIsImporting = false;
	private String mRestoredFavoris;

	public static abstract class OnFavoriActionListener {
		public void onImport() {
		};

		public void onAdd(final Stop item) {
		};

		public void onRemove(final int id) {
		};

		public void onUpdate() {
		};
	}

	private static StopBookmarkManager instance;

	private final StopBookmark.Builder mBuilder;

	public static synchronized StopBookmarkManager getInstance() {
		if (instance == null) {
			instance = new StopBookmarkManager();
		}
		return instance;
	}

	protected StopBookmarkManager() {
		super(StopBookmarkProvider.CONTENT_URI);
		mBuilder = new StopBookmark.Builder();
	}

	/**
	 * @param contentResolver
	 * @return les favoris avec les données sur la route et le direction
	 */
	public List<StopBookmark> getFull(final ContentResolver contentResolver) {
		final Uri.Builder builder = StopBookmarkProvider.CONTENT_URI.buildUpon();
		builder.path(StopBookmarkProvider.FAVORIS_FULL_URI_PATH_QUERY);

		return getFromCursorFull(contentResolver.query(builder.build(), null, null, null, null));
	}

	/**
	 * Transformer un curseur en liste d'éléments
	 * 
	 * @param c
	 *            un Curseur
	 * @return une liste d'éléments
	 */
	protected List<StopBookmark> getFromCursorFull(final Cursor c) {
		final List<StopBookmark> items = new ArrayList<StopBookmark>();
		if (c.getCount() > 0) {
			c.moveToFirst();
			while (!c.isAfterLast()) {
				items.add(getSingleFromCursorFull(c));
				c.moveToNext();
			}
		}
		c.close();
		return items;
	}

	public StopBookmark getSingleFromCursorFull(final Cursor c) {
		mBuilder.setId(c.getInt(c.getColumnIndex(StopBookmarkTable._ID)));
		mBuilder.setCodeLigne(c.getString(c.getColumnIndex(StopBookmarkTable.ROUTE_CODE)));
		mBuilder.setCodeSens(c.getString(c.getColumnIndex(StopBookmarkTable.DIRECTION_CODE)));
		mBuilder.setCodeArret(c.getString(c.getColumnIndex(StopBookmarkTable.STOP_CODE)));
		mBuilder.setBookmarkName(c.getString(c.getColumnIndex(StopBookmarkTable.BOOKMARK_NAME)));
		mBuilder.setNomArret(c.getString(c.getColumnIndex(EquipmentTable.EQUIPMENT_NAME)));

		final int couleurBackground = c.getInt(c.getColumnIndex(RouteTable.BACK_COLOR));
		final int couleurFront = c.getInt(c.getColumnIndex(RouteTable.FRONT_COLOR));
		mBuilder.setBackColor(couleurBackground);
		mBuilder.setFrontColor(couleurFront);

		mBuilder.setDirectionName(c.getString(c.getColumnIndex(DirectionTable.DIRECTION_NAME)));
		mBuilder.setLettre(c.getString(c.getColumnIndex(RouteTable.LETTER)));
		return mBuilder.build();
	}

	@Override
	public StopBookmark getSingleFromCursor(final Cursor c) {
		mBuilder.setId(c.getInt(c.getColumnIndex(StopBookmarkTable._ID)));
		mBuilder.setCodeLigne(c.getString(c.getColumnIndex(StopBookmarkTable.ROUTE_CODE)));
		mBuilder.setCodeSens(c.getString(c.getColumnIndex(StopBookmarkTable.DIRECTION_CODE)));
		mBuilder.setCodeArret(c.getString(c.getColumnIndex(StopBookmarkTable.STOP_CODE)));
		mBuilder.setNomArret(c.getString(c.getColumnIndex(StopBookmarkTable.BOOKMARK_NAME)));

		mBuilder.setBookmarkName(null);
		mBuilder.setBackColor(0);
		mBuilder.setFrontColor(0);
		mBuilder.setDirectionName(null);
		mBuilder.setLettre(null);

		return mBuilder.build();
	}

	public void addFavori(final ContentResolver contentResolver, final Stop item) {
		final ContentValues values = getContentValues(item);
		contentResolver.insert(StopBookmarkProvider.CONTENT_URI, values);

		if (mIsImporting == false)
			notifyOnAdd(item);
	}

	public void addFavori(final ContentResolver contentResolver, final StopBookmark item) {
		final ContentValues values = getContentValues(item);
		contentResolver.insert(StopBookmarkProvider.CONTENT_URI, values);

		if (mIsImporting == false)
			notifyOnAdd(item);
	}

	public void addFavori(final SQLiteDatabase db, final StopBookmark item) {
		final ContentValues values = getContentValues(item);
		db.insert(StopBookmarkTable.TABLE_NAME, null, values);

		if (mIsImporting == false)
			notifyOnAdd(item);
	}

	public void removeFavori(final ContentResolver contentResolver, final int id) {
		contentResolver.delete(StopBookmarkProvider.CONTENT_URI, StopBookmarkTable._ID + "=?", new String[] { String.valueOf(id) });

		if (mIsImporting == false)
			notifyOnRemove(id);
	}

	public void setFavori(final ContentResolver contentResolver, final StopBookmark item) {
		final ContentValues values = getContentValues(item);
		contentResolver.update(StopBookmarkProvider.CONTENT_URI, values, StopBookmarkTable._ID + "=?",
				new String[] { String.valueOf(item.getId()) });
	}

	public boolean isFavori(final ContentResolver contentResolver, final int arretId) {
		return getSingle(contentResolver, arretId) != null;
	}

	@Override
	protected ContentValues getContentValues(final StopBookmark item) {
		final ContentValues values = new ContentValues();
		values.put(StopBookmarkTable._ID, item.getId());
		values.put(StopBookmarkTable.ROUTE_CODE, item.getCodeLigne());
		values.put(StopBookmarkTable.DIRECTION_CODE, item.getCodeSens());
		values.put(StopBookmarkTable.STOP_CODE, item.getCodeArret());
		values.put(StopBookmarkTable.BOOKMARK_NAME, item.getBookmarkName());

		return values;
	}

	/**
	 * getContentValues for ArretItem
	 * 
	 * @param item
	 * @return a ContentValue filled with ArretItem values, for a FavoriTable
	 *         structure
	 */
	private ContentValues getContentValues(final Stop item) {
		final ContentValues values = new ContentValues();
		values.put(StopBookmarkTable._ID, item.getId());
		values.put(StopBookmarkTable.ROUTE_CODE, item.getCodeLigne());
		values.put(StopBookmarkTable.DIRECTION_CODE, item.getCodeSens());
		values.put(StopBookmarkTable.STOP_CODE, item.getCodeArret());
		return values;
	}

	/**
	 * Renvoyer la liste des favoris au format Json.
	 * 
	 * @return la liste des favoris au format Json
	 */
	public String toJson(final ContentResolver contentResolver) {
		final BookmarkGroupManager groupeManager = BookmarkGroupManager.getInstance();
		final List<StopBookmark> favoris = getAll(contentResolver, null, null);
		final List<BookmarkGroup> groupes = groupeManager.getAll(contentResolver);

		final FavoriContainer container = new FavoriContainer();
		for (final BookmarkGroup groupe : groupes) {
			container.addGroupe(groupe.getId(), groupe.getName(), groupe.getOrder());
		}
		for (final StopBookmark favori : favoris) {
			final List<BookmarkGroup> favoriGroupes = groupeManager.getAll(contentResolver, favori.getId());
			final List<Integer> idGroupes = new ArrayList<Integer>();
			for (final BookmarkGroup groupe : favoriGroupes) {
				idGroupes.add(groupe.getId());
			}
			container.addFavori(favori.getCodeLigne(), favori.getCodeSens(), favori.getCodeArret(),
					favori.getBookmarkName(), idGroupes);
		}

		final FavoriController controller = new FavoriController();
		return controller.toJson(container);
	}

	/**
	 * Remplacer les favoris par ceux fournis en Json
	 * 
	 * @param contentResolver
	 * @param json
	 * @throws JSONException
	 */
	public void fromJson(final ContentResolver contentResolver, final String json) throws JSONException {
		final FavoriController controller = new FavoriController();
		final FavoriContainer favoris = controller.parseJsonObject(json);
		fromList(contentResolver, favoris);
	}

	/**
	 * Remplacer les favoris par ceux de la liste
	 * 
	 * @param contentResolver
	 * @param container
	 */
	private void fromList(final ContentResolver contentResolver, final FavoriContainer container) {
		final StopManager arretManager = StopManager.getInstance();
		final BookmarkGroupManager groupeManager = BookmarkGroupManager.getInstance();
		final SparseIntArray groupeMapping = new SparseIntArray();

		// Delete old items
		contentResolver.delete(StopBookmarkProvider.CONTENT_URI, null, null);
		contentResolver.delete(StopBookmarkGroupProvider.CONTENT_URI, null, null);

		// Add new items
		for (final net.naonedbus.rest.container.FavoriContainer.Groupe g : container.groupes) {
			final BookmarkGroup groupe = new BookmarkGroup();
			groupe.setName(g.nom);
			groupe.setOrder(g.ordre);

			final int idLocal = groupeManager.add(contentResolver, groupe).intValue();
			groupeMapping.put(g.id, idLocal);
		}

		Integer itemId;
		final StopBookmark.Builder builder = new StopBookmark.Builder();
		for (final net.naonedbus.rest.container.FavoriContainer.Favori f : container.favoris) {
			builder.setCodeArret(f.codeArret);
			builder.setCodeSens(f.directionCode);
			builder.setCodeLigne(f.routeCode);
			builder.setBookmarkName(f.nomFavori);

			itemId = arretManager.getIdByFavori(contentResolver, builder.build());
			if (itemId != null) {
				builder.setId(itemId);
				addFavori(contentResolver, builder.build());

				// Associer aux groupes
				final List<Integer> favoriGroupes = f.idGroupes;
				if (favoriGroupes != null)
					for (final Integer idGroupe : favoriGroupes) {
						if (groupeMapping.indexOfKey(idGroupe) > -1) {
							groupeManager.addFavoriToGroup(contentResolver, groupeMapping.get(idGroupe), itemId);
						}
					}
			}
		}
	}

	/**
	 * Importer les favoris depuis le cloud
	 * 
	 * @throws IOException
	 * @throws JSONException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	public void importFavoris(final ContentResolver contentResolver, final String id) throws IOException, JSONException {
		final FavoriController controller = new FavoriController();
		final FavoriContainer favoris = controller.get(id);

		mIsImporting = true;

		fromList(contentResolver, favoris);

		mIsImporting = false;

		notifyOnImport();
	}

	public void addActionListener(final OnFavoriActionListener l) {
		mListeners.add(l);
	}

	public void removeActionListener(final OnFavoriActionListener l) {
		mListeners.remove(l);
	}

	public void setRestoredFavoris(final String values) {
		mRestoredFavoris = values;
	}

	public String getRestoredFavoris() {
		return mRestoredFavoris;
	}

	private void notifyOnAdd(final Stop item) {
		for (final OnFavoriActionListener l : mListeners) {
			l.onAdd(item);
			l.onUpdate();
		}
	}

	private void notifyOnRemove(final int id) {
		for (final OnFavoriActionListener l : mListeners) {
			l.onRemove(id);
			l.onUpdate();
		}
	}

	private void notifyOnImport() {
		for (final OnFavoriActionListener l : mListeners) {
			l.onImport();
			l.onUpdate();
		}
	}
}
