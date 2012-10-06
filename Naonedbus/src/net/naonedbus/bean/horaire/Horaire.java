/**
 *  Copyright (C) 2011 Romain Guefveneu
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
package net.naonedbus.bean.horaire;

import java.util.Date;

import net.naonedbus.model.common.IHoraire;
import net.naonedbus.widget.item.SectionItem;

/**
 * @author romain
 * 
 */
public class Horaire implements IHoraire, SectionItem {

	private static final long serialVersionUID = 6005990920131960102L;

	private Integer id;
	private String terminus;
	private Long dayTrip;
	private Long timestamp;

	private Date date;
	private String delai;
	private Object section;
	private boolean isBeforeNow;

	public Horaire() {
	}

	public Horaire(Horaire horaire) {
		this.id = horaire.getId();
		this.dayTrip = horaire.getDayTrip();
		this.terminus = horaire.getTerminus();
		this.timestamp = horaire.getTimestamp();
	}

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public void setId(Integer id) {
		this.id = id;
	}

	@Override
	public String getTerminus() {
		return terminus;
	}

	@Override
	public Long getTimestamp() {
		return timestamp;
	}

	public Long getDayTrip() {
		return dayTrip;
	}

	public void setDayTrip(Long dayTrip) {
		this.dayTrip = dayTrip;
	}

	@Override
	public void setTerminus(String terminus) {
		this.terminus = terminus;
	}

	@Override
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
		this.date = new Date(timestamp);
	}

	public Date getDate() {
		return date;
	}

	public String getDelai() {
		return delai;
	}

	public void setDelai(String delai) {
		this.delai = delai;
	}

	public boolean isBeforeNow() {
		return isBeforeNow;
	}

	public void setBeforeNow(boolean isBeforeNow) {
		this.isBeforeNow = isBeforeNow;
	}

	public void setSection(Object section) {
		this.section = section;
	}

	@Override
	public Object getSection() {
		return section;
	}

}
