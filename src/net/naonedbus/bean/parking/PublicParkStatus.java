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
package net.naonedbus.bean.parking;

import net.naonedbus.R;

public enum PublicParkStatus {

	//@formatter:off
	INVALID(0, R.string.parking_invalide, R.color.parking_state_undefined), 
	CLOSED(1, R.string.parking_ferme,R.color.parking_state_closed), 
	SUBSCRIBERS(2, R.string.parking_abonne, R.color.parking_state_subscriber), 
	OPEN(5, R.string.parking, R.color.parking_state_blue);
	//@formatter:on

	private int mValue;
	private int mTitleRes;
	private int mColorRes;

	private PublicParkStatus(int value, int titleRes, int colorRes) {
		mValue = value;
		mTitleRes = titleRes;
		mColorRes = colorRes;
	}

	public int getValue() {
		return mValue;
	}

	public int getTitleRes() {
		return mTitleRes;
	}

	public int getColorRes() {
		return mColorRes;
	}

}
