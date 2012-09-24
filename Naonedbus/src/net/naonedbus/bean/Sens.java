/*
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
package net.naonedbus.bean;

import java.io.Serializable;

public class Sens implements Serializable {

	private static final long serialVersionUID = 1604101176510697928L;

	public int _id;
	public String code;
	public String codeLigne;
	public String text;

	public Sens() {
	}

	public Sens(final int id, final String nom) {
		this._id = id;
		this.text = nom;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Sens)) {
			return false;
		}

		final Sens autreSens = (Sens) o;
		return autreSens._id == _id;
	}

}
