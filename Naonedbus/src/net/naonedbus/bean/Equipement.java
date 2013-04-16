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

import net.naonedbus.R;

/**
 * @author romain.guefveneu
 * 
 */
public class Equipement implements IEquipement {

	/**
	 * Type d'équipement.
	 * 
	 * @author romain
	 * 
	 */
	public static enum Type {
		/**
		 * Défini le type Arrêt Tan.
		 */
		TYPE_ARRET(0, R.string.map_calque_arret, R.drawable.map_layer_arret, R.color.map_pin_arret),
		/**
		 * Défini le type Parking.
		 */
		TYPE_PARKING(1, R.string.map_calque_parkings, R.drawable.map_layer_parking, R.color.map_pin_parking),
		/**
		 * Défini le type Bicloo.
		 */
		TYPE_BICLOO(2, R.string.map_calque_bicloo, R.drawable.map_layer_bicloo, R.color.map_pin_bicloo),
		/**
		 * Défini le type Marguerite.
		 */
		TYPE_MARGUERITE(3, R.string.map_calque_marguerite, R.drawable.map_layer_marguerite, R.color.map_pin_marguerite),
		/**
		 * Défini le type Covoiturage.
		 */
		TYPE_COVOITURAGE(4, R.string.map_calque_covoiturage, R.drawable.map_layer_covoiturage,
				R.color.map_pin_covoiturage),
		/**
		 * Défini le type Lila.
		 */
		TYPE_LILA(5, R.string.map_calque_lila, R.drawable.map_layer_arret, R.color.map_pin_lila);

		private int id;
		private int titleRes;
		private int drawableRes;
		private int backgroundColorRes;

		private Type(int id, int titleRes, int drawableRes, int backgroundColorRes) {
			this.id = id;
			this.titleRes = titleRes;
			this.drawableRes = drawableRes;
			this.backgroundColorRes = backgroundColorRes;
		}

		public int getId() {
			return this.id;
		}

		public int getTitleRes() {
			return titleRes;
		}

		public int getDrawableRes() {
			return drawableRes;
		}

		public int getBackgroundColorRes() {
			return backgroundColorRes;
		}

		public static Type getTypeById(int id) {
			for (Type type : Type.values()) {
				if (type.getId() == id) {
					return type;
				}
			}
			return null;
		}
	}

	private Integer id;
	private Type type;
	private Integer sousType;
	private String nom;
	private String normalizedNom;
	private String adresse;
	private String telephone;
	private String details;
	private String url;
	private Double latitude;
	private Double longitude;
	private Float distance;
	private Object section;
	private Object tag;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getSousType() {
		return sousType;
	}

	public void setSousType(Integer sousType) {
		this.sousType = sousType;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public void setType(int idType) {
		this.type = Type.getTypeById(idType);
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public String getNormalizedNom() {
		return normalizedNom;
	}

	public void setNormalizedNom(String normalizedNom) {
		this.normalizedNom = normalizedNom;
	}

	public String getAdresse() {
		return adresse;
	}

	public void setAdresse(String adresse) {
		this.adresse = adresse;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
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

	/**
	 * @return La distance en mètres.
	 */
	public Float getDistance() {
		return distance;
	}

	/**
	 * @param distance
	 *            La distance en mètres.
	 */
	public void setDistance(Float distance) {
		this.distance = distance;
	}

	public void setSection(Object section) {
		this.section = section;
	}

	@Override
	public Object getSection() {
		return this.section;
	}

	public Object getTag() {
		return tag;
	}

	public void setTag(Object tag) {
		this.tag = tag;
	}

	@Override
	public String toString() {
		return new StringBuilder("[").append(this.id).append(";").append(this.nom).append(";").append(this.latitude)
				.append(";").append(this.longitude).append("]").toString();
	}

	@Override
	public int compareTo(Equipement another) {
		if (another == null || another.getNom() == null || this.getNom() == null) {
			return 0;
		}
		return this.getNom().compareTo(another.getNom());
	}

}
