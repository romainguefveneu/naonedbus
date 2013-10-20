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
package net.naonedbus.widget.adapter.impl;

import java.util.List;

import net.naonedbus.R;
import net.naonedbus.bean.parking.pub.ParkingPublic;
import net.naonedbus.bean.parking.pub.ParkingPublicStatut;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.utils.DistanceUtils;
import net.naonedbus.utils.ParkingUtils;
import net.naonedbus.widget.adapter.ArraySectionAdapter;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class PublicParkArrayAdapter extends ArraySectionAdapter<ParkingPublic> {

	static class ViewHolder {
		TextView nom;
		TextView details;
		ImageView icone;
		TextView distance;
	}

	public PublicParkArrayAdapter(Context context) {
		super(context, R.layout.list_item_parking);
	}

	public PublicParkArrayAdapter(Context context, List<ParkingPublic> objects) {
		super(context, R.layout.list_item_parking, objects);
	}

	@Override
	public void bindView(View view, Context context, int position) {
		final ViewHolder holder = (ViewHolder) view.getTag();
		final ParkingPublic item = getItem(position);

		// Ajouter les données UI
		if (item.getBackgroundDrawable() == null) {
			fillParking(item);
		}

		holder.nom.setText(item.getNom());
		holder.icone.setBackgroundDrawable(item.getBackgroundDrawable());
		holder.details.setText(item.getDetail());
		if (item.getDistance() != null) {
			holder.distance.setText(DistanceUtils.formatDist(item.getDistance()));
		}
	}

	@Override
	public void bindViewHolder(View view) {
		final ViewHolder holder = new ViewHolder();
		holder.nom = (TextView) view.findViewById(R.id.itemTitle);
		holder.details = (TextView) view.findViewById(R.id.itemDescription);
		holder.icone = (ImageView) view.findViewById(R.id.itemSymbole);
		holder.distance = (TextView) view.findViewById(R.id.itemDistance);

		view.setTag(holder);
	}

	/**
	 * Remplir les champs "détail", "backgroundDrawable" et "distance".
	 * 
	 * @param parkingPublic
	 */
	private void fillParking(ParkingPublic parkingPublic) {
		final Context context = getContext();
		int couleur;
		String detail;

		if (parkingPublic.getStatut() == ParkingPublicStatut.OUVERT) {
			final int placesDisponibles = parkingPublic.getPlacesDisponibles();
			couleur = context.getResources().getColor(ParkingUtils.getSeuilCouleurId(placesDisponibles));
			if (placesDisponibles > 0) {
				detail = context.getResources().getQuantityString(R.plurals.parking_places_disponibles,
						placesDisponibles, placesDisponibles);
			} else {
				detail = context.getString(R.string.parking_places_disponibles_zero);
			}
		} else {
			detail = context.getString(parkingPublic.getStatut().getTitleRes());
			couleur = context.getResources().getColor(parkingPublic.getStatut().getColorRes());
		}

		parkingPublic.setBackgroundDrawable(ColorUtils.getRoundedGradiant(couleur));
		parkingPublic.setDetail(detail);
	}

}
