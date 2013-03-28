/**
 *  Copyright (C) 2011 Romain Guefveneu
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

import net.naonedbus.bean.Favori;
import android.location.Location;
import android.location.LocationManager;

/**
 * @author romain
 * 
 */
public class FavoriDistanceComparator implements Comparator<Favori> {

	private Location referentiel;

	public void setReferentiel(final Location referentiel) {
		this.referentiel = referentiel;
	}

	@Override
	public int compare(final Favori favori1, final Favori favori2) {

		if (favori1 == null || favori2 == null || referentiel == null)
			return 0;

		if (favori1.latitude == null || favori1.longitude == null)
			return 0;
		if (favori2.latitude == null || favori2.longitude == null)
			return 0;

		if (favori1.section != null && favori2.section != null) {
			if (!favori1.section.equals(favori2.section)) {
				return Integer.valueOf(favori1.ordre).compareTo(favori2.ordre);
			}
		}

		final Location location1 = new Location(LocationManager.GPS_PROVIDER);
		location1.setLatitude(favori1.latitude);
		location1.setLongitude(favori1.longitude);

		final Location location2 = new Location(LocationManager.GPS_PROVIDER);
		location2.setLatitude(favori2.latitude);
		location2.setLongitude(favori2.longitude);

		final Float distance1 = referentiel.distanceTo(location1);
		final Float distance2 = referentiel.distanceTo(location2);

		return distance1.compareTo(distance2);
	}
}
