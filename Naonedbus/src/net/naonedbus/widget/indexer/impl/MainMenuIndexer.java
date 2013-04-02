package net.naonedbus.widget.indexer.impl;

import net.naonedbus.R;
import net.naonedbus.widget.indexer.ArraySectionIndexer;
import net.naonedbus.widget.item.impl.MainMenuItem;
import android.content.Context;
import android.util.SparseArray;

public class MainMenuIndexer extends ArraySectionIndexer<MainMenuItem> {

	private final SparseArray<String> mSectionTitles;

	public MainMenuIndexer(final Context context) {
		mSectionTitles = new SparseArray<String>();
		mSectionTitles.append(0, context.getString(R.string.menu_section_naonedbus));
		mSectionTitles.append(1, context.getString(R.string.menu_section_options));
	}

	@Override
	protected String getSectionLabel(final Context context, final MainMenuItem item) {
		return mSectionTitles.get((Integer) item.getSection());
	}

	@Override
	protected void prepareSection(final MainMenuItem item) {
	}

}
