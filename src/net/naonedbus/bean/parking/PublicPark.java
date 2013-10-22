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
package net.naonedbus.bean.parking;

import java.util.Date;

import android.graphics.drawable.Drawable;

public class PublicPark implements CarPark {

	private static final long serialVersionUID = 8221252657746459456L;

	private Integer mId;
	private String mName;
	private int mStatusValue;
	private int mAvailableSpaces;
	private int mTotalSpaces;
	private int mFullLimit;
	private String mTimestamp;

	private PublicParkStatus mStatus;

	private Double mLatitude;
	private Double mLongitude;
	private String mAddress;
	private String mPhone;
	private String mUrl;
	private Date mUpdateDate;

	private transient Drawable mBackgroundDrawable;
	private String mDetail;
	private Float mDistance;
	private Object mSection;

	public Integer getId() {
		return mId;
	}

	public void setId(Integer id) {
		mId = id;
	}

	public String getName() {
		return mName;
	}

	public void setName(String nom) {
		mName = nom;
	}

	public Double getLatitude() {
		return mLatitude;
	}

	public void setLatitude(Double latitude) {
		mLatitude = latitude;
	}

	public Double getLongitude() {
		return mLongitude;
	}

	public void setLongitude(Double longitude) {
		mLongitude = longitude;
	}

	public int getAvailableSpaces() {
		return mAvailableSpaces;
	}

	public void setAvailableSpaces(int availableSpaces) {
		mAvailableSpaces = availableSpaces;
	}

	public int getTotalSpaces() {
		return mTotalSpaces;
	}

	public void setTotalSpaces(int totalSpaces) {
		mTotalSpaces = totalSpaces;
	}

	public int getFullLimit() {
		return mFullLimit;
	}

	public void setFullLimit(int fullLimit) {
		mFullLimit = fullLimit;
	}

	public String getTimestamp() {
		return mTimestamp;
	}

	public void setTimestamp(String horodatage) {
		mTimestamp = horodatage;
	}

	public int getStatusValue() {
		return mStatusValue;
	}

	public void setStatusValue(int value) {
		mStatusValue = value;
	}

	public PublicParkStatus getStatus() {
		return mStatus;
	}

	public void setStatus(PublicParkStatus status) {
		mStatus = status;
	}

	public String getAddress() {
		return mAddress;
	}

	public void setAdress(String address) {
		mAddress = address;
	}

	public String getPhone() {
		return mPhone;
	}

	public void setPhone(String phone) {
		mPhone = phone;
	}

	public String getUrl() {
		return mUrl;
	}

	public void setUrl(String url) {
		mUrl = url;
	}

	public Date getUpdateDate() {
		return mUpdateDate;
	}

	public void setUpdateDate(Date updateDate) {
		mUpdateDate = updateDate;
	}

	public Drawable getBackgroundDrawable() {
		return mBackgroundDrawable;
	}

	public void setBackgroundDrawable(Drawable backgroundDrawable) {
		mBackgroundDrawable = backgroundDrawable;
	}

	public String getDetail() {
		return mDetail;
	}

	public void setDetail(String detail) {
		mDetail = detail;
	}

	public Float getDistance() {
		return mDistance;
	}

	public void setDistance(Float distance) {
		mDistance = distance;
	}

	@Override
	public String toString() {
		return new StringBuilder("[").append(getId()).append(";").append(getName()).append(";")
				.append(getAvailableSpaces()).append(";").append(getStatusValue()).append("]").toString();
	}

	@Override
	public Object getSection() {
		return mSection;
	}

	@Override
	public void setSection(Object section) {
		mSection = section;
	}
}
