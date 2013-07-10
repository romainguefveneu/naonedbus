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

import net.naonedbus.bean.Equipement;
import net.naonedbus.manager.impl.EquipementManager;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.twotoasters.clusterkraf.InputPoint;

/**
 * @author romain.guefveneu
 * 
 */
public class EquipementMapLayer implements MapLayer<Equipement> {

	private EquipementManager mEquipementManager;
	private Equipement.Type mTypeEquipement;

	public EquipementMapLayer(final Equipement.Type typeEquipement) {
		mEquipementManager = EquipementManager.getInstance();
		mTypeEquipement = typeEquipement;
	}

	@Override
	public View getInfoContents(Marker marker) {
		return null;
	}

	@Override
	public ArrayList<InputPoint> getInputPoints(Context context) {
		final List<Equipement> equipements = mEquipementManager.getEquipementsByType(context.getContentResolver(),
				mTypeEquipement);
		final ArrayList<InputPoint> result = new ArrayList<InputPoint>(equipements.size());

		for (final Equipement equipement : equipements) {
			result.add(createInputPoint(equipement));
		}

		return result;
	}

	public String getTitle(Context context, Equipement item) {
		return item.getNom();
	};

	@Override
	public String getDescription(Context context, Equipement item) {
		return item.getDetails();
	}

	@Override
	public Integer getResourceAction(Equipement item) {
		return null;
	}

	@Override
	public List<View> getSubview(ViewGroup root) {
		return null;
	}

	@Override
	public Intent getIntent(Context context, Equipement item) {
		return null;
	}

	private InputPoint createInputPoint(final Equipement equipement) {
		final LatLng latLng = new LatLng(equipement.getLatitude(), equipement.getLongitude());
		final InputPoint inputPoint = new InputPoint(latLng);
		inputPoint.setTag(equipement);

		return inputPoint;
	}

}
