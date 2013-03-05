package net.naonedbus.widget.indexer.impl;

import android.content.Context;
import net.naonedbus.bean.Favori;
import net.naonedbus.widget.indexer.ArraySectionIndexer;

public class FavoriArrayIndexer extends ArraySectionIndexer<Favori> {

	@Override
	protected void prepareSection(Favori item) {

	}

	@Override
	protected String getSectionLabel(Context context, Favori item) {
		return ((String) item.getSection());
	}

}
