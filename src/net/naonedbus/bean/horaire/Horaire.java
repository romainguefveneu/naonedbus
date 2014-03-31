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
package net.naonedbus.bean.horaire;

import java.util.Date;

import net.naonedbus.widget.item.SectionItem;

import org.joda.time.DateTime;

public class Horaire implements SectionItem {

	private int mId;
	private String mTerminus;
	private int mYear;
	private int mDayOfYear;
	private int mMinutes;

	private DateTime mDate;
	private String mDelai;
	private Object mSection;
	private boolean mIsBeforeNow;

	public Horaire() {
	}

	public Horaire(final Horaire horaire) {
		mId = horaire.getId();
		mDayOfYear = horaire.getDayOfYear();
		mTerminus = horaire.getTerminus();
		mMinutes = horaire.getMinutes();
	}

	public int getId() {
		return mId;
	}

	public void setId(final int id) {
		mId = id;
	}

	public String getTerminus() {
		return mTerminus;
	}


	public void setTerminus(final String terminus) {
		mTerminus = terminus;
	}

	public int getYear() {
		return mYear;
	}

	public void setYear(int year) {
		mYear = year;
	}

	public int getDayOfYear() {
		return mDayOfYear;
	}

	public void setDayOfYear(int dayOfYear) {
		mDayOfYear = dayOfYear;
	}

	public int getMinutes() {
		return mMinutes;
	}

	public void setMinutes(int minutes) {
		mMinutes = minutes;
	}

	public DateTime getDateTime() {
		return mDate;
	}

	public void setDateTime(final DateTime date) {
		mDate = date;
	}

	public String getDelai() {
		return mDelai;
	}

	public void setDelai(final String delai) {
		mDelai = delai;
	}

	public boolean isBeforeNow() {
		return mIsBeforeNow;
	}

	public void setBeforeNow(final boolean isBeforeNow) {
		mIsBeforeNow = isBeforeNow;
	}
	
	public long getTimestamp() {
		return mDate.getMillis();
	}

	public void setSection(final Object section) {
		mSection = section;
	}

	@Override
	public Object getSection() {
		return mSection;
	}

	@Override
	public String toString() {
		return mDate.toString();
	}



}
