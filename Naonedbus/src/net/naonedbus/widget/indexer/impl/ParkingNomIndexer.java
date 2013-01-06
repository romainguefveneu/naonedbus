package net.naonedbus.widget.indexer.impl;

import net.naonedbus.bean.parking.pub.ParkingPublic;
import net.naonedbus.widget.indexer.ArraySectionIndexer;
import android.content.Context;

public class ParkingNomIndexer extends ArraySectionIndexer<ParkingPublic> {

	@Override
	protected String getSectionLabel(Context context, ParkingPublic item) {
		return item.getNom().substring(0, 1);
	}

	@Override
	protected void prepareSection(ParkingPublic item) {
		item.setSection(item.getNom().substring(0, 1));
	}

}
