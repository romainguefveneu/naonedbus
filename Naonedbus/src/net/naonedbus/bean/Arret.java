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

public class Arret implements Serializable {

	private static final long serialVersionUID = 6371968112229796931L;

	public int _id;
	public String codeLigne;
	public String lettre;
	public String codeSens;
	public String code;
	public String codeEquipement;
	public String normalizedNom;
	public float latitude;
	public float longitude;
	public int idStation;
	public String text;

	public Arret() {
	}

	public Arret(int id, String nom) {
		super();
		this._id = id;
		this.text = nom;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Arret)) {
			return false;
		}
		final Arret autreArret = (Arret) o;
		return autreArret._id == _id;
	}
}