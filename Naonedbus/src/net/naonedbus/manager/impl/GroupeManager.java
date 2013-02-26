package net.naonedbus.manager.impl;

import net.naonedbus.bean.Groupe;
import net.naonedbus.manager.SQLiteManager;
import net.naonedbus.provider.impl.GroupeProvider;
import net.naonedbus.provider.table.FavorisGroupesTable;
import net.naonedbus.provider.table.GroupeTable;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class GroupeManager extends SQLiteManager<Groupe> {

	private static GroupeManager instance;

	public static synchronized GroupeManager getInstance() {
		if (instance == null) {
			instance = new GroupeManager();
		}
		return instance;
	}

	private GroupeManager() {
		super(GroupeProvider.CONTENT_URI);
	}

	@Override
	public Groupe getSingleFromCursor(final Cursor c) {
		final Groupe groupe = new Groupe();
		groupe.setId(c.getInt(c.getColumnIndex(GroupeTable._ID)));
		groupe.setNom(c.getString(c.getColumnIndex(GroupeTable.NOM)));
		groupe.setVisibility(c.getInt(c.getColumnIndex(GroupeTable.VISIBILITE)));
		return groupe;
	}

	public void add(final ContentResolver contentResolver, final Groupe groupe) {
		final ContentValues contentValues = new ContentValues();
		contentValues.put(GroupeTable.NOM, groupe.getNom());
		contentValues.put(GroupeTable.VISIBILITE, String.valueOf(groupe.getVisibility()));

		contentResolver.insert(GroupeProvider.CONTENT_URI, contentValues);
	}

	public void delete(final ContentResolver contentResolver, final int idGroupe) {
		final Uri.Builder builder = GroupeProvider.CONTENT_URI.buildUpon();
		builder.appendPath(String.valueOf(idGroupe));

		contentResolver.delete(builder.build(), null, null);
	}

	public void addFavoriToGroup(final ContentResolver contentResolver, final int idGroupe, final int idFavori) {
		final Uri.Builder builder = GroupeProvider.CONTENT_URI.buildUpon();
		builder.path(GroupeProvider.FAVORIS_GROUPES_URI_PATH_QUERY);

		final ContentValues contentValues = new ContentValues();
		contentValues.put(FavorisGroupesTable.ID_GROUPE, String.valueOf(idGroupe));
		contentValues.put(FavorisGroupesTable.ID_FAVORI, String.valueOf(idFavori));

		contentResolver.insert(builder.build(), contentValues);
	}

	public void removeFavoriToGroup(final ContentResolver contentResolver, final int idGroupe, final int idFavori) {
		final Uri.Builder builder = GroupeProvider.CONTENT_URI.buildUpon();
		builder.appendPath(String.valueOf(idGroupe));
		builder.appendPath(String.valueOf(idFavori));
		contentResolver.delete(builder.build(), null, null);
	}

}
