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

import net.naonedbus.bean.parking.pub.ParkingPublic;
import android.location.Location;
import android.location.LocationManager;

/**
 * @author romain
 * 
 */
public class ParkingDistanceComparator implements Comparator<ParkingPublic> {

	private Location referentiel;

	public void setReferentiel(Location referentiel) {
		this.referentiel = referentiel;
	}

	@Override
	public int compare(ParkingPublic parking1, ParkingPublic parking2) {

		if (parking1 == null || parking2 == null || referentiel == null)
			return 0;

		if (parking1.getLatitude() == null || parking1.getLongitude() == null)
			return 0;
		if (parking2.getLatitude() == null || parking2.getLongitude() == null)
			return 0;

		final Location location1 = new Location(LocationManager.GPS_PROVIDER);
		location1.setLatitude(parking1.getLatitude());
		location1.setLongitude(parking1.getLongitude());

		final Location location2 = new Location(LocationManager.GPS_PROVIDER);
		location2.setLatitude(parking2.getLatitude());
		location2.setLongitude(parking2.getLongitude());

		final Float distance1 = referentiel.distanceTo(location1);
		final Float distance2 = referentiel.distanceTo(location2);

		return distance1.compareTo(distance2);
	}

}
