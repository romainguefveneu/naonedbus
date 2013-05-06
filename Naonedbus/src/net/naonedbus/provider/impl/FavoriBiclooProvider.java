package net.naonedbus.provider.impl;

import net.naonedbus.provider.CustomContentProvider;
import net.naonedbus.provider.table.FavoriBiclooTable;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class FavoriBiclooProvider extends CustomContentProvider {

	public static final int FAVORIS = 100;
	public static final int FAVORI_ID = 110;

	private static final String AUTHORITY = "net.naonedbus.provider.FavoriBiclooProvider";
	private static final String FAVORIS_BASE_PATH = "favoris";

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + FAVORIS_BASE_PATH);

	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		URI_MATCHER.addURI(AUTHORITY, FAVORIS_BASE_PATH, FAVORIS);
		URI_MATCHER.addURI(AUTHORITY, FAVORIS_BASE_PATH + "/#", FAVORI_ID);
	}

	@Override
	public int delete(final Uri uri, final String selection, final String[] selectionArgs) {
		final SQLiteDatabase db = getWritableDatabase();

		int count;
		switch (URI_MATCHER.match(uri)) {
		case FAVORIS:
			count = db.delete(FavoriBiclooTable.TABLE_NAME, selection, selectionArgs);
			break;
		case FAVORI_ID:
			final String segment = uri.getLastPathSegment();
			count = db.delete(FavoriBiclooTable.TABLE_NAME,
					FavoriBiclooTable._ID + "=" + segment
							+ (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI (" + URI_MATCHER.match(uri) + ") " + uri);
		}

		if (count > 0)
			getContext().getContentResolver().notifyChange(uri, null);

		return count;
	}

	@Override
	public String getType(final Uri arg0) {
		return null;
	}

	@Override
	public Uri insert(final Uri uri, final ContentValues initialValues) {
		ContentValues values;

		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}

		if (URI_MATCHER.match(uri) != FAVORIS) {
			throw new IllegalArgumentException("Unknown URI " + uri + " (" + URI_MATCHER.match(uri) + ")");
		}

		final SQLiteDatabase db = getWritableDatabase();
		final long rowId = db.insert(FavoriBiclooTable.TABLE_NAME, null, values);
		if (rowId > 0) {
			final Uri insertUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(uri, null);
			return insertUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public Cursor query(final Uri uri, final String[] projection, final String selection, final String[] selectionArgs,
			String sortOrder) {
		final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(FavoriBiclooTable.TABLE_NAME);
		if (sortOrder == null) {
			sortOrder = FavoriBiclooTable.NOM_EQUIPEMENT;
		}

		final int uriType = URI_MATCHER.match(uri);
		switch (uriType) {
		case FAVORI_ID:
			queryBuilder.appendWhere(FavoriBiclooTable._ID + "=" + uri.getLastPathSegment());
			break;
		case FAVORIS:

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
	public int update(final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs) {
		final SQLiteDatabase db = getWritableDatabase();
		final int rowCount = db.update(FavoriBiclooTable.TABLE_NAME, values, selection, selectionArgs);
		if (rowCount > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return rowCount;
	}

}
