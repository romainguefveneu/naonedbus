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
package net.naonedbus.bean.parking.pub;

import net.naonedbus.R;

/**
 * Status des parkings de Nantes.
 * 
 * @author romain
 * 
 */
public enum ParkingPublicStatut {
	INVALIDE(0, R.string.parking_invalide, R.color.parking_state_undefined), FERME(1, R.string.parking_ferme,
			R.color.parking_state_closed), ABONNES(2, R.string.parking_abonne, R.color.parking_state_subscriber), OUVERT(5, R.string.parking,
			R.color.parking_state_blue);

	private int value;
	private int titleRes;
	private int colorRes;

	private ParkingPublicStatut(int value, int titleRes, int colorRes) {
		this.value = value;
		this.titleRes = titleRes;
		this.colorRes = colorRes;
	}

	public int getValue() {
		return value;
	}

	public int getTitleRes() {
		return titleRes;
	}

	public int getColorRes() {
		return colorRes;
	}

}
