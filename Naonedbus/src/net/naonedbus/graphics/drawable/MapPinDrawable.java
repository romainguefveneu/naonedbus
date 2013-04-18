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
package net.naonedbus.graphics.drawable;

import net.naonedbus.R;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;

import com.google.android.maps.MapView;

/**
 * A {@link Drawable} dedicated to {@link MapView}s. A {@link MapPinDrawable}
 * displays a rounded pin with a dot in the middle. This class lets you easily
 * change the color of the pin as well as the dot in the center of the pin.
 * 
 * @author Cyril Mottier
 */
public class MapPinDrawable extends Drawable {

	private static final int COLOR_MODE_UNKNOWN = -1;
	private static final int COLOR_MODE_COLOR = 1;
	private static final int COLOR_MODE_COLOR_STATE_LIST = 2;

	private static final Paint sClearerPaint;

	static {
		sClearerPaint = new Paint();
		sClearerPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
	}

	private int mColorMode = COLOR_MODE_UNKNOWN;
	private int mPinColor;
	private int mDotRes = R.drawable.gd_map_pin_dot;
	private ColorStateList mPinColorStateList;
	private ColorStateList mDotColorStateList;

	private int mCurrentPinColor;

	private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint mMapPinPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

	private Bitmap mBase;
	private Bitmap mPin;
	private Bitmap mDot;
	private Bitmap mMapPin;

	private boolean mNeedRebuild;
	private boolean mSelected = false;

	/**
	 * Create a new {@link MapPinDrawable} that has a single color.
	 * 
	 * @param res
	 *            The application resources
	 * @param color
	 *            The color of the pin/dot
	 */
	public MapPinDrawable(final Resources res, final int color) {
		this(res, color, color);
	}

	/**
	 * Create a new {@link MapPinDrawable}.
	 * 
	 * @param res
	 *            The application resources
	 * @param pinClor
	 *            The color of the pin
	 * @param dotColor
	 *            The color of the dot
	 */
	public MapPinDrawable(final Resources res, final int pinColor, final int dotRes) {
		mDotRes = dotRes;
		initBitmaps(res);
		setColors(res.getColor(pinColor), res.getColor(pinColor));
	}

	/**
	 * Create a new {@link MapPinDrawable} that may change color depending on
	 * its current state.
	 * 
	 * @param res
	 *            The application resources
	 * @param color
	 *            A {@link ColorStateList} object giving a set of colors
	 *            changing depending on the current {@link Drawable}'s state
	 */
	public MapPinDrawable(final Resources res, final ColorStateList color) {
		this(res, color, color);
	}

	/**
	 * Create a new {@link MapPinDrawable} that may change color depending on
	 * its current state.
	 * 
	 * @param res
	 *            The application resources
	 * @param pinColor
	 *            A {@link ColorStateList} object giving a set of colors for the
	 *            pin changing depending on the current {@link Drawable} 's
	 *            state
	 * @param dotColor
	 *            A {@link ColorStateList} object giving a set of colors for the
	 *            dot changing depending on the current {@link Drawable} 's
	 *            state
	 */
	public MapPinDrawable(final Resources res, final ColorStateList pinColor, final ColorStateList dotColor) {
		initBitmaps(res);
		setColors(pinColor, dotColor);
	}

	/**
	 * Create a copy of a {@link MapPinDrawable}.
	 * 
	 * @param mapPinDrawable
	 */
	public MapPinDrawable(final MapPinDrawable mapPinDrawable) {
		mBase = mapPinDrawable.mBase;
		mPin = mapPinDrawable.mPin;
		mDot = mapPinDrawable.mDot;

		mColorMode = COLOR_MODE_COLOR;
		mPinColor = mCurrentPinColor = mapPinDrawable.mPinColor;
		mNeedRebuild = true;
	}

	private void initBitmaps(final Resources res) {
		// TODO Cyril: Share those Bitmaps between all instances of
		// MapPinDrawable in order to save memory
		mBase = BitmapFactory.decodeResource(res, R.drawable.gd_map_pin_base);
		mPin = BitmapFactory.decodeResource(res, R.drawable.gd_map_pin_pin);
		mDot = BitmapFactory.decodeResource(res, mDotRes);
	}

	/**
	 * Set the color for the pin/dot
	 * 
	 * @param pinClor
	 *            The color of the pin
	 * @param dotColor
	 *            The color of the dot
	 */
	public void setColors(final int pinColor, final int dotColor) {
		if (mColorMode != COLOR_MODE_COLOR || mPinColor != pinColor) {
			mColorMode = COLOR_MODE_COLOR;
			mPinColor = mCurrentPinColor = pinColor;
			mNeedRebuild = true;
		}
	}

	/**
	 * Set the color for the pin/dot
	 * 
	 * @param pinClor
	 *            The color of the pin
	 * @param dotColor
	 *            The color of the dot
	 */
	public void setColors(final ColorStateList pinColor, final ColorStateList dotColor) {
		if (mColorMode != COLOR_MODE_COLOR_STATE_LIST || mPinColorStateList != pinColor
				|| mDotColorStateList != dotColor) {
			mColorMode = COLOR_MODE_COLOR_STATE_LIST;
			mPinColorStateList = pinColor;
			mDotColorStateList = dotColor;
			mNeedRebuild = true;
		}
	}

	@Override
	public boolean isStateful() {
		return true;
	}

	@Override
	public int getIntrinsicWidth() {
		return (mBase != null) ? mBase.getWidth() : -1;
	}

	@Override
	public int getIntrinsicHeight() {
		return (mBase != null) ? mBase.getHeight() : -1;
	}

	@Override
	protected boolean onStateChange(final int[] stateSet) {
		if (mColorMode == COLOR_MODE_COLOR_STATE_LIST) {
			final int pinColor = (mPinColorStateList != null) ? mPinColorStateList.getColorForState(stateSet, Color.BLACK)
					: Color.BLACK;
			if (mCurrentPinColor != pinColor) {
				mCurrentPinColor = pinColor;
				mNeedRebuild = true;
				return true;
			}
		}
		return false;
	}

	@Override
	public void draw(final Canvas canvas) {
		if (mNeedRebuild) {

			if (mMapPin == null) {
				mMapPin = Bitmap.createBitmap(mBase.getWidth(), mBase.getHeight(), Bitmap.Config.ARGB_8888);
			}
			final Canvas c = new Canvas(mMapPin);
			c.drawRect(0, 0, mMapPin.getWidth(), mMapPin.getHeight(), sClearerPaint);

			// 1 - Draw the base
			c.drawBitmap(mBase, 0, 0, null);
			// 2 - Draw the pin on top of it
			mPaint.setColorFilter(new LightingColorFilter(Color.BLACK, mCurrentPinColor));
			c.drawBitmap(mPin, 0, 0, mPaint);
			// 3 - Draw the dot on top of everything
			mPaint.setColorFilter(new LightingColorFilter(Color.WHITE, mCurrentPinColor));
			c.drawBitmap(mDot, 0, 0, mPaint);

			mNeedRebuild = false;
		}

		canvas.drawBitmap(mMapPin, null, getBounds(), mMapPinPaint);
	}

	@Override
	public void setAlpha(final int alpha) {
		mMapPinPaint.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(final ColorFilter cf) {
		mMapPinPaint.setColorFilter(cf);
	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}

	public boolean isSelected() {
		return mSelected;
	}

	public void setSelected(final boolean selected) {
		this.mSelected = selected;
	}

}
