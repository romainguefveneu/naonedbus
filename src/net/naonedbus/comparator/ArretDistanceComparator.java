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

import net.naonedbus.bean.Stop;
import net.naonedbus.bean.Equipment;
import android.location.Location;
import android.location.LocationManager;

/**
 * @author romain
 * 
 */
public class ArretDistanceComparator implements Comparator<Stop> {

	/**
	 * Référentiel servant à la comparaison via la méthode
	 * {@link #referentielCompare(Equipment, Equipment)}.
	 */
	private Location referentiel;

	public void setReferentiel(Location referentiel) {
		this.referentiel = referentiel;
	}

	@Override
	public int compare(Stop e1, Stop e2) {
		if (e1 == null || e2 == null)
			return 0;

		return referentielCompare(e1, e2);
	}

	/**
	 * Comparer les 2 équipements par rapport au {@link #referentiel}
	 * 
	 * @param e1
	 * @param e2
	 * @return
	 */
	private int referentielCompare(Stop e1, Stop e2) {
		if (e1.getLatitude() == null || e1.getLongitude() == null)
			return 0;
		if (e2.getLatitude() == null || e2.getLongitude() == null)
			return 0;

		final Location location1 = new Location(LocationManager.GPS_PROVIDER);
		location1.setLatitude(e1.getLatitude());
		location1.setLongitude(e1.getLongitude());

		final Location location2 = new Location(LocationManager.GPS_PROVIDER);
		location2.setLatitude(e2.getLatitude());
		location2.setLongitude(e2.getLongitude());

		final Float distance1 = referentiel.distanceTo(location1);
		final Float distance2 = referentiel.distanceTo(location2);

		return distance1.compareTo(distance2);
	}

}
