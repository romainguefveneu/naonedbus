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

import net.naonedbus.bean.Favori;
import net.naonedbus.manager.SQLiteManager;
import net.naonedbus.provider.impl.FavorisViewProvider;
import net.naonedbus.provider.table.FavoriViewTable;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.utils.QueryUtils;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;

public class FavorisViewManager extends SQLiteManager<Favori> {

	private static FavorisViewManager instance;

	private final Favori.Builder mBuilder;

	public static synchronized FavorisViewManager getInstance() {
		if (instance == null) {
			instance = new FavorisViewManager();
		}
		return instance;
	}

	protected FavorisViewManager() {
		super(FavorisViewProvider.CONTENT_URI);
		mBuilder = new Favori.Builder();
	}

	/**
	 * Récupérer tous les favoris d'un ensemble de groupe.
	 * 
	 * @param contentResolver
	 * @return la liste de tous les favoris appartenant à un des groupes
	 */
	public List<Favori> getAll(final ContentResolver contentResolver, final List<Integer> idGroupes) {
		final Uri.Builder builder = FavorisViewProvider.CONTENT_URI.buildUpon();
		builder.path(FavorisViewProvider.FAVORIS_GROUPES_URI_PATH_QUERY);
		builder.appendQueryParameter(FavorisViewProvider.QUERY_PARAMETER_GROUPES_IDS,
				QueryUtils.listToInStatement(idGroupes));

		return getFromCursor(contentResolver.query(builder.build(), null, null, null, null));
	}

	public List<Favori> getUnique(final ContentResolver contentResolver) {
		final Uri.Builder builder = FavorisViewProvider.CONTENT_URI.buildUpon();
		return getFromCursor(contentResolver.query(builder.build(), null, null, null, null));
	}

	@Override
	public Favori getSingleFromCursor(final Cursor c) {

		mBuilder.setId(c.getInt(c.getColumnIndex(FavoriViewTable._ID)));
		mBuilder.setCodeLigne(c.getString(c.getColumnIndex(FavoriViewTable.CODE_LIGNE)));
		mBuilder.setCodeEquipement(c.getString(c.getColumnIndex(FavoriViewTable.CODE_EQUIPEMENT)));
		mBuilder.setCodeSens(c.getString(c.getColumnIndex(FavoriViewTable.CODE_SENS)));
		mBuilder.setCodeArret(c.getString(c.getColumnIndex(FavoriViewTable.CODE_ARRET)));
		mBuilder.setNomFavori(c.getString(c.getColumnIndex(FavoriViewTable.NOM_FAVORI)));
		mBuilder.setNomSens(c.getString(c.getColumnIndex(FavoriViewTable.NON_SENS)));
		mBuilder.setIdStation(c.getInt(c.getColumnIndex(FavoriViewTable.ID_STATION)));
		mBuilder.setLettre(c.getString(c.getColumnIndex(FavoriViewTable.LETTRE)));

		final int couleurBackground = c.getInt(c.getColumnIndex(FavoriViewTable.COULEUR));
		mBuilder.setCouleurBackground(couleurBackground);
		mBuilder.setCouleurTexte((ColorUtils.isLightColor(couleurBackground)) ? Color.BLACK : Color.WHITE);

		mBuilder.setNomArret(c.getString(c.getColumnIndex(FavoriViewTable.NOM_ARRET)));
		mBuilder.setNormalizedNom(c.getString(c.getColumnIndex(FavoriViewTable.NOM_NORMALIZED)));
		mBuilder.setLatitude(c.getFloat(c.getColumnIndex(FavoriViewTable.LATITUDE)));
		mBuilder.setLongitude(c.getFloat(c.getColumnIndex(FavoriViewTable.LONGITUDE)));
		mBuilder.setNomGroupe(c.getString(c.getColumnIndex(FavoriViewTable.NOM_GROUPE)));

		int index = c.getColumnIndex(FavoriViewTable.NEXT_HORAIRE);
		if (c.isNull(index)) {
			mBuilder.setNextHoraire(null);
		} else {
			mBuilder.setNextHoraire(c.getInt(index));
		}

		index = c.getColumnIndex(FavoriViewTable.ID_GROUPE);
		if (c.isNull(index)) {
			mBuilder.setIdGroupe(-1);
			mBuilder.setSection(-1);
		} else {
			mBuilder.setIdGroupe(c.getInt(index));
			mBuilder.setSection(c.getInt(index));
		}

		return mBuilder.build();
	}

	@Override
	protected ContentValues getContentValues(final Favori item) {
		return null;
	}
}
