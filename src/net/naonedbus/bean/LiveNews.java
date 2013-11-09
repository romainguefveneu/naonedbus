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
package net.naonedbus.bean;

import net.naonedbus.widget.item.SectionItem;

import org.joda.time.DateTime;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

public class LiveNews implements SectionItem, Parcelable {

	private int mId;
	private String mRouteCode;
	private String mDirectionCode;
	private String mStopCode;
	private String mMessage;
	private String mSource;
	private long mTimestamp;

	private Object mSection;
	private String mDelay;
	private DateTime mDateTime;
	private transient Drawable mBackground;
	private transient Route mRoute;
	private transient Direction mDirection;
	private transient Stop mStop;

	public LiveNews() {
	}

	public Integer getId() {
		return mId;
	}

	public void setId(final Integer id) {
		mId = id;
	}

	public String getRouteCode() {
		return mRouteCode;
	}

	public void setRouteCode(final String routeCode) {
		mRouteCode = routeCode;
	}

	public String getDirectionCode() {
		return mDirectionCode;
	}

	public void setDirectionCode(final String directionCode) {
		mDirectionCode = directionCode;
	}

	public String getStopCode() {
		return mStopCode;
	}

	public void setStopCode(final String stopCode) {
		mStopCode = stopCode;
	}

	public String getMessage() {
		return mMessage;
	}

	public void setMessage(final String message) {
		mMessage = message;
	}

	public Long getTimestamp() {
		return mTimestamp;
	}

	public void setTimestamp(final Long timestamp) {
		mTimestamp = timestamp;
	}

	public void setSource(final String source) {
		mSource = source;
	}

	public String getSource() {
		return mSource;
	}

	public void setSection(final Object section) {
		mSection = section;
	}

	@Override
	public Object getSection() {
		return mSection;
	}

	public String getDelay() {
		return mDelay;
	}

	public void setDelay(final String delay) {
		mDelay = delay;
	}

	public DateTime getDateTime() {
		return mDateTime;
	}

	public void setDateTime(final DateTime dateTime) {
		mDateTime = dateTime;
	}

	public Drawable getBackground() {
		return mBackground;
	}

	public void setBackground(final Drawable background) {
		mBackground = background;
	}

	public void setRoute(final Route route) {
		mRoute = route;
	}

	public Route getRoute() {
		return mRoute;
	}

	public void setDirection(final Direction route) {
		mDirection = route;
	}

	public Direction getDirection() {
		return mDirection;
	}

	public void setStop(final Stop stop) {
		mStop = stop;
	}

	public Stop getStop() {
		return mStop;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append(mRouteCode).append(" | ").append(mDirectionCode).append(" | ").append(mStopCode).append(" | ")
				.append(mSource).append(" | ").append(mMessage);
		return builder.toString();
	}

	protected LiveNews(final Parcel in) {
		mId = in.readInt();
		mRouteCode = in.readString();
		mDirectionCode = in.readString();
		mStopCode = in.readString();
		mMessage = in.readString();
		mSource = in.readString();
		mTimestamp = in.readLong();
		mDelay = in.readString();
		mRoute = in.readParcelable(Route.class.getClassLoader());
		mDirection = in.readParcelable(Direction.class.getClassLoader());
		mStop = in.readParcelable(Stop.class.getClassLoader());
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeInt(mId);
		dest.writeString(mRouteCode);
		dest.writeString(mDirectionCode);
		dest.writeString(mStopCode);
		dest.writeString(mMessage);
		dest.writeString(mSource);
		dest.writeLong(mTimestamp);
		dest.writeString(mDelay);
		dest.writeParcelable(mRoute, 0);
		dest.writeParcelable(mDirection, 0);
		dest.writeParcelable(mStop, 0);
	}

	public static final Parcelable.Creator<LiveNews> CREATOR = new Parcelable.Creator<LiveNews>() {
		@Override
		public LiveNews createFromParcel(final Parcel in) {
			return new LiveNews(in);
		}

		@Override
		public LiveNews[] newArray(final int size) {
			return new LiveNews[size];
		}
	};
}
