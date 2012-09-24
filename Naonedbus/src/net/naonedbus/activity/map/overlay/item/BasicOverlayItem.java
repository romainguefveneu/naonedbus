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
package net.naonedbus.activity.map.overlay.item;

import net.naonedbus.activity.map.overlay.TypeOverlayItem;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

/**
 * @author romain.guefveneu
 * 
 */
public class BasicOverlayItem extends OverlayItem {

	private TypeOverlayItem type;
	private Integer id = 0;

	public BasicOverlayItem(GeoPoint point, String title, TypeOverlayItem type) {
		super(point, title, null);
		this.type = type;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public TypeOverlayItem getType() {
		return type;
	}

	public void setType(TypeOverlayItem type) {
		this.type = type;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof BasicOverlayItem && type != null && id != null) {
			final BasicOverlayItem item = (BasicOverlayItem) o;
			return (type.equals(item.getType()) && id.equals(item.getId()));
		} else {
			return false;
		}
	}
}
