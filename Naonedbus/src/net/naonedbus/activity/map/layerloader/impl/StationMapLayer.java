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
package net.naonedbus.activity.map.layerloader.impl;

import java.util.ArrayList;
import java.util.List;

import net.naonedbus.R;
import net.naonedbus.activity.map.layerloader.EquipementMapLayer;
import net.naonedbus.activity.map.layerloader.ItemSelectedInfo;
import net.naonedbus.activity.map.overlay.BasicItemizedOverlay;
import net.naonedbus.activity.map.overlay.StationItemizedOverlay;
import net.naonedbus.activity.map.overlay.TypeOverlayItem;
import net.naonedbus.activity.map.overlay.item.BasicOverlayItem;
import net.naonedbus.bean.Equipement;
import net.naonedbus.bean.Equipement.Type;
import net.naonedbus.bean.Ligne;
import net.naonedbus.manager.impl.LigneManager;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.utils.GeoPointUtils;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;

/**
 * @author romain
 * 
 */
public class StationMapLayer extends EquipementMapLayer {
	private static final int MAX_STATIONS = 50;
	private static LigneManager ligneManager;

	public StationMapLayer() {
		super(Type.TYPE_ARRET, TypeOverlayItem.TYPE_STATION);
		ligneManager = LigneManager.getInstance();
	}

	@Override
	protected BasicItemizedOverlay getOverlay(Resources resources) {
		return new StationItemizedOverlay(resources);
	}

	@Override
	public ItemSelectedInfo getItemInfo(final Context context, final BasicOverlayItem item) {
		return new ItemSelectedInfo() {

			@Override
			public String getTitle() {
				return item.getTitle();
			}

			@Override
			public List<View> getSubview(ViewGroup root) {
				final LayoutInflater layoutInflater = LayoutInflater.from(context);
				final List<View> subview = new ArrayList<View>();
				final Equipement station = getItemById(item.getId());
				final List<Ligne> lignes = ligneManager.getLignesFromStation(context.getContentResolver(),
						station.getId());

				for (final Ligne ligneItem : lignes) {
					final TextView textView = (TextView) layoutInflater.inflate(R.layout.ligne_code_item, root, false);
					textView.setTextColor(ligneItem.couleurTexte);
					textView.setBackgroundDrawable(ColorUtils.getGradiant(ligneItem.couleurBackground));
					textView.setText(ligneItem.code);
					subview.add(textView);
				}

				return subview;
			}

			@Override
			public Integer getResourceDrawable() {
				return R.drawable.map_layer_arret;
			}

			@Override
			public Integer getResourceColor() {
				return R.color.map_pin_arret;
			}

			@Override
			public Integer getResourceAction() {
				return null;
			}

			@Override
			public String getDescription(final Context context) {
				return null;
			}

			@Override
			public Intent getIntent(final Context context) {
				// final ParamIntent intent = new ParamIntent(context,
				// ParcoursActivity.class);
				// intent.putExtra(ParcoursActivity.Param.idStation,
				// item.getId());
				// return intent;
				return null;
			}

			@Override
			public GeoPoint getGeoPoint() {
				return item.getPoint();
			}

		};

	}

	@Override
	public BasicItemizedOverlay getOverlay(final Context context, final Location location) {
		final StationItemizedOverlay newItemizedOverlay = new StationItemizedOverlay(context.getResources());
		BasicOverlayItem stationOverlayItem;

		if (location != null) {
			final List<Equipement> stationsProches = getEquipementManager().getEquipementsByLocation(
					context.getContentResolver(), Type.TYPE_ARRET, location, MAX_STATIONS);

			for (Equipement station : stationsProches) {
				stationOverlayItem = new BasicOverlayItem(GeoPointUtils.getGeoPoint(station), station.getNom(),
						TypeOverlayItem.TYPE_STATION);
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
