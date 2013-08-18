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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.PageIndicator;

public class TutorialActivity extends SherlockActivity implements OnPageChangeListener {

	private TutorialPagerAdapter mTutorialPagerAdapter;

	private MenuItem mSkipMenuItem;
	private Button mNextButton;
	private Button mPreviousButton;
	private ViewPager mViewPager;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tutorial);

		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayUseLogoEnabled(true);
		actionBar.setLogo(R.drawable.ic_logo);

		mTutorialPagerAdapter = new TutorialPagerAdapter(this);
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

		final PageIndicator pageIndicator = (PageIndicator) findViewById(R.id.viewPagerIndicator);
		pageIndicator.setViewPager(mViewPager);
		pageIndicator.setOnPageChangeListener(this);

		mNextButton = (Button) findViewById(R.id.tutorialNext);
		mNextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				if (mViewPager.getCurrentItem() == mTutorialPagerAdapter.getCount() - 1) {
					finish();
				} else {
					moveToNext();
				}
			}
		});
		mPreviousButton = (Button) findViewById(R.id.tutorialPrevious);
		mPreviousButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				moveToPrevious();
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_tutorial, menu);
		mSkipMenuItem = menu.findItem(R.id.menu_skip);
		mSkipMenuItem.setVisible(mViewPager.getCurrentItem() < mTutorialPagerAdapter.getCount() - 1);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (item.getItemId() == R.id.menu_skip) {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	private void moveToPrevious() {
		mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1, true);
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
		if (position == mTutorialPagerAdapter.getCount() - 1) {
			mNextButton.setText(android.R.string.ok);
			if (mSkipMenuItem != null)
				mSkipMenuItem.setVisible(false);
		} else {
			mNextButton.setText(R.string.next);
			if (mSkipMenuItem != null)
				mSkipMenuItem.setVisible(true);
		}
		mPreviousButton.setVisibility(position > 0 && position < mTutorialPagerAdapter.getCount() - 1 ? View.VISIBLE
				: View.GONE);
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
