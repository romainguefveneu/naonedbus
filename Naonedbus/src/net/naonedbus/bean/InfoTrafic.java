package net.naonedbus.bean;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.gson.annotations.SerializedName;

public class InfoTrafic implements Serializable {

	private static final long serialVersionUID = -2516041836875800927L;
	private static final DateTimeFormatter fullDateParser = DateTimeFormat.forPattern("dd/MM/yyyy");
	private static final DateTimeFormatter simpleDateParser = DateTimeFormat.forPattern("MM/yyyy");
	private static final DateTimeFormatter timeParser = DateTimeFormat.forPattern("HH:mm");

	@SerializedName("CODE")
	private String code;
	@SerializedName("INTITULE")
	private String intitule;
	@SerializedName("RESUME")
	private String resume;
	@SerializedName("TEXTE_VOCAL")
	private String texteVocal;
	@SerializedName("DATE_DEBUT")
	private String dateDebutString;
	@SerializedName("DATE_FIN")
	private String dateFinString;
	@SerializedName("HEURE_DEBUT")
	private String heureDebutString;
	@SerializedName("HEURE_FIN")
	private String heureFinString;
	@SerializedName("PERTURBATION_TERMINEE")
	private boolean perturbationTerminee;
	@SerializedName("TRONCONS")
	private String troncons;

	private Set<String> lignes = new TreeSet<String>();

	private DateTime dateDebut;
	private DateTime dateFin;

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

	@Override
	public String toString() {
		return new StringBuilder("[").append(this.code).append(";").append(this.intitule).append("]").toString();
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
		if (heure != null && heure.length() == 5) {
			dateTime = dateTime.plusMinutes(timeParser.parseDateTime(heure).minuteOfDay().get());
		}
		return dateTime;
	}
}
