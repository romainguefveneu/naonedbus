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

import net.naonedbus.R;
import net.naonedbus.graphics.drawable.MapPinDrawable;
import android.content.res.Resources;

/**
 * @author romain
 * 
 */
public class LilaItemizedOverlay extends BasicItemizedOverlay {
	/**
	 * @param defaultMarker
	 * @param selectedMarker
	 */
	public LilaItemizedOverlay(Resources r) {
		super(TypeOverlayItem.TYPE_LILA,
				new MapPinDrawable(r, R.color.map_pin_lila, R.drawable.gd_map_pin_dot_bus));
	}
}
