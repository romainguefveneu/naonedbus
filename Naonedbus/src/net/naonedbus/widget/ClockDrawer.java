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
package net.naonedbus.widget;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

public class ClockDrawer {
	final private Paint mPaint;
	final private Paint mClearPaint;
	final float mHandWidth;
	final float mHandHeight;

	public ClockDrawer(final float handWidth, final float handHeight) {

		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setPathEffect(new CornerPathEffect(5));
		mPaint.setStrokeWidth(handWidth);

		mHandWidth = handWidth;
		mHandHeight = handHeight;

		mClearPaint = new Paint();
		mClearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

	}

	public void drawClockBitmap(final Bitmap background, final int color, final Date date) {
		final Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(date);

		final float min = calendar.get(Calendar.MINUTE);
		final float hour = calendar.get(Calendar.HOUR_OF_DAY) + min / 60.0f;
		final float x = background.getWidth() / 2;
		final float y = background.getHeight() / 2;
		final float hourHandHeight = (2 * mHandHeight) / 3;

		final Canvas canvas = new Canvas(background);
		// Clear canvas
		canvas.drawRect(0, 0, background.getWidth(), background.getHeight(), mClearPaint);

		mPaint.setColor(color);

		canvas.drawCircle(x, y, mHandHeight + mHandWidth, mPaint);

		// Heure
		canvas.drawLine(x, y, (float) (x + hourHandHeight * Math.cos(Math.toRadians((hour / 12.0f * 360.0f) - 90f))),
				(float) (y + hourHandHeight * Math.sin(Math.toRadians((hour / 12.0f * 360.0f) - 90f))), mPaint);
		canvas.save();

		// Minute
		canvas.drawLine(x, y, (float) (x + mHandHeight * Math.cos(Math.toRadians((min / 60.0f * 360.0f) - 90f))),
				(float) (y + mHandHeight * Math.sin(Math.toRadians((min / 60.0f * 360.0f) - 90f))), mPaint);
		canvas.save();

	}
}
