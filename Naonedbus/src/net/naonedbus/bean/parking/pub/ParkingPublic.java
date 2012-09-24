package net.naonedbus.bean.parking.pub;

import java.util.Date;

import net.naonedbus.bean.parking.Parking;
import android.graphics.drawable.Drawable;

import com.google.gson.annotations.SerializedName;

/**
 * Description d'un parking de la ville de Nantes.
 * 
 * @author romain
 * 
 */
public class ParkingPublic implements Parking {

	private static final long serialVersionUID = 8972295085025346469L;

	@SerializedName("IdObj")
	private Integer id;
	@SerializedName("Grp_nom")
	private String nom;
	@SerializedName("Grp_statut")
	private int statutValue;
	@SerializedName("Grp_disponible")
	private int placesDisponibles;
	@SerializedName("Grp_exploitation")
	private int placesTotales;
	@SerializedName("Grp_complet")
	private int seuilComplet;
	@SerializedName("Grp_horodatage")
	private String horodatage;
	private ParkingPublicStatut statut;

	private Double latitude;
	private Double longitude;
	private String adresse;
	private String telephone;
	private String url;
	private Date updateDate;

	private transient Drawable backgroundDrawable;
	private String detail;
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

	public int getPlacesDisponibles() {
		return placesDisponibles;
	}

	public void setPlacesDisponibles(int placesDisponibles) {
		this.placesDisponibles = placesDisponibles;
	}

	public int getPlacesTotales() {
		return placesTotales;
	}

	public void setPlacesTotales(int placesTotales) {
		this.placesTotales = placesTotales;
	}

	public int getSeuilComplet() {
		return seuilComplet;
	}

	public void setSeuilComplet(int seuilComplet) {
		this.seuilComplet = seuilComplet;
	}

	public String getHorodatage() {
		return horodatage;
	}

	public void setHorodatage(String horodatage) {
		this.horodatage = horodatage;
	}

	public int getStatutValue() {
		return statutValue;
	}

	public ParkingPublicStatut getStatut() {
		return statut;
	}

	public void setStatut(ParkingPublicStatut statut) {
		this.statut = statut;
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

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public Drawable getBackgroundDrawable() {
		return backgroundDrawable;
	}

	public void setBackgroundDrawable(Drawable backgroundDrawable) {
		this.backgroundDrawable = backgroundDrawable;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public Float getDistance() {
		return distance;
	}

	public void setDistance(Float distance) {
		this.distance = distance;
	}

	@Override
	public String toString() {
		return new StringBuilder("[").append(this.getId()).append(";").append(this.getNom()).append(";")
				.append(this.getPlacesDisponibles()).append(";").append(this.getStatutValue()).append("]").toString();
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
