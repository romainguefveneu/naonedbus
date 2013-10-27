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
package net.naonedbus.utils;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.View;

public abstract class ColorUtils {

	public static GradientDrawable getGradiant(final int color) {
		int[] colors;
		int darkerColor;
		// Get darker color
		final float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[2] *= 0.9f; // value component
		darkerColor = Color.HSVToColor(hsv);

		colors = new int[2];
		colors[0] = darkerColor;
		colors[1] = color;

		final GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, colors);
		d.setDither(true);

		return d;
	}

	public static int getDarkerColor(final int color) {
		// Get darker color
		final float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[2] *= 0.7f;
		return Color.HSVToColor(hsv);
	}

	public static int getStackedBackgroundColor(final int color) {
		// Get darker color
		final float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[2] *= 0.2f;
		return Color.HSVToColor(hsv);
	}

	public static int getLighterColor(final int color) {
		// Get lighter color
		final float[] hsv = new float[3];

		Color.colorToHSV(color, hsv);
		hsv[1] *= 0.2f; // Saturation
		hsv[2] *= 1.5f; // Value
		return Color.HSVToColor(hsv);
	}

	public static GradientDrawable getRoundedGradiant(final int color) {
		if (color == Color.TRANSPARENT) {
			return null;
		}

		final GradientDrawable d = getGradiant(color);
		d.setCornerRadius(3);
		return d;
	}

	public static GradientDrawable getHorizontalGradiant(final int color, final int backgroundColor) {
		final int[] colors = new int[2];
		colors[0] = backgroundColor;
		colors[1] = color;

		final GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, colors);
		d.setDither(true);

		return d;
	}

	public static void setBackgroundGradiant(View view, int color) {
		setBackground(view, getGradiant(color));
	}

	public static void setBackgroundRoundedGradiant(View view, int color) {
		setBackground(view, getRoundedGradiant(color));
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@SuppressWarnings("deprecation")
	public static void setBackground(View view, Drawable drawable) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			view.setBackgroundDrawable(drawable);
		} else {
			view.setBackground(drawable);
		}
	}
}
