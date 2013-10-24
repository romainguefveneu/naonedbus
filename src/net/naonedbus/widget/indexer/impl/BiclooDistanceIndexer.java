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
import net.naonedbus.bean.Bicloo;
import net.naonedbus.widget.indexer.ArraySectionIndexer;
import android.content.Context;

public class BiclooDistanceIndexer extends ArraySectionIndexer<Bicloo> {

	@Override
	protected String getSectionLabel(final Context context, final Bicloo item) {
		return context.getString((Integer) item.getSection());
	}

	@Override
	protected void prepareSection(final Bicloo bicloo) {
		if (bicloo.getDistance() == null) {
			bicloo.setSection(R.string.no_distance_information);
		} else if (bicloo.getDistance() > 100000) {
			bicloo.setSection(R.string.much_too_far);
		} else if (bicloo.getDistance() > 50000) {
			bicloo.setSection(R.string.less_than_100km);
		} else if (bicloo.getDistance() > 40000) {
			bicloo.setSection(R.string.less_than_50km);
		} else if (bicloo.getDistance() > 30000) {
			bicloo.setSection(R.string.less_than_40km);
		} else if (bicloo.getDistance() > 20000) {
			bicloo.setSection(R.string.less_than_30km);
		} else if (bicloo.getDistance() > 10000) {
			bicloo.setSection(R.string.less_than_20km);
		} else if (bicloo.getDistance() > 5000) {
			bicloo.setSection(R.string.less_than_10km);
		} else if (bicloo.getDistance() > 1000) {
			bicloo.setSection(R.string.less_than_5km);
		} else if (bicloo.getDistance() > 500) {
			bicloo.setSection(R.string.less_than_1km);
		} else {
			bicloo.setSection(R.string.less_than_500m);
		}
	}

}
