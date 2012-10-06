package net.naonedbus.utils;

public abstract class SymbolesUtils {

	public static final String SENS_ARROW = "\u2192";

	private SymbolesUtils() {
	}

	public static String formatSens(String sens) {
		return SENS_ARROW + " " + sens;
	}

}
