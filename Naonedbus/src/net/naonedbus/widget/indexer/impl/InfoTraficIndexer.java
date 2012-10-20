package net.naonedbus.widget.indexer.impl;

import net.naonedbus.bean.InfoTraficDetail;
import net.naonedbus.widget.indexer.CustomSectionIndexer;
import android.content.Context;

public class InfoTraficIndexer extends CustomSectionIndexer<InfoTraficDetail> {

	public InfoTraficIndexer() {
	}

	@Override
	protected String getSectionLabel(Context context, InfoTraficDetail item) {
		return "Ligne " + item.getSection();
	}

	@Override
	protected void prepareSection(InfoTraficDetail item) {
	}

}
