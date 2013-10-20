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

import java.util.ArrayList;
import java.util.List;

import net.naonedbus.widget.item.SectionItem;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.os.Parcel;
import android.os.Parcelable;

public class InfoTrafic implements Parcelable, SectionItem {

	private static final DateTimeFormatter sFullDateParser = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC();
	private static final DateTimeFormatter sSimpleDateParser = DateTimeFormat.forPattern("MM/yyyy").withZoneUTC();
	private static final DateTimeFormatter sTimeParser = DateTimeFormat.forPattern("HH:mm").withZoneUTC();

	private String mCode;
	private String mTitle;
	private String mContent;
	private String mSpeechContent;
	private String mDateStartString;
	private String mDateEndString;
	private String mTimeStartString;
	private String mTimeEndString;
	private boolean mEnded;
	private String mRoadSection;

	private List<String> mRoutes = new ArrayList<String>();

	private Object mSection;

	private DateTime mStartDate;
	private DateTime mEndDate;
	private String mDateFormated;

	public InfoTrafic() {

	}

	public InfoTrafic(final Parcel in) {
		mCode = in.readString();
		mDateStartString = in.readString();
		mDateEndString = in.readString();
		mDateFormated = in.readString();
		mTimeStartString = in.readString();
		mTimeEndString = in.readString();
		mTitle = in.readString();
		mEnded = in.readInt() == 1;
		mContent = in.readString();
		mSpeechContent = in.readString();
		mRoadSection = in.readString();
		in.readList(mRoutes, String.class.getClassLoader());
	}

	public String getCode() {
		return mCode;
	}

	public String getTitle() {
		return mTitle;
	}

	public String getContent() {
		return mContent;
	}

	public void setContent(final String resume) {
		mContent = resume;
	}

	public String getSpeechContent() {
		return mSpeechContent;
	}

	public DateTime getStartDate() {
		if (mStartDate == null) {
			mStartDate = parseDate(mDateStartString, mTimeStartString);
		}
		return mStartDate;
	}

	public DateTime getEndDate() {
		if (mEndDate == null) {
			mEndDate = parseDate(mDateEndString, mTimeEndString);
		}
		return mEndDate;
	}

	public String getDateFormated() {
		return mDateFormated;
	}

	public void setDateFormated(final String dateFormated) {
		mDateFormated = dateFormated;
	}

	public boolean isEnded() {
		return mEnded;
	}

	public String getRoadSection() {
		return mRoadSection;
	}

	public void addRoute(final String routeCode) {
		mRoutes.add(routeCode);
	}

	public List<String> getRoutes() {
		return mRoutes;
	}

	public void setSection(final Object section) {
		mSection = section;
	}

	public void setCode(final String code) {
		mCode = code;
	}

	public void setTitle(final String title) {
		mTitle = title;
	}

	public void setSpeechContent(final String speechContent) {
		mSpeechContent = speechContent;
	}

	public void setDateStartString(final String dateStartString) {
		mDateStartString = dateStartString;
	}

	public void setDateEndString(final String dateEndString) {
		mDateEndString = dateEndString;
	}

	public void setTimeStartString(final String timeStartString) {
		mTimeStartString = timeStartString;
	}

	public void setTimeEndString(final String timeEndString) {
		mTimeEndString = timeEndString;
	}

	public void setEnded(final boolean ended) {
		mEnded = ended;
	}

	public void setRoadSection(final String roadSection) {
		mRoadSection = roadSection;
	}

	public void setRoutes(final List<String> routes) {
		mRoutes = routes;
	}

	public void setStartDate(final DateTime startDate) {
		mStartDate = startDate;
	}

	public void setEndDate(final DateTime endDate) {
		mEndDate = endDate;
	}

	@Override
	public Object getSection() {
		return mSection;
	}

	@Override
	public InfoTrafic clone() {
		final InfoTrafic clone = new InfoTrafic();
		clone.mCode = mCode;
		clone.mStartDate = mStartDate;
		clone.mDateStartString = mDateStartString;
		clone.mEndDate = mEndDate;
		clone.mDateEndString = mDateEndString;
		clone.mDateFormated = mDateFormated;
		clone.mTimeStartString = mTimeStartString;
		clone.mTimeEndString = mTimeEndString;
		clone.mTitle = mTitle;
		clone.mRoutes = mRoutes;
		clone.mEnded = mEnded;
		clone.mContent = mContent;
		clone.mSpeechContent = mSpeechContent;
		clone.mRoadSection = mRoadSection;
		return clone;
	}

	@Override
	public String toString() {
		return new StringBuilder("[").append(mCode).append(";").append(mTitle).append(";").append(mSection)
				.append("]").toString();
	}

	/**
	 * Parser une date selon les différents formats possibles : jj/mm/aaaa ou
	 * mm/aaaa et éventuellement une heure.
	 * 
	 * @param date
	 * @param heure
	 * @return le DateTime correspondant
	 */
	private static final DateTime parseDate(final String date, final String heure) {
		DateTime dateTime = null;
		if (date.length() == 10) {
			dateTime = sFullDateParser.parseDateTime(date);
		} else if (date.length() == 7) {
			dateTime = sSimpleDateParser.parseDateTime(date);
		}
		if (dateTime != null && heure != null && heure.length() == 5) {
			dateTime = dateTime.plusMinutes(sTimeParser.parseDateTime(heure).minuteOfDay().get());
		}
		return dateTime;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeString(mCode);
		dest.writeString(mDateStartString);
		dest.writeString(mDateEndString);
		dest.writeString(mDateFormated);
		dest.writeString(mTimeStartString);
		dest.writeString(mTimeEndString);
		dest.writeString(mTitle);
		dest.writeInt(mEnded ? 1 : 0);
		dest.writeString(mContent);
		dest.writeString(mSpeechContent);
		dest.writeString(mRoadSection);
		dest.writeList(mRoutes);
	}

	public static final Parcelable.Creator<InfoTrafic> CREATOR = new Parcelable.Creator<InfoTrafic>() {
		@Override
		public InfoTrafic createFromParcel(final Parcel in) {
			return new InfoTrafic(in);
		}

		@Override
		public InfoTrafic[] newArray(final int size) {
			return new InfoTrafic[size];
		}
	};

}
