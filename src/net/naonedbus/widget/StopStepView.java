package net.naonedbus.widget;

import net.naonedbus.R;
import net.naonedbus.utils.ColorUtils;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.TextView;

public class StopStepView extends TextView {

	private static final int COLUMN_WIDTH = 32;
	private static final int POST_LINES_ALPHA = 30;

	private static final int DOT_RADIUS = 6;
	private static final int DOT_RADIUS_HEADSIGN = 10;
	private static final int DOT_RADIUS_HEADSIGN_CENTER = 4;
	private static final int DOT_RADIUS_BORDER = 1;
	private static final float STROKE_WIDTH = 6f;
	private static final float STROKE_BORDER_WIDTH = 2f;
	private static final float TEXT_PADDING = 8f;

	private static final int ORIENTATION_NONE = 0;
	private static final int ORIENTATION_RTL = 1;
	private static final int ORIENTATION_LTR = 2;
	private static final int ORIENTATION_STRAIGHT = 3;

	private static final int STEP_MIDDLE = 0;
	private static final int STEP_FIRST = 1;
	private static final int STEP_LAST = 2;

	private Paint mPaint;
	private int mColor;
	private int mSecondaryColor;
	private int mBorderColor;
	// private int mBorderAlternativeColor;
	// private int mAlternativeColor;
	private int mPointColor;
	private int mBackgroundColor;

	/**
	 * 1 = First 0 = Middle 2 = Last
	 */
	private int mStyle = 1;
	private int mDepth = 2;
	private int mMaxDepth = 2;
	/**
	 * 1 : / 2 : \ 3 : | 0 : vide
	 */
	private int mOrientationTop = 0;
	private int mOrientationBottom = 0;

	private int mColumnWidth;
	private int mDotRadius;
	private int mDotRadiusHeadsign;
	private int mDotRadiusHeadsignCenter;
	private int mDotRadiusBorder;
	private int mTextPadding;
	private float mStrokeWidth;
	private float mStrokeBorderWidth;

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
		mTextPadding = Math.round(TEXT_PADDING * metrics.density);

		mBackgroundColor = getResources().getColor(R.color.activity_background_light);

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeWidth(mStrokeWidth);

		setColor(Color.rgb(0, 121, 69));
	}

	public void setColor(final int color) {
		mColor = color;
		mBorderColor = ColorUtils.getDarkerColor(color);
		mPointColor = ColorUtils.getLighterColor(color);
		mSecondaryColor = ColorUtils.getDarkerColor(color, 0.9f);

		// mAlternativeColor = ColorUtils.blend(color, mBackgroundColor, 0.1f);
		// mBorderAlternativeColor = ColorUtils.blend(mBorderColor,
		// mBackgroundColor, 0.1f);

		mPaint.setColor(mColor);
	}

	public void setStyle(final int style) {
		mStyle = style;
	}

	public void setDepth(final int depth) {
		mDepth = depth;
		setPadding(mDepth * mColumnWidth + mTextPadding, 0, 0, 0);
	}

	public void setOrientationTop(final int orientation) {
		mOrientationTop = orientation;
	}

	public void setOrientationBottom(final int orientation) {
		mOrientationBottom = orientation;
	}

	public void setMaxDepth(final int maxDepth) {
		mMaxDepth = maxDepth;
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		if (mOrientationBottom == ORIENTATION_LTR)
			drawLeftToRightBottom(canvas);
		else if (mOrientationBottom == ORIENTATION_RTL)
			drawRightToLeftBottom(canvas);
		else if (mOrientationBottom == ORIENTATION_NONE)
			drawLineBottom(canvas);

		if (mOrientationTop == ORIENTATION_LTR)
			drawLeftToRightTop(canvas);
		else if (mOrientationTop == ORIENTATION_RTL)
			drawRightToLeftTop(canvas);
		else if (mOrientationTop == ORIENTATION_NONE)
			drawLineTop(canvas);

		drawMainLines(canvas);

		if (mStyle == STEP_MIDDLE)
			drawNormalDot(canvas);
		else
			drawHeadsignDot(canvas);

		super.onDraw(canvas);
	}

	private void drawMainLines(final Canvas canvas) {
		int alpha = 255;
		for (int i = 0; i <= mMaxDepth; i++) {
			if (i != mDepth) {
				final int x = i * mColumnWidth - mColumnWidth / 2;

				if (i > mDepth) {
					alpha = POST_LINES_ALPHA;
					// mPaint.setShader(new LinearGradient(0, 0, 0, getHeight()
					// / 5, mBorderColor,
					// mBorderAlternativeColor, Shader.TileMode.CLAMP));
					// mPaint.setColor(mBorderColor);
					// mPaint.setStrokeWidth(mStrokeWidth + mStrokeBorderWidth);
					// canvas.drawLine(x, 0, x, getHeight(), mPaint);
					//
					// mPaint.setShader(new LinearGradient(0, 0, 0, getHeight()
					// / 5, mColor, mAlternativeColor,
					// Shader.TileMode.MIRROR));
					// mPaint.setColor(mColor);
					// mPaint.setStrokeWidth(mStrokeWidth);
					// canvas.drawLine(x, 0, x, getHeight(), mPaint);
				}

				mPaint.setColor(mBorderColor);
				mPaint.setAlpha(alpha);
				mPaint.setStrokeWidth(mStrokeWidth + mStrokeBorderWidth);
				canvas.drawLine(x, 0, x, getHeight(), mPaint);

				mPaint.setColor(mColor);
				mPaint.setAlpha(alpha);
				mPaint.setStrokeWidth(mStrokeWidth);
				canvas.drawLine(x, 0, x, getHeight(), mPaint);

			}
		}
		// mPaint.setShader(null);
		mPaint.setAlpha(255);
	}

	private void drawLineBottom(final Canvas canvas) {
		final int x = mColumnWidth * mDepth - mColumnWidth / 2;

		mPaint.setColor(mBorderColor);
		mPaint.setStrokeWidth(mStrokeWidth + mStrokeBorderWidth);
		canvas.drawLine(x, getHeight() / 2, x, getHeight(), mPaint);

		mPaint.setColor(mColor);
		mPaint.setStrokeWidth(mStrokeWidth);
		canvas.drawLine(x, getHeight() / 2, x, getHeight(), mPaint);
	}

	private void drawLineTop(final Canvas canvas) {
		final int x = mColumnWidth * mDepth - mColumnWidth / 2;

		mPaint.setColor(mBorderColor);
		mPaint.setStrokeWidth(mStrokeWidth + mStrokeBorderWidth);
		canvas.drawLine(x, 0, x, getHeight() / 2, mPaint);

		mPaint.setColor(mColor);
		mPaint.setStrokeWidth(mStrokeWidth);
		canvas.drawLine(x, 0, x, getHeight() / 2, mPaint);
	}

	private void drawPath(final Canvas canvas, final Path path) {
		drawPath(canvas, path, mColor);
	}

	private void drawPath(final Canvas canvas, final Path path, final int color) {
		mPaint.setColor(mBorderColor);
		mPaint.setStrokeWidth(mStrokeWidth + mStrokeBorderWidth);
		canvas.drawPath(path, mPaint);

		mPaint.setColor(color);
		mPaint.setStrokeWidth(mStrokeWidth);
		canvas.drawPath(path, mPaint);

		mPaint.setAlpha(255);
	}

	private void drawLeftToRightBottom(final Canvas canvas) {
		final int startX = mColumnWidth * mDepth - mColumnWidth / 2;
		final int stopX = mColumnWidth * mDepth + mColumnWidth / 2;

		final int middleX = stopX - mColumnWidth / 2;
		final int middleY = getHeight() - getHeight() / 2;

		final int startY = getHeight() / 2;
		final int stopY = getHeight();

		final Path path = new Path();
		path.moveTo(startX, startY);
		path.cubicTo(startX, startY, middleX, middleY, stopX, stopY);
		drawPath(canvas, path);
		path.close();
	}

	private void drawRightToLeftBottom(final Canvas canvas) {
		final int color = (mOrientationTop == ORIENTATION_LTR) ? mSecondaryColor : mColor;

		final int startX = mColumnWidth * mDepth - mColumnWidth / 2;
		final int startY = getHeight() / 2;

		final int stopX = mColumnWidth * (mDepth - 1) - mColumnWidth / 2;
		final int stopY = getHeight();

		final int middleX = stopX;
		final int middleY = getHeight() / 2;

		final Path path = new Path();
		path.moveTo(startX, startY);
		path.cubicTo(startX, startY, middleX, middleY, stopX, stopY);
		drawPath(canvas, path, color);
		path.close();
	}

	private void drawLeftToRightTop(final Canvas canvas) {
		final int alpha = (mOrientationBottom == ORIENTATION_RTL) ? mSecondaryColor : mColor;

		final int startX = mColumnWidth * (mDepth - 1) - mColumnWidth / 2;
		final int startY = 1;

		final int stopX = mColumnWidth * mDepth - mColumnWidth / 2;
		final int stopY = getHeight() / 2;

		final int middleX = stopX - mColumnWidth;
		final int middleY = getHeight() / 2;

		final Path path = new Path();
		path.moveTo(startX, startY);
		path.cubicTo(startX, startY, middleX, middleY, stopX, stopY);
		drawPath(canvas, path, alpha);
		path.close();
	}

	private void drawRightToLeftTop(final Canvas canvas) {
		final int startX = mColumnWidth * mDepth + mColumnWidth / 2;
		final int startY = 1;

		final int stopX = mColumnWidth * mDepth - mColumnWidth / 2;
		final int stopY = getHeight() / 2;

		final int middleX = stopX + mColumnWidth / 2;
		final int middleY = getHeight() / 2;

		final Path path = new Path();
		path.moveTo(startX, startY);
		path.cubicTo(startX, startY, middleX, middleY, stopX, stopY);
		drawPath(canvas, path);
		path.close();
	}

	private void drawHeadsignDot(final Canvas canvas) {
		drawDot(canvas, mDotRadiusHeadsign, true);
	}

	private void drawNormalDot(final Canvas canvas) {
		drawDot(canvas, mDotRadius, false);
	}

	private void drawDot(final Canvas canvas, final int radius, final boolean drawPoint) {
		final int x = mColumnWidth * mDepth - mColumnWidth / 2;
		final int y = getHeight() / 2;

		mPaint.setStyle(Paint.Style.FILL);

		mPaint.setColor(mBorderColor);
		canvas.drawCircle(x, y, radius + mDotRadiusBorder, mPaint);

		mPaint.setColor(mPointColor);
		canvas.drawCircle(x, y, radius, mPaint);

		if (drawPoint) {
			mPaint.setColor(mColor);
			canvas.drawCircle(x, y, mDotRadiusHeadsignCenter, mPaint);
			mPaint.setColor(mColor);
		}

		mPaint.setStyle(Paint.Style.STROKE);
	}
}
