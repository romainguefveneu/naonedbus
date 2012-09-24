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
package net.naonedbus.activity.map.overlay;

/**
 * Types de marqueurs sur la map.
 * 
 * @author romain
 * 
 */
public enum TypeOverlayItem {

	TYPE_STATION(0), TYPE_PARKING(1), TYPE_BICLOO(2), TYPE_MARGUERITE(3), TYPE_COVOITURAGE(4), TYPE_LILA(5);

	private int id;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	private TypeOverlayItem(int id) {
		this.id = id;
	}

	public static synchronized TypeOverlayItem getById(int id) {
		for (final TypeOverlayItem typeOverlayItem : TypeOverlayItem.values()) {
			if (typeOverlayItem.getId() == id) {
				return typeOverlayItem;
			}
		}
		return null;
	}
}
