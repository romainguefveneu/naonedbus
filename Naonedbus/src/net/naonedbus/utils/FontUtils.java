package net.naonedbus.utils;

import android.content.Context;
import android.graphics.Typeface;

public abstract class FontUtils {

	public static Typeface getRobotoLight(final Context context) {
		return Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
	}

}
