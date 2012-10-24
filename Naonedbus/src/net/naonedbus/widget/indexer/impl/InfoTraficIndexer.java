package net.naonedbus.widget.indexer.impl;

import net.naonedbus.bean.InfoTrafic;
import net.naonedbus.bean.Ligne;
import net.naonedbus.widget.indexer.CustomSectionIndexer;
import android.content.Context;

public class InfoTraficIndexer extends CustomSectionIndexer<InfoTrafic> {

	@Override
	protected String getSectionLabel(Context context, InfoTrafic item) {
		return "Ligne " + ((Ligne) item.getSection()).lettre;
	}

	@Override
	protected void prepareSection(InfoTrafic item) {
	}

}
