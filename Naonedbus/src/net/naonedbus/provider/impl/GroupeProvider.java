package net.naonedbus.provider.impl;

import net.naonedbus.provider.CustomContentProvider;
import net.naonedbus.provider.table.FavorisGroupesTable;
import net.naonedbus.provider.table.GroupeTable;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class GroupeProvider extends CustomContentProvider {

	public static final int GROUPES = 100;
	public static final int GROUPE_ID = 110;

	public static final int FAVORIS_GROUPES = 200;
	public static final String FAVORIS_GROUPES_URI_PATH_QUERY = "favorisGroupes";

	private static final String AUTHORITY = "net.naonedbus.provider.GroupeProvider";
	private static final String GROUPES_BASE_PATH = "groupes";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + GROUPES_BASE_PATH);

	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		URI_MATCHER.addURI(AUTHORITY, GROUPES_BASE_PATH, GROUPES);
		URI_MATCHER.addURI(AUTHORITY, GROUPES_BASE_PATH + "/#", GROUPE_ID);
		URI_MATCHER.addURI(AUTHORITY, GROUPES_BASE_PATH + "/#/#", FAVORIS_GROUPES);
		URI_MATCHER.addURI(AUTHORITY, FAVORIS_GROUPES_URI_PATH_QUERY, FAVORIS_GROUPES);
	}

	@Override
	public int delete(final Uri uri, final String selection, final String[] selectionArgs) {
		final SQLiteDatabase db = getWritableDatabase();
		final String segment;

		int count;
		switch (URI_MATCHER.match(uri)) {
		case GROUPES:
			count = db.delete(GroupeTable.TABLE_NAME, selection, selectionArgs);
			break;
		case GROUPE_ID:
			segment = uri.getLastPathSegment();
			count = db.delete(GroupeTable.TABLE_NAME, GroupeTable._ID + "=" + segment
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
			break;

		case FAVORIS_GROUPES:
			segment = uri.getPathSegments().get(1);
			final String idFavori = uri.getLastPathSegment();
			count = db.delete(FavorisGroupesTable.TABLE_NAME, FavorisGroupesTable.ID_GROUPE + "=" + segment + " AND "
					+ FavorisGroupesTable.ID_FAVORI + "=" + idFavori, null);
		default:
			throw new IllegalArgumentException("Unknown URI (" + URI_MATCHER.match(uri) + ") " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);

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
			rowId = db.insert(GroupeTable.TABLE_NAME, null, values);
			break;
		case FAVORIS_GROUPES:
			rowId = db.insert(FavorisGroupesTable.TABLE_NAME, null, values);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri + " (" + URI_MATCHER.match(uri) + ")");
		}

		if (rowId > 0) {
			final Uri insertUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(uri, null);
			return insertUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(GroupeTable.TABLE_NAME);

		int uriType = URI_MATCHER.match(uri);
		switch (uriType) {
		case GROUPE_ID:
			queryBuilder.appendWhere(GroupeTable._ID + "=" + uri.getLastPathSegment());
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
		final int rowCount = db.update(GroupeTable.TABLE_NAME, values, selection, selectionArgs);
		if (rowCount > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return rowCount;
	}

}
