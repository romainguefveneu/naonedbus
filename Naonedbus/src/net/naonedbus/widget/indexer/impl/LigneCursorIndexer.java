package net.naonedbus.widget.indexer.impl;

import java.util.List;

import net.naonedbus.widget.indexer.CursorSectionIndexer;
import android.database.Cursor;

public class LigneCursorIndexer extends CursorSectionIndexer {

	private final List<String> mTypes;

	public LigneCursorIndexer(Cursor cursor, List<String> typesLignes, String columnSectionName) {
		super(cursor, columnSectionName, typesLignes.size());
		mTypes = typesLignes;
	}

	@Override
	protected String getSectionLabel(int section) {
		return mTypes.get(section);
	}

}
