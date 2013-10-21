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

import net.naonedbus.bean.Equipment;
import android.location.Location;
import android.location.LocationManager;

public class EquipmentDistanceComparator<T extends Equipment> implements Comparator<T> {

	/**
	 * Référentiel servant à la comparaison via la méthode
	 * {@link #referentielCompare(Equipment, Equipment)}.
	 */
	private Location referentiel;

	public void setReferentiel(final Location referentiel) {
		this.referentiel = referentiel;
	}

	@Override
	public int compare(final T e1, final T e2) {
		if (e1 == null || e2 == null)
			return 0;

		if (referentiel == null) {
			return simpleCompare(e1, e2);
		} else {
			return referentielCompare(e1, e2);
		}
	}

	/**
	 * Comparer la distance entre les 2 équipements. Utilise champ
	 * {@code distance}.
	 * 
	 * @param e1
	 * @param e2
	 * @return
	 */
	private int simpleCompare(final T e1, final T e2) {
		if (e1.getDistance() == null || e1.getDistance() == null)
			return 0;

		return e1.getDistance().compareTo(e2.getDistance());
	}

	/**
	 * Comparer les 2 équipements par rapport au {@link #referentiel}
	 * 
	 * @param e1
	 * @param e2
	 * @return
	 */
	private int referentielCompare(final T e1, final T e2) {
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
