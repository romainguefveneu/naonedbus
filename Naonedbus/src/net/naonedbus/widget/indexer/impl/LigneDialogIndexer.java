package net.naonedbus.widget.indexer.impl;

import java.util.HashMap;
import java.util.Map;

import net.naonedbus.bean.Sens;
import net.naonedbus.widget.indexer.ArraySectionIndexer;
import net.naonedbus.widget.item.SectionItem;
import android.content.Context;

public class LigneDialogIndexer extends ArraySectionIndexer<SectionItem> {

	private Map<Class<? extends SectionItem>, String> sections;

	public LigneDialogIndexer() {
		sections = new HashMap<Class<? extends SectionItem>, String>();
		sections.put(Sens.class, "Sens");
	}

	@Override
	protected String getSectionLabel(Context context, SectionItem item) {
		return sections.get(item.getClass());
	}

	@Override
	protected void prepareSection(SectionItem item) {

	}

}
