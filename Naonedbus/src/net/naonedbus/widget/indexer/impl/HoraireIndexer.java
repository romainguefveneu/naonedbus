package net.naonedbus.widget.indexer.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import net.naonedbus.bean.horaire.Horaire;
import net.naonedbus.widget.indexer.CustomSectionIndexer;
import android.content.Context;

public class HoraireIndexer extends CustomSectionIndexer<Horaire> {

	private DateFormat dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.FULL);

	@Override
	protected void prepareSection(Horaire item) {

	}

	@Override
	protected String getSectionLabel(Context context, Horaire item) {
		return dateFormat.format(item.getDate());
	}

}
