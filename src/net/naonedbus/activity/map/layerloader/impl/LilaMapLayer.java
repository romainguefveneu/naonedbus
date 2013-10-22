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
import net.naonedbus.activity.map.overlay.LilaItemizedOverlay;
import net.naonedbus.activity.map.overlay.TypeOverlayItem;
import net.naonedbus.activity.map.overlay.item.BasicOverlayItem;
import net.naonedbus.bean.Equipment;
import net.naonedbus.bean.Equipment.Type;
import net.naonedbus.utils.GeoPointUtils;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.maps.GeoPoint;

/**
 * @author romain
 * 
 */
public class LilaMapLayer extends EquipementMapLayer {

	private static int MAX_ITEMS = 50;

	/**
	 * @param typeEquipement
	 * @param typeOverlayItem
	 */
	public LilaMapLayer() {
		super(Equipment.Type.TYPE_LILA, TypeOverlayItem.TYPE_LILA);
	}

	@Override
	protected BasicItemizedOverlay getOverlay(Resources resources) {
		return new LilaItemizedOverlay(resources);
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
				return R.drawable.map_layer_arret;
			}

			@Override
			public Integer getResourceColor() {
				return R.color.map_pin_lila;
			}

			@Override
			public Integer getResourceAction() {
				return R.drawable.balloon_navigation;
			}

			@Override
			public String getDescription(final Context context) {
				return context.getString(R.string.map_calque_lila);
			}

			@Override
			public Intent getIntent(final Context context) {
				final Equipment equipment = getItemById(item.getId());
				return getNavigationIntent(context, equipment);
			}

			@Override
			public GeoPoint getGeoPoint() {
				return item.getPoint();
			}

		};

	}

	@Override
	public BasicItemizedOverlay getOverlay(final Context context, final Location location) {
		final LilaItemizedOverlay newItemizedOverlay = new LilaItemizedOverlay(context.getResources());
		BasicOverlayItem stationOverlayItem;

		if (location != null) {
			final List<Equipment> stationsProches = getEquipementManager().getByLocation(
					context.getContentResolver(), Type.TYPE_LILA, location, MAX_ITEMS);

			for (Equipment station : stationsProches) {
				stationOverlayItem = new BasicOverlayItem(GeoPointUtils.getGeoPoint(station), station.getName(),
						TypeOverlayItem.TYPE_LILA);
				stationOverlayItem.setId(station.getId());
				newItemizedOverlay.addOverlay(stationOverlayItem);
				addEquipement(station);
			}
		}

		return newItemizedOverlay;
	}

	@Override
	public boolean isMoveable() {
		return true;
	}

}
