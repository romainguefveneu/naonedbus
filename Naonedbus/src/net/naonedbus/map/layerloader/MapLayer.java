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
package net.naonedbus.map.layerloader;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.Marker;
import com.twotoasters.clusterkraf.InputPoint;

public interface MapLayer<T> {

	/**
	 * Intent de navigation.
	 */
	static final String NAVIGATION_INTENT = "google.navigation:q=%f,%f";

	/**
	 * @param context
	 * @return La liste des InputPoint
	 */
	ArrayList<InputPoint> getInputPoints(Context context);

	View getInfoContents(final Marker marker);

	/**
	 * @return Titre de l'élément.
	 */
	String getTitle(final Context context, T item);

	/**
	 * @return Description de l'élément. N'est pas visible si une sous vue est
	 *         définie.
	 */
	String getDescription(final Context context, T item);

	/**
	 * @return Sous vue personnalisée, à afficher à la place de la description.
	 */
	List<View> getSubview(final ViewGroup root);

	/**
	 * @return Element graphique lié à l'action. Peut être null.
	 */
	Integer getResourceAction(T item);

	/**
	 * @return L'intent de la description. Peut être null.
	 */
	Intent getIntent(final Context context, T item);

}
