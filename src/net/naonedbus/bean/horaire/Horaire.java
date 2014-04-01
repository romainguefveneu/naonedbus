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

import net.naonedbus.widget.item.SectionItem;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

public class Horaire implements SectionItem {

	private long mId;
	private String mTerminus;
	private DateMidnight mJour;
	private DateTime mHoraire;

	private String mDelai;
	private Object mSection;
	private boolean mIsBeforeNow;

	public Horaire() {
	}

	public Horaire(final Horaire horaire) {
		mId = horaire.getId();
		mJour = horaire.getJour();
		mTerminus = horaire.getTerminus();
		mHoraire = horaire.getHoraire();
	}

	public long getId() {
		return mId;
	}

	public void setId(long id) {
		mId = id;
	}

	public String getTerminus() {
		return mTerminus;
	}

	public void setTerminus(String terminus) {
		mTerminus = terminus;
	}

	public DateMidnight getJour() {
		return mJour;
	}

	public void setJour(DateMidnight jour) {
		mJour = jour;
	}

	public DateTime getHoraire() {
		return mHoraire;
	}

	public void setHoraire(DateTime horaire) {
		mHoraire = horaire;
	}

	public String getDelai() {
		return mDelai;
	}

	public void setDelai(String delai) {
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

	@Override
	public String toString() {
		return "[" + mId + ";" + mJour + ";" + mHoraire + "]";
	}

}
