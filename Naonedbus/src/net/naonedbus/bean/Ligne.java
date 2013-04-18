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

import net.naonedbus.widget.item.SectionItem;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

public class Ligne implements SectionItem, Parcelable {

	public int _id;
	public String code;
	public String lettre;
	public String depuis;
	public String vers;
	public String nom;
	public Drawable background;
	public int couleurBackground;
	public int couleurTexte;
	public Object section;

	public Ligne() {
	}

	public Ligne(final int id, final String nom) {
		this._id = id;
		this.nom = nom;
	}

	public Ligne(final int id, final String nom, final String lettre) {
		this(id, nom);
		this.lettre = lettre;
	}

	protected Ligne(Parcel in) {
		_id = in.readInt();
		code = in.readString();
		lettre = in.readString();
		depuis = in.readString();
		vers = in.readString();
		nom = in.readString();
		couleurBackground = in.readInt();
		couleurTexte = in.readInt();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Ligne))
			return false;

		final Ligne autreLigne = (Ligne) o;
		return autreLigne._id == _id;
	}

	@Override
	public Object getSection() {
		return this.section;
	}

	@Override
	public String toString() {
		return new StringBuilder("[").append(_id).append(";").append(code).append(";").append(depuis).append(";")
				.append(vers).append("]").toString();
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(_id);
		dest.writeString(code);
		dest.writeString(lettre);
		dest.writeString(depuis);
		dest.writeString(vers);
		dest.writeString(nom);
		dest.writeInt(couleurBackground);
		dest.writeInt(couleurTexte);
	}

	public static final Parcelable.Creator<Ligne> CREATOR = new Parcelable.Creator<Ligne>() {
		public Ligne createFromParcel(Parcel in) {
			return new Ligne(in);
		}

		public Ligne[] newArray(int size) {
			return new Ligne[size];
		}
	};
}
