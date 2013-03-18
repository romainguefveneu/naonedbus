package net.naonedbus.card;

import net.naonedbus.R;
import net.naonedbus.utils.FontUtils;
import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.app.LoaderManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public abstract class Card {

	private final Context mContext;
	private final LoaderManager mLoaderManager;
	private final int mLayoutId;
	private final int mTitleId;

	private View mProgress;
	private TextView mMessage;
	private ViewGroup mContent;

	private Typeface mRobotoLight;
	private Typeface mRobotoBold;

	public Card(final Context context, final LoaderManager loaderManager, final int titleId, final int layoutId) {
		mContext = context;
		mLoaderManager = loaderManager;
		mTitleId = titleId;
		mLayoutId = layoutId;
	}

	public void onStart() {

	}

	public void onStop() {

	}

	protected Context getContext() {
		return mContext;
	}

	protected LoaderManager getLoaderManager() {
		return mLoaderManager;
	}

	protected String getString(final int resId) {
		return mContext.getString(resId);
	}

	protected String getString(final int resId, final Object... formatArgs) {
		return mContext.getString(resId, formatArgs);
	}

	public View getView(final ViewGroup container) {
		final LayoutInflater inflater = LayoutInflater.from(mContext);

		final ViewGroup base = (ViewGroup) inflater.inflate(R.layout.card_base, container, false);
		final View view = inflater.inflate(mLayoutId, base, false);

		final TextView title = (TextView) base.findViewById(android.R.id.title);
		setTypefaceRobotoLight(title);
		title.setText(mTitleId);

		mProgress = base.findViewById(android.R.id.progress);
		mMessage = (TextView) base.findViewById(android.R.id.message);
		mContent = (ViewGroup) base.findViewById(android.R.id.content);
		mContent.addView(view);

		bindView(container.getContext(), view);

		return base;
	}

	protected void showContent() {
		if (mContent.getVisibility() != View.VISIBLE) {
			mProgress.setVisibility(View.GONE);
			mContent.setVisibility(View.VISIBLE);
			mContent.startAnimation(AnimationUtils.loadAnimation(mContent.getContext(), android.R.anim.fade_in));
		}
	}

	protected void showMessage(final int messageId, final int drawableId) {
		mMessage.setCompoundDrawablesWithIntrinsicBounds(drawableId, 0, 0, 0);
		showMessage(messageId);
	}

	protected void showMessage(final int messageId) {
		if (mMessage.getVisibility() != View.VISIBLE) {
			mProgress.setVisibility(View.GONE);
			mContent.setVisibility(View.GONE);
			mMessage.setText(messageId);
			mMessage.setVisibility(View.VISIBLE);
			mMessage.startAnimation(AnimationUtils.loadAnimation(mContent.getContext(), android.R.anim.fade_in));
		}
	}

	protected void setTypefaceRobotoLight(final TextView textView) {
		if (mRobotoLight == null) {
			mRobotoLight = FontUtils.getRobotoLight(textView.getContext());
		}
		textView.setTypeface(mRobotoLight);
	}

	protected void setTypefaceRobotoBold(final TextView textView) {
		if (mRobotoBold == null) {
			mRobotoBold = FontUtils.getRobotoMedium(textView.getContext());
		}
		textView.setTypeface(mRobotoBold);
	}

	protected abstract void bindView(final Context context, final View view);

}
