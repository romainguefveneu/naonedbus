package net.naonedbus.utils;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;

public abstract class SymbolesUtils {

	public static final String SENS_ARROW = "\u2192";
	public static final String TOUT_LE_RESEAU = "\u221E";
	public static final String DOT = "\u2022";

	private SymbolesUtils() {
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

	public static CharSequence formatTime(final Context context, final String time) {
		if (android.text.format.DateFormat.is24HourFormat(context)) {
			return time;
		} else {
			final SpannableString spannable = new SpannableString(time);
			spannable.setSpan(new RelativeSizeSpan(0.45f), time.length() - 3, time.length(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

			return spannable;
		}
	}
}
