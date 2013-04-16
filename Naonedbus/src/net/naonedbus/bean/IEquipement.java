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
