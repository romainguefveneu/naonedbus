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

public class Equipment implements SectionItem, Comparable<Equipment> {

	/**
	 * Type d'équipement.
	 */
	public static enum Type {
		/**
		 * Défini le type Arrêt Tan.
		 */
		TYPE_STOP(0, R.string.map_calque_arret, R.drawable.map_layer_arret, R.color.map_pin_arret,
				R.drawable.map_pin_arret),
		/**
		 * Défini le type Parking.
		 */
		TYPE_PARK(1, R.string.map_calque_parkings, R.drawable.map_layer_parking, R.color.map_pin_parking,
				R.drawable.map_pin_parking),
		/**
		 * Défini le type Bicloo.
		 */
		TYPE_BICLOO(2, R.string.map_calque_bicloo, R.drawable.map_layer_bicloo, R.color.map_pin_bicloo,
				R.drawable.map_pin_bicloo),
		/**
		 * Défini le type Marguerite.
		 */
		TYPE_MARGUERITE(3, R.string.map_calque_marguerite, R.drawable.map_layer_marguerite, R.color.map_pin_marguerite,
				R.drawable.map_pin_marguerite),
		/**
		 * Défini le type Covoiturage.
		 */
		TYPE_CARPOOL(4, R.string.map_calque_covoiturage, R.drawable.map_layer_covoiturage, R.color.map_pin_covoiturage,
				R.drawable.map_pin_covoiturage),
		/**
		 * Défini le type Lila.
		 */
		TYPE_LILA(5, R.string.map_calque_lila, R.drawable.map_layer_arret, R.color.map_pin_lila,
				R.drawable.map_pin_lila);

		private final int mId;
		private final int mTitleRes;
		private final int mDrawableRes;
		private final int mBackgroundColorRes;
		private final int mMapPin;

		private Type(final int id, final int titleRes, final int drawableRes, final int backgroundColorRes,
				final int mapPin) {
			mId = id;
			mTitleRes = titleRes;
			mDrawableRes = drawableRes;
			mBackgroundColorRes = backgroundColorRes;
			mMapPin = mapPin;
		}

		public int getId() {
			return mId;
		}

		public int getTitleRes() {
			return mTitleRes;
		}

		public int getDrawableRes() {
			return mDrawableRes;
		}

		public int getBackgroundColorRes() {
			return mBackgroundColorRes;
		}

		public int getMapPin() {
			return mMapPin;
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
	private Integer mSubType;
	private String mName;
	private String mNormalizedName;
	private String mAddress;
	private String mPhone;
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

	public Integer getSubType() {
		return mSubType;
	}

	public void setSubType(final Integer subtype) {
		mSubType = subtype;
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

	public String getName() {
		return mName;
	}

	public void setName(final String name) {
		mName = name;
	}

	public String getNormalizedName() {
		return mNormalizedName;
	}

	public void setNormalizedName(final String normalizedName) {
		mNormalizedName = normalizedName;
	}

	public String getAddress() {
		return mAddress;
	}

	public void setAddress(final String address) {
		mAddress = address;
	}

	public String getDetails() {
		return mDetails;
	}

	public void setDetails(final String details) {
		mDetails = details;
	}

	public String getPhone() {
		return mPhone;
	}

	public void setPhone(final String phone) {
		mPhone = phone;
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
		return new StringBuilder("[").append(mId).append(";").append(mName).append(";").append(mLatitude).append(";")
				.append(mLongitude).append("]").toString();
	}

	@Override
	public int compareTo(final Equipment another) {
		if (another == null || another.getName() == null || getName() == null) {
			return 0;
		}
		return getName().compareTo(another.getName());
	}

}
