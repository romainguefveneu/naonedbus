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
package net.naonedbus.comparator;

import java.util.Comparator;

import net.naonedbus.bean.parking.PublicPark;

public class PublicParkSpacesComparator implements Comparator<PublicPark> {

	@Override
	public int compare(PublicPark parking1, PublicPark parking2) {
		if (parking1 == null || parking2 == null)
			return 0;

		return (Integer.valueOf(parking1.getAvailableSpaces())).compareTo(parking2.getAvailableSpaces()) * -1;
	}
}
