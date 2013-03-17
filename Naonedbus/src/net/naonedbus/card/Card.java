package net.naonedbus.card;

import net.naonedbus.R;
import net.naonedbus.utils.FontUtils;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public abstract class Card {

	private final int mLayoutId;
	private final String mTitleString;
	private final int mTitleId;

	private View mProgress;
	private ViewGroup mContent;

	private Typeface mRobotoLight;
	private Typeface mRobotoBold;

	public Card(final String title, final int layoutId) {
		mTitleString = title;
		mLayoutId = layoutId;
		mTitleId = -1;
	}

	public Card(final int title, final int layoutId) {
		mTitleId = title;
		mLayoutId = layoutId;
		mTitleString = null;
	}

	public View getView(final Context context, final ViewGroup root) {
		final LayoutInflater inflater = LayoutInflater.from(context);

		final ViewGroup base = (ViewGroup) inflater.inflate(R.layout.card_base, root, false);
		final View view = inflater.inflate(mLayoutId, base, false);

		final TextView title = (TextView) base.findViewById(android.R.id.title);
		setTypefaceRobotoLight(title);
		if (mTitleString == null) {
			title.setText(mTitleId);
		} else {
			title.setText(mTitleString);
		}

		mProgress = base.findViewById(android.R.id.progress);
		mContent = (ViewGroup) base.findViewById(android.R.id.content);
		mContent.addView(view);

		bindView(context, view);

		return base;
	}

	protected void showContent() {
		if (mContent.getVisibility() != View.VISIBLE) {
			mProgress.setVisibility(View.GONE);
			mContent.setVisibility(View.VISIBLE);
			mContent.startAnimation(AnimationUtils.loadAnimation(mContent.getContext(), android.R.anim.fade_in));
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
