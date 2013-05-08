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
package net.naonedbus.activity.map.overlay;

import java.util.ArrayList;

import net.naonedbus.activity.map.overlay.item.BasicOverlayItem;
import net.naonedbus.graphics.drawable.MapPinDrawable;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;

public class BasicItemizedOverlay extends ItemizedOverlay<BasicOverlayItem> {

	public interface OnBasicItemTapListener {
		void onItemTap(BasicOverlayItem item);
	}

	private final ArrayList<BasicOverlayItem> mOverlays = new ArrayList<BasicOverlayItem>();
	private final Drawable mDefaultMarker;
	private final TypeOverlayItem mType;

	private BasicOverlayItem mLastSelectedItem;
	private OnBasicItemTapListener mOnBasicItemTapListener;

	public BasicItemizedOverlay(final TypeOverlayItem type, final MapPinDrawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
		mDefaultMarker = defaultMarker;
		mType = type;
	}

	public void addOverlay(final BasicOverlayItem overlay) {
		if (!mOverlays.contains(overlay)) {
			mOverlays.add(overlay);
			populate();
		}
	}

	public void addSelectedOverlay(final BasicOverlayItem overlay) {
		if (!mOverlays.contains(overlay)) {
			mLastSelectedItem = overlay;
			addOverlay(overlay);
		}
	}

	public void setOnBasicItemTapListener(final OnBasicItemTapListener onBasicItemTapeListener) {
		mOnBasicItemTapListener = onBasicItemTapeListener;
	}

	public void clear() {
		mOverlays.clear();
	}

	/**
	 * Annuler la sélection courante.
	 */
	public void resetFocus() {
		if ((mLastSelectedItem != null)) {
			mLastSelectedItem.setMarker(mDefaultMarker);
			mLastSelectedItem = null;
		}
	}

	@Override
	public void setFocus(final BasicOverlayItem item) {

		final BasicOverlayItem localItem = getItemById(item.getId());

		if (localItem != null) {
			super.setFocus(localItem);
			if (mOnBasicItemTapListener != null) {
				mOnBasicItemTapListener.onItemTap(localItem);
			}
		}

	}

	public BasicOverlayItem getItemById(final Integer id) {
		for (final BasicOverlayItem item : mOverlays) {
			final Integer itemId = item.getId();
			if (itemId != null && itemId.equals(id)) {
				return item;
			}
		}
		return null;
	}

	@Override
	protected BasicOverlayItem createItem(final int i) {
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		return mOverlays.size();
	}

	@Override
	protected boolean onTap(final int index) {
		final BasicOverlayItem item = mOverlays.get(index);
		setFocus(item);
		return true;
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event, final MapView mapView) {
		return super.onTouchEvent(event, mapView);
	}

	public TypeOverlayItem getType() {
		return mType;
	}

}
