package net.naonedbus.manager.impl;

import java.util.List;

import net.naonedbus.bean.Favori;
import net.naonedbus.manager.SQLiteManager;
import net.naonedbus.provider.impl.FavorisViewProvider;
import net.naonedbus.provider.table.FavoriViewTable;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.utils.QueryUtils;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;

public class FavorisViewManager extends SQLiteManager<Favori> {

	private static FavorisViewManager instance;

	public static synchronized FavorisViewManager getInstance() {
		if (instance == null) {
			instance = new FavorisViewManager();
		}
		return instance;
	}

	protected FavorisViewManager() {
		super(FavorisViewProvider.CONTENT_URI);
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

	@Override
	public Favori getSingleFromCursor(final Cursor c) {
		final Favori item = new Favori();
		item._id = c.getInt(c.getColumnIndex(FavoriViewTable._ID));
		item.codeLigne = c.getString(c.getColumnIndex(FavoriViewTable.CODE_LIGNE));
		item.codeEquipement = c.getString(c.getColumnIndex(FavoriViewTable.CODE_EQUIPEMENT));
		item.codeSens = c.getString(c.getColumnIndex(FavoriViewTable.CODE_SENS));
		item.codeArret = c.getString(c.getColumnIndex(FavoriViewTable.CODE_ARRET));
		item.nomFavori = c.getString(c.getColumnIndex(FavoriViewTable.NOM_FAVORI));
		item.nomSens = c.getString(c.getColumnIndex(FavoriViewTable.NON_SENS));
		item.idStation = c.getInt(c.getColumnIndex(FavoriViewTable.ID_STATION));
		item.couleurBackground = c.getInt(c.getColumnIndex(FavoriViewTable.COULEUR));
		item.lettre = c.getString(c.getColumnIndex(FavoriViewTable.LETTRE));
		item.couleurTexte = (ColorUtils.isLightColor(item.couleurBackground)) ? Color.BLACK : Color.WHITE;
		item.nomArret = c.getString(c.getColumnIndex(FavoriViewTable.NOM_ARRET));
		item.normalizedNom = c.getString(c.getColumnIndex(FavoriViewTable.NOM_NORMALIZED));
		item.latitude = c.getFloat(c.getColumnIndex(FavoriViewTable.LATITUDE));
		item.longitude = c.getFloat(c.getColumnIndex(FavoriViewTable.LONGITUDE));

		final int index = c.getColumnIndex(FavoriViewTable.ID_GROUPE);
		if (!c.isNull(index)) {
			item.section = c.getInt(index);
		} else {
			item.section = -1;
		}

		return item;
	}
}
