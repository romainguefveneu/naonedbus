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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.naonedbus.R;
import net.naonedbus.activity.impl.BiclooDetailActivity;
import net.naonedbus.activity.map.layerloader.EquipementSelectedInfo;
import net.naonedbus.activity.map.layerloader.ItemSelectedInfo;
import net.naonedbus.activity.map.layerloader.MapLayer;
import net.naonedbus.activity.map.overlay.BasicItemizedOverlay;
import net.naonedbus.activity.map.overlay.BiclooItemizedOverlay;
import net.naonedbus.activity.map.overlay.TypeOverlayItem;
import net.naonedbus.activity.map.overlay.item.BasicOverlayItem;
import net.naonedbus.bean.Bicloo;
import net.naonedbus.intent.ParamIntent;
import net.naonedbus.manager.impl.BiclooManager;
import net.naonedbus.utils.GeoPointUtils;

import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.bugsense.trace.BugSenseHandler;
import com.google.android.maps.GeoPoint;

public class BiclooMapLayer implements MapLayer {

	private final BiclooManager mBiclooManager;
	private final SparseArray<Bicloo> mBicloos = new SparseArray<Bicloo>();

	public BiclooMapLayer() {
		mBiclooManager = BiclooManager.getInstance();
	}

	@Override
	public ItemSelectedInfo getItemInfo(final Context context, final BasicOverlayItem item) {
		return new EquipementSelectedInfo() {

			@Override
			public String getTitle() {
				return item.getTitle();
			}

			@Override
			public List<View> getSubview(final ViewGroup root) {
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
				return null;
			}

			@Override
			public String getDescription(final Context context) {
				final Bicloo bicloo = mBicloos.get(item.getId());
				final int availableBikes = bicloo.getAvailableBike();
				final int availableStands = bicloo.getAvailableBikeStands();
				final String bikes = context.getResources().getQuantityString(R.plurals.bicloo_velos_disponibles,
						availableBikes, availableBikes);
				final String stands = context.getResources().getQuantityString(R.plurals.bicloo_places_disponibles,
						availableStands, availableStands);

				final String description = context.getResources().getQuantityString(R.plurals.bicloo,
						availableBikes + availableStands, bikes, stands);

				return description;
			}

			@Override
			public Intent getIntent(final Context context) {
				final Bicloo bicloo = mBicloos.get(item.getId());
				final ParamIntent intent = new ParamIntent(context, BiclooDetailActivity.class);
				intent.putExtra(BiclooDetailActivity.PARAM_BICLOO, bicloo);
				return intent;
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

	@Override
	public BasicItemizedOverlay getOverlay(final Context context, final Location location) {
		final List<Bicloo> bicloos = new ArrayList<Bicloo>();
		try {
			bicloos.addAll(mBiclooManager.getAll(context));
		} catch (final IOException e) {
			BugSenseHandler.sendException(e);
		} catch (final JSONException e) {
			BugSenseHandler.sendException(e);
		}

		final BasicItemizedOverlay newItemizedOverlay = new BiclooItemizedOverlay(context.getResources());
		BasicOverlayItem overlayItem;

		for (final Bicloo bicloo : bicloos) {
			overlayItem = new BasicOverlayItem(GeoPointUtils.getGeoPoint(bicloo.getLocation()), bicloo.getName(),
					TypeOverlayItem.TYPE_BICLOO);
			overlayItem.setId(bicloo.getNumber());
			newItemizedOverlay.addOverlay(overlayItem);
			mBicloos.put(bicloo.getNumber(), bicloo);
		}

		return newItemizedOverlay;
	}

	@Override
	public BasicItemizedOverlay getOverlay(final Context context, final int defaultItemId) {
		final BasicItemizedOverlay newItemizedOverlay;
		final Bicloo item = mBicloos.get(defaultItemId);
		if (item != null) {
			newItemizedOverlay = getOverlay(context, item.getLocation());
		} else {
			newItemizedOverlay = new BiclooItemizedOverlay(context.getResources());
		}
		return newItemizedOverlay;
	}

}
