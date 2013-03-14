package net.naonedbus.manager.impl;

import java.util.List;

import net.naonedbus.bean.Groupe;
import net.naonedbus.manager.SQLiteManager;
import net.naonedbus.provider.impl.FavoriGroupeProvider;
import net.naonedbus.provider.impl.GroupeProvider;
import net.naonedbus.provider.table.FavorisGroupesTable;
import net.naonedbus.provider.table.GroupeTable;
import net.naonedbus.utils.QueryUtils;
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
		groupe.setOrdre(c.getInt(c.getColumnIndex(GroupeTable.ORDRE)));
		groupe.setVisibility(c.getInt(c.getColumnIndex(GroupeTable.VISIBILITE)));
		return groupe;
	}

	public Cursor getCursor(final ContentResolver contentResolver, final List<Integer> idFavoris) {
		final Uri.Builder builder = FavoriGroupeProvider.CONTENT_URI.buildUpon();
		builder.path(FavoriGroupeProvider.FAVORI_ID_BASE_PATH);
		builder.appendQueryParameter(FavoriGroupeProvider.QUERY_PARAMETER_IDS, QueryUtils.listToInStatement(idFavoris));

		return contentResolver.query(builder.build(), null, null, null, null);
	}

	public void add(final ContentResolver contentResolver, final Groupe groupe) {
		final ContentValues contentValues = new ContentValues();
		contentValues.put(GroupeTable.NOM, groupe.getNom());
		contentValues.put(GroupeTable.ORDRE, groupe.getOrdre());
		contentValues.put(GroupeTable.VISIBILITE, String.valueOf(groupe.getVisibility()));

		contentResolver.insert(GroupeProvider.CONTENT_URI, contentValues);
	}

	public void delete(final ContentResolver contentResolver, final int idGroupe) {
		final Uri.Builder builder = GroupeProvider.CONTENT_URI.buildUpon();
		builder.appendPath(String.valueOf(idGroupe));

		contentResolver.delete(builder.build(), null, null);
	}

	public void update(final ContentResolver contentResolver, final Groupe groupe) {
		final ContentValues contentValues = new ContentValues();
		contentValues.put(GroupeTable.NOM, groupe.getNom());
		contentValues.put(GroupeTable.ORDRE, groupe.getOrdre());
		contentValues.put(GroupeTable.VISIBILITE, String.valueOf(groupe.getVisibility()));

		contentResolver.update(GroupeProvider.CONTENT_URI, contentValues, GroupeTable._ID + "=?",
				new String[] { String.valueOf(groupe.getId()) });
	}

	public boolean isFavoriAssociated(final ContentResolver contentResolver, final int idGroupe, final int idFavori) {
		boolean result;

		final Cursor c = contentResolver.query(FavoriGroupeProvider.CONTENT_URI, null, FavorisGroupesTable.ID_GROUPE
				+ "=? AND " + FavorisGroupesTable.ID_FAVORI + "=?",
				new String[] { String.valueOf(idGroupe), String.valueOf(idFavori) }, null);
		result = c.getCount() > 0;
		c.close();

		return result;
	}

	public void addFavoriToGroup(final ContentResolver contentResolver, final int idGroupe, final int idFavori) {
		final ContentValues contentValues = new ContentValues();
		contentValues.put(FavorisGroupesTable.ID_GROUPE, String.valueOf(idGroupe));
		contentValues.put(FavorisGroupesTable.ID_FAVORI, String.valueOf(idFavori));

		contentResolver.insert(FavoriGroupeProvider.CONTENT_URI, contentValues);
	}

	public void removeFavoriFromGroup(final ContentResolver contentResolver, final int idGroupe, final int idFavori) {
		final Uri.Builder builder = FavoriGroupeProvider.CONTENT_URI.buildUpon();
		builder.appendPath(String.valueOf(idGroupe));
		builder.appendPath(String.valueOf(idFavori));
		contentResolver.delete(builder.build(), null, null);
	}

	public void move(final ContentResolver contentResolver, final Cursor cursor, final int from, final int to) {
		Groupe groupe;
		final int start = Math.min(from, to);
		final int stop = Math.max(from, to);
		int position = start;

		cursor.moveToPosition(start);
		while (cursor.getPosition() <= stop && !cursor.isAfterLast()) {
			groupe = getSingleFromCursor(cursor);
			groupe.setOrdre(position);
			
			update(contentResolver, groupe);
			
			cursor.moveToNext();
			position++;
		}

	}

}
