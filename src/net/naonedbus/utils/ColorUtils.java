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

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

public abstract class ColorUtils {

	public static synchronized GradientDrawable getGradiant(final int color) {
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

	public static synchronized int getDarkerColor(final int color) {
		// Get darker color
		final float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[2] *= 0.7f;
		return Color.HSVToColor(hsv);
	}

	public static synchronized int getStackedBackgroundColor(final int color) {
		// Get darker color
		final float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[2] *= 0.2f;
		return Color.HSVToColor(hsv);
	}

	public static synchronized int getLighterColor(final int color) {
		// Get lighter color
		final float[] hsv = new float[3];

		Color.colorToHSV(color, hsv);
		hsv[1] *= 0.2f; // Saturation
		hsv[2] *= 1.5f; // Value
		return Color.HSVToColor(hsv);
	}

	public static int getDarkerColor(final int color, final float ratio) {
		// Get darker color
		final float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[2] *= ratio;
		return Color.HSVToColor(hsv);
	}

	/**
	 * Blend two colors.
	 * 
	 * @param color1
	 *            First color to blend.
	 * @param color2
	 *            Second color to blend.
	 * @param ratio
	 *            Blend ratio. 0.5 will give even blend, 1.0 will return color1,
	 *            0.0 will return color2 and so on.
	 * @return Blended color.
	 */
	public static int blend(final int color1, final int color2, final double ratio) {
		final float r = (float) ratio;
		final float ir = (float) 1.0 - r;

		final float rgb1[] = new float[3];
		final float rgb2[] = new float[3];

		rgb1[0] = Color.red(color1);
		rgb1[1] = Color.green(color1);
		rgb1[2] = Color.blue(color1);
		rgb2[0] = Color.red(color2);
		rgb2[1] = Color.green(color2);
		rgb2[2] = Color.blue(color2);

		final int color = Color.rgb(Math.round(rgb1[0] * r + rgb2[0] * ir), Math.round(rgb1[1] * r + rgb2[1] * ir),
				Math.round(rgb1[2] * r + rgb2[2] * ir));

		return color;
	}

	public static synchronized GradientDrawable getRoundedGradiant(final int color) {
		if (color == Color.TRANSPARENT) {
			return null;
		}

		final GradientDrawable d = getGradiant(color);
		d.setCornerRadius(3);
		return d;
	}

	public static synchronized GradientDrawable getHorizontalGradiant(final int color, final int backgroundColor) {
		final int[] colors = new int[2];
		colors[0] = backgroundColor;
		colors[1] = color;

		final GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, colors);
		d.setDither(true);

		return d;
	}

}
