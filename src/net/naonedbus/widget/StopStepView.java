package net.naonedbus.widget;

import net.naonedbus.utils.ColorUtils;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.TextView;

public class StopStepView extends TextView {

	public static enum Type {
		FIRST, LAST, MIDDLE;
	}

	private static final int COLUMN_WIDTH = 40;

	private static final int DOT_RADIUS = 4;
	private static final int DOT_RADIUS_HEADSIGN = 8;
	private static final int DOT_RADIUS_BORDER = 1;

	private static final float STROKE_WIDTH = 2f;

	private Paint mPaint;
	private int mColor;
	private int mSecondaryColor;

	private int mColumnWidth;
	private int mDotRadius;
	private int mDotRadiusHeadsign;
	private int mDotRadiusBorder;
	private float mStrokeWidth;

	private Type mType = Type.MIDDLE;

	public StopStepView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public StopStepView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public StopStepView(final Context context) {
		super(context);
		init();
	}

	private void init() {
		final DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
		mColumnWidth = Math.round(COLUMN_WIDTH * metrics.density);
		mDotRadius = Math.round(DOT_RADIUS * metrics.density);
		mDotRadiusBorder = Math.round(DOT_RADIUS_BORDER * metrics.density);
		mDotRadiusHeadsign = Math.round(DOT_RADIUS_HEADSIGN * metrics.density);
		mStrokeWidth = STROKE_WIDTH * metrics.density;

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeWidth(mStrokeWidth);

		setColor(Color.rgb(100, 100, 100));
	}

	public void setType(final Type type) {
		if (mType != type) {
			mType = type;
			invalidate();
		}
	}

	public void setColor(final int color) {
		mColor = color;
		mSecondaryColor = ColorUtils.getLighterColor(color);
		mPaint.setColor(mColor);
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		switch (mType) {
		case MIDDLE:
			drawLine(canvas);
			break;
		case FIRST:
			drawBottomLine(canvas);
			break;
		case LAST:
			drawTopLine(canvas);
			break;
		}

		if (mType == Type.MIDDLE) {
			drawNormalDot(canvas);
		} else
			drawHeadsignDot(canvas);

		super.onDraw(canvas);
	}

	private void drawLine(final Canvas canvas) {
		drawLine(canvas, 0, getMeasuredHeight());
	}

	private void drawTopLine(final Canvas canvas) {
		drawLine(canvas, 0, getMeasuredHeight() / 2);
	}

	private void drawBottomLine(final Canvas canvas) {
		drawLine(canvas, getMeasuredHeight() / 2, getMeasuredHeight());
	}

	private void drawLine(final Canvas canvas, final int top, final int bottom) {
		mPaint.setColor(mColor);
		mPaint.setStrokeWidth(mStrokeWidth);
		canvas.drawLine(mColumnWidth / 2, top, mColumnWidth / 2, bottom, mPaint);
	}

	private void drawHeadsignDot(final Canvas canvas) {
		final int x = mColumnWidth - mColumnWidth / 2;
		final int y = getHeight() / 2;

		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setColor(mColor);
		canvas.drawCircle(x, y, mDotRadiusHeadsign, mPaint);
		mPaint.setStyle(Paint.Style.STROKE);
	}

	private void drawNormalDot(final Canvas canvas) {
		final int x = mColumnWidth - mColumnWidth / 2;
		final int y = getHeight() / 2;

		mPaint.setColor(mColor);
		canvas.drawCircle(x, y, mDotRadius, mPaint);

		mPaint.setStyle(Paint.Style.FILL);

		mPaint.setColor(mSecondaryColor);
		canvas.drawCircle(x, y, mDotRadius, mPaint);

		mPaint.setStyle(Paint.Style.STROKE);
	}
}
