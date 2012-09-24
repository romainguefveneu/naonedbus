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
package net.naonedbus.utils;

import android.content.Context;
import android.util.TypedValue;

/**
 * @author romain.guefveneu
 * 
 */
public abstract class DpiUtils {

	/**
	 * Transformer une unité en Pixel en DPI
	 * 
	 * @param context
	 * @param px
	 * @return Le nombre en DPI.
	 */
	public static int getDpiFromPx(final Context context, final int px) {
		int value = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) px, context.getResources()
				.getDisplayMetrics());
		return value;

	}

}
