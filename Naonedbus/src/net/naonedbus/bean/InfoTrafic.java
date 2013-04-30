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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.naonedbus.widget.item.SectionItem;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.os.Parcel;
import android.os.Parcelable;

public class InfoTrafic implements Serializable, Parcelable, SectionItem {

	private static final long serialVersionUID = -2516041836875800927L;
	private static final DateTimeFormatter fullDateParser = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC();
	private static final DateTimeFormatter simpleDateParser = DateTimeFormat.forPattern("MM/yyyy").withZoneUTC();
	private static final DateTimeFormatter timeParser = DateTimeFormat.forPattern("HH:mm").withZoneUTC();

	private String mCode;
	private String mIntitule;
	private String mResume;
	private String mTexteVocal;
	private String mDateDebutString;
	private String mDateFinString;
	private String mHeureDebutString;
	private String mHeureFinString;
	private boolean mPerturbationTerminee;
	private String mTroncons;

	private List<String> mLignes = new ArrayList<String>();

	private Object mSection;

	private DateTime mDateDebut;
	private DateTime mDateFin;
	private String mDateFormated;

	public InfoTrafic() {

	}

	public InfoTrafic(final Parcel in) {
		mCode = in.readString();
		mDateDebutString = in.readString();
		mDateFinString = in.readString();
		mDateFormated = in.readString();
		mHeureDebutString = in.readString();
		mHeureFinString = in.readString();
		mIntitule = in.readString();
		mPerturbationTerminee = in.readInt() == 1;
		mResume = in.readString();
		mTexteVocal = in.readString();
		mTroncons = in.readString();
		in.readList(mLignes, Ligne.class.getClassLoader());
	}

	public String getCode() {
		return mCode;
	}

	public String getIntitule() {
		return mIntitule;
	}

	public String getResume() {
		return mResume;
	}

	public void setResume(String resume) {
		this.mResume = resume;
	}

	public String getTexteVocal() {
		return mTexteVocal;
	}

	public DateTime getDateDebut() {
		if (this.mDateDebut == null) {
			this.mDateDebut = parseDate(this.mDateDebutString, this.mHeureDebutString);
		}
		return this.mDateDebut;
	}

	public DateTime getDateFin() {
		if (this.mDateFin == null) {
			this.mDateFin = parseDate(this.mDateFinString, this.mHeureFinString);
		}
		return this.mDateFin;
	}

	public String getDateFormated() {
		return mDateFormated;
	}

	public void setDateFormated(String dateFormated) {
		this.mDateFormated = dateFormated;
	}

	public boolean isPerturbationTerminee() {
		return mPerturbationTerminee;
	}

	public String getTroncons() {
		return mTroncons;
	}

	public void addLignes(String ligne) {
		this.mLignes.add(ligne);
	}

	public List<String> getLignes() {
		return mLignes;
	}

	public void setSection(Object section) {
		this.mSection = section;
	}

	public void setCode(String code) {
		this.mCode = code;
	}

	public void setIntitule(String intitule) {
		this.mIntitule = intitule;
	}

	public void setTexteVocal(String texteVocal) {
		this.mTexteVocal = texteVocal;
	}

	public void setDateDebutString(String dateDebutString) {
		this.mDateDebutString = dateDebutString;
	}

	public void setDateFinString(String dateFinString) {
		this.mDateFinString = dateFinString;
	}

	public void setHeureDebutString(String heureDebutString) {
		this.mHeureDebutString = heureDebutString;
	}

	public void setHeureFinString(String heureFinString) {
		this.mHeureFinString = heureFinString;
	}

	public void setPerturbationTerminee(boolean perturbationTerminee) {
		this.mPerturbationTerminee = perturbationTerminee;
	}

	public void setTroncons(String troncons) {
		this.mTroncons = troncons;
	}

	public void setLignes(List<String> lignes) {
		this.mLignes = lignes;
	}

	public void setDateDebut(DateTime dateDebut) {
		this.mDateDebut = dateDebut;
	}

	public void setDateFin(DateTime dateFin) {
		this.mDateFin = dateFin;
	}

	@Override
	public Object getSection() {
		return mSection;
	}

	public InfoTrafic clone() {
		final InfoTrafic clone = new InfoTrafic();
		clone.mCode = mCode;
		clone.mDateDebut = mDateDebut;
		clone.mDateDebutString = mDateDebutString;
		clone.mDateFin = mDateFin;
		clone.mDateFinString = mDateFinString;
		clone.mDateFormated = mDateFormated;
		clone.mHeureDebutString = mHeureDebutString;
		clone.mHeureFinString = mHeureFinString;
		clone.mIntitule = mIntitule;
		clone.mLignes = mLignes;
		clone.mPerturbationTerminee = mPerturbationTerminee;
		clone.mResume = mResume;
		clone.mTexteVocal = mTexteVocal;
		clone.mTroncons = mTroncons;
		return clone;
	}

	@Override
	public String toString() {
		return new StringBuilder("[").append(this.mCode).append(";").append(this.mIntitule).append(";")
				.append((Ligne) mSection).append("]").toString();
	}

	/**
	 * Parser une date selon les différents formats possibles : jj/mm/aaaa ou
	 * mm/aaaa et éventuellement une heure.
	 * 
	 * @param date
	 * @param heure
	 * @return le DateTime correspondant
	 */
	private static final DateTime parseDate(String date, String heure) {
		DateTime dateTime = null;
		if (date.length() == 10) {
			dateTime = fullDateParser.parseDateTime(date);
		} else if (date.length() == 7) {
			dateTime = simpleDateParser.parseDateTime(date);
		}
		if (dateTime != null && heure != null && heure.length() == 5) {
			dateTime = dateTime.plusMinutes(timeParser.parseDateTime(heure).minuteOfDay().get());
		}
		return dateTime;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mCode);
		dest.writeString(mDateDebutString);
		dest.writeString(mDateFinString);
		dest.writeString(mDateFormated);
		dest.writeString(mHeureDebutString);
		dest.writeString(mHeureFinString);
		dest.writeString(mIntitule);
		dest.writeInt(mPerturbationTerminee ? 1 : 0);
		dest.writeString(mResume);
		dest.writeString(mTexteVocal);
		dest.writeString(mTroncons);
		dest.writeList(mLignes);
	}

	public static final Parcelable.Creator<InfoTrafic> CREATOR = new Parcelable.Creator<InfoTrafic>() {
		public InfoTrafic createFromParcel(Parcel in) {
			return new InfoTrafic(in);
		}

		public InfoTrafic[] newArray(int size) {
			return new InfoTrafic[size];
		}
	};

}
