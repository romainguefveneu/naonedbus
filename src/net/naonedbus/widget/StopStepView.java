package net.naonedbus.widget;

import net.naonedbus.utils.ColorUtils;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

public class StopStepView extends View {

	private static final int COLUMN_WIDTH = 42;

	private static final int DOT_RADIUS = 6;
	private static final int DOT_RADIUS_HEADSIGN = 10;
	private static final int DOT_RADIUS_HEADSIGN_CENTER = 4;
	private static final int DOT_RADIUS_BORDER = 1;
	private static final float STROKE_WIDTH = 6f;
	private static final float STROKE_BORDER_WIDTH = 2f;

	private Paint mPaint;
	private int mColor;
	private int mBorderColor;
	private int mPointColor;

	/**
	 * 1 = First 0 = Middle 2 = Last
	 */
	private int mStyle = 1;
	private int mDepth = 2;
	/**
	 * 1 : / 2 : \ 3 : | 0 : vide
	 */
	private int mOrientationTop = 0;
	private int mOrientationBottom = 0;
	private boolean mShowOtherLines;

	private int mColumnWidth;
	private int mDotRadius;
	private int mDotRadiusHeadsign;
	private int mDotRadiusHeadsignCenter;
	private int mDotRadiusBorder;
	private float mStrokeWidth;
	private float mStrokeBorderWidth;

	public StopStepView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public StopStepView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public StopStepView(Context context) {
		super(context);
		init();
	}

	private void init() {
		DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
		mColumnWidth = Math.round(COLUMN_WIDTH * metrics.density);
		mDotRadius = Math.round(DOT_RADIUS * metrics.density);
		mDotRadiusBorder = Math.round(DOT_RADIUS_BORDER * metrics.density);
		mDotRadiusHeadsign = Math.round(DOT_RADIUS_HEADSIGN * metrics.density);
		mDotRadiusHeadsignCenter = Math.round(DOT_RADIUS_HEADSIGN_CENTER * metrics.density);
		mStrokeWidth = STROKE_WIDTH * metrics.density;
		mStrokeBorderWidth = STROKE_BORDER_WIDTH * metrics.density;

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeWidth(mStrokeWidth);

		setColor(Color.rgb(0, 121, 69));
	}

	public void setColor(int color) {
		mColor = color;
		mBorderColor = ColorUtils.getDarkerColor(color);
		mPointColor = ColorUtils.getLighterColor(color);
		mPaint.setColor(mColor);
	}

	public void setStyle(int style) {
		mStyle = style;
	}

	public void setDepth(int depth) {
		this.mDepth = depth + 1;
	}

	public void setOrientationTop(int orientation) {
		this.mOrientationTop = orientation;
	}

	public void setOrientationBottom(int orientation) {
		this.mOrientationBottom = orientation;
	}

	public void setShowOtherLines(boolean showOtherLines) {
		mShowOtherLines = showOtherLines;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int desiredWidth = mColumnWidth * mDepth;
		int desiredHeight = mColumnWidth;

		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		int width;
		int height;

		// Measure Width
		if (widthMode == MeasureSpec.EXACTLY) {
			// Must be this size
			width = widthSize;
		} else if (widthMode == MeasureSpec.AT_MOST) {
			// Can't be bigger than...
			width = Math.min(desiredWidth, widthSize);
		} else {
			// Be whatever you want
			width = desiredWidth;
		}

		// Measure Height
		if (heightMode == MeasureSpec.EXACTLY || heightMode == MeasureSpec.AT_MOST) {
			// Must be this size
			height = heightSize;
		} else {
			// Be whatever you want
			height = desiredHeight;
		}

		setMeasuredDimension(width, height);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (mShowOtherLines)
			drawMainLines(canvas);

		if (mOrientationBottom == 2)
			drawLeftToRightBottom(canvas);
		else if (mOrientationBottom == 1)
			drawRightToLeftBottom(canvas);
		else if (mOrientationBottom == 0)
			drawLineBottom(canvas);

		if (mOrientationTop == 2)
			drawLeftToRightTop(canvas);
		else if (mOrientationTop == 1)
			drawRightToLeftTop(canvas);
		else if (mOrientationTop == 0)
			drawLineTop(canvas);

		if (mStyle == 0)
			drawNormalDot(canvas);
		else
			drawHeadsignDot(canvas);
	}

	private void drawMainLines(Canvas canvas) {
		for (int i = 0; i < mDepth; i++) {
			int x = i * mColumnWidth - mColumnWidth / 2;
			mPaint.setColor(mBorderColor);
			mPaint.setStrokeWidth(mStrokeWidth + mStrokeBorderWidth);
			canvas.drawLine(x, 0, x, getHeight(), mPaint);

			mPaint.setColor(mColor);
			mPaint.setStrokeWidth(mStrokeWidth);
			canvas.drawLine(x, 0, x, getHeight(), mPaint);
		}
	}

	private void drawLineBottom(Canvas canvas) {
		int x = mColumnWidth * mDepth - mColumnWidth / 2;

		mPaint.setColor(mBorderColor);
		mPaint.setStrokeWidth(mStrokeWidth + mStrokeBorderWidth);
		canvas.drawLine(x, getHeight() / 2, x, getHeight(), mPaint);

		mPaint.setColor(mColor);
		mPaint.setStrokeWidth(mStrokeWidth);
		canvas.drawLine(x, getHeight() / 2, x, getHeight(), mPaint);
	}

	private void drawLineTop(Canvas canvas) {
		int x = mColumnWidth * mDepth - mColumnWidth / 2;

		mPaint.setColor(mBorderColor);
		mPaint.setStrokeWidth(mStrokeWidth + mStrokeBorderWidth);
		canvas.drawLine(x, 0, x, getHeight() / 2, mPaint);

		mPaint.setColor(mColor);
		mPaint.setStrokeWidth(mStrokeWidth);
		canvas.drawLine(x, 0, x, getHeight() / 2, mPaint);
	}

	private void drawPath(Canvas canvas, Path path) {
		mPaint.setColor(mBorderColor);
		mPaint.setStrokeWidth(mStrokeWidth + mStrokeBorderWidth);
		canvas.drawPath(path, mPaint);

		mPaint.setColor(mColor);
		mPaint.setStrokeWidth(mStrokeWidth);
		canvas.drawPath(path, mPaint);
	}

	private void drawLeftToRightBottom(Canvas canvas) {
		int startX = mColumnWidth * mDepth - mColumnWidth / 2;
		int stopX = mColumnWidth * mDepth + mColumnWidth / 2;

		int middleX = stopX - mColumnWidth / 2;
		int middleY = getHeight() - getHeight() / 2;

		int startY = getHeight() / 2;
		int stopY = getHeight();

		Path path = new Path();
		path.moveTo(startX, startY);
		path.cubicTo(startX, startY, middleX, middleY, stopX, stopY);
		drawPath(canvas, path);
		path.close();
	}

	private void drawRightToLeftBottom(Canvas canvas) {
		int startX = mColumnWidth * mDepth - mColumnWidth / 2;
		int startY = getHeight() / 2;

		int stopX = mColumnWidth * (mDepth - 1) - mColumnWidth / 2;
		int stopY = getHeight();

		int middleX = stopX;
		int middleY = getHeight() / 2;

		Path path = new Path();
		path.moveTo(startX, startY);
		path.cubicTo(startX, startY, middleX, middleY, stopX, stopY);
		drawPath(canvas, path);
		path.close();
	}

	private void drawLeftToRightTop(Canvas canvas) {
		int startX = mColumnWidth * (mDepth - 1) - mColumnWidth / 2;
		int startY = 1;

		int stopX = mColumnWidth * mDepth - mColumnWidth / 2;
		int stopY = getHeight() / 2;

		int middleX = stopX - mColumnWidth;
		int middleY = getHeight() / 2;

		Path path = new Path();
		path.moveTo(startX, startY);
		path.cubicTo(startX, startY, middleX, middleY, stopX, stopY);
		drawPath(canvas, path);
		path.close();
	}

	private void drawRightToLeftTop(Canvas canvas) {
		int startX = mColumnWidth * mDepth + mColumnWidth / 2;
		int startY = 1;

		int stopX = mColumnWidth * mDepth - mColumnWidth / 2;
		int stopY = getHeight() / 2;

		int middleX = stopX + mColumnWidth / 2;
		int middleY = getHeight() / 2;

		Path path = new Path();
		path.moveTo(startX, startY);
		path.cubicTo(startX, startY, middleX, middleY, stopX, stopY);
		drawPath(canvas, path);
		path.close();
	}

	private void drawHeadsignDot(Canvas canvas) {
		drawDot(canvas, mDotRadiusHeadsign, true);
	}

	private void drawNormalDot(Canvas canvas) {
		drawDot(canvas, mDotRadius, false);
	}

	private void drawDot(Canvas canvas, int radius, boolean drawPoint) {
		int x = mColumnWidth * mDepth - mColumnWidth / 2;
		int y = getHeight() / 2;

		mPaint.setStyle(Paint.Style.FILL);

		mPaint.setColor(mBorderColor);
		canvas.drawCircle(x, y, radius + mDotRadiusBorder, mPaint);

		mPaint.setColor(mColor);
		canvas.drawCircle(x, y, radius, mPaint);

		if (drawPoint) {
			mPaint.setColor(mPointColor);
			canvas.drawCircle(x, y, mDotRadiusHeadsignCenter, mPaint);
			mPaint.setColor(mColor);
		}

		mPaint.setStyle(Paint.Style.STROKE);
	}
}
