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

import android.os.Parcel;
import android.os.Parcelable;

public class Arret implements Parcelable {

	public int _id;
	public String codeLigne;
	public String lettre;
	public String codeSens;
	public String codeArret;
	public String codeEquipement;
	public String normalizedNom;
	public Float latitude;
	public Float longitude;
	public int idStation;
	public int ordre;
	public String nomArret;
	public Float distance;

	public Arret() {
	}

	public Arret(final int id, final String nom) {
		super();
		this._id = id;
		this.nomArret = nom;
	}

	protected Arret(final Parcel in) {
		_id = in.readInt();
		codeLigne = in.readString();
		lettre = in.readString();
		codeSens = in.readString();
		codeArret = in.readString();
		codeEquipement = in.readString();
		normalizedNom = in.readString();
		latitude = in.readFloat();
		longitude = in.readFloat();
		idStation = in.readInt();
		ordre = in.readInt();
		nomArret = in.readString();
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof Arret)) {
			return false;
		}
		final Arret autreArret = (Arret) o;
		return autreArret._id == _id;
	}

	@Override
	public int hashCode() {
		return _id * 31;
	}

	public int getId() {
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
		return codeArret;
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
		return nomArret;
	}

	public int getOrder() {
		return ordre;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeInt(_id);
		dest.writeString(codeLigne);
		dest.writeString(lettre);
		dest.writeString(codeSens);
		dest.writeString(codeArret);
		dest.writeString(codeEquipement);
		dest.writeString(normalizedNom);
		dest.writeFloat(latitude);
		dest.writeFloat(longitude);
		dest.writeInt(idStation);
		dest.writeInt(ordre);
		dest.writeString(nomArret);
	}

	public static final Parcelable.Creator<Arret> CREATOR = new Parcelable.Creator<Arret>() {
		@Override
		public Arret createFromParcel(final Parcel in) {
			return new Arret(in);
		}

		@Override
		public Arret[] newArray(final int size) {
			return new Arret[size];
		}
	};

	@Override
	public String toString() {
		return "[" + nomArret + ";" + codeArret + ";" + codeSens + ";" + codeLigne + "]";
	}
}
