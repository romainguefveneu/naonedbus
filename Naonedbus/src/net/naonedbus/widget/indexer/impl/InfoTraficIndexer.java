package net.naonedbus.widget.indexer.impl;

import net.naonedbus.R;
import net.naonedbus.bean.InfoTrafic;
import net.naonedbus.bean.Ligne;
import net.naonedbus.widget.indexer.ArraySectionIndexer;
import android.content.Context;

public class InfoTraficIndexer extends ArraySectionIndexer<InfoTrafic> {

	@Override
	protected String getSectionLabel(final Context context, final InfoTrafic item) {
		return context.getString(R.string.dialog_title_menu_lignes, ((Ligne) item.getSection()).lettre);
	}

	@Override
	protected void prepareSection(final InfoTrafic item) {
	}

}
