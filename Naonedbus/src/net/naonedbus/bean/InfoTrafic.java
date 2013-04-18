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

	private String code;
	private String intitule;
	private String resume;
	private String texteVocal;
	private String dateDebutString;
	private String dateFinString;
	private String heureDebutString;
	private String heureFinString;
	private boolean perturbationTerminee;
	private String troncons;

	private List<String> lignes = new ArrayList<String>();

	private Object section;

	private DateTime dateDebut;
	private DateTime dateFin;
	private String dateFormated;

	public InfoTrafic() {

	}

	public InfoTrafic(final Parcel in) {
		code = in.readString();
		dateDebutString = in.readString();
		dateFinString = in.readString();
		dateFormated = in.readString();
		heureDebutString = in.readString();
		heureFinString = in.readString();
		intitule = in.readString();
		perturbationTerminee = in.readInt() == 1;
		resume = in.readString();
		texteVocal = in.readString();
		troncons = in.readString();
		in.readList(lignes, Ligne.class.getClassLoader());
	}

	public String getCode() {
		return code;
	}

	public String getIntitule() {
		return intitule;
	}

	public String getResume() {
		return resume;
	}

	public void setResume(String resume) {
		this.resume = resume;
	}

	public String getTexteVocal() {
		return texteVocal;
	}

	public DateTime getDateDebut() {
		if (this.dateDebut == null) {
			this.dateDebut = parseDate(this.dateDebutString, this.heureDebutString);
		}
		return this.dateDebut;
	}

	public DateTime getDateFin() {
		if (this.dateFin == null) {
			this.dateFin = parseDate(this.dateFinString, this.heureFinString);
		}
		return this.dateFin;
	}

	public String getDateFormated() {
		return dateFormated;
	}

	public void setDateFormated(String dateFormated) {
		this.dateFormated = dateFormated;
	}

	public boolean isPerturbationTerminee() {
		return perturbationTerminee;
	}

	public String getTroncons() {
		return troncons;
	}

	public void addLignes(String ligne) {
		this.lignes.add(ligne);
	}

	public List<String> getLignes() {
		return lignes;
	}

	public void setSection(Object section) {
		this.section = section;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void setIntitule(String intitule) {
		this.intitule = intitule;
	}

	public void setTexteVocal(String texteVocal) {
		this.texteVocal = texteVocal;
	}

	public void setDateDebutString(String dateDebutString) {
		this.dateDebutString = dateDebutString;
	}

	public void setDateFinString(String dateFinString) {
		this.dateFinString = dateFinString;
	}

	public void setHeureDebutString(String heureDebutString) {
		this.heureDebutString = heureDebutString;
	}

	public void setHeureFinString(String heureFinString) {
		this.heureFinString = heureFinString;
	}

	public void setPerturbationTerminee(boolean perturbationTerminee) {
		this.perturbationTerminee = perturbationTerminee;
	}

	public void setTroncons(String troncons) {
		this.troncons = troncons;
	}

	public void setLignes(List<String> lignes) {
		this.lignes = lignes;
	}

	public void setDateDebut(DateTime dateDebut) {
		this.dateDebut = dateDebut;
	}

	public void setDateFin(DateTime dateFin) {
		this.dateFin = dateFin;
	}

	@Override
	public Object getSection() {
		return section;
	}

	public InfoTrafic clone() {
		final InfoTrafic clone = new InfoTrafic();
		clone.code = code;
		clone.dateDebut = dateDebut;
		clone.dateDebutString = dateDebutString;
		clone.dateFin = dateFin;
		clone.dateFinString = dateFinString;
		clone.dateFormated = dateFormated;
		clone.heureDebutString = heureDebutString;
		clone.heureFinString = heureFinString;
		clone.intitule = intitule;
		clone.lignes = lignes;
		clone.perturbationTerminee = perturbationTerminee;
		clone.resume = resume;
		clone.texteVocal = texteVocal;
		clone.troncons = troncons;
		return clone;
	}

	@Override
	public String toString() {
		return new StringBuilder("[").append(this.code).append(";").append(this.intitule).append(";")
				.append((Ligne) section).append("]").toString();
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
		dest.writeString(code);
		dest.writeString(dateDebutString);
		dest.writeString(dateFinString);
		dest.writeString(dateFormated);
		dest.writeString(heureDebutString);
		dest.writeString(heureFinString);
		dest.writeString(intitule);
		dest.writeInt(perturbationTerminee ? 1 : 0);
		dest.writeString(resume);
		dest.writeString(texteVocal);
		dest.writeString(troncons);
		dest.writeList(lignes);
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
