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
import java.util.Locale;

import net.naonedbus.R;
import net.naonedbus.activity.map.layerloader.ItemSelectedInfo;
import net.naonedbus.activity.map.layerloader.MapLayer;
import net.naonedbus.activity.map.overlay.BasicItemizedOverlay;
import net.naonedbus.activity.map.overlay.ParkingItemizedOverlay;
import net.naonedbus.activity.map.overlay.TypeOverlayItem;
import net.naonedbus.activity.map.overlay.item.BasicOverlayItem;
import net.naonedbus.bean.Equipement;
import net.naonedbus.bean.parking.Parking;
import net.naonedbus.bean.parking.pub.ParkingPublic;
import net.naonedbus.bean.parking.pub.ParkingPublicStatut;
import net.naonedbus.bean.parking.relai.ParkingRelai;
import net.naonedbus.manager.impl.EquipementManager;
import net.naonedbus.manager.impl.ParkingPublicManager;
import net.naonedbus.manager.impl.ParkingRelaiManager;
import net.naonedbus.utils.GeoPointUtils;
import net.naonedbus.utils.ParkingUtils;

import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.bugsense.trace.BugSenseHandler;
import com.google.android.maps.GeoPoint;

/**
 * @author romain
 * 
 */
public class ParkingMapLayer implements MapLayer {

	private static EquipementManager equipementManager;
	private final SparseArray<Parking> parkingList = new SparseArray<Parking>();

	public ParkingMapLayer() {
		equipementManager = EquipementManager.getInstance();
	}

	@Override
	public ItemSelectedInfo getItemInfo(final Context context, final BasicOverlayItem item) {
		return new ItemSelectedInfo() {

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
				final Parking parking = parkingList.get(item.getId());
				if (parking instanceof ParkingPublic) {
					return R.drawable.map_layer_parking;
				} else {
					return R.drawable.map_layer_parking_relai;
				}
			}

			@Override
			public Integer getResourceColor() {
				final Parking parking = parkingList.get(item.getId());
				if (parking instanceof ParkingPublic) {
					final ParkingPublic parkingPublic = ((ParkingPublic) parking);
					int resColor = 0;
					if (parkingPublic.getStatut() == ParkingPublicStatut.OUVERT) {
						resColor = ParkingUtils.getSeuilCouleurId(parkingPublic.getPlacesDisponibles());
					} else {
						resColor = parkingPublic.getStatut().getColorRes();
					}
					return resColor;
				} else {
					return R.color.parking_state_blue;
				}
			}

			@Override
			public Integer getResourceAction() {
				return R.drawable.balloon_navigation;
			}

			@Override
			public String getDescription(final Context context) {
				final Parking parking = parkingList.get(item.getId());
				String details = "";
				int placesDisponibles = 0;

				if (parking instanceof ParkingPublic) {
					final ParkingPublic parkingPublic = ((ParkingPublic) parking);
					placesDisponibles = ((ParkingPublic) parking).getPlacesDisponibles();
					if (parkingPublic.getStatut() == ParkingPublicStatut.OUVERT) {
						if (placesDisponibles > 0) {
							details = context.getResources().getQuantityString(R.plurals.parking_places_disponibles,
									placesDisponibles, placesDisponibles);
						} else {
							details = context.getResources().getString(R.string.parking_places_disponibles_zero);
						}
					} else {
						details = context.getResources().getString(parkingPublic.getStatut().getTitleRes());
					}

				} else if (parking instanceof ParkingRelai) {
					details = context.getResources().getString(R.string.map_calque_parking_relai);
				}

				return details;
			}

			@Override
			public Intent getIntent(final Context context) {
				final Parking parking = parkingList.get(item.getId());

				final Uri uri = Uri.parse(String.format(Locale.ENGLISH, NAVIGATION_INTENT, parking.getLatitude(),
						parking.getLongitude()));

				return new Intent(Intent.ACTION_VIEW, uri);
			}

			@Override
			public GeoPoint getGeoPoint() {
				return item.getPoint();
			}

		};

	}

	@Override
	public BasicItemizedOverlay getOverlay(final Context context, final Location location) {
		final List<Parking> parkings = new ArrayList<Parking>();

		try {
			if (parkings.size() == 0) {
				final ParkingPublicManager publicManager = ParkingPublicManager.getInstance();
				final ParkingRelaiManager relaiManager = ParkingRelaiManager.getInstance();

				parkings.addAll(publicManager.getAll(context));
				parkings.addAll(relaiManager.getAll(context.getContentResolver()));
			}
		} catch (final IOException e) {
			BugSenseHandler.sendException(e);
		} catch (final JSONException e) {
			BugSenseHandler.sendException(e);
		}

		final ParkingItemizedOverlay newItemizedOverlay = new ParkingItemizedOverlay(context.getResources());
		BasicOverlayItem parkingOverlayItem;

		for (final Parking parking : parkings) {
			parkingOverlayItem = new BasicOverlayItem(GeoPointUtils.getGeoPoint(parking), parking.getNom(),
					TypeOverlayItem.TYPE_PARKING);
			parkingOverlayItem.setId(parking.getId());
			newItemizedOverlay.addOverlay(parkingOverlayItem);
			parkingList.put(parking.getId(), parking);
		}

		return newItemizedOverlay;
	}

	@Override
	public BasicItemizedOverlay getOverlay(final Context context, final int defaultItemId) {
		final BasicItemizedOverlay newItemizedOverlay;
		final Equipement item = equipementManager.getSingle(context.getContentResolver(), defaultItemId);
		if (item != null) {
			final Location location = new Location(LocationManager.GPS_PROVIDER);
			location.setLatitude(item.getLatitude());
			location.setLongitude(item.getLongitude());
			newItemizedOverlay = getOverlay(context, location);
		} else {
			newItemizedOverlay = new ParkingItemizedOverlay(context.getResources());
		}
		return newItemizedOverlay;
	}

	@Override
	public boolean isMoveable() {
		return false;
	}
}
