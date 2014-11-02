package net.naonedbus.fragment;

import net.naonedbus.BuildConfig;
import net.naonedbus.R;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.astuetz.PagerSlidingTabStrip;

public abstract class ViewPagerFragment extends SherlockFragment implements TabListener {

	private static final String LOG_TAG = "ViewPagerFragment";
	private static final boolean DBG = BuildConfig.DEBUG;

	// private static String BUNDLE_TABS_CURRENT = "tabsCurrent";
	// private static String BUNDLE_TABS_TITLES = "tabsTitles";
	// private static String BUNDLE_TABS_CLASSES = "tabsClasses";

	/** Titres des fragments. */
	private int[] mTitles;
	/** Classes des fragments */
	private String[] mClasses;
	/** Fragments tags. */
	private String[] mFragmentsTags;

	private PagerSlidingTabStrip mTabs;
	private ViewPager mViewPager;
	private SectionsPagerAdapter mSectionsPagerAdapter;

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_viewpager, container, false);

		mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());

		mViewPager = (ViewPager) view.findViewById(R.id.pager);
		mViewPager.setOffscreenPageLimit(mClasses.length);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(final int position) {
				getSupportActionBar().setSelectedNavigationItem(position);
			}
		});

		mTabs = (PagerSlidingTabStrip) view.findViewById(R.id.tabs);
		mTabs.setViewPager(mViewPager);

		return view;
	}

	// @Override
	// protected void onSaveInstanceState(final Bundle outState) {
	// super.onSaveInstanceState(outState);
	// outState.putInt(BUNDLE_TABS_CURRENT,
	// getSupportActionBar().getSelectedNavigationIndex());
	// outState.putIntArray(BUNDLE_TABS_TITLES, mTitles);
	// outState.putStringArray(BUNDLE_TABS_CLASSES, mClasses);
	// }
	//
	// @Override
	// protected void onRestoreInstanceState(final Bundle savedInstanceState) {
	// if (savedInstanceState.containsKey(BUNDLE_TABS_TITLES)) {
	// final int[] titles = savedInstanceState.getIntArray(BUNDLE_TABS_TITLES);
	// final String[] classes =
	// savedInstanceState.getStringArray(BUNDLE_TABS_CLASSES);
	// final int selectedPosition =
	// savedInstanceState.getInt(BUNDLE_TABS_CURRENT);
	//
	// addFragments(titles, classes);
	//
	// getSupportActionBar().setSelectedNavigationItem(selectedPosition > -1 ?
	// selectedPosition : 0);
	// }
	// super.onRestoreInstanceState(savedInstanceState);
	// }

	@SuppressLint("NewApi")
	@Override
	public void onTabSelected(final Tab tab, final FragmentTransaction ft) {
		if (DBG)
			Log.d(LOG_TAG, "onTabSelected " + tab.getPosition());

		mViewPager.setCurrentItem(tab.getPosition());
		getSherlockActivity().invalidateOptionsMenu();
	}

	@Override
	public void onTabUnselected(final Tab tab, final FragmentTransaction ft) {
		if (DBG)
			Log.d(LOG_TAG, "onTabUnselected " + tab.getPosition());
	}

	@Override
	public void onTabReselected(final Tab tab, final FragmentTransaction ft) {
		if (DBG)
			Log.d(LOG_TAG, "onTabReselected " + tab.getPosition());
	}

	protected void addFragments(final int[] titles, final String[] classes) {
		if (DBG)
			Log.d(LOG_TAG, "addFragments " + titles.length);

		mClasses = classes;
		mTitles = titles;
		mFragmentsTags = new String[classes.length];
	}

	protected ActionBar getSupportActionBar() {
		return ((SherlockFragmentActivity) getActivity()).getSupportActionBar();
	}

	protected ContentResolver getContentResolver() {
		return getActivity().getContentResolver();
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

	protected void addDelayedFragments(final int[] titles, final Class<?>[] classes) {
		mClasses = new String[classes.length];
		for (int i = 0; i < classes.length; i++) {
			mClasses[i] = classes[i].getName();
		}
		mTitles = titles;
	}

	protected void loadDelayedFragments() {
		addFragments(mTitles, mClasses);
	}

	protected void setSelectedTab(final int position) {
		if (DBG)
			Log.d(LOG_TAG, "setSelectedTab " + position);
	}

	/**
	 * Get the current Fragment.
	 * 
	 * @return the current Fragment, or <code>null</code> if we can't find it.
	 */
	protected SherlockFragment getCurrentFragment() {
		final int currentTab = mViewPager.getCurrentItem();
		if (currentTab != -1) {
			return (SherlockFragment) getChildFragmentManager().findFragmentByTag(mFragmentsTags[currentTab]);
		}
		return null;
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		private static final String LOG_TAG = ViewPagerFragment.LOG_TAG + "$SectionsPagerAdapter";

		public SectionsPagerAdapter(final FragmentManager fm) {
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
				Log.d(LOG_TAG, "getItem " + position);
			final Fragment fragment = Fragment.instantiate(getActivity(), mClasses[position]);
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

		public String makeFragmentName(final int viewId, final long id) {
			return "android:switcher:" + viewId + ":" + id;
		}
	}

}
