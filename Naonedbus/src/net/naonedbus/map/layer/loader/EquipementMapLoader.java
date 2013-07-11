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
package net.naonedbus.map.layer.loader;

import java.util.ArrayList;
import java.util.List;

import net.naonedbus.bean.Equipement;
import net.naonedbus.manager.impl.EquipementManager;
import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.twotoasters.clusterkraf.InputPoint;

public class EquipementMapLoader implements MapLayerLoader {

	private final EquipementManager mEquipementManager;
	private final Equipement.Type mTypeEquipement;

	public EquipementMapLoader(final Equipement.Type typeEquipement) {
		mEquipementManager = EquipementManager.getInstance();
		mTypeEquipement = typeEquipement;

	}

	@Override
	public ArrayList<InputPoint> getInputPoints(final Context context) {
		final List<Equipement> equipements = mEquipementManager.getEquipementsByType(context.getContentResolver(),
				mTypeEquipement);
		final ArrayList<InputPoint> result = new ArrayList<InputPoint>(equipements.size());

		for (final Equipement equipement : equipements) {
			result.add(createInputPoint(equipement));
		}

		return result;
	}

	private InputPoint createInputPoint(final Equipement equipement) {
		final LatLng latLng = new LatLng(equipement.getLatitude(), equipement.getLongitude());
		final InputPoint inputPoint = new InputPoint(latLng);
		inputPoint.setTag(equipement);

		return inputPoint;
	}

}
