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
package net.naonedbus.activity.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.naonedbus.R;
import net.naonedbus.utils.FontUtils;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.viewpagerindicator.CirclePageIndicator;

public class TutorialActivity extends Activity implements OnPageChangeListener {

	private TutorialPagerAdapter mTutorialPagerAdapter;

	private View mNextButton;
	private ViewPager mViewPager;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tutorial);

		mTutorialPagerAdapter = new TutorialPagerAdapter(this);
		mTutorialPagerAdapter.addView(new TutorialView(R.layout.tutorial_view_welcome, R.string.tuto_0_title,
				R.string.tuto_0_summary, R.drawable.logo));
		mTutorialPagerAdapter.addView(new TutorialView(R.layout.tutorial_view_simple, R.string.tuto_about_title,
				R.string.tuto_about_summary, 0));

		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB) {
			mTutorialPagerAdapter.addView(new TutorialView(R.string.tuto_menu_title, R.string.tuto_menu_summary,
					R.drawable.tuto_menu));
		}
		mTutorialPagerAdapter.addView(new TutorialView(R.string.tuto_favoris_title, R.string.tuto_favoris_summary,
				R.drawable.tuto_favoris));
		mTutorialPagerAdapter.addView(new TutorialView(R.string.tuto_commentaires_title,
				R.string.tuto_commentaires_summary, R.drawable.tuto_infotrafic));
		mTutorialPagerAdapter.addView(new TutorialView(R.string.tuto_parkings_title, R.string.tuto_parkings_summary,
				R.drawable.tuto_parkings));
		mTutorialPagerAdapter.addView(new TutorialView(R.string.tuto_mobilite_title, R.string.tuto_mobilite_summary,
				R.drawable.tuto_mobilite));
		mTutorialPagerAdapter.addView(new TutorialView(R.string.tuto_widgets_title, R.string.tuto_widgets_summary,
				R.drawable.tuto_widgets));

		mViewPager = (ViewPager) findViewById(R.id.viewPager);
		mViewPager.setAdapter(mTutorialPagerAdapter);

		final CirclePageIndicator circlePageIndicator = (CirclePageIndicator) findViewById(R.id.viewPagerIndicator);
		circlePageIndicator.setViewPager(mViewPager);
		circlePageIndicator.setOnPageChangeListener(this);

		final View closeButton = findViewById(android.R.id.closeButton);
		closeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				finish();
			}
		});

		mNextButton = findViewById(R.id.nextButton);
		mNextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				moveToNext();
			}
		});
	}

	private void moveToNext() {
		mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
	}

	@Override
	public void onPageScrollStateChanged(final int position) {

	}

	@Override
	public void onPageScrolled(final int from, final float arg1, final int to) {
	}

	@Override
	public void onPageSelected(final int position) {
		mNextButton.setEnabled(position < mTutorialPagerAdapter.getCount() - 1);
	}

	private static class TutorialPagerAdapter extends PagerAdapter {

		private final Context mContext;
		private final List<TutorialView> mTutorialViews;
		private final LayoutInflater mLayoutInflater;
		private final Typeface mRoboto;

		public TutorialPagerAdapter(final Context context) {
			mContext = context;
			mTutorialViews = new ArrayList<TutorialActivity.TutorialView>();
			mLayoutInflater = LayoutInflater.from(context);
			mRoboto = FontUtils.getRobotoLight(context);
		}

		public void addView(final TutorialView view) {
			mTutorialViews.add(view);
		}

		@Override
		public int getCount() {
			return mTutorialViews.size();
		}

		@Override
		public Object instantiateItem(final View collection, final int position) {
			final TutorialView tutorialView = mTutorialViews.get(position);
			final View layout = mLayoutInflater.inflate(tutorialView.layoutId, null);

			final TextView title = (TextView) layout.findViewById(android.R.id.title);
			title.setText(mContext.getString(tutorialView.titleResId).toUpperCase(Locale.getDefault()));
			title.setTypeface(mRoboto);

			final TextView summary = (TextView) layout.findViewById(android.R.id.summary);
			summary.setText(Html.fromHtml(mContext.getString(tutorialView.summaryResId).toString()));

			if (tutorialView.imageResId != 0) {
				final ImageView icon = (ImageView) layout.findViewById(android.R.id.icon);
				icon.setImageResource(tutorialView.imageResId);
			}

			((ViewPager) collection).addView(layout);

			return layout;
		}

		@Override
		public void destroyItem(final View collection, final int position, final Object view) {
			((ViewPager) collection).removeView((View) view);
		}

		@Override
		public boolean isViewFromObject(final View view, final Object object) {
			return view == object;
		}

	}

	private static class TutorialView {
		private int layoutId = R.layout.tutorial_view;
		private final int titleResId;
		private final int summaryResId;
		private final int imageResId;

		private TutorialView(final int titleResId, final int summaryResId, final int imageResId) {
			this.titleResId = titleResId;
			this.summaryResId = summaryResId;
			this.imageResId = imageResId;
		}

		private TutorialView(final int layoutId, final int titleResId, final int summaryResId, final int imageResId) {
			this.layoutId = layoutId;
			this.titleResId = titleResId;
			this.summaryResId = summaryResId;
			this.imageResId = imageResId;
		}

	}

}
