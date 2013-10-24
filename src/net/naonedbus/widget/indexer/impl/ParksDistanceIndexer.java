/**
 * Copyright (C) 2013 Romain Guefveneu.
 *   
 *  This file is part of naonedbus.
 *   
 *  Naonedbus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  Naonedbus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.naonedbus.widget.indexer.impl;

import net.naonedbus.R;
import net.naonedbus.bean.parking.PublicPark;
import net.naonedbus.widget.indexer.ArraySectionIndexer;
import android.content.Context;

public class ParksDistanceIndexer extends ArraySectionIndexer<PublicPark> {

	@Override
	protected String getSectionLabel(Context context, PublicPark item) {
		return context.getString((Integer) item.getSection());
	}

	@Override
	protected void prepareSection(PublicPark item) {
		if (item.getDistance() == null) {
			item.setSection(R.string.no_distance_information);
		} else if (item.getDistance() > 100000) {
			item.setSection(R.string.much_too_far);
		} else if (item.getDistance() > 50000) {
			item.setSection(R.string.less_than_100km);
		} else if (item.getDistance() > 40000) {
			item.setSection(R.string.less_than_50km);
		} else if (item.getDistance() > 30000) {
			item.setSection(R.string.less_than_40km);
		} else if (item.getDistance() > 20000) {
			item.setSection(R.string.less_than_30km);
		} else if (item.getDistance() > 10000) {
			item.setSection(R.string.less_than_20km);
		} else if (item.getDistance() > 5000) {
			item.setSection(R.string.less_than_10km);
		} else if (item.getDistance() > 1000) {
			item.setSection(R.string.less_than_5km);
		} else if (item.getDistance() > 500) {
			item.setSection(R.string.less_than_1km);
		} else {
			item.setSection(R.string.less_than_500m);
		}
	}

}
