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
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.naonedbus.BuildConfig;
import net.naonedbus.bean.Arret;
import net.naonedbus.bean.Favori;
import net.naonedbus.manager.SQLiteManager;
import net.naonedbus.provider.impl.FavoriProvider;
import net.naonedbus.provider.table.ArretTable;
import net.naonedbus.provider.table.EquipementTable;
import net.naonedbus.provider.table.FavoriTable;
import net.naonedbus.provider.table.LigneTable;
import net.naonedbus.provider.table.SensTable;
import net.naonedbus.rest.controller.impl.FavoriController;
import net.naonedbus.utils.ColorUtils;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.util.Log;

import com.bugsense.trace.BugSenseHandler;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;

public class FavoriManager extends SQLiteManager<Favori> {

	private static final String LOG_TAG = "FavoriManager";
	private static final boolean DBG = BuildConfig.DEBUG;

	private List<OnFavoriActionListener> mListeners = new ArrayList<OnFavoriActionListener>();
	private boolean mIsImporting = false;
	private String mRestoredFavoris;

	public static abstract class OnFavoriActionListener {
		public void onImport() {
		};

		public void onAdd(Arret item) {
		};

		public void onRemove(int id) {
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

	private FavoriManager() {
		super(FavoriProvider.CONTENT_URI);
	}

	@Override
	public Favori getSingle(ContentResolver contentResolver, int id) {
		final Cursor c = getCursor(contentResolver, "f." + FavoriTable._ID + " = ?",
				new String[] { String.valueOf(id) });
		try {
			if (c.getCount() > 0) {
				c.moveToFirst();
				return getSingleFromCursor(c);
			} else {
				return null;
			}
		} finally {
			c.close();
		}
	}

	@Override
	public Favori getSingleFromCursor(Cursor c) {
		final Favori item = new Favori();
		item._id = c.getInt(c.getColumnIndex(FavoriTable._ID));
		item.codeLigne = c.getString(c.getColumnIndex(FavoriTable.CODE_LIGNE));
		item.codeEquipement = c.getString(c.getColumnIndex(EquipementTable.CODE));
		item.codeSens = c.getString(c.getColumnIndex(FavoriTable.CODE_SENS));
		item.codeArret = c.getString(c.getColumnIndex(FavoriTable.CODE_ARRET));
		item.nomFavori = c.getString(c.getColumnIndex(FavoriTable.NOM));

		if (c.getColumnIndex(SensTable.NOM) != -1) {
			item.nomSens = c.getString(c.getColumnIndex(SensTable.NOM));
			item.idStation = c.getInt(c.getColumnIndex(ArretTable.ID_STATION));
			item.couleurBackground = c.getInt(c.getColumnIndex(LigneTable.COULEUR));
			item.lettre = c.getString(c.getColumnIndex(LigneTable.LETTRE));
			item.couleurTexte = (ColorUtils.isLightColor(item.couleurBackground)) ? Color.BLACK : Color.WHITE;
		}

		if (c.getColumnIndex(EquipementTable.NOM) != -1) {
			item.nomArret = c.getString(c.getColumnIndex(EquipementTable.NOM));
			item.latitude = c.getFloat(c.getColumnIndex(EquipementTable.LATITUDE));
			item.longitude = c.getFloat(c.getColumnIndex(EquipementTable.LONGITUDE));
		}

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

	public void removeFavori(ContentResolver contentResolver, int id) {
		contentResolver.delete(FavoriProvider.CONTENT_URI, FavoriTable._ID + "=?", new String[] { String.valueOf(id) });

		if (mIsImporting == false)
			notifyOnRemove(id);
	}

	public void setFavori(ContentResolver contentResolver, Favori item) {
		final ContentValues values = getContentValues(item);
		contentResolver.update(FavoriProvider.CONTENT_URI, values, FavoriTable._ID + "=?",
				new String[] { String.valueOf(item._id) });
	}

	/**
	 * getContentValues for Favori
	 * 
	 * @param item
	 * @return a ContentValue filled with Favori values
	 */
	private ContentValues getContentValues(Favori item) {
		ContentValues values = new ContentValues();
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
	private ContentValues getContentValues(Arret item) {
		ContentValues values = new ContentValues();
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
		return toJson(favoris);
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
		return toJson(favoris);
	}

	/**
	 * Renvoyer la liste des favoris au format Json.
	 * 
	 * @param favoris
	 * @return la liste des favoris au format Json
	 */
	public String toJson(final List<Favori> favoris) {
		final StringWriter stringWriter = new StringWriter();
		final JsonWriter writer = new JsonWriter(stringWriter);

		try {
			writer.beginArray();
			for (Favori favoriItem : favoris) {
				writer.beginObject();
				writer.name("codeArret").value(favoriItem.codeArret);
				writer.name("codeSens").value(favoriItem.codeSens);
				writer.name("codeLigne").value(favoriItem.codeLigne);
				writer.name("nomFavori").value(favoriItem.nomFavori);
				writer.endObject();
			}
			writer.endArray();
			writer.close();
		} catch (IOException e) {
			BugSenseHandler.sendExceptionMessage("Erreur lors de la conversion Json des favoris", null, e);
		}

		return stringWriter.toString();
	}

	/**
	 * Remplacer les favoris par ceux fournis en Json
	 * 
	 * @param contentResolver
	 * @param json
	 */
	public void fromJson(ContentResolver contentResolver, String json) {
		final Type type = new TypeToken<ArrayList<HashMap<String, String>>>() {
		}.getType();
		final Gson gson = new Gson();
		final ArrayList<HashMap<String, String>> favoris = gson.fromJson(json, type);

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

		final Type type = new TypeToken<ArrayList<HashMap<String, String>>>() {
		}.getType();
		final Gson gson = new Gson();
		final ArrayList<HashMap<String, String>> favoris = gson.fromJson(json, type);

		fromList(db, favoris);
	}

	/**
	 * Remplacer les favoris par ceux de la liste
	 * 
	 * @param contentResolver
	 * @param favoris
	 */
	private void fromList(final SQLiteDatabase db, final List<HashMap<String, String>> favoris) {
		if (DBG)
			Log.d(LOG_TAG, "fromList");

		Integer itemId;
		final ArretManager arretManager = ArretManager.getInstance();

		// Delete old items
		db.delete(FavoriTable.TABLE_NAME, null, null);

		// Add new items
		for (HashMap<String, String> element : favoris) {
			final Favori favoriItem = toFavori(element);

			itemId = arretManager.getIdByFavori(db, favoriItem);
			if (itemId != null) {
				favoriItem._id = itemId;
				addFavori(db, favoriItem);
			}
		}
	}

	/**
	 * Remplacer les favoris par ceux de la liste
	 * 
	 * @param contentResolver
	 * @param favoris
	 */
	private void fromList(ContentResolver contentResolver, List<HashMap<String, String>> favoris) {
		Integer itemId;
		final ArretManager arretManager = ArretManager.getInstance();

		// Delete old items
		contentResolver.delete(FavoriProvider.CONTENT_URI, null, null);

		// Add new items
		for (HashMap<String, String> element : favoris) {
			final Favori favoriItem = toFavori(element);

			itemId = arretManager.getIdByFavori(contentResolver, favoriItem);
			if (itemId != null) {
				favoriItem._id = itemId;
				addFavori(contentResolver, favoriItem);
			}
		}
	}

	/**
	 * Remplacer les favoris par ceux de la liste
	 * 
	 * @param contentResolver
	 * @param favoris
	 */
	private void fromListFavoris(ContentResolver contentResolver, List<Favori> favoris) {
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
	 * Convertir une map d'éléments en Favori.
	 * 
	 * @param element
	 * @return un Favori
	 */
	private Favori toFavori(final HashMap<String, String> element) {
		final Favori favoriItem = new Favori();
		favoriItem.codeArret = element.get("codeArret");
		favoriItem.codeSens = element.get("codeSens");
		favoriItem.codeLigne = element.get("codeLigne");
		favoriItem.nomFavori = element.get("nomFavori");
		return favoriItem;
	}

	/**
	 * Importer les favoris depuis le cloud
	 * 
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	public void importFavoris(ContentResolver contentResolver, String id) throws JsonParseException, IOException {
		final FavoriController controller = new FavoriController();
		final List<Favori> favoris = controller.get(id);

		mIsImporting = true;

		fromListFavoris(contentResolver, favoris);

		mIsImporting = false;

		notifyOnImport();
	}

	public void addActionListener(OnFavoriActionListener l) {
		mListeners.add(l);
	}

	public void removeActionListener(OnFavoriActionListener l) {
		mListeners.remove(l);
	}

	public void setRestoredFavoris(String values) {
		mRestoredFavoris = values;
	}

	public String getRestoredFavoris() {
		return mRestoredFavoris;
	}

	private void notifyOnAdd(Arret item) {
		for (OnFavoriActionListener l : mListeners) {
			l.onAdd(item);
			l.onUpdate();
		}
	}

	private void notifyOnRemove(int id) {
		for (OnFavoriActionListener l : mListeners) {
			l.onRemove(id);
			l.onUpdate();
		}
	}

	private void notifyOnImport() {
		for (OnFavoriActionListener l : mListeners) {
			l.onImport();
			l.onUpdate();
		}
	}
}
