package net.naonedbus.bean;

import net.naonedbus.bean.Equipement.Type;
import net.naonedbus.widget.item.SectionItem;

public interface IEquipement extends SectionItem, Comparable<Equipement> {

	Integer getId();

	void setId(Integer id);

	Integer getSousType();

	void setSousType(Integer sousType);

	Type getType();

	void setType(Type type);

	void setType(int idType);

	String getNom();

	void setNom(String nom);

	String getNormalizedNom();

	void setNormalizedNom(String normalizedNom);

	String getAdresse();

	void setAdresse(String adresse);

	String getDetails();

	void setDetails(String details);

	String getTelephone();

	void setTelephone(String telephone);

	String getUrl();

	void setUrl(String url);

	Double getLatitude();

	void setLatitude(Double latitude);

	Double getLongitude();

	void setLongitude(Double longitude);

	/**
	 * @return La distance en mètres.
	 */
	Float getDistance();

	/**
	 * @param distance
	 *            La distance en mètres.
	 */
	void setDistance(Float distance);

	void setSection(Object section);

	Object getSection();

}
