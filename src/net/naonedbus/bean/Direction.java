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
import android.os.Parcel;
import android.os.Parcelable;

public class Direction implements SectionItem, Parcelable {

	public static Object section = new Object();

	private int mId;
	private int mServiceId;
	private String mCode;
	private String mRouteCode;
	private String mName;

	public Direction() {
	}

	public Direction(final int id, final String directionName) {
		mId = id;
		mName = directionName;
	}

	protected Direction(final Parcel in) {
		mId = in.readInt();
		mServiceId = in.readInt();
		mCode = in.readString();
		mRouteCode = in.readString();
		mName = in.readString();
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof Direction)) {
			return false;
		}

		final Direction autreSens = (Direction) o;
		return autreSens.mId == mId;
	}

	public int getId() {
		return mId;
	}

	public String getCode() {
		return mCode;
	}

	public String getRouteCode() {
		return mRouteCode;
	}

	public String getName() {
		return mName;
	}

	public int getServiceId() {
		return mServiceId;
	}

	@Override
	public Object getSection() {
		return section;
	}

	public void setId(final int id) {
		mId = id;
	}

	public void setCode(final String code) {
		mCode = code;
	}

	public void setRouteCode(final String routeCode) {
		mRouteCode = routeCode;
	}

	public void setName(final String name) {
		mName = name;
	}

	public void setServiceId(final int serviceId) {
		mServiceId = serviceId;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeInt(mId);
		dest.writeInt(mServiceId);
		dest.writeString(mCode);
		dest.writeString(mRouteCode);
		dest.writeString(mName);
	}

	public static final Parcelable.Creator<Direction> CREATOR = new Parcelable.Creator<Direction>() {
		@Override
		public Direction createFromParcel(final Parcel in) {
			return new Direction(in);
		}

		@Override
		public Direction[] newArray(final int size) {
			return new Direction[size];
		}
	};
}
