package net.naonedbus.provider.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import net.naonedbus.provider.CustomContentProvider;

public class GroupeProvider extends CustomContentProvider {

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return 0;
	}

}
