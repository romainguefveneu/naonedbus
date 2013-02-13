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
import android.os.Parcel;
import android.os.Parcelable;

public class Sens implements SectionItem, Parcelable {

	public static Object section = new Object();

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

	protected Sens(Parcel in) {
		_id = in.readInt();
		code = in.readString();
		codeLigne = in.readString();
		text = in.readString();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Sens)) {
			return false;
		}

		final Sens autreSens = (Sens) o;
		return autreSens._id == _id;
	}

	@Override
	public Object getSection() {
		return section;
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(_id);
		dest.writeString(code);
		dest.writeString(codeLigne);
		dest.writeString(text);
	}

	public static final Parcelable.Creator<Sens> CREATOR = new Parcelable.Creator<Sens>() {
		public Sens createFromParcel(Parcel in) {
			return new Sens(in);
		}

		public Sens[] newArray(int size) {
			return new Sens[size];
		}
	};
}
