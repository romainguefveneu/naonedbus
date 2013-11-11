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

import net.naonedbus.R;
import net.naonedbus.widget.item.SectionItem;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

public class Route implements SectionItem, Parcelable {

	public static class Builder {
		private int mId;
		private String mCode;
		private String mLetter;
		private String mHeadsignFrom;
		private String mHeadsignTo;
		private int mBackColor;
		private int mFrontColor;
		private Object mSection;

		public Builder setId(final int id) {
			mId = id;
			return this;
		}

		public Builder setCode(final String code) {
			mCode = code;
			return this;
		}

		public Builder setLetter(final String letter) {
			mLetter = letter;
			return this;
		}

		public Builder setHeadsignFrom(final String headsignFrom) {
			mHeadsignFrom = headsignFrom;
			return this;
		}

		public Builder setHeadsignTo(final String headsignTo) {
			mHeadsignTo = headsignTo;
			return this;
		}

		public Builder setBackColor(final int color) {
			mBackColor = color;
			return this;
		}

		public Builder setFrontColor(final int color) {
			mFrontColor = color;
			return this;
		}

		public Builder setSection(final Object section) {
			mSection = section;
			return this;
		}

		public Route build() {
			return new Route(this);
		}
	}

	private final int mId;
	private final String mCode;
	private final String mLetter;
	private final String mHeadsignFrom;
	private final String mHeadsignTo;
	private final String mName;
	private final int mBackColor;
	private final int mFrontColor;

	private Object mSection;
	private Drawable mBackground;

	public static Route buildAllLigneItem(final Context context) {
		return new Route(context.getString(R.string.all_routes), context.getString(R.string.infinity_symbole));
	}

	private Route(final String name, final String letter) {
		mId = -1;
		mCode = null;
		mLetter = letter;
		mHeadsignFrom = null;
		mHeadsignTo = null;
		mName = name;
		mBackColor = 0;
		mFrontColor = Color.BLACK;
		mSection = null;
	}

	private Route(final Builder builder) {
		mId = builder.mId;
		mCode = builder.mCode;
		mLetter = builder.mLetter;
		mHeadsignFrom = builder.mHeadsignFrom;
		mHeadsignTo = builder.mHeadsignTo;
		mName = mHeadsignFrom + " \u2194 " + mHeadsignTo;
		mBackColor = builder.mBackColor;
		mFrontColor = builder.mFrontColor;
		mSection = builder.mSection;
	}

	protected Route(final Parcel in) {
		mId = in.readInt();
		mCode = in.readString();
		mLetter = in.readString();
		mHeadsignFrom = in.readString();
		mHeadsignTo = in.readString();
		mName = in.readString();
		mBackColor = in.readInt();
		mFrontColor = in.readInt();
	}

	public int getId() {
		return mId;
	}

	public String getCode() {
		return mCode;
	}

	public String getLetter() {
		return mLetter;
	}

	public String getHeadsigneFrom() {
		return mHeadsignFrom;
	}

	public String getHeadsignTo() {
		return mHeadsignTo;
	}

	public String getName() {
		return mName;
	}

	public int getBackColor() {
		return mBackColor;
	}

	public int getFrontColor() {
		return mFrontColor;
	}

	public Drawable getBackground() {
		return mBackground;
	}

	public void setBackground(final Drawable background) {
		mBackground = background;
	}

	public void setSection(final Object section) {
		mSection = section;
	}

	@Override
	public Object getSection() {
		return mSection;
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof Route))
			return false;

		final Route other = (Route) o;
		return other.mId == mId;
	}

	@Override
	public int hashCode() {
		return mId * 31;
	}

	@Override
	public String toString() {
		return new StringBuilder("[").append(mId).append(";").append(mCode).append(";").append(mHeadsignFrom)
				.append(";").append(mHeadsignTo).append("]").toString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeInt(mId);
		dest.writeString(mCode);
		dest.writeString(mLetter);
		dest.writeString(mHeadsignFrom);
		dest.writeString(mHeadsignTo);
		dest.writeString(mName);
		dest.writeInt(mBackColor);
		dest.writeInt(mFrontColor);
	}

	public static final Parcelable.Creator<Route> CREATOR = new Parcelable.Creator<Route>() {
		@Override
		public Route createFromParcel(final Parcel in) {
			return new Route(in);
		}

		@Override
		public Route[] newArray(final int size) {
			return new Route[size];
		}
	};
}
