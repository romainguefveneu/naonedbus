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

import net.naonedbus.R;

/**
 * @author romain.guefveneu
 * 
 */
public class ParkingUtils {
	private static final double SEUIL_ROUGE = 1;
	private static final double SEUIL_ORANGE = 15;

	public static int getSeuilCouleurId(int placesDisponibles) {
		int couleurResId;
		if (placesDisponibles < SEUIL_ROUGE) {
			couleurResId = R.color.parking_state_red;
		} else if (placesDisponibles < SEUIL_ORANGE) {
			couleurResId = R.color.parking_state_orange;
		} else {
			couleurResId = R.color.parking_state_blue;
		}

		return couleurResId;
	}

	public static int getSeuilTextId(int placesDisponibles) {
		int textResId;
		if (placesDisponibles < SEUIL_ROUGE) {
			textResId = R.string.thats_full;
		} else if (placesDisponibles < SEUIL_ORANGE) {
			textResId = R.string.not_many_spaces_left;
		} else {
			textResId = R.string.many_spaces;
		}

		return textResId;
	}

}
