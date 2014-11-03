package net.naonedbus.widget;

import net.naonedbus.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.MeasureSpec;

public class ContactView extends View {

	private static final float TEXT_PADDING = 8f;
	private static final float SUBTITLE_PADDING = 2f;

	private Typeface mTypeface;
	private TextPaint mTitlePaint;
	private TextPaint mSubtitlePaint;
	private Paint mAvatarPaint;

	private String mTitle;
	private String mSubtitle;
	private String mUrl;
	private Drawable mAvatarDrawable;
	private Bitmap mAvatarBitmap;

	private int mTextPadding;
	private int mSubtitlePadding;
	private int mAvatarSize;

	private final RectF mTitleBounds = new RectF();
	private final RectF mSubtitleBounds = new RectF();
	private final Rect mAvatarBounds = new Rect();

	public ContactView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

	public ContactView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ContactView(final Context context) {
		this(context, null);
	}

	private void init(final Context context, final AttributeSet attrs, final int defStyle) {
		final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		mTextPadding = Math.round(TEXT_PADDING * metrics.density);
		mSubtitlePadding = Math.round(SUBTITLE_PADDING * metrics.density);

		mTitlePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		if (mTypeface != null)
			mTitlePaint.setTypeface(mTypeface);
		mSubtitlePaint = new TextPaint(mTitlePaint);

		mAvatarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

		final TypedArray values = context.obtainStyledAttributes(attrs, R.styleable.ContactView, defStyle, 0);
		final int titleStyle = values.getResourceId(R.styleable.ContactView_titleTextStyle, 0);
		final int subtitleStyle = values.getResourceId(R.styleable.ContactView_subtitleTextStyle, 0);

		final int[] textSizeAttr = new int[] { android.R.attr.textSize, android.R.attr.textColor };
		TypedArray a = context.obtainStyledAttributes(attrs, textSizeAttr, 0, titleStyle);
		final int titleTextSize = a.getDimensionPixelSize(0, -1);
		final int titleTextColor = a.getColor(1, -1);
		a.recycle();

		a = context.obtainStyledAttributes(attrs, textSizeAttr, 0, subtitleStyle);
		final int subtitleTextSize = a.getDimensionPixelSize(0, -1);
		final int subtitleTextColor = a.getColor(1, -1);
		a.recycle();

		mTitlePaint.setTextSize(titleTextSize);
		mSubtitlePaint.setTextSize(subtitleTextSize);

		mTitlePaint.setColor(titleTextColor);
		mSubtitlePaint.setColor(subtitleTextColor);

		mTitle = values.getString(R.styleable.ContactView_android_title);
		mSubtitle = values.getString(R.styleable.ContactView_subtitle);
		mAvatarSize = values.getDimensionPixelSize(R.styleable.ContactView_avatarSize, 0);
		mUrl = values.getString(R.styleable.ContactView_url);

		mAvatarBounds.set(0, 0, mAvatarSize, mAvatarSize);
		mAvatarBounds.offset(getPaddingLeft(), getPaddingTop());

		getTextBound(mTitle, mTitlePaint, mTitleBounds);
		getTextBound(mSubtitle, mSubtitlePaint, mSubtitleBounds);

		setAvatarDrawable(values.getDrawable(R.styleable.ContactView_android_icon));

		values.recycle();
	}

	public void setTitle(final String title) {
		mTitle = title;
		getTextBound(title, mTitlePaint, mTitleBounds);

		requestLayout();
	}

	public void setSubtitle(final String subtitle) {
		mSubtitle = subtitle;
		getTextBound(subtitle, mSubtitlePaint, mSubtitleBounds);

		requestLayout();
	}

	public void setAvatarDrawable(final Drawable avatarDrawable) {
		mAvatarDrawable = avatarDrawable;
		mAvatarBitmap = drawableToBitmap(mAvatarDrawable);
		requestLayout();
	}

	public String getUrl() {
		return mUrl;
	}

	private void getTextBound(final String text, final Paint paint, final RectF out) {
		out.setEmpty();
		if (text != null) {
			out.right = paint.measureText(text);
			out.bottom = paint.descent() - paint.ascent();
		}
	}

	@Override
	protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		final int textY = (int) ((h - (mTitleBounds.height() + mSubtitleBounds.height())) / 2);

		mAvatarBounds.offsetTo(getPaddingLeft(), getPaddingTop());
		mTitleBounds.offsetTo(mAvatarBounds.right + mTextPadding * 2f, textY);
		mSubtitleBounds.offsetTo(mAvatarBounds.right + mTextPadding * 2f, mTitleBounds.bottom + mSubtitlePadding);
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int height;
		final int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

		if (heightMode == MeasureSpec.AT_MOST) {
			height = heightSize;
		} else {
			final int textHeight = Math.round(mTitleBounds.height() + mSubtitleBounds.height() + mSubtitlePadding);
			final int avatarHeight = mAvatarBounds.height();
			final int desiredHeight = Math.max(textHeight, avatarHeight) + getPaddingTop() + getPaddingBottom();
			height = desiredHeight;
		}

		final int widthSize = MeasureSpec.getSize(widthMeasureSpec);

		setMeasuredDimension(widthSize, height);
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		super.onDraw(canvas);

		drawBitmap(mAvatarBitmap, mAvatarBounds, canvas);
		drawText(mTitle, mTitleBounds, canvas, mTitlePaint, Alignment.ALIGN_NORMAL);
		drawText(mSubtitle, mSubtitleBounds, canvas, mSubtitlePaint, Alignment.ALIGN_NORMAL);
	}

	private void drawText(final String text, final RectF bounds, final Canvas canvas, final TextPaint textPaint,
			final Layout.Alignment alignment) {
		if (text == null || bounds.width() <= 0)
			return;

		final CharSequence ellipsizedText = TextUtils.ellipsize(text, textPaint, bounds.width(),
				TextUtils.TruncateAt.END);

		final StaticLayout sl = new StaticLayout(ellipsizedText, textPaint, Math.round(bounds.width()), alignment, 1f,
				0f, false);

		canvas.save();
		canvas.translate(bounds.left, bounds.centerY() + textPaint.ascent() + textPaint.descent());
		sl.draw(canvas);
		canvas.restore();
	}

	private void drawBitmap(final Bitmap bitmap, final Rect bounds, final Canvas canvas) {
		final BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
		mAvatarPaint.setShader(shader);
		mAvatarPaint.setAntiAlias(true);
		mAvatarPaint.setDither(true);

		canvas.save();
		canvas.translate(bounds.left, bounds.top);
		canvas.drawCircle(mAvatarSize / 2, mAvatarSize / 2, bounds.width() / 2, mAvatarPaint);
		canvas.restore();
	}

	public Bitmap drawableToBitmap(final Drawable drawable) {
		if (drawable == null) {
			return null;
		} else if (drawable instanceof BitmapDrawable) {
			final Bitmap result = ((BitmapDrawable) drawable).getBitmap();
			return Bitmap.createScaledBitmap(result, mAvatarSize, mAvatarSize, true);
		}

		final Bitmap bitmap = Bitmap.createBitmap(mAvatarSize, mAvatarSize, Bitmap.Config.ARGB_8888);
		final Canvas canvas = new Canvas(bitmap);
		canvas.drawBitmap(bitmap, 0, 0, mAvatarPaint);

		return bitmap;
	}

}
