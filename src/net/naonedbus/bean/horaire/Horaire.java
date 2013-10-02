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

public class Horaire implements SectionItem {

	private int mId;
	private String mTerminus;
	private long mDayTrip;
	private long mTimestamp;

	private Date mDate;
	private String mDelai;
	private Object mSection;
	private boolean mIsBeforeNow;

	public Horaire() {
	}

	public Horaire(final Horaire horaire) {
		mId = horaire.getId();
		mDayTrip = horaire.getDayTrip();
		mTerminus = horaire.getTerminus();
		mTimestamp = horaire.getTimestamp();
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

	public long getTimestamp() {
		return mTimestamp;
	}

	public long getDayTrip() {
		return mDayTrip;
	}

	public void setDayTrip(final long dayTrip) {
		mDayTrip = dayTrip;
	}

	public void setTerminus(final String terminus) {
		mTerminus = terminus;
	}

	public void setTimestamp(final long timestamp) {
		mTimestamp = timestamp;
		mDate = new Date(timestamp);
	}

	public Date getDate() {
		return mDate;
	}

	public void setDate(final Date date) {
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

	public void setSection(final Object section) {
		mSection = section;
	}

	@Override
	public Object getSection() {
		return mSection;
	}

}
