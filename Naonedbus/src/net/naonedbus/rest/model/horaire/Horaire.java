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
package net.naonedbus.rest.model.horaire;

import net.naonedbus.model.common.IHoraire;

/**
 * @author romain
 *
 */
public class Horaire implements IHoraire {

	private static final long serialVersionUID = 6005990920131960102L;

	private Integer id;
	private String terminus;
	private Long timestamp;
	
	public Horaire(){}
	
	public Horaire(Horaire horaire){
		this.id = horaire.getId();
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

	@Override
	public void setTerminus(String terminus) {
		this.terminus = terminus;
	}

	@Override
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

}
