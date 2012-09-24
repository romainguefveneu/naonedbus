package net.naonedbus.bean.parking.relai;

import net.naonedbus.bean.parking.Parking;

/**
 * Description d'un parking relai TAN.
 * 
 * @author romain
 * 
 */
public class ParkingRelai implements Parking {

	private static final long serialVersionUID = -943315044622968292L;

	private Integer id;
	private String nom;
	private Double latitude;
	private Double longitude;
	private String adresse;
	private String telephone;
	private String url;
	private Float distance;
	private Object section;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public String getAdresse() {
		return adresse;
	}

	public void setAdresse(String adresse) {
		this.adresse = adresse;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Float getDistance() {
		return distance;
	}

	public void setDistance(Float distance) {
		this.distance = distance;
	}

	@Override
	public String toString() {
		return new StringBuilder("[").append(this.getId()).append(";").append(this.getNom()).append("]").toString();
	}

	@Override
	public Object getSection() {
		return this.section;
	}

	@Override
	public void setSection(Object section) {
		this.section = section;
	}
}
