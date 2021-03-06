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
import net.naonedbus.widget.item.SectionItem;

/**
 * @author romain.guefveneu
 * 
 */
public class Equipement implements SectionItem, Comparable<Equipement> {

	/**
	 * Type d'équipement.
	 */
	public static enum Type {
		/**
		 * Défini le type Arrêt Tan.
		 */
		TYPE_ARRET(0, R.string.map_calque_arret, R.drawable.ic_directions_bus, R.color.map_pin_arret,
				R.drawable.map_pin_arret),
		/**
		 * Défini le type Parking.
		 */
		TYPE_PARKING(1, R.string.map_calque_parkings, R.drawable.ic_local_parking, R.color.map_pin_parking,
				R.drawable.map_pin_parking),
		/**
		 * Défini le type Bicloo.
		 */
		TYPE_BICLOO(2, R.string.map_calque_bicloo, R.drawable.ic_directions_bike, R.color.map_pin_bicloo,
				R.drawable.map_pin_bicloo);

		private final int id;
		private final int titleRes;
		private final int drawableRes;
		private final int backgroundColorRes;
		private final int mapPin;

		private Type(final int id, final int titleRes, final int drawableRes, final int backgroundColorRes,
				final int mapPin) {
			this.id = id;
			this.titleRes = titleRes;
			this.drawableRes = drawableRes;
			this.backgroundColorRes = backgroundColorRes;
			this.mapPin = mapPin;
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

		public int getMapPin() {
			return mapPin;
		}

		public static Type getTypeById(final int id) {
			for (final Type type : Type.values()) {
				if (type.getId() == id) {
					return type;
				}
			}
			return null;
		}
	}

	private Integer mId;
	private Type mType;
	private Integer mSousType;
	private String mCode;
	private String mNom;
	private String mNormalizedNom;
	private String mAdresse;
	private String mTelephone;
	private String mDetails;
	private String mUrl;
	private Double mLatitude;
	private Double mLongitude;
	private Float mDistance;
	private Object mSection;
	private Object mTag;

	public Integer getId() {
		return mId;
	}

	public void setId(final Integer id) {
		mId = id;
	}

	public Integer getSousType() {
		return mSousType;
	}

	public void setSousType(final Integer sousType) {
		mSousType = sousType;
	}

	public Type getType() {
		return mType;
	}

	public void setType(final Type type) {
		mType = type;
	}

	public void setType(final int idType) {
		mType = Type.getTypeById(idType);
	}

	public String getCode() {
		return mCode;
	}

	public void setCode(String code) {
		mCode = code;
	}

	public String getNom() {
		return mNom;
	}

	public void setNom(final String nom) {
		mNom = nom;
	}

	public String getNormalizedNom() {
		return mNormalizedNom;
	}

	public void setNormalizedNom(final String normalizedNom) {
		mNormalizedNom = normalizedNom;
	}

	public String getAdresse() {
		return mAdresse;
	}

	public void setAdresse(final String adresse) {
		mAdresse = adresse;
	}

	public String getDetails() {
		return mDetails;
	}

	public void setDetails(final String details) {
		mDetails = details;
	}

	public String getTelephone() {
		return mTelephone;
	}

	public void setTelephone(final String telephone) {
		mTelephone = telephone;
	}

	public String getUrl() {
		return mUrl;
	}

	public void setUrl(final String url) {
		mUrl = url;
	}

	public Double getLatitude() {
		return mLatitude;
	}

	public void setLatitude(final Double latitude) {
		mLatitude = latitude;
	}

	public Double getLongitude() {
		return mLongitude;
	}

	public void setLongitude(final Double longitude) {
		mLongitude = longitude;
	}

	/**
	 * @return La distance en mètres.
	 */

	public Float getDistance() {
		return mDistance;
	}

	/**
	 * @param distance
	 *            La distance en mètres.
	 */

	public void setDistance(final Float distance) {
		mDistance = distance;
	}

	public void setSection(final Object section) {
		mSection = section;
	}

	@Override
	public Object getSection() {
		return mSection;
	}

	public Object getTag() {
		return mTag;
	}

	public void setTag(final Object tag) {
		mTag = tag;
	}

	@Override
	public String toString() {
		return new StringBuilder("[").append(mId).append(";").append(mNom).append(";").append(mLatitude).append(";")
				.append(mLongitude).append("]").toString();
	}

	@Override
	public int compareTo(final Equipement another) {
		if (another == null || another.getNom() == null || this.getNom() == null) {
			return 0;
		}
		return this.getNom().compareTo(another.getNom());
	}

}
