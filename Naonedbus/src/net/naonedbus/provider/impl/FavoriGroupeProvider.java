package net.naonedbus.provider.impl;

import net.naonedbus.provider.CustomContentProvider;
import net.naonedbus.provider.table.FavorisGroupesTable;
import net.naonedbus.provider.table.GroupeTable;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class FavoriGroupeProvider extends CustomContentProvider {

	public static final int GROUPES = 100;
	public static final int GROUPE_ID = 110;
	public static final int FAVORI_ID = 200;

	public static final String QUERY_PARAMETER_IDS = "ids";

	private static final String AUTHORITY = "net.naonedbus.provider.FavoriGroupeProvider";
	public static final String FAVORI_ID_BASE_PATH = "favori";

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		URI_MATCHER.addURI(AUTHORITY, null, GROUPES);
		URI_MATCHER.addURI(AUTHORITY, "#/#", GROUPES);
		URI_MATCHER.addURI(AUTHORITY, "#", GROUPE_ID);
		URI_MATCHER.addURI(AUTHORITY, FAVORI_ID_BASE_PATH, FAVORI_ID);
	}

	@Override
	public int delete(final Uri uri, final String selection, final String[] selectionArgs) {
		final SQLiteDatabase db = getWritableDatabase();

		int count;
		switch (URI_MATCHER.match(uri)) {
		case GROUPES:
			final String idGroupe = uri.getPathSegments().get(0);
			final String idFavori = uri.getLastPathSegment();
			count = db.delete(FavorisGroupesTable.TABLE_NAME, FavorisGroupesTable.ID_GROUPE + "=" + idGroupe + " AND "
					+ FavorisGroupesTable.ID_FAVORI + "=" + idFavori, null);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI (" + URI_MATCHER.match(uri) + ") " + uri);
		}

		if (count > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}

		return count;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(final Uri uri, final ContentValues initialValues) {
		final SQLiteDatabase db = getWritableDatabase();
		final long rowId;
		ContentValues values;

		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}

		switch (URI_MATCHER.match(uri)) {
		case GROUPES:
			rowId = db.insertWithOnConflict(FavorisGroupesTable.TABLE_NAME, null, values,
					SQLiteDatabase.CONFLICT_IGNORE);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri + " (" + URI_MATCHER.match(uri) + ")");
		}

		if (rowId > 0) {
			final Uri insertUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(uri, null);
			return insertUri;
		}

		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(FavorisGroupesTable.TABLE_NAME);

		int uriType = URI_MATCHER.match(uri);
		switch (uriType) {
		case FAVORI_ID:
			final String favoriId = uri.getQueryParameter(FavoriGroupeProvider.QUERY_PARAMETER_IDS);
			final String query = String.format(LinkQuery.SELECT, favoriId);
			return getReadableDatabase().rawQuery(query, null);

		case GROUPE_ID:
			queryBuilder.appendWhere(FavorisGroupesTable.ID_GROUPE + "=" + uri.getLastPathSegment());
			break;

		case GROUPES:
			break;

		default:
			throw new IllegalArgumentException("Unknown URI (" + uri + ")");
		}

		final Cursor cursor = queryBuilder.query(getReadableDatabase(), projection, selection, selectionArgs, null,
				null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		final SQLiteDatabase db = getWritableDatabase();
		final int rowCount = db.update(FavorisGroupesTable.TABLE_NAME, values, selection, selectionArgs);
		if (rowCount > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return rowCount;
	}

	/**
	 * Composants de la requête de sélection des groupes avec l'association à un
	 * favori.
	 * 
	 * @author romain.guefveneu
	 * 
	 */
	private static interface LinkQuery {
		final String GROUPE_COUNT = "SELECT MIN(COUNT(1),1) FROM " + FavorisGroupesTable.TABLE_NAME + " fgt WHERE fgt."
				+ FavorisGroupesTable.ID_GROUPE + "=" + GroupeTable.TABLE_NAME + "." + GroupeTable._ID + " AND fgt."
				+ FavorisGroupesTable.ID_FAVORI + " IN (%s)";

		public static final String SELECT = "SELECT " + GroupeTable._ID + ", " + GroupeTable.NOM + ", "
				+ GroupeTable.VISIBILITE + ", (" + GROUPE_COUNT + ") as " + FavorisGroupesTable.LINKED + " FROM "
				+ GroupeTable.TABLE_NAME;
	}

}
