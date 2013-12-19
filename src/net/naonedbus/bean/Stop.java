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

public class Stop implements Parcelable {

	public static final int ORIENTATION_NONE = 0;
	public static final int ORIENTATION_RIGHT_LEFT = 1;
	public static final int ORIENTATION_LEFT_RIGHT = 2;
	public static final int ORIENTATION_STRAIGHT = 3;

	public static class Builder {
		private int mId;
		private String mRouteCode;
		private String mRouteLetter;
		private String mDirectionCode;
		private String mStopCode;
		private String mEquipmentCode;
		private String mNormalizedName;
		private Float mLatitude;
		private Float mLongitude;
		private int mEquipmentId;
		private int mOrder;
		private long mStepType;
		private String mName;
		private Float mDistance;

		public Builder setId(final int id) {
			mId = id;
			return this;
		}

		public Builder setNomArret(final String stopName) {
			mName = stopName;
			return this;
		}

		public Builder setCodeLigne(final String routeCode) {
			mRouteCode = routeCode;
			return this;
		}

		public Builder setLettre(final String lettre) {
			mRouteLetter = lettre;
			return this;
		}

		public Builder setCodeSens(final String directionCode) {
			mDirectionCode = directionCode;
			return this;
		}

		public Builder setCodeArret(final String codeArret) {
			mStopCode = codeArret;
			return this;
		}

		public Builder setCodeEquipement(final String codeEquipement) {
			mEquipmentCode = codeEquipement;
			return this;
		}

		public Builder setNormalizedNom(final String normalizedNom) {
			mNormalizedName = normalizedNom;
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
			mEquipmentId = idStation;
			return this;
		}

		public Builder setOrdre(final int ordre) {
			mOrder = ordre;
			return this;
		}

		public Builder setStepType(final long stepType) {
			mStepType = stepType;
			return this;
		}

		public Builder setDistance(final Float distance) {
			mDistance = distance;
			return this;
		}

		public Stop build() {
			return new Stop(this);
		}
	}

	private final int mId;
	private final String mRouteCode;
	private final String mRouteLetter;
	private final String mDirectionCode;
	private final String mStopCode;
	private final String mEquipmentCode;
	private final String mNormalizedName;
	private final String mName;
	private final Float mLatitude;
	private final Float mLongitude;
	private final int mEquipmentId;
	private final int mOrder;
	private final long mStepType;
	private Float mDistance;

	protected Stop(final Builder builder) {
		mId = builder.mId;
		mRouteCode = builder.mRouteCode;
		mRouteLetter = builder.mRouteLetter;
		mDirectionCode = builder.mDirectionCode;
		mStopCode = builder.mStopCode;
		mEquipmentCode = builder.mEquipmentCode;
		mNormalizedName = builder.mNormalizedName;
		mLatitude = builder.mLatitude;
		mLongitude = builder.mLongitude;
		mEquipmentId = builder.mEquipmentId;
		mOrder = builder.mOrder;
		mStepType = builder.mStepType;
		mName = builder.mName;
		mDistance = builder.mDistance;
	}

	protected Stop(final Parcel in) {
		mId = in.readInt();
		mRouteCode = in.readString();
		mRouteLetter = in.readString();
		mDirectionCode = in.readString();
		mStopCode = in.readString();
		mEquipmentCode = in.readString();
		mNormalizedName = in.readString();
		mLatitude = in.readFloat();
		mLongitude = in.readFloat();
		mEquipmentId = in.readInt();
		mOrder = in.readInt();
		mStepType = in.readLong();
		mName = in.readString();
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof Stop)) {
			return false;
		}
		final Stop other = (Stop) o;
		return other.mId == mId;
	}

	@Override
	public int hashCode() {
		return mId * 31;
	}

	public int getId() {
		return mId;
	}

	public String getCodeLigne() {
		return mRouteCode;
	}

	public String getLettre() {
		return mRouteLetter;
	}

	public String getCodeSens() {
		return mDirectionCode;
	}

	public String getCodeArret() {
		return mStopCode;
	}

	public String getCodeEquipement() {
		return mEquipmentCode;
	}

	public String getNormalizedNom() {
		return mNormalizedName;
	}

	public Float getLatitude() {
		return mLatitude;
	}

	public Float getLongitude() {
		return mLongitude;
	}

	public int getIdStation() {
		return mEquipmentId;
	}

	public int getOrdre() {
		return mOrder;
	}

	public String getName() {
		return mName;
	}

	public void setDistance(final Float distance) {
		mDistance = distance;
	}

	public Float getDistance() {
		return mDistance;
	}

	public long getStepType() {
		return mStepType;
	}

	public int getStepStyle() {
		return (int) mStepType & 3;
	}

	public int getStepOrientationTop() {
		return (int) (mStepType >> 2) & 3;
	}

	public int getStepOrientationBottom() {
		return (int) (mStepType >> 4) & 3;
	}

	public int getStepOtherLinesStyleTop() {
		return (int) (mStepType >> 6) & 1;
	}

	public int getStepOtherLinesStyleBottom() {
		return (int) (mStepType >> 7) & 1;
	}

	public int getStepDepth() {
		return (int) (mStepType >> 8) & 15;
	}

	public int getStepDepthsVisibility() {
		return (int) mStepType >> 12;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeInt(mId);
		dest.writeString(mRouteCode);
		dest.writeString(mRouteLetter);
		dest.writeString(mDirectionCode);
		dest.writeString(mStopCode);
		dest.writeString(mEquipmentCode);
		dest.writeString(mNormalizedName);
		dest.writeFloat(mLatitude);
		dest.writeFloat(mLongitude);
		dest.writeInt(mEquipmentId);
		dest.writeInt(mOrder);
		dest.writeLong(mStepType);
		dest.writeString(mName);
	}

	public static final Parcelable.Creator<Stop> CREATOR = new Parcelable.Creator<Stop>() {
		@Override
		public Stop createFromParcel(final Parcel in) {
			return new Stop(in);
		}

		@Override
		public Stop[] newArray(final int size) {
			return new Stop[size];
		}
	};

	@Override
	public String toString() {
		return "[" + mName + ";" + mStopCode + ";" + mDirectionCode + ";" + mRouteCode + "]";
	}
}
