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

	private static final int COLUMN_WIDTH = 36;

	private static final int DOT_RADIUS = 8;
	private static final int DOT_RADIUS_HEADSIGN = 12;
	private static final int DOT_RADIUS_HEADSIGN_CENTER = 4;
	private static final int DOT_RADIUS_BORDER = 1;

	private static final float STROKE_WIDTH = 6f;
	private static final float STROKE_BORDER_WIDTH = 2f;

	private Paint mPaint;
	private final int[] mColor = new int[2];
	private final int[] mSecondaryColor = new int[2];
	private final int[] mBorderColor = new int[2];
	private final int[] mBorderAlternativeColor = new int[2];
	private final int[] mAlternativeColor = new int[2];
	private final int[] mPointColor = new int[2];
	private int mBackgroundColor;

	private int mColumnWidth;
	private int mDotRadius;
	private int mDotRadiusHeadsign;
	private int mDotRadiusHeadsignCenter;
	private int mDotRadiusBorder;
	private float mStrokeWidth;
	private float mStrokeBorderWidth;

	private int mColorIndex = 0;

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
		mDotRadiusHeadsignCenter = Math.round(DOT_RADIUS_HEADSIGN_CENTER * metrics.density);
		mStrokeWidth = STROKE_WIDTH * metrics.density;
		mStrokeBorderWidth = STROKE_BORDER_WIDTH * metrics.density;

		mBackgroundColor = getResources().getColor(android.R.color.background_light);

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeWidth(mStrokeWidth);

		setColor(Color.rgb(100, 100, 100));
	}

	public void setType(final Type type) {
		mType = type;
	}

	public void setColor(final int color) {
		// Enabled
		mColor[0] = color;
		mBorderColor[0] = ColorUtils.getDarkerColor(color);
		mPointColor[0] = ColorUtils.getLighterColor(color);
		mSecondaryColor[0] = ColorUtils.getDarkerColor(color, 0.9f);

		mAlternativeColor[0] = ColorUtils.blend(color, mBackgroundColor, 0.1f);
		mBorderAlternativeColor[0] = ColorUtils.blend(mBorderColor[0], mBackgroundColor, 0.1f);

		// Disabled
		mColor[1] = ColorUtils.blend(mColor[0], mBackgroundColor, 0.5f);
		mBorderColor[1] = ColorUtils.blend(mBorderColor[0], mBackgroundColor, 0.5f);
		mPointColor[1] = ColorUtils.blend(mPointColor[0], mBackgroundColor, 0.5f);
		mSecondaryColor[1] = ColorUtils.blend(mSecondaryColor[0], mBackgroundColor, 0.5f);

		mAlternativeColor[1] = ColorUtils.blend(mAlternativeColor[0], mBackgroundColor, 0.5f);
		mBorderAlternativeColor[1] = ColorUtils.blend(mBorderAlternativeColor[0], mBackgroundColor, 0.5f);

		// Set paint color
		mPaint.setColor(mColor[0]);
	}

	@Override
	public void setEnabled(final boolean enabled) {
		super.setEnabled(enabled);
		mColorIndex = enabled ? 0 : 1;
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
			drawNormalDot(canvas, false);
		} else
			drawHeadsignDot(canvas, true);

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
		mPaint.setColor(mBorderColor[mColorIndex]);
		mPaint.setStrokeWidth(mStrokeWidth + mStrokeBorderWidth);
		canvas.drawLine(mColumnWidth / 2, top, mColumnWidth / 2, bottom, mPaint);

		mPaint.setColor(mColor[mColorIndex]);
		mPaint.setStrokeWidth(mStrokeWidth);
		canvas.drawLine(mColumnWidth / 2, top, mColumnWidth / 2, bottom, mPaint);
	}

	private void drawHeadsignDot(final Canvas canvas, final boolean drawPoint) {
		drawDot(canvas, mDotRadiusHeadsign, drawPoint);
	}

	private void drawNormalDot(final Canvas canvas, final boolean drawPoint) {
		if (drawPoint) {
			drawDot(canvas, mDotRadiusHeadsign, false);
		}
		drawDot(canvas, mDotRadius, drawPoint);
	}

	private void drawDot(final Canvas canvas, final int radius, final boolean drawPoint) {
		final int x = mColumnWidth - mColumnWidth / 2;
		final int y = getHeight() / 2;

		mPaint.setStyle(Paint.Style.FILL);

		mPaint.setColor(mBorderColor[mColorIndex]);
		canvas.drawCircle(x, y, radius + mDotRadiusBorder, mPaint);

		mPaint.setColor(mPointColor[mColorIndex]);
		canvas.drawCircle(x, y, radius, mPaint);

		if (drawPoint) {
			mPaint.setColor(mColor[mColorIndex]);
			canvas.drawCircle(x, y, mDotRadiusHeadsignCenter, mPaint);
			mPaint.setColor(mColor[mColorIndex]);
		}

		mPaint.setStyle(Paint.Style.STROKE);
	}
}
