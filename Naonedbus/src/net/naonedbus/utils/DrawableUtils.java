package net.naonedbus.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;

public abstract class DrawableUtils {

	private DrawableUtils() {
	}

	public static void drawClockBitmap(Bitmap background, int color, float handWidth, float handHeight, final Date date) {
		final Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(date);

		final float min = (float) calendar.get(Calendar.MINUTE);
		final float hour = (float) calendar.get(Calendar.HOUR_OF_DAY) + min / 60.0f;
		final float x = background.getWidth() / 2;
		final float y = background.getHeight() / 2;
		final float hourHandHeight = (2 * handHeight) / 3;

		final Canvas canvas = new Canvas(background);
		final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setPathEffect(new CornerPathEffect(5));
		paint.setColor(color);
		paint.setStrokeWidth(handWidth);

		canvas.drawCircle(x, y, handHeight + handWidth, paint);

		// Heure
		canvas.drawLine(x, y, (float) (x + hourHandHeight * Math.cos(Math.toRadians((hour / 12.0f * 360.0f) - 90f))),
				(float) (y + hourHandHeight * Math.sin(Math.toRadians((hour / 12.0f * 360.0f) - 90f))), paint);
		canvas.save();

		// Minute
		canvas.drawLine(x, y, (float) (x + handHeight * Math.cos(Math.toRadians((min / 60.0f * 360.0f) - 90f))),
				(float) (y + handHeight * Math.sin(Math.toRadians((min / 60.0f * 360.0f) - 90f))), paint);
		canvas.save();

	}
}
