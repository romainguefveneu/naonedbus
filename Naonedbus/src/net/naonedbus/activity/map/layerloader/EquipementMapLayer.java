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
package net.naonedbus.activity.map.layerloader;

import java.util.List;

import net.naonedbus.activity.map.overlay.BasicItemizedOverlay;
import net.naonedbus.activity.map.overlay.TypeOverlayItem;
import net.naonedbus.activity.map.overlay.item.BasicOverlayItem;
import net.naonedbus.bean.Equipement;
import net.naonedbus.manager.impl.EquipementManager;
import net.naonedbus.utils.GeoPointUtils;
import android.content.Context;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.util.SparseArray;

/**
 * @author romain.guefveneu
 * 
 */
public abstract class EquipementMapLayer implements MapLayer {

	private EquipementManager equipementManager;
	private SparseArray<Equipement> equipements = new SparseArray<Equipement>();
	private Equipement.Type typeEquipement;
	private TypeOverlayItem typeOverlayItem;

	protected EquipementMapLayer(final Equipement.Type typeEquipement, final TypeOverlayItem typeOverlayItem) {
		this.equipementManager = EquipementManager.getInstance();
		this.typeEquipement = typeEquipement;
		this.typeOverlayItem = typeOverlayItem;
	}

	/**
	 * Récupérer un équipement par son id.
	 * 
	 * @param id
	 * @return L'équipement.
	 */
	protected Equipement getItemById(int id) {
		return equipements.get(id);
	}

	/**
	 * Ajouter un équipement au cache.
	 * 
	 * @param equipement
	 */
	protected void addEquipement(Equipement equipement) {
		equipements.put(equipement.getId(), equipement);
	}

	/**
	 * @return L'equipementManager.
	 */
	protected EquipementManager getEquipementManager() {
		return equipementManager;
	}

	/**
	 * Construire un BasicItemizedOverlay.
	 * 
	 * @param resources
	 * @return le BasicItemizedOverlay
	 */
	protected abstract BasicItemizedOverlay getOverlay(Resources resources);

	@Override
	public BasicItemizedOverlay getOverlay(Context context, Location location) {
		final BasicItemizedOverlay newItemizedOverlay = getOverlay(context.getResources());
		BasicOverlayItem overlayItem;

		final List<Equipement> localEquipements = equipementManager.getEquipementsByType(context.getContentResolver(),
				typeEquipement);

		for (final Equipement equipement : localEquipements) {
			overlayItem = new BasicOverlayItem(GeoPointUtils.getGeoPoint(equipement), equipement.getNom(),
					typeOverlayItem);
			overlayItem.setId(equipement.getId());
			newItemizedOverlay.addOverlay(overlayItem);
			equipements.put(equipement.getId(), equipement);
		}

		return newItemizedOverlay;
	}

	@Override
	public BasicItemizedOverlay getOverlay(Context context, int defaultItemId) {
		final BasicItemizedOverlay newItemizedOverlay;
		final Equipement item = equipementManager.getSingle(context.getContentResolver(), defaultItemId);
		if (item != null) {
			final Location location = new Location(LocationManager.GPS_PROVIDER);
			location.setLatitude(item.getLatitude());
			location.setLongitude(item.getLongitude());
			newItemizedOverlay = getOverlay(context, location);
		} else {
			newItemizedOverlay = getOverlay(context.getResources());
		}
		return newItemizedOverlay;
	}

	@Override
	public boolean isMoveable() {
		return false;
	}

}
