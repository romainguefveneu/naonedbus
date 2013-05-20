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

import java.text.DateFormatSymbols;

import net.naonedbus.R;

import org.joda.time.DateTime;
import org.joda.time.base.BaseDateTime;

import android.content.Context;

/**
 * Helper pour le formattage d'une date pour un format plus human-friendly.
 * 
 * @author romain.guefveneu
 * 
 */
public class DateTimeFormatHelper {

	private static final String ARROW = " \u2192 ";

	private final Context context;
	private final DateTime now;
	private final String[] months;

	public DateTimeFormatHelper(final Context context) {
		this.context = context;
		this.now = new DateTime();
		this.months = new DateFormatSymbols().getMonths();
	}

	/**
	 * Formatter 2 dates en évitant de répéter 2 fois le même jour
	 * 
	 * @param debut
	 * @param fin
	 * @return Les dates formattée.
	 */
	public String formatDuree(final DateTime debut, final DateTime fin) {
		final StringBuilder builder = new StringBuilder();
		builder.append(format(debut));

		if (fin != null) {
			builder.append(ARROW);
			if (debut.toDateMidnight().equals(fin.toDateMidnight())) {
				// Même jour
				formatTime(fin, builder);
			} else {
				builder.append(format(fin));
			}
		}

		return builder.toString();
	}

	/**
	 * Formatter la date selon un format plus human-friendly.
	 * 
	 * @param dateTime
	 * @return La date formattée.
	 */
	public String format(final BaseDateTime dateTime) {
		final StringBuilder builder = new StringBuilder();

		if (dateTime.getYear() == now.getYear()) {
			// Même année, on regarde le jour
			if (dateTime.getDayOfYear() == now.getDayOfYear()) {
				builder.append(context.getString(R.string.today));
			} else if (dateTime.getDayOfYear() == now.getDayOfYear() - 1) {
				builder.append(context.getString(R.string.yesterday));
			} else if (dateTime.getDayOfYear() == now.getDayOfYear() + 1) {
				builder.append(context.getString(R.string.tomorrow));
			} else {
				builder.append(dateTime.getDayOfMonth()).append(" ").append(months[dateTime.getMonthOfYear() - 1]);
			}
		} else {
			// On affiche toute la date
			builder.append(dateTime.getDayOfMonth()).append("/").append(twoDigitFormat(dateTime.getMonthOfYear()))
					.append("/").append(dateTime.getYear());
		}
		builder.append(" ");

		// Heure
		formatTime(dateTime, builder);

		return builder.toString();
	}

	/**
	 * Formatter l'heure, si différent de 0.
	 * 
	 * @param dateTime
	 * @return L'heure formattée.
	 */
	public void formatTime(final BaseDateTime dateTime, final StringBuilder builder) {
		if (dateTime.getHourOfDay() > 0) {
			builder.append(dateTime.getHourOfDay()).append("h").append(twoDigitFormat(dateTime.getMinuteOfHour()));
		}
	}

	/**
	 * Formatter un nombre sur 2 caractères.
	 * 
	 * @param value
	 * @return Le nombre avec un 0 préfixé si besoin.
	 */

	private String twoDigitFormat(final int value) {
		if (value < 10) {
			return "0" + value;
		} else {
			return String.valueOf(value);
		}
	}

}
