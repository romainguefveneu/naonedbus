package net.naonedbus.widget.indexer.impl;

import net.naonedbus.bean.Bicloo;
import net.naonedbus.widget.indexer.ArraySectionIndexer;
import android.content.Context;

public class BiclooNameIndexer extends ArraySectionIndexer<Bicloo> {

	@Override
	protected void prepareSection(final Bicloo item) {
		item.setSection(item.getName().substring(0, 1));
	}

	@Override
	protected String getSectionLabel(final Context context, final Bicloo item) {
		return item.getName().substring(0, 1);
	}

}
