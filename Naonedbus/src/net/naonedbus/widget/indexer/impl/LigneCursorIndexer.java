package net.naonedbus.widget.indexer.impl;

import net.naonedbus.widget.indexer.CursorSectionIndexer;
import android.database.Cursor;

public class LigneCursorIndexer extends CursorSectionIndexer {

	private final String[] mTypes;

	public LigneCursorIndexer(final Cursor cursor, final String[] typesLignes, final String columnSectionName) {
		super(cursor, columnSectionName, typesLignes.length);
		mTypes = typesLignes;
	}

	@Override
	protected String getSectionLabel(final int section) {
		return mTypes[section];
	}

}
