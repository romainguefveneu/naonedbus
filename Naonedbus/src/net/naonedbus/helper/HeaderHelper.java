package net.naonedbus.helper;

import net.naonedbus.R;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.utils.FontUtils;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class HeaderHelper {

	private final Activity mActivity;

	private View mHeader;
	private TextView mTitle;
	private TextView mSubTitle;
	private TextView mLigneCode;

	public HeaderHelper(final Activity activity) {
		mActivity = activity;
		init();
	}

	private void init() {
		final View view = mActivity.getWindow().getDecorView();
		final Typeface robotoMedium = FontUtils.getRobotoMedium(mActivity);
		final Typeface robotoBold = FontUtils.getRobotoBoldCondensed(mActivity);

		mHeader = view.findViewById(R.id.headerView);

		mTitle = (TextView) view.findViewById(R.id.headerTitle);
		mSubTitle = (TextView) view.findViewById(R.id.headerSubTitle);
		mLigneCode = (TextView) view.findViewById(R.id.headerCode);

		mTitle.setTypeface(robotoBold);
		mSubTitle.setTypeface(robotoBold);
		mLigneCode.setTypeface(robotoBold);
	}

	public void setBackgroundColor(final int color) {
		final int textColor = ColorUtils.isLightColor(color) ? Color.BLACK : Color.WHITE;
		setBackgroundColor(color, textColor);
	}

	public void setBackgroundColor(final int color, final int textColor) {
		mHeader.setBackgroundDrawable(ColorUtils.getGradiant(color));

		mTitle.setTextColor(textColor);
		mSubTitle.setTextColor(textColor);
		mLigneCode.setTextColor(textColor);
	}

	public void setCode(final CharSequence code) {
		mLigneCode.setText(code);
		if (code == null || code.length() == 0) {
			mLigneCode.setVisibility(View.GONE);
		} else {
			mLigneCode.setVisibility(View.VISIBLE);
		}
	}

	public void setTitle(final CharSequence title) {
		mTitle.setText(title);
	}

	public void setTitleIcon(final int iconResId) {
		mTitle.setCompoundDrawablesWithIntrinsicBounds(iconResId, 0, 0, 0);
	}

	public void setSubTitle(final CharSequence subtitle) {
		mSubTitle.setText(subtitle);
		if (subtitle == null || subtitle.length() == 0) {
			mSubTitle.setVisibility(View.GONE);
		} else {
			mSubTitle.setVisibility(View.VISIBLE);
		}
	}

	public void setSubTitleAnimated(final CharSequence subtitle) {
		final Animation fadeOut = AnimationUtils.loadAnimation(mActivity, android.R.anim.fade_out);
		final Animation fadeIn = AnimationUtils.loadAnimation(mActivity, android.R.anim.fade_in);

		fadeOut.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(final Animation animation) {

			}

			@Override
			public void onAnimationRepeat(final Animation animation) {

			}

			@Override
			public void onAnimationEnd(final Animation animation) {
				setSubTitle(subtitle);
				mSubTitle.startAnimation(fadeIn);
			}
		});

		mSubTitle.startAnimation(fadeOut);
	}
}
