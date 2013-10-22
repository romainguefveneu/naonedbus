/**
 * Copyright (C) 2013 Romain Guefveneu.
 *   
 *  This file is part of naonedbus.
 *   
 *  Naonedbus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  Naonedbus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.naonedbus.helper;

import net.naonedbus.R;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.utils.FontUtils;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class HeaderHelper {

	private final Context mContext;

	private View mHeader;
	private TextView mTitle;
	private TextView mSubTitle;

	public HeaderHelper(final Activity activity) {
		mContext = activity;
		init(activity.getWindow().getDecorView());
	}

	public HeaderHelper(final Context context, final View view) {
		mContext = context;
		init(view);
	}

	private void init(final View view) {
		final Typeface robotoBold = FontUtils.getRobotoBoldCondensed(mContext);

		mHeader = view.findViewById(R.id.headerView);

		mTitle = (TextView) view.findViewById(R.id.headerTitle);
		mSubTitle = (TextView) view.findViewById(R.id.headerSubTitle);

		mTitle.setTypeface(robotoBold);
		mSubTitle.setTypeface(robotoBold);
	}

	public void setColor(final int backColor, final int textColor) {
		ColorUtils.setBackgroundGradiant(mHeader, backColor);

		mTitle.setTextColor(textColor);
		mSubTitle.setTextColor(textColor);
	}

	public void setTitle(final CharSequence title) {
		mTitle.setText(title);
	}

	public void setTitle(final int title) {
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
		final Animation fadeOut = AnimationUtils.loadAnimation(mContext, android.R.anim.fade_out);
		final Animation fadeIn = AnimationUtils.loadAnimation(mContext, android.R.anim.fade_in);

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
