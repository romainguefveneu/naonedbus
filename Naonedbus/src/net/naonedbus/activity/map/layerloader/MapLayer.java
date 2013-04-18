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
package net.naonedbus.activity.map.layerloader;

import net.naonedbus.activity.map.overlay.BasicItemizedOverlay;
import net.naonedbus.activity.map.overlay.item.BasicOverlayItem;
import android.content.Context;
import android.location.Location;

/**
 * @author romain
 * 
 */
public interface MapLayer {

	/**
	 * Déclenchée lors de la sélection d'un item sur la carte.
	 * 
	 * @param item
	 *            L'élément sélectionné
	 */

	public ItemSelectedInfo getItemInfo(final Context context, final BasicOverlayItem item);

	/**
	 * Récupérer une couche de la map, en précisant la position centrale.
	 */

	public BasicItemizedOverlay getOverlay(final Context context, final Location location);

	/**
	 * Récupérer une couche de la map, en précisant l'élément central.
	 */

	public BasicItemizedOverlay getOverlay(final Context context, final int defaultItemId);

	/**
	 * @return <code>true</code> si le calque doit etre redessiné avec le
	 *         changement de location.
	 */
	boolean isMoveable();

}
