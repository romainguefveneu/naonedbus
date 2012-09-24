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
package net.naonedbus.utils;

/**
 * @author romain
 * 
 */
public class DistanceUtils {

	public static String formatDist(float meters) {
		if (meters < 1000) {
			return ((int) meters) + " m";
		} else if (meters < 10000) {
			return formatDec(meters / 1000f, 1) + " km";
		} else {
			return ((int) (meters / 1000f)) + " km";
		}
	}

	private static String formatDec(float val, int dec) {
		int factor = (int) Math.pow(10, dec);

		int front = (int) (val);
		int back = (int) Math.abs(val * (factor)) % factor;

		return front + "," + back;
	}
}
