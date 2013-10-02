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
package net.naonedbus.activity;

import net.naonedbus.BuildConfig;
import net.naonedbus.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;

public abstract class FragmentsActivity extends SherlockFragmentActivity {

	private static final String LOG_TAG = "FragmentsActivity";
	private static final boolean DBG = BuildConfig.DEBUG;

	private static String BUNDLE_TABS_CURRENT = "tabsCurrent";
	private static String BUNDLE_TABS_TITLES = "tabsTitles";
	private static String BUNDLE_TABS_CLASSES = "tabsClasses";

	/** Layout de l'activitée courante. */
	private final int mLayoutId;

	/** Titres des fragments. */
	private int[] mTitles;
	/** Classes des fragments */
	private String[] mClasses = new String[0];
	/** Fragments tags. */
	private String[] mFragmentsTags = new String[0];

	private PagerSlidingTabStrip mTabs;
	private ViewPager mViewPager;
	private TabsAdapter mSectionsPagerAdapter;

	public FragmentsActivity(final int layoutId) {
		this.mLayoutId = layoutId;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(mLayoutId);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mSectionsPagerAdapter = new TabsAdapter(getSupportFragmentManager());

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		mTabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		mTabs.setViewPager(mViewPager);
	}

	/**
	 * Gérer les click sur les menus.
	 */
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		outState.putInt(BUNDLE_TABS_CURRENT, mViewPager.getCurrentItem());
		outState.putIntArray(BUNDLE_TABS_TITLES, mTitles);
		outState.putStringArray(BUNDLE_TABS_CLASSES, mClasses);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(final Bundle savedInstanceState) {
		if (savedInstanceState.containsKey(BUNDLE_TABS_TITLES)) {
			final int[] titles = savedInstanceState.getIntArray(BUNDLE_TABS_TITLES);
			final String[] classes = savedInstanceState.getStringArray(BUNDLE_TABS_CLASSES);
			final int selectedPosition = savedInstanceState.getInt(BUNDLE_TABS_CURRENT);

			addFragments(titles, classes);
			mViewPager.setCurrentItem(selectedPosition);
		}
		super.onRestoreInstanceState(savedInstanceState);
	}

	/**
	 * Ajouter les informations de fragments.
	 * 
	 * @param titles
	 *            Les titres (ressources).
	 * @param classes
	 *            Les classes des fragments.
	 */
	protected void addFragments(final int[] titles, final Class<?>[] classes) {
		mClasses = new String[classes.length];
		for (int i = 0; i < classes.length; i++) {
			mClasses[i] = classes[i].getName();
		}
		addFragments(titles, mClasses);
	}

	protected void addFragments(final int[] titles, final String[] classes) {
		if (DBG)
			Log.d(LOG_TAG, "addFragments " + titles.length);

		// clearFragments();

		mClasses = classes;
		mTitles = titles;
		mFragmentsTags = new String[classes.length];

		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOffscreenPageLimit(classes.length);
		mTabs.notifyDataSetChanged();
	}

	private void clearFragments() {
		final FragmentManager fragmentManager = getSupportFragmentManager();
		final FragmentTransaction transaction = fragmentManager.beginTransaction();
		for (final String tag : mFragmentsTags) {
			final Fragment fragment = fragmentManager.findFragmentByTag(tag);
			if (fragment != null) {
				transaction.remove(fragment);
			}
		}
		transaction.commit();
	}

	public class TabsAdapter extends FragmentPagerAdapter {
		private static final String LOG_TAG = FragmentsActivity.LOG_TAG + "$TabsAdapter";

		public TabsAdapter(final FragmentManager fm) {
			super(fm);
		}

		@Override
		public Object instantiateItem(final ViewGroup container, final int position) {
			if (DBG)
				Log.d(LOG_TAG, "instantiateItem " + position);

			final Fragment fragment = (Fragment) super.instantiateItem(container, position);
			// fragment.setRetainInstance(true);
			mFragmentsTags[position] = fragment.getTag();
			return fragment;
		}

		@Override
		public Fragment getItem(final int position) {
			if (DBG)
				Log.d(LOG_TAG, "getItem " + position + " : " + mClasses[position]);

			final Fragment fragment = Fragment.instantiate(FragmentsActivity.this, mClasses[position]);
			// fragment.setRetainInstance(true);
			return fragment;
		}

		@Override
		public int getCount() {
			return mClasses.length;
		}

		@Override
		public CharSequence getPageTitle(final int position) {
			if (DBG)
				Log.d(LOG_TAG, "getPageTitle " + position);

			return getString(mTitles[position]);
		}
	}
}
