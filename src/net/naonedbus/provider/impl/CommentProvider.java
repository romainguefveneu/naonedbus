package net.naonedbus.provider.impl;

import net.naonedbus.provider.CustomContentProvider;
import net.naonedbus.provider.table.CommentTable;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class CommentProvider extends CustomContentProvider {

	public static final int COMMENTAIRES = 100;
	public static final int COMMENTAIRE_LIGNE = 200;
	public static final int COMMENTAIRE_LIGNE_SENS = 210;

	private static final String AUTHORITY = "net.naonedbus.provider.CommentProvider";
	private static final String BASE_PATH = "commentaires";

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		URI_MATCHER.addURI(AUTHORITY, BASE_PATH, COMMENTAIRES);
		URI_MATCHER.addURI(AUTHORITY, BASE_PATH + "/*", COMMENTAIRE_LIGNE);
		URI_MATCHER.addURI(AUTHORITY, BASE_PATH + "/*/*", COMMENTAIRE_LIGNE_SENS);
	}

	@Override
	public int delete(final Uri uri, final String selection, final String[] selectionArgs) {
		final SQLiteDatabase db = getWritableDatabase();

		int count;
		switch (URI_MATCHER.match(uri)) {
		case COMMENTAIRES:
			count = db.delete(CommentTable.TABLE_NAME, selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI (" + URI_MATCHER.match(uri) + ") " + uri);
		}

		if (count > 0)
			getContext().getContentResolver().notifyChange(uri, null);

		return count;
	}

	@Override
	public String getType(final Uri uri) {
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

		if (URI_MATCHER.match(uri) != COMMENTAIRES) {
			throw new IllegalArgumentException("Unknown URI " + uri + " (" + URI_MATCHER.match(uri) + ")");
		}

		final SQLiteDatabase db = getWritableDatabase();
		final long rowId = db.insert(CommentTable.TABLE_NAME, null, values);
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
		queryBuilder.setTables(CommentTable.TABLE_NAME);
		if (sortOrder == null) {
			sortOrder = CommentTable.TIMESTAMP + " DESC";
		}

		final int uriType = URI_MATCHER.match(uri);
		switch (uriType) {
		case COMMENTAIRE_LIGNE_SENS:
			queryBuilder.appendWhere(" ( ");
			queryBuilder.appendWhere(CommentTable.ROUTE_CODE + "=");
			queryBuilder.appendWhereEscapeString(uri.getPathSegments().get(1));
			queryBuilder.appendWhere(" AND ");
			queryBuilder.appendWhere(CommentTable.DIRECTION_CODE + "=");
			queryBuilder.appendWhereEscapeString(uri.getPathSegments().get(2));
			queryBuilder.appendWhere(" ) ");
			queryBuilder.appendWhere(" OR ");
			queryBuilder.appendWhere(CommentTable.ROUTE_CODE + " IS NULL");
			break;
		case COMMENTAIRE_LIGNE:
			queryBuilder.appendWhere(CommentTable.ROUTE_CODE + "=");
			queryBuilder.appendWhereEscapeString(uri.getLastPathSegment());
			queryBuilder.appendWhere(" OR ");
			queryBuilder.appendWhere(CommentTable.ROUTE_CODE + " IS NULL");
			break;
		case COMMENTAIRES:

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
		return 0;
	}

}
