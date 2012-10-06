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


/**
 * @author romain.guefveneu
 * 
 */
public class HoraireToken {

	private Long date;
	private Integer id;

	public HoraireToken(Long date, Integer id) {
		this.date = date;
		this.id = id;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof HoraireToken) {
			HoraireToken element = (HoraireToken) o;
			return (element.date.equals(this.date) && element.id.equals(this.id));
		} else {
			return super.equals(o);
		}
	}

	@Override
	public int hashCode() {
		return date.hashCode() * 31 + id.hashCode() * 31;
	}

	@Override
	public String toString() {
		return id + " : " + date;
	}

}
