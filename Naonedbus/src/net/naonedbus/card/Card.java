package net.naonedbus.card;

import java.util.HashMap;
import java.util.Map;

import net.naonedbus.R;
import net.naonedbus.utils.FontUtils;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public abstract class Card<T> implements LoaderCallbacks<T> {

	private static final Map<Class<?>, Integer> sLoaderIds = new HashMap<Class<?>, Integer>();

	private Context mContext;
	private final LoaderManager mLoaderManager;
	private final int mLayoutId;
	private final int mTitleId;

	private View mProgress;
	private TextView mMessage;
	private ViewGroup mContent;
	private TextView mMoreActionView;

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

	public void onResume() {
	}

	public void onPause() {
	}

	public void onDestroy() {
	}

	protected Context getContext() {
		return mContext;
	}

	public void setContext(final Context context) {
		mContext = context;
	}

	protected Loader<T> initLoader(final Bundle bundle, final LoaderCallbacks<T> callbacks) {
		return mLoaderManager.initLoader(getLoaderId(), bundle, callbacks);
	}

	protected Loader<T> restartLoader(final Bundle bundle, final LoaderCallbacks<T> callbacks) {
		return mLoaderManager.restartLoader(getLoaderId(), bundle, callbacks);
	}

	private synchronized int getLoaderId() {
		if (!sLoaderIds.containsKey(getClass())) {
			sLoaderIds.put(getClass(), Integer.MAX_VALUE - sLoaderIds.size());
		}
		return sLoaderIds.get(getClass());
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
		mMoreActionView = (TextView) base.findViewById(android.R.id.button1);
		mContent = (ViewGroup) base.findViewById(android.R.id.content);
		mContent.addView(view);

		bindView(container.getContext(), view);

		return base;
	}

	protected void showLoader() {
		if (mProgress.getVisibility() != View.VISIBLE) {
			mProgress.setVisibility(View.VISIBLE);
		}
		if (mMessage.getVisibility() != View.GONE) {
			mMessage.startAnimation(AnimationUtils.loadAnimation(mMessage.getContext(), R.anim.card_disable_content));
		}
		if (mContent.getVisibility() != View.GONE) {
			mContent.startAnimation(AnimationUtils.loadAnimation(mContent.getContext(), R.anim.card_disable_content));
		}
	}

	protected void showContent() {
		if (mContent.getVisibility() != View.VISIBLE) {
			mContent.setVisibility(View.VISIBLE);
			mContent.startAnimation(AnimationUtils.loadAnimation(mContent.getContext(), android.R.anim.fade_in));
		} else if (mProgress.getVisibility() == View.VISIBLE) {
			mContent.startAnimation(AnimationUtils.loadAnimation(mContent.getContext(), R.anim.card_enable_content));

		}

		mMessage.clearAnimation();
		mMessage.setVisibility(View.GONE);
		mProgress.setVisibility(View.GONE);

		showMoreAction();
	}

	protected void showMessage(final int messageId, final int drawableId) {
		mMessage.setCompoundDrawablesWithIntrinsicBounds(drawableId, 0, 0, 0);
		showMessage(messageId);
	}

	protected void showMessage(final int messageId) {
		mMessage.setText(messageId);

		if (mMessage.getVisibility() != View.VISIBLE) {
			mMessage.setVisibility(View.VISIBLE);
			mMessage.startAnimation(AnimationUtils.loadAnimation(mMessage.getContext(), android.R.anim.fade_in));
		} else if (mProgress.getVisibility() == View.VISIBLE) {
			mMessage.startAnimation(AnimationUtils.loadAnimation(mMessage.getContext(), R.anim.card_enable_content));
		}

		mProgress.setVisibility(View.GONE);
		mContent.clearAnimation();
		mContent.setVisibility(View.GONE);

		showMoreAction();
	}

	private void showMoreAction() {
		final Intent moreIntent = getMoreIntent();
		if (moreIntent != null) {

			final int title = moreIntent.getIntExtra(Intent.EXTRA_TITLE, 0);
			if (title != 0) {
				mMoreActionView.setText(title);
			}

			final int icon = moreIntent.getIntExtra(Intent.EXTRA_SHORTCUT_ICON, 0);
			if (icon != 0) {
				mMoreActionView.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
			}

			mMoreActionView.setVisibility(View.VISIBLE);
			mMoreActionView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View v) {
					mContext.startActivity(moreIntent);
				}
			});

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

	protected abstract Intent getMoreIntent();

	@Override
	public abstract Loader<T> onCreateLoader(final int idLoader, final Bundle args);

	@Override
	public abstract void onLoadFinished(final Loader<T> loader, final T results);

	@Override
	public void onLoaderReset(final Loader<T> loader) {

	}
}
