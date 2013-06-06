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
import android.graphics.Color;
import android.location.Address;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;

public abstract class FormatUtils {

	public static final String SENS_ARROW = "\u2192";
	public static final String TOUT_LE_RESEAU = "\u221E";
	public static final String DOT = "\u2022";

	private FormatUtils() {
	}

	public static String formatSens(final String sens) {
		return SENS_ARROW + " " + sens;
	}

	public static String formatArretSens(final String arret, final String sens) {
		return arret + " " + SENS_ARROW + " " + sens;
	}

	public static String formatTitle(final String ligne, final String sens) {
		return ligne + " " + SENS_ARROW + " " + sens;
	}

	public static String formatTitle(final String ligne, final String arret, final String sens) {
		return ligne + " " + DOT + " " + arret + " " + SENS_ARROW + " " + sens;
	}

	public static String formatWithDot(final String a, final String b) {
		return a + " " + DOT + " " + b;
	}

	public static CharSequence formatTimeAmPm(final Context context, final String time) {
		if (android.text.format.DateFormat.is24HourFormat(context)) {
			return time;
		} else {
			final SpannableString spannable = new SpannableString(time);
			spannable.setSpan(new RelativeSizeSpan(0.45f), time.length() - 3, time.length(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

			return spannable;
		}
	}

	public static String formatMinutes(final Context context, final long millisecondes) {
		String delay = "";
		final int minutes = (int) (millisecondes / 60000);

		if (minutes < 60) {
			delay = context.getString(R.string.format_minutes, minutes);
		} else {
			final int heures = minutes / 60;
			final int reste = minutes - heures * 60;
			delay = context.getString(R.string.format_heures, heures, (reste < 10) ? "0" + reste : reste);
		}

		return delay;
	}

	public static String formatMetres(final Context context, final double metres) {
		String result = "";

		if (metres < 1000) {
			result = context.getString(R.string.format_metres, Math.round(metres));
		} else {
			result = context.getString(R.string.format_km, metres / 1000);
		}

		return result;
	}

	public static String formatAddress(final Address address, StringBuilder stringBuilder) {
		if (stringBuilder == null) {
			stringBuilder = new StringBuilder();
		}

		stringBuilder.setLength(0);
		final int addressLineSize = address.getMaxAddressLineIndex();
		for (int i = 0; i < addressLineSize; i++) {
			stringBuilder.append(address.getAddressLine(i));
			if (i != addressLineSize - 1) {
				stringBuilder.append(", ");
			}
		}
		return stringBuilder.toString();
	}

	public static CharSequence formatAddressTwoLine(final Address address) {

		final int addressLineSize = address.getMaxAddressLineIndex();
		final StringBuilder stringBuilder = new StringBuilder();
		String firstLine = "";
		SpannableString spannable = null;

		if (addressLineSize > 0) {
			for (int i = 0; i < addressLineSize; i++) {
				if (i == 0) {
					firstLine = address.getAddressLine(i) + "\n";
					stringBuilder.append(firstLine);
				} else {
					stringBuilder.append(address.getAddressLine(i));
				}

				if (i > 0 && i != addressLineSize - 1) {
					stringBuilder.append(", ");
				}
			}

			spannable = new SpannableString(stringBuilder.toString());
			spannable.setSpan(new RelativeSizeSpan(0.8f), firstLine.length(), stringBuilder.length(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			spannable.setSpan(new ForegroundColorSpan(Color.GRAY), firstLine.length(), stringBuilder.length(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}

		return spannable;

	}
}
