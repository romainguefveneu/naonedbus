package net.naonedbus.widget.indexer.impl;

import net.naonedbus.R;
import net.naonedbus.bean.parking.pub.ParkingPublic;
import net.naonedbus.widget.indexer.ArraySectionIndexer;
import android.content.Context;

public class ParkingDistanceIndexer extends ArraySectionIndexer<ParkingPublic> {

	@Override
	protected String getSectionLabel(Context context, ParkingPublic item) {
		return context.getString((Integer) item.getSection());
	}

	@Override
	protected void prepareSection(ParkingPublic item) {
		if (item.getDistance() == null) {
			item.setSection(R.string.section_distance_none);
		} else if (item.getDistance() > 100000) {
			item.setSection(R.string.section_distance_100000);
		} else if (item.getDistance() > 50000) {
			item.setSection(R.string.section_distance_50000);
		} else if (item.getDistance() > 40000) {
			item.setSection(R.string.section_distance_40000);
		} else if (item.getDistance() > 30000) {
			item.setSection(R.string.section_distance_30000);
		} else if (item.getDistance() > 20000) {
			item.setSection(R.string.section_distance_20000);
		} else if (item.getDistance() > 10000) {
			item.setSection(R.string.section_distance_10000);
		} else if (item.getDistance() > 5000) {
			item.setSection(R.string.section_distance_5000);
		} else if (item.getDistance() > 1000) {
			item.setSection(R.string.section_distance_1000);
		} else if (item.getDistance() > 500) {
			item.setSection(R.string.section_distance_500);
		} else {
			item.setSection(R.string.section_distance_0);
		}
	}

}
