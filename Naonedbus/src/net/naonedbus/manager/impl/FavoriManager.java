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
import java.util.List;

import net.naonedbus.BuildConfig;
import net.naonedbus.bean.Arret;
import net.naonedbus.bean.Favori;
import net.naonedbus.manager.SQLiteManager;
import net.naonedbus.provider.impl.FavoriProvider;
import net.naonedbus.provider.table.FavoriTable;
import net.naonedbus.rest.controller.impl.FavoriController;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class FavoriManager extends SQLiteManager<Favori> {

	private static final String LOG_TAG = "FavoriManager";
	private static final boolean DBG = BuildConfig.DEBUG;

	private final List<OnFavoriActionListener> mListeners = new ArrayList<OnFavoriActionListener>();
	private boolean mIsImporting = false;
	private String mRestoredFavoris;

	public static abstract class OnFavoriActionListener {
		public void onImport() {
		};

		public void onAdd(final Arret item) {
		};

		public void onRemove(final int id) {
		};

		public void onUpdate() {
		};
	}

	private static FavoriManager instance;

	public static synchronized FavoriManager getInstance() {
		if (instance == null) {
			instance = new FavoriManager();
		}
		return instance;
	}

	protected FavoriManager() {
		super(FavoriProvider.CONTENT_URI);
	}

	@Override
	public Favori getSingleFromCursor(final Cursor c) {
		final Favori item = new Favori();
		item._id = c.getInt(c.getColumnIndex(FavoriTable._ID));
		item.codeLigne = c.getString(c.getColumnIndex(FavoriTable.CODE_LIGNE));
		item.codeSens = c.getString(c.getColumnIndex(FavoriTable.CODE_SENS));
		item.codeArret = c.getString(c.getColumnIndex(FavoriTable.CODE_ARRET));
		item.nomFavori = c.getString(c.getColumnIndex(FavoriTable.NOM));
		return item;
	}

	public void addFavori(final ContentResolver contentResolver, final Arret item) {
		final ContentValues values = getContentValues(item);
		contentResolver.insert(FavoriProvider.CONTENT_URI, values);

		if (mIsImporting == false)
			notifyOnAdd(item);
	}

	public void addFavori(final ContentResolver contentResolver, final Favori item) {
		final ContentValues values = getContentValues(item);
		contentResolver.insert(FavoriProvider.CONTENT_URI, values);

		if (mIsImporting == false)
			notifyOnAdd(item);
	}

	public void addFavori(final SQLiteDatabase db, final Favori item) {
		final ContentValues values = getContentValues(item);
		db.insert(FavoriTable.TABLE_NAME, null, values);

		if (mIsImporting == false)
			notifyOnAdd(item);
	}

	public void removeFavori(final ContentResolver contentResolver, final int id) {
		contentResolver.delete(FavoriProvider.CONTENT_URI, FavoriTable._ID + "=?", new String[] { String.valueOf(id) });

		if (mIsImporting == false)
			notifyOnRemove(id);
	}

	public void setFavori(final ContentResolver contentResolver, final Favori item) {
		final ContentValues values = getContentValues(item);
		contentResolver.update(FavoriProvider.CONTENT_URI, values, FavoriTable._ID + "=?",
				new String[] { String.valueOf(item._id) });
	}

	public boolean isFavori(final ContentResolver contentResolver, final int arretId) {
		return getSingle(contentResolver, arretId) != null;
	}

	/**
	 * getContentValues for Favori
	 * 
	 * @param item
	 * @return a ContentValue filled with Favori values
	 */
	private ContentValues getContentValues(final Favori item) {
		final ContentValues values = new ContentValues();
		values.put(FavoriTable._ID, item._id);
		values.put(FavoriTable.CODE_LIGNE, item.codeLigne);
		values.put(FavoriTable.CODE_SENS, item.codeSens);
		values.put(FavoriTable.CODE_ARRET, item.codeArret);
		values.put(FavoriTable.NOM, item.nomFavori);

		return values;
	}

	/**
	 * getContentValues for ArretItem
	 * 
	 * @param item
	 * @return a ContentValue filled with ArretItem values, for a FavoriTable
	 *         structure
	 */
	private ContentValues getContentValues(final Arret item) {
		final ContentValues values = new ContentValues();
		values.put(FavoriTable._ID, item._id);
		values.put(FavoriTable.CODE_LIGNE, item.codeLigne);
		values.put(FavoriTable.CODE_SENS, item.codeSens);
		values.put(FavoriTable.CODE_ARRET, item.codeArret);
		return values;
	}

	/**
	 * Renvoyer la liste des favoris au format Json.
	 * 
	 * @return la liste des favoris au format Json
	 */
	public String toJson(final ContentResolver contentResolver) {
		final List<Favori> favoris = getAll(contentResolver, null, null);
		final FavoriController controller = new FavoriController();
		return controller.toJson(favoris);
	}

	/**
	 * Renvoyer la liste des favoris sous forme simple (uniquement la table
	 * Favoris) au format Json.
	 * 
	 * @return la liste des favoris au format Json
	 */
	public String toJsonSimple(final ContentResolver contentResolver) {
		final Cursor c = contentResolver.query(FavoriProvider.CONTENT_URI, FavoriTable.PROJECTION, null, null, null);
		final List<Favori> favoris = getFromCursor(c);
		final FavoriController controller = new FavoriController();
		return controller.toJson(favoris);
	}

	/**
	 * Remplacer les favoris par ceux fournis en Json
	 * 
	 * @param contentResolver
	 * @param json
	 */
	public void fromJson(final ContentResolver contentResolver, final String json) {
		final FavoriController controller = new FavoriController();
		final List<Favori> favoris = controller.parseJsonArray(json);
		fromList(contentResolver, favoris);
	}

	/**
	 * Remplacer les favoris par ceux fournis en Json
	 * 
	 * @param contentResolver
	 * @param json
	 */
	public void fromJson(final SQLiteDatabase db, final String json) {
		if (DBG)
			Log.d(LOG_TAG, "fromJson : " + json);

		final FavoriController controller = new FavoriController();
		final List<Favori> favoris = controller.parseJsonArray(json);

		fromList(db, favoris);
	}

	/**
	 * Remplacer les favoris par ceux de la liste
	 * 
	 * @param contentResolver
	 * @param favoris
	 */
	private void fromList(final SQLiteDatabase db, final List<Favori> favoris) {
		if (DBG)
			Log.d(LOG_TAG, "fromList");

		Integer itemId;
		final ArretManager arretManager = ArretManager.getInstance();

		// Delete old items
		db.delete(FavoriTable.TABLE_NAME, null, null);

		// Add new items
		for (final Favori favori : favoris) {
			itemId = arretManager.getIdByFavori(db, favori);
			if (itemId != null) {
				favori._id = itemId;
				addFavori(db, favori);
			}
		}
	}

	/**
	 * Remplacer les favoris par ceux de la liste
	 * 
	 * @param contentResolver
	 * @param favoris
	 */
	private void fromList(final ContentResolver contentResolver, final List<Favori> favoris) {
		Integer itemId;
		final ArretManager arretManager = ArretManager.getInstance();

		// Delete old items
		contentResolver.delete(FavoriProvider.CONTENT_URI, null, null);

		// Add new items
		for (final Favori favori : favoris) {
			itemId = arretManager.getIdByFavori(contentResolver, favori);
			if (itemId != null) {
				favori._id = itemId;
				addFavori(contentResolver, favori);
			}
		}
	}

	/**
	 * Remplacer les favoris par ceux de la liste
	 * 
	 * @param contentResolver
	 * @param favoris
	 */
	private void fromListFavoris(final ContentResolver contentResolver, final List<Favori> favoris) {
		Integer itemId;
		final ArretManager arretManager = ArretManager.getInstance();

		// Delete old items
		contentResolver.delete(FavoriProvider.CONTENT_URI, null, null);

		// Add new items
		for (final Favori favori : favoris) {
			itemId = arretManager.getIdByFavori(contentResolver, favori);

			if (itemId != null) {
				favori._id = itemId;
				addFavori(contentResolver, favori);
			}
		}
	}

	/**
	 * Importer les favoris depuis le cloud
	 * 
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	public void importFavoris(final ContentResolver contentResolver, final String id) throws IOException {
		final FavoriController controller = new FavoriController();
		final List<Favori> favoris = controller.get(id);

		mIsImporting = true;

		fromListFavoris(contentResolver, favoris);

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

	private void notifyOnAdd(final Arret item) {
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
