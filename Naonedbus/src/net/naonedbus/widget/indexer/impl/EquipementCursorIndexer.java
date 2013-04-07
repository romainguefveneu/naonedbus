package net.naonedbus.widget.indexer.impl;

import net.naonedbus.widget.indexer.CursorSectionIndexer;
import android.database.Cursor;

public class EquipementCursorIndexer extends CursorSectionIndexer {

	private final String[] mTypes;

	public EquipementCursorIndexer(final Cursor cursor, final String[] types, final String columnSectionName) {
		super(cursor, columnSectionName, types.length);
		mTypes = types;
	}

	@Override
	protected String getSectionLabel(final int section) {
		return mTypes[section];
	}

}
