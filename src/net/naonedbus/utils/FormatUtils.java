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
import net.naonedbus.bean.parking.PublicParkStatus;
import android.content.Context;
import android.location.Address;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;

public abstract class FormatUtils {

	public static final String SENS_ARROW = "\u2192";
	public static final String TOUT_LE_RESEAU = "\u221E";
	public static final String DOT = "\u2022";

	private FormatUtils() {
	}

	public static String formatSens(final String direction) {
		return SENS_ARROW + " " + direction;
	}

	public static String formatSens(final CharSequence before, final CharSequence direction) {
		return before + " " + SENS_ARROW + " " + direction;
	}

	public static String formatLigneArretSens(final Context context, final String route, final String stop,
			final String direction) {
		return context.getString(R.string.dialog_title_menu_lignes, route) + " " + DOT + " " + stop + " " + SENS_ARROW
				+ " " + direction;
	}

	public static String formatTitle(final String route, final String direction) {
		return route + " " + SENS_ARROW + " " + direction;
	}

	public static String formatTitle(final String route, final String stop, final String direction) {
		return route + " " + DOT + " " + stop + " " + SENS_ARROW + " " + direction;
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

	public static CharSequence formatTerminusLetter(final Context context, final CharSequence text) {
		final SpannableString spannable = new SpannableString(text);
		spannable.setSpan(new AbsoluteSizeSpan(18, true), text.length() - 1, text.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return spannable;
	}

	public static CharSequence formatColorAndSize(final Context context, final int color, final String text) {
		final SpannableString spannable = new SpannableString(text);
		spannable.setSpan(new ForegroundColorSpan(color), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		spannable.setSpan(new RelativeSizeSpan(0.9f), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return spannable;
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

	public static String[] formatAddressTwoLine(final Address address) {
		final String[] result = new String[2];
		final int addressLineSize = address.getMaxAddressLineIndex();
		final StringBuilder stringBuilder = new StringBuilder();

		if (addressLineSize > 0) {
			for (int i = 0; i < addressLineSize; i++) {
				if (i == 0) {
					result[0] = address.getAddressLine(i);
				} else {
					stringBuilder.append(address.getAddressLine(i));
				}

				if (i > 0 && i != addressLineSize - 1) {
					stringBuilder.append(", ");
				}
			}
			result[1] = stringBuilder.toString();
		}

		return result;
	}

	public static String formatBicloos(final Context context, final int availableBikes, final int availableStands) {
		final String bikes = context.getResources().getQuantityString(R.plurals.bicloo_velos_disponibles,
				availableBikes, availableBikes);
		final String stands = context.getResources().getQuantityString(R.plurals.bicloo_places_disponibles,
				availableStands, availableStands);

		final String description = context.getResources().getQuantityString(R.plurals.bicloo,
				availableBikes + availableStands, bikes, stands);

		return description;
	}

	public static String formatParkingPublic(final Context context, final PublicParkStatus status,
			final int placesDisponibles) {
		String detail;
		if (status == PublicParkStatus.OPEN) {
			if (placesDisponibles > 0) {
				detail = context.getResources().getQuantityString(R.plurals.parking_places_disponibles,
						placesDisponibles, placesDisponibles);
			} else {
				detail = context.getString(R.string.parking_places_disponibles_zero);
			}
		} else {
			detail = context.getString(status.getTitleRes());
		}

		return detail;
	}
}
