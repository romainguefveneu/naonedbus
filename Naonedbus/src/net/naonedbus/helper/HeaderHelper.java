package net.naonedbus.helper;

import net.naonedbus.R;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.utils.FontUtils;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

public class HeaderHelper {

	private Activity mActivity;

	private View mHeader;
	private TextView mTitle;
	private TextView mSubTitle;
	private TextView mLigneCode;

	public HeaderHelper(Activity activity) {
		mActivity = activity;
		init();
	}

	private void init() {
		final View view = mActivity.getWindow().getDecorView();
		final Typeface robotoLight = FontUtils.getRobotoLight(mActivity);
		final Typeface robotoMedium = FontUtils.getRobotoMedium(mActivity);

		mHeader = view.findViewById(R.id.headerView);

		mTitle = (TextView) view.findViewById(R.id.headerTitle);
		mSubTitle = (TextView) view.findViewById(R.id.headerSubTitle);
		mLigneCode = (TextView) view.findViewById(R.id.headerCode);

		mTitle.setTypeface(robotoLight);
		mSubTitle.setTypeface(robotoLight);
		mLigneCode.setTypeface(robotoMedium);
	}

	public void setBackgroundColor(int color) {
		final int textColor = ColorUtils.isLightColor(color) ? Color.BLACK : Color.WHITE;
		setBackgroundColor(color, textColor);
	}

	public void setBackgroundColor(int color, int textColor) {
		mHeader.setBackgroundDrawable(ColorUtils.getGradiant(color));

		mTitle.setTextColor(textColor);
		mSubTitle.setTextColor(textColor);
		mLigneCode.setTextColor(textColor);
	}

	public void setCode(CharSequence code) {
		mLigneCode.setText(code);
		if (code == null || code.length() == 0) {
			mLigneCode.setVisibility(View.GONE);
		} else {
			mLigneCode.setVisibility(View.VISIBLE);
		}
	}

	public void setTitle(CharSequence title) {
		mTitle.setText(title);
	}

	public void setTitleIcon(int iconResId) {
		mTitle.setCompoundDrawablesWithIntrinsicBounds(iconResId, 0, 0, 0);
	}

	public void setSubTitle(CharSequence subtitle) {
		mSubTitle.setText(subtitle);
		if (subtitle == null || subtitle.length() == 0) {
			mSubTitle.setVisibility(View.GONE);
		} else {
			mSubTitle.setVisibility(View.VISIBLE);
		}
	}

}
