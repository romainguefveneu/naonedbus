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

import net.naonedbus.bean.Arret;
import net.naonedbus.bean.Favori;
import net.naonedbus.bean.Groupe;
import net.naonedbus.manager.SQLiteManager;
import net.naonedbus.provider.impl.FavoriProvider;
import net.naonedbus.provider.impl.GroupeProvider;
import net.naonedbus.provider.table.EquipementTable;
import net.naonedbus.provider.table.FavoriTable;
import net.naonedbus.provider.table.LigneTable;
import net.naonedbus.provider.table.SensTable;
import net.naonedbus.rest.container.FavoriContainer;
import net.naonedbus.rest.controller.impl.FavoriController;
import net.naonedbus.utils.ColorUtils;

import org.json.JSONException;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.util.SparseIntArray;

public class FavoriManager extends SQLiteManager<Favori> {

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

	/**
	 * @param contentResolver
	 * @return les favoris avec les données sur la ligne et le sens
	 */
	public List<Favori> getFull(final ContentResolver contentResolver) {
		final Uri.Builder builder = FavoriProvider.CONTENT_URI.buildUpon();
		builder.path(FavoriProvider.FAVORIS_FULL_URI_PATH_QUERY);

		return getFromCursorFull(contentResolver.query(builder.build(), null, null, null, null));
	}

	/**
	 * Transformer un curseur en liste d'éléments
	 * 
	 * @param c
	 *            un Curseur
	 * @return une liste d'éléments
	 */
	protected List<Favori> getFromCursorFull(final Cursor c) {
		final List<Favori> items = new ArrayList<Favori>();
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

	public Favori getSingleFromCursorFull(final Cursor c) {
		final Favori item = new Favori();
		item._id = c.getInt(c.getColumnIndex(FavoriTable._ID));
		item.codeLigne = c.getString(c.getColumnIndex(FavoriTable.CODE_LIGNE));
		item.codeSens = c.getString(c.getColumnIndex(FavoriTable.CODE_SENS));
		item.codeArret = c.getString(c.getColumnIndex(FavoriTable.CODE_ARRET));
		item.nomFavori = c.getString(c.getColumnIndex(FavoriTable.NOM));
		item.nomArret = c.getString(c.getColumnIndex(EquipementTable.NOM));
		item.couleurBackground = c.getInt(c.getColumnIndex(LigneTable.COULEUR));
		item.couleurTexte = ColorUtils.isLightColor(item.couleurBackground) ? Color.BLACK : Color.WHITE;

		item.nomSens = c.getString(c.getColumnIndex(SensTable.NOM));
		item.lettre = c.getString(c.getColumnIndex(LigneTable.LETTRE));
		return item;
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

	@Override
	protected ContentValues getContentValues(final Favori item) {
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
		final GroupeManager groupeManager = GroupeManager.getInstance();
		final List<Favori> favoris = getAll(contentResolver, null, null);
		final List<Groupe> groupes = groupeManager.getAll(contentResolver);

		final FavoriContainer container = new FavoriContainer();
		for (final Groupe groupe : groupes) {
			container.addGroupe(groupe.getId(), groupe.getNom(), groupe.getOrdre());
		}
		for (final Favori favori : favoris) {
			final List<Groupe> favoriGroupes = groupeManager.getAll(contentResolver, favori._id);
			final List<Integer> idGroupes = new ArrayList<Integer>();
			for (final Groupe groupe : favoriGroupes) {
				idGroupes.add(groupe.getId());
			}
			container.addFavori(favori.codeLigne, favori.codeSens, favori.codeArret, favori.nomFavori, idGroupes);
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
		final ArretManager arretManager = ArretManager.getInstance();
		final GroupeManager groupeManager = GroupeManager.getInstance();
		final SparseIntArray groupeMapping = new SparseIntArray();

		// Delete old items
		contentResolver.delete(FavoriProvider.CONTENT_URI, null, null);
		contentResolver.delete(GroupeProvider.CONTENT_URI, null, null);

		// Add new items
		for (final net.naonedbus.rest.container.FavoriContainer.Groupe g : container.groupes) {
			final Groupe groupe = new Groupe();
			groupe.setNom(g.nom);
			groupe.setOrdre(g.ordre);

			final int idLocal = groupeManager.add(contentResolver, groupe).intValue();

			groupeMapping.put(g.id, idLocal);
		}
		for (final net.naonedbus.rest.container.FavoriContainer.Favori f : container.favoris) {

			Integer itemId;

			final Favori favori = new Favori();
			favori.codeArret = f.codeArret;
			favori.codeSens = f.codeSens;
			favori.codeLigne = f.codeLigne;
			favori.nomFavori = f.nomFavori;

			itemId = arretManager.getIdByFavori(contentResolver, favori);
			if (itemId != null) {
				favori._id = itemId;
				addFavori(contentResolver, favori);
			}

			// Associer aux groupes
			final List<Integer> favoriGroupes = f.idGroupes;
			if (favoriGroupes != null)
				for (final Integer idGroupe : favoriGroupes) {
					if (groupeMapping.indexOfKey(idGroupe) > -1) {
						groupeManager.addFavoriToGroup(contentResolver, groupeMapping.get(idGroupe), favori._id);
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
