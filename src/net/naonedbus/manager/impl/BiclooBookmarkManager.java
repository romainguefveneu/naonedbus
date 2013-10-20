package net.naonedbus.manager.impl;

import net.naonedbus.bean.Bicloo;
import net.naonedbus.manager.SQLiteManager;
import net.naonedbus.provider.impl.BiclooBookmarkProvider;
import net.naonedbus.provider.table.BiclooBookmarkTable;
import android.content.ContentValues;
import android.database.Cursor;

public class BiclooBookmarkManager extends SQLiteManager<Bicloo> {

	private static BiclooBookmarkManager sInstance;

	private int mColId;
	private int mColNom;

	public static synchronized BiclooBookmarkManager getInstance() {
		if (sInstance == null) {
			sInstance = new BiclooBookmarkManager();
		}

		return sInstance;
	}

	private BiclooBookmarkManager() {
		super(BiclooBookmarkProvider.CONTENT_URI);
	}

	@Override
	public void onIndexCursor(final Cursor c) {
		mColId = c.getColumnIndex(BiclooBookmarkTable._ID);
		mColNom = c.getColumnIndex(BiclooBookmarkTable.EQUIPMENT_NAME);
	}

	@Override
	public Bicloo getSingleFromCursor(final Cursor c) {
		final Bicloo bicloo = new Bicloo();
		bicloo.setId(c.getInt(mColId));
		bicloo.setName(c.getString(mColNom));
		return bicloo;
	}

	@Override
	protected ContentValues getContentValues(final Bicloo item) {
		final ContentValues values = new ContentValues();
		values.put(BiclooBookmarkTable._ID, item.getId());
		values.put(BiclooBookmarkTable.EQUIPMENT_NAME, item.getName());

		return values;
	}
}
