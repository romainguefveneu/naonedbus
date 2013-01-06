package net.naonedbus.widget.indexer.impl;

import net.naonedbus.R;
import net.naonedbus.bean.Equipement;
import net.naonedbus.widget.indexer.ArraySectionIndexer;
import android.content.Context;

public class EquipementDistanceIndexer extends ArraySectionIndexer<Equipement> {

	@Override
	protected String getSectionLabel(Context context, Equipement item) {
		return context.getString((Integer) item.getSection());
	}

	@Override
	protected void prepareSection(Equipement equipement) {
		if (equipement.getDistance() == null) {
			equipement.setSection(R.string.section_distance_none);
		} else if (equipement.getDistance() > 100000) {
			equipement.setSection(R.string.section_distance_100000);
		} else if (equipement.getDistance() > 50000) {
			equipement.setSection(R.string.section_distance_50000);
		} else if (equipement.getDistance() > 40000) {
			equipement.setSection(R.string.section_distance_40000);
		} else if (equipement.getDistance() > 30000) {
			equipement.setSection(R.string.section_distance_30000);
		} else if (equipement.getDistance() > 20000) {
			equipement.setSection(R.string.section_distance_20000);
		} else if (equipement.getDistance() > 10000) {
			equipement.setSection(R.string.section_distance_10000);
		} else if (equipement.getDistance() > 5000) {
			equipement.setSection(R.string.section_distance_5000);
		} else if (equipement.getDistance() > 1000) {
			equipement.setSection(R.string.section_distance_1000);
		} else if (equipement.getDistance() > 500) {
			equipement.setSection(R.string.section_distance_500);
		} else {
			equipement.setSection(R.string.section_distance_0);
		}
	}

}
