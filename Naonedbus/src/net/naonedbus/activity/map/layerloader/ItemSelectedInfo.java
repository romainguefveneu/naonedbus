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

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.maps.GeoPoint;

/**
 * @author romain
 * 
 */
public interface ItemSelectedInfo {

	/**
	 * Intent de navigation.
	 */
	static final String NAVIGATION_INTENT = "google.navigation:q=%f,%f";

	/**
	 * @return Titre de l'élément.
	 */
	public String getTitle();

	/**
	 * @return Description de l'élément. N'est pas visible si une sous vue est
	 *         définie.
	 */
	public String getDescription(final Context context);

	/**
	 * @return Sous vue personnalisée, à afficher à la place de la description.
	 */
	public List<View> getSubview(final ViewGroup root);

	/**
	 * @return Element graphique à afficher à côté du titre.
	 */
	public Integer getResourceDrawable();

	/**
	 * @return Element graphique lié à l'action. Peut être null.
	 */
	public Integer getResourceAction();

	/**
	 * @return La couleur de fond de l'élément graphique.
	 */
	public Integer getResourceColor();

	/**
	 * @return L'intent de la desctiption. Peut être null.
	 */
	public Intent getIntent(final Context context);

	/**
	 * @return Le GeoPoint de l'élément.
	 */
	public GeoPoint getGeoPoint();

}
