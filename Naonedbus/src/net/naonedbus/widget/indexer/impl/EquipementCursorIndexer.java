package net.naonedbus.widget.indexer.impl;

import java.util.List;

import net.naonedbus.widget.indexer.CursorSectionIndexer;
import android.database.Cursor;

public class EquipementCursorIndexer extends CursorSectionIndexer {

	private final List<String> mTypes;

	public EquipementCursorIndexer(Cursor cursor, List<String> types, String columnSectionName) {
		super(cursor, columnSectionName, types.size());
		mTypes = types;
	}

	@Override
	protected String getSectionLabel(int section) {
		return mTypes.get(section);
	}

}
