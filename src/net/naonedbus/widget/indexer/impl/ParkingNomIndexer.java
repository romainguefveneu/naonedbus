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
