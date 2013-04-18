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
package net.naonedbus.activity.map.layerloader.impl;

import java.util.List;

import net.naonedbus.R;
import net.naonedbus.activity.map.layerloader.EquipementMapLayer;
import net.naonedbus.activity.map.layerloader.EquipementSelectedInfo;
import net.naonedbus.activity.map.layerloader.ItemSelectedInfo;
import net.naonedbus.activity.map.overlay.BasicItemizedOverlay;
import net.naonedbus.activity.map.overlay.BiclooItemizedOverlay;
import net.naonedbus.activity.map.overlay.TypeOverlayItem;
import net.naonedbus.activity.map.overlay.item.BasicOverlayItem;
import net.naonedbus.bean.Equipement;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.maps.GeoPoint;

/**
 * @author romain
 * 
 */
public class BiclooMapLayer extends EquipementMapLayer {

	public BiclooMapLayer() {
		super(Equipement.Type.TYPE_BICLOO, TypeOverlayItem.TYPE_BICLOO);
	}

	@Override
	protected BasicItemizedOverlay getOverlay(Resources resources) {
		return new BiclooItemizedOverlay(resources);
	}

	@Override
	public ItemSelectedInfo getItemInfo(final Context context, final BasicOverlayItem item) {
		return new EquipementSelectedInfo() {

			@Override
			public String getTitle() {
				return item.getTitle();
			}

			@Override
			public List<View> getSubview(ViewGroup root) {
				return null;
			}

			@Override
			public Integer getResourceDrawable() {
				return R.drawable.map_layer_bicloo;
			}

			@Override
			public Integer getResourceColor() {
				return R.color.map_pin_bicloo;
			}

			@Override
			public Integer getResourceAction() {
				return R.drawable.balloon_navigation;
			}

			@Override
			public String getDescription(final Context context) {
				return context.getString(R.string.map_calque_bicloo);
			}

			@Override
			public Intent getIntent(final Context context) {
				final Equipement equipement = getItemById(item.getId());
				return getNavigationIntent(context, equipement);
			}

			@Override
			public GeoPoint getGeoPoint() {
				return item.getPoint();
			}

		};

	}

	@Override
	public boolean isMoveable() {
		return false;
	}

}
