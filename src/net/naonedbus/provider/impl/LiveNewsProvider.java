package net.naonedbus.provider.impl;

import net.naonedbus.provider.CustomContentProvider;
import net.naonedbus.provider.table.LiveNewsTable;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class LiveNewsProvider extends CustomContentProvider {

	public static final int LIVE_NEWS = 100;
	public static final int LIVE_NEWS_ROUTE = 200;
	public static final int LIVE_NEWS_ROUTE_DIRECTION = 210;

	private static final String AUTHORITY = "net.naonedbus.provider.LiveNewsProvider";
	private static final String BASE_PATH = "liveNews";

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		URI_MATCHER.addURI(AUTHORITY, BASE_PATH, LIVE_NEWS);
		URI_MATCHER.addURI(AUTHORITY, BASE_PATH + "/*", LIVE_NEWS_ROUTE);
		URI_MATCHER.addURI(AUTHORITY, BASE_PATH + "/*/*", LIVE_NEWS_ROUTE_DIRECTION);
	}

	@Override
	public int delete(final Uri uri, final String selection, final String[] selectionArgs) {
		final SQLiteDatabase db = getWritableDatabase();

		int count;
		switch (URI_MATCHER.match(uri)) {
		case LIVE_NEWS:
			count = db.delete(LiveNewsTable.TABLE_NAME, selection, selectionArgs);
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

		if (URI_MATCHER.match(uri) != LIVE_NEWS) {
			throw new IllegalArgumentException("Unknown URI " + uri + " (" + URI_MATCHER.match(uri) + ")");
		}

		final SQLiteDatabase db = getWritableDatabase();
		final long rowId = db.insert(LiveNewsTable.TABLE_NAME, null, values);
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
		queryBuilder.setTables(LiveNewsTable.TABLE_NAME);
		if (sortOrder == null) {
			sortOrder = LiveNewsTable.TIMESTAMP + " DESC";
		}

		final int uriType = URI_MATCHER.match(uri);
		switch (uriType) {
		case LIVE_NEWS_ROUTE_DIRECTION:
			queryBuilder.appendWhere(" ( ");
			queryBuilder.appendWhere(LiveNewsTable.ROUTE_CODE + "=");
			queryBuilder.appendWhereEscapeString(uri.getPathSegments().get(1));
			queryBuilder.appendWhere(" AND ");
			queryBuilder.appendWhere(LiveNewsTable.DIRECTION_CODE + "=");
			queryBuilder.appendWhereEscapeString(uri.getPathSegments().get(2));
			queryBuilder.appendWhere(" ) ");
			queryBuilder.appendWhere(" OR ");
			queryBuilder.appendWhere(LiveNewsTable.ROUTE_CODE + " IS NULL");
			break;
		case LIVE_NEWS_ROUTE:
			queryBuilder.appendWhere(LiveNewsTable.ROUTE_CODE + "=");
			queryBuilder.appendWhereEscapeString(uri.getLastPathSegment());
			queryBuilder.appendWhere(" OR ");
			queryBuilder.appendWhere(LiveNewsTable.ROUTE_CODE + " IS NULL");
			break;
		case LIVE_NEWS:

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
