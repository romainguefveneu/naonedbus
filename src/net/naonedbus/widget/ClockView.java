package net.naonedbus.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class ClockView extends View {

	private static final int DISABLED_ALPHA = 150;

	private static final int HAND_WIDTH = 2;
	private static final int HAND_HEIGHT = 2;

	private Paint mPaint;
	private Paint mBackgroundPaint;
	private float mHandWidth;
	private int mMinutes;

	public ClockView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public ClockView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ClockView(final Context context) {
		this(context, null);
	}

	private void init() {
		setWillNotDraw(false);

		final float density = getContext().getResources().getDisplayMetrics().density;
		mHandWidth = HAND_WIDTH * density;

		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setPathEffect(new CornerPathEffect(10f));
		mPaint.setColor(Color.WHITE);
		mPaint.setStrokeWidth(mHandWidth);

		mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mBackgroundPaint.setStyle(Paint.Style.FILL);
		mBackgroundPaint.setColor(Color.LTGRAY);
	}

	@Override
	public void setBackgroundColor(final int color) {
		mBackgroundPaint.setColor(color);
	}

	public void setColor(final int color) {
		mPaint.setColor(color);
	}

	public void setMinutes(final int minutes) {
		mMinutes = minutes;
	}

	@Override
	public void setEnabled(final boolean enabled) {
		mPaint.setAlpha(enabled ? 255 : DISABLED_ALPHA);
		mBackgroundPaint.setAlpha(enabled ? 255 : DISABLED_ALPHA);
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		super.onDraw(canvas);

		final int hour = mMinutes / 60;
		final int min = mMinutes - (hour * 60);
		final float x = getWidth() / 2f;
		final float y = getHeight() / 2f;
		final float minHandHeight = (getHeight() / 2f) * 0.6f;
		final float hourHandHeight = (getHeight() / 2f) * 0.4f;

		canvas.drawCircle(x, y, getWidth() / 2f, mBackgroundPaint);

		// Hour
		canvas.drawLine(x, y, (float) (x + hourHandHeight * Math.cos(Math.toRadians((hour / 12.0f * 360.0f) - 90f))),
				(float) (y + hourHandHeight * Math.sin(Math.toRadians((hour / 12.0f * 360.0f) - 90f))), mPaint);

		// Minute
		canvas.drawLine(x, y, (float) (x + minHandHeight * Math.cos(Math.toRadians((min / 60.0f * 360.0f) - 90f))),
				(float) (y + minHandHeight * Math.sin(Math.toRadians((min / 60.0f * 360.0f) - 90f))), mPaint);
	}

}
