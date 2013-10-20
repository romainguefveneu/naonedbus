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

import net.naonedbus.model.common.ICommentaire;
import net.naonedbus.widget.item.SectionItem;

import org.joda.time.DateTime;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

public class Comment implements ICommentaire, SectionItem, Parcelable {

	private static final long serialVersionUID = -9031229899288954850L;

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

	public Comment() {
	}

	@Override
	public Integer getId() {
		return mId;
	}

	@Override
	public void setId(final Integer id) {
		mId = id;
	}

	@Override
	public String getCodeLigne() {
		return mRouteCode;
	}

	@Override
	public void setCodeLigne(final String codeLigne) {
		mRouteCode = codeLigne;
	}

	@Override
	public String getCodeSens() {
		return mDirectionCode;
	}

	@Override
	public void setCodeSens(final String codeSens) {
		mDirectionCode = codeSens;
	}

	@Override
	public String getCodeArret() {
		return mStopCode;
	}

	@Override
	public void setCodeArret(final String codeArret) {
		mStopCode = codeArret;
	}

	@Override
	public String getMessage() {
		return mMessage;
	}

	@Override
	public void setMessage(final String message) {
		mMessage = message;
	}

	@Override
	public Long getTimestamp() {
		return mTimestamp;
	}

	@Override
	public void setTimestamp(final Long timestamp) {
		mTimestamp = timestamp;
	}

	@Override
	public void setSource(final String source) {
		mSource = source;
	}

	@Override
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

	protected Comment(final Parcel in) {
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

	public static final Parcelable.Creator<Comment> CREATOR = new Parcelable.Creator<Comment>() {
		@Override
		public Comment createFromParcel(final Parcel in) {
			return new Comment(in);
		}

		@Override
		public Comment[] newArray(final int size) {
			return new Comment[size];
		}
	};
}
