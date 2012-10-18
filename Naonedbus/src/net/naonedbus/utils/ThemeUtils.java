package net.naonedbus.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.TypedValue;

public abstract class ThemeUtils {

	private ThemeUtils() {
	}

	public static int getDimensionPixelSize(Context context, int attrId) {
		final TypedValue typedValue = new TypedValue();
		final TypedArray array = context.obtainStyledAttributes(typedValue.resourceId, new int[] { attrId });
		return array.getDimensionPixelSize(0, 0);
	}
}
