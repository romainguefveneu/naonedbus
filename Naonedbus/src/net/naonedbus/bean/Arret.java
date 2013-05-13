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

	private final int mId;
	private final String mCodeLigne;
	private final String mLettre;
	private final String mCodeSens;
	private final String mCodeArret;
	private final String mCodeEquipement;
	private final String mNormalizedNom;
	private final Float mLatitude;
	private final Float mLongitude;
	private final int mIdStation;
	private final int mOrdre;
	private final String mNomArret;
	private Float mDistance;

	public static class Builder {
		private int mId;
		private String mCodeLigne;
		private String mLettre;
		private String mCodeSens;
		private String mCodeArret;
		private String mCodeEquipement;
		private String mNormalizedNom;
		private Float mLatitude;
		private Float mLongitude;
		private int mIdStation;
		private int mOrdre;
		private String mNomArret;
		private Float mDistance;

		public Builder setId(final int id) {
			mId = id;
			return this;
		}

		public Builder setNomArret(final String nomArret) {
			mNomArret = nomArret;
			return this;
		}

		public Builder setCodeLigne(final String codeLigne) {
			mCodeLigne = codeLigne;
			return this;
		}

		public Builder setLettre(final String lettre) {
			mLettre = lettre;
			return this;
		}

		public Builder setCodeSens(final String codeSens) {
			mCodeSens = codeSens;
			return this;
		}

		public Builder setCodeArret(final String codeArret) {
			mCodeArret = codeArret;
			return this;
		}

		public Builder setCodeEquipement(final String codeEquipement) {
			mCodeEquipement = codeEquipement;
			return this;
		}

		public Builder setNormalizedNom(final String normalizedNom) {
			mNormalizedNom = normalizedNom;
			return this;
		}

		public Builder setLatitude(final Float latitude) {
			mLatitude = latitude;
			return this;
		}

		public Builder setLongitude(final Float longitude) {
			mLongitude = longitude;
			return this;
		}

		public Builder setIdStation(final int idStation) {
			mIdStation = idStation;
			return this;
		}

		public Builder setOrdre(final int ordre) {
			mOrdre = ordre;
			return this;
		}

		public Builder setDistance(final Float distance) {
			mDistance = distance;
			return this;
		}

		public Arret build() {
			return new Arret(this);
		}
	}

	protected Arret(final Builder builder) {
		mId = builder.mId;
		mCodeLigne = builder.mCodeLigne;
		mLettre = builder.mLettre;
		mCodeSens = builder.mCodeSens;
		mCodeArret = builder.mCodeArret;
		mCodeEquipement = builder.mCodeEquipement;
		mNormalizedNom = builder.mNormalizedNom;
		mLatitude = builder.mLatitude;
		mLongitude = builder.mLongitude;
		mIdStation = builder.mIdStation;
		mOrdre = builder.mOrdre;
		mNomArret = builder.mNomArret;
		mDistance = builder.mDistance;
	}

	protected Arret(final Parcel in) {
		mId = in.readInt();
		mCodeLigne = in.readString();
		mLettre = in.readString();
		mCodeSens = in.readString();
		mCodeArret = in.readString();
		mCodeEquipement = in.readString();
		mNormalizedNom = in.readString();
		mLatitude = in.readFloat();
		mLongitude = in.readFloat();
		mIdStation = in.readInt();
		mOrdre = in.readInt();
		mNomArret = in.readString();
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof Arret)) {
			return false;
		}
		final Arret autreArret = (Arret) o;
		return autreArret.mId == mId;
	}

	@Override
	public int hashCode() {
		return mId * 31;
	}

	public int getId() {
		return mId;
	}

	public String getCodeLigne() {
		return mCodeLigne;
	}

	public String getLettre() {
		return mLettre;
	}

	public String getCodeSens() {
		return mCodeSens;
	}

	public String getCodeArret() {
		return mCodeArret;
	}

	public String getCodeEquipement() {
		return mCodeEquipement;
	}

	public String getNormalizedNom() {
		return mNormalizedNom;
	}

	public Float getLatitude() {
		return mLatitude;
	}

	public Float getLongitude() {
		return mLongitude;
	}

	public int getIdStation() {
		return mIdStation;
	}

	public int getOrdre() {
		return mOrdre;
	}

	public String getNomArret() {
		return mNomArret;
	}

	public void setDistance(final Float distance) {
		mDistance = distance;
	}

	public Float getDistance() {
		return mDistance;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeInt(mId);
		dest.writeString(mCodeLigne);
		dest.writeString(mLettre);
		dest.writeString(mCodeSens);
		dest.writeString(mCodeArret);
		dest.writeString(mCodeEquipement);
		dest.writeString(mNormalizedNom);
		dest.writeFloat(mLatitude);
		dest.writeFloat(mLongitude);
		dest.writeInt(mIdStation);
		dest.writeInt(mOrdre);
		dest.writeString(mNomArret);
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
		return "[" + mNomArret + ";" + mCodeArret + ";" + mCodeSens + ";" + mCodeLigne + "]";
	}
}
