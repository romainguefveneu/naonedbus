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
import net.naonedbus.bean.Equipment;
import net.naonedbus.widget.indexer.ArraySectionIndexer;
import android.content.Context;

public class EquipmentDistanceIndexer extends ArraySectionIndexer<Equipment> {

	@Override
	protected String getSectionLabel(Context context, Equipment item) {
		return context.getString((Integer) item.getSection());
	}

	@Override
	protected void prepareSection(Equipment equipment) {
		if (equipment.getDistance() == null) {
			equipment.setSection(R.string.no_distance_information);
		} else if (equipment.getDistance() > 100000) {
			equipment.setSection(R.string.much_too_far);
		} else if (equipment.getDistance() > 50000) {
			equipment.setSection(R.string.less_than_100km);
		} else if (equipment.getDistance() > 40000) {
			equipment.setSection(R.string.less_than_50km);
		} else if (equipment.getDistance() > 30000) {
			equipment.setSection(R.string.less_than_40km);
		} else if (equipment.getDistance() > 20000) {
			equipment.setSection(R.string.less_than_30km);
		} else if (equipment.getDistance() > 10000) {
			equipment.setSection(R.string.less_than_20km);
		} else if (equipment.getDistance() > 5000) {
			equipment.setSection(R.string.less_than_10km);
		} else if (equipment.getDistance() > 1000) {
			equipment.setSection(R.string.less_than_5km);
		} else if (equipment.getDistance() > 500) {
			equipment.setSection(R.string.less_than_1km);
		} else {
			equipment.setSection(R.string.less_than_500m);
		}
	}

}
