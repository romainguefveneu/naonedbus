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
package net.naonedbus.helper;

import org.joda.time.DateTime;
import org.joda.time.base.BaseDateTime;

import android.content.Context;
import android.text.format.DateUtils;

/**
 * Helper pour le formattage d'une date pour un format plus human-friendly.
 * 
 * @author romain.guefveneu
 * 
 */
public class DateTimeFormatHelper {

	private static final String ARROW = " \u2192 ";

	private final Context mContext;

	public DateTimeFormatHelper(final Context context) {
		mContext = context;
	}

	/**
	 * Formater 2 dates en évitant de répéter 2 fois le même jour
	 * 
	 * @param debut
	 * @param fin
	 * @return Les dates formatées.
	 */
	public String formatDuree(final DateTime debut, final DateTime fin) {
		final StringBuilder builder = new StringBuilder();
		builder.append(formatDateTime(debut));

		if (fin != null) {
			builder.append(ARROW);
			if (debut.toDateMidnight().equals(fin.toDateMidnight())) {
				// Même jour
				builder.append(DateUtils.formatDateTime(this.mContext, fin.getMillis(), DateUtils.FORMAT_SHOW_TIME));
			} else {
				builder.append(formatDateTime(fin));
			}
		}

		return builder.toString();
	}

	/**
	 * Formater la date selon un format plus human-friendly.
	 * 
	 * @param dateTime
	 * @return La date formatée.
	 */
	public String formatDateTime(final BaseDateTime dateTime) {
		return DateUtils.formatDateTime(this.mContext, dateTime.getMillis(), DateUtils.FORMAT_SHOW_DATE
				| DateUtils.FORMAT_SHOW_TIME);
	}

}
