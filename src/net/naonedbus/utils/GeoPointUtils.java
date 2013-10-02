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
package net.naonedbus.utils;

import net.naonedbus.bean.Equipement;
import net.naonedbus.bean.parking.Parking;
import android.location.Location;
import android.location.LocationManager;

import com.google.android.maps.GeoPoint;

public abstract class GeoPointUtils {

	/**
	 * Calculer le GeoPoint d'un parking.
	 * 
	 * @param parking
	 * @return le GeoPoint
	 */
	public static GeoPoint getGeoPoint(final Parking parking) {
		return new GeoPoint((int) (parking.getLatitude() * 1E6), (int) (parking.getLongitude() * 1E6));
	}

	/**
	 * Calculer le GeoPoint d'un equipement.
	 * 
	 * @param item
	 * @return le GeoPoint
	 */
	public static GeoPoint getGeoPoint(final Equipement item) {
		return new GeoPoint((int) (item.getLatitude() * 1E6), (int) (item.getLongitude() * 1E6));
	}

	/**
	 * Calculer le GeoPoint d'une Location.
	 * 
	 * @param itemTitle
	 * @return le GeoPoint
	 */
	public static GeoPoint getGeoPoint(final Location location) {
		if (location == null)
			return null;
		return new GeoPoint((int) (location.getLatitude() * 1E6), (int) (location.getLongitude() * 1E6));
	}

	/**
	 * Convertir un GeoPoint en Location.
	 * 
	 * @param geoPoint
	 * @return l'object Location avec la latitude et longitude du GeoPoint.
	 */
	public static Location getLocation(final GeoPoint geoPoint) {
		if (geoPoint == null)
			return null;
		final Location location = new Location(LocationManager.GPS_PROVIDER);
		location.setLatitude(geoPoint.getLatitudeE6() / 1E6);
		location.setLongitude(geoPoint.getLongitudeE6() / 1E6);
		return location;
	}
}
