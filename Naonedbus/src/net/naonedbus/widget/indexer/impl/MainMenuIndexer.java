package net.naonedbus.widget.indexer.impl;

import net.naonedbus.widget.indexer.ArraySectionIndexer;
import net.naonedbus.widget.item.impl.MainMenuItem;
import android.content.Context;
import android.util.SparseArray;

public class MainMenuIndexer extends ArraySectionIndexer<MainMenuItem> {

	private static SparseArray<String> SECTION_TITLES;
	static {
		SECTION_TITLES = new SparseArray<String>();
		SECTION_TITLES.append(0, "Naonedbus");
		SECTION_TITLES.append(1, "Options");
	}

	@Override
	protected String getSectionLabel(Context context, MainMenuItem item) {
		return SECTION_TITLES.get((Integer) item.getSection());
	}

	@Override
	protected void prepareSection(MainMenuItem item) {
	}

}
