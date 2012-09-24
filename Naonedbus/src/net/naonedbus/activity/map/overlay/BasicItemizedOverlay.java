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
package net.naonedbus.activity.map.overlay;

import java.util.ArrayList;

import net.naonedbus.activity.map.overlay.item.BasicOverlayItem;
import net.naonedbus.graphics.drawable.MapPinDrawable;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;

public class BasicItemizedOverlay extends ItemizedOverlay<BasicOverlayItem> {

	/**
	 * Interface d'évenement de sélection d'un item
	 * 
	 * @author romain.guefveneu
	 * 
	 */
	public interface OnBasicItemTapListener {
		void onItemTap(BasicOverlayItem item);
	}

	private ArrayList<BasicOverlayItem> mOverlays = new ArrayList<BasicOverlayItem>();
	private BasicOverlayItem lastSelectedItem;
	private Drawable selectedMarker;
	private Drawable defaultMarker;
	private OnBasicItemTapListener onBasicItemTapListener;
	private TypeOverlayItem type;

	public BasicItemizedOverlay(TypeOverlayItem type, MapPinDrawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
		this.defaultMarker = defaultMarker;
		this.type = type;

		final MapPinDrawable marker = new MapPinDrawable(defaultMarker);
		marker.setSelected(true);
		this.selectedMarker = boundCenterBottom(marker);
		populate();
	}

	public void addOverlay(BasicOverlayItem overlay) {
		if (!mOverlays.contains(overlay)) {
			mOverlays.add(overlay);
			populate();
		}
	}

	public void addSelectedOverlay(BasicOverlayItem overlay) {
		if (!mOverlays.contains(overlay)) {
			lastSelectedItem = overlay;
			overlay.setMarker(selectedMarker);
			addOverlay(overlay);
		}
	}

	public void setOnBasicItemTapListener(OnBasicItemTapListener onBasicItemTapeListener) {
		this.onBasicItemTapListener = onBasicItemTapeListener;
	}

	public void clear() {
		mOverlays.clear();
	}

	/**
	 * Annuler la sélection courante.
	 */
	public void resetFocus() {
		if ((lastSelectedItem != null)) {
			lastSelectedItem.setMarker(defaultMarker);
			lastSelectedItem = null;
		}
	}

	public void setFocus(final BasicOverlayItem item) {

		final BasicOverlayItem localItem = getItemById(item.getId());

		if (localItem != null) {
			super.setFocus(localItem);

			if ((lastSelectedItem != null) && (!lastSelectedItem.equals(localItem))) {
				lastSelectedItem.setMarker(defaultMarker);
			}

			if (onBasicItemTapListener != null) {
				onBasicItemTapListener.onItemTap(localItem);
			}

			localItem.setMarker(selectedMarker);
			lastSelectedItem = localItem;
		}

	}

	public BasicOverlayItem getItemById(Integer id) {
		for (BasicOverlayItem item : mOverlays) {
			final Integer itemId = item.getId();
			if (itemId != null && itemId.equals(id)) {
				return item;
			}
		}
		return null;
	}

	@Override
	protected BasicOverlayItem createItem(int i) {
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		return mOverlays.size();
	}

	@Override
	protected boolean onTap(int index) {
		final BasicOverlayItem item = mOverlays.get(index);
		setFocus(item);
		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView) {
		return super.onTouchEvent(event, mapView);
	}

	public TypeOverlayItem getType() {
		return type;
	}

	public void setType(TypeOverlayItem type) {
		this.type = type;
	}

}
