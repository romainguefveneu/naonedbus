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

import net.naonedbus.R;
import android.content.Context;

public abstract class FavorisUtil {
	private static final Integer MIN_HOUR = 60;
	private static final Integer MIN_DURATION = 0;

	private FavorisUtil() {

	}

	public static String formatDelayLoading(final Context context, final Integer minutes) {
		return formatDelay(context, minutes, true);
	}

	public static String formatDelayNoDeparture(final Context context, final Integer minutes) {
		return formatDelay(context, minutes, false);
	}

	private static String formatDelay(final Context context, final Integer minutes, final boolean loading) {
		String delay = "";

		if (minutes == null) {
			delay = loading ? null : context.getString(R.string.msg_aucun_depart_24h);
		} else {
			if (minutes >= MIN_DURATION) {
				if (minutes == MIN_DURATION) {
					delay = context.getString(R.string.msg_depart_proche);
				} else if (minutes <= MIN_HOUR) {
					delay = context.getString(R.string.msg_depart_min, minutes);
				} else {
					delay = context.getString(R.string.msg_depart_heure, minutes / MIN_HOUR);
				}
			}
		}

		return delay;
	}

}
