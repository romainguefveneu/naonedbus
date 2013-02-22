package net.naonedbus.bean;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

import net.naonedbus.widget.item.SectionItem;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class InfoTrafic implements Serializable, SectionItem {

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

	private Set<String> lignes = new TreeSet<String>();

	private Object section;

	private DateTime dateDebut;
	private DateTime dateFin;
	private String dateFormated;

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

	public Set<String> getLignes() {
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

	public void setLignes(Set<String> lignes) {
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

}
