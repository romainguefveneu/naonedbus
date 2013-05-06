package net.naonedbus.manager.impl;

import net.naonedbus.bean.Bicloo;
import net.naonedbus.manager.SQLiteManager;
import net.naonedbus.provider.impl.FavoriBiclooProvider;
import net.naonedbus.provider.table.FavoriBiclooTable;
import android.content.ContentValues;
import android.database.Cursor;

public class FavoriBiclooManager extends SQLiteManager<Bicloo> {

	private static FavoriBiclooManager sInstance;

	private int mColId;
	private int mColNom;

	public static synchronized FavoriBiclooManager getInstance() {
		if (sInstance == null) {
			sInstance = new FavoriBiclooManager();
		}

		return sInstance;
	}

	private FavoriBiclooManager() {
		super(FavoriBiclooProvider.CONTENT_URI);
	}

	@Override
	public void onIndexCursor(final Cursor c) {
		mColId = c.getColumnIndex(FavoriBiclooTable._ID);
		mColNom = c.getColumnIndex(FavoriBiclooTable.NOM_EQUIPEMENT);
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
		values.put(FavoriBiclooTable._ID, item.getNumber());
		values.put(FavoriBiclooTable.NOM_EQUIPEMENT, item.getName());

		return values;
	}
}
