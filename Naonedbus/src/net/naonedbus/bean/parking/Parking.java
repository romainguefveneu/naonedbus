package net.naonedbus.bean.parking;

import java.io.Serializable;

import net.naonedbus.widget.item.SectionItem;

/**
 * Description d'un parking.
 * 
 * @author romain
 * 
 */
public interface Parking extends SectionItem, Serializable {

	Integer getId();

	void setId(Integer id);

	String getNom();

	void setNom(String nom);

	String getAdresse();

	void setAdresse(String adresse);

	String getTelephone();

	void setTelephone(String telephone);

	String getUrl();

	void setUrl(String url);

	Double getLatitude();

	void setLatitude(Double latitude);

	Double getLongitude();

	Float getDistance();

	void setLongitude(Double longitude);

	void setSection(Object section);

}
