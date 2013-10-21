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


public class IncentivePark implements CarPark {

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

	public String getName() {
		return nom;
	}

	public void setName(String nom) {
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

	public String getAddress() {
		return adresse;
	}

	public void setAdress(String adresse) {
		this.adresse = adresse;
	}

	public String getPhone() {
		return telephone;
	}

	public void setPhone(String telephone) {
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
		return new StringBuilder("[").append(this.getId()).append(";").append(this.getName()).append("]").toString();
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
