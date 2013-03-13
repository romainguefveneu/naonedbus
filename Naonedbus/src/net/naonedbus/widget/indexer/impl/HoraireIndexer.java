package net.naonedbus.widget.indexer.impl;

import java.text.DateFormat;

import net.naonedbus.bean.horaire.Horaire;
import net.naonedbus.widget.indexer.ArraySectionIndexer;
import android.content.Context;

public class HoraireIndexer extends ArraySectionIndexer<Horaire> {

	private DateFormat mDateFormat;

	@Override
	protected void prepareSection(final Horaire item) {

	}

	@Override
	protected String getSectionLabel(final Context context, final Horaire item) {
		if (mDateFormat == null) {
			mDateFormat = DateFormat.getDateInstance(DateFormat.FULL);
		}
		return mDateFormat.format(item.getDate());
	}

}
