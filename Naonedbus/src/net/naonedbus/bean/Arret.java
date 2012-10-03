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
	public Float latitude;
	public Float longitude;
	public int idStation;
	public String nom;

	public Arret() {
	}

	public Arret(int id, String nom) {
		super();
		this._id = id;
		this.nom = nom;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Arret)) {
			return false;
		}
		final Arret autreArret = (Arret) o;
		return autreArret._id == _id;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public int get_id() {
		return _id;
	}

	public String getCodeLigne() {
		return codeLigne;
	}

	public String getLettre() {
		return lettre;
	}

	public String getCodeSens() {
		return codeSens;
	}

	public String getCode() {
		return code;
	}

	public String getCodeEquipement() {
		return codeEquipement;
	}

	public String getNormalizedNom() {
		return normalizedNom;
	}

	public Float getLatitude() {
		return latitude;
	}

	public Float getLongitude() {
		return longitude;
	}

	public int getIdStation() {
		return idStation;
	}

	public String getNom() {
		return nom;
	}

}
