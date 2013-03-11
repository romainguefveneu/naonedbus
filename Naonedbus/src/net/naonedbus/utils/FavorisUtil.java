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
