package net.naonedbus.activity;

import net.naonedbus.BuildConfig;
import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.fragment.CustomFragmentActions;
import net.naonedbus.helper.SlidingMenuHelper;
import net.naonedbus.intent.IIntentParamKey;
import net.simonvt.menudrawer.MenuDrawer;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public abstract class SlidingMenuActivity extends SherlockFragmentActivity implements TabListener {

	private static final String LOG_TAG = "SlidingMenuActivity";
	private static final boolean DBG = BuildConfig.DEBUG;

	/**
	 * Default delay from {@link #peekDrawer()} is called until first animation
	 * is run.
	 */
	private static final long DEFAULT_PEEK_START_DELAY = 1000;

	/**
	 * Default delay between each subsequent animation, after
	 * {@link #peekDrawer()} has been called.
	 */
	private static final long DEFAULT_PEEK_DELAY = 8000;

	private static String BUNDLE_TABS_CURRENT = "tabsCurrent";
	private static String BUNDLE_TABS_TITLES = "tabsTitles";
	private static String BUNDLE_TABS_CLASSES = "tabsClasses";

	/** Layout de l'activitée courante. */
	private int mLayoutId;
	/** Sert à la détection du changement de thème. */
	private int mCurrentTheme = NBApplication.THEME;

	/** Titres des fragments. */
	private int[] mTitles;
	/** Classes des fragments */
	private String[] mClasses;
	/** Bundles des fragments. */
	private Bundle[] mBundles;
	/** Fragments tags. */
	private String[] mFragmentsTags;

	/** Gestion du menu latéral. */
	private MenuDrawer mMenuDrawer;
	/** Gestion du menu latéral. */
	private SlidingMenuHelper mSlidingMenuHelper;

	/** The {@link ViewPager} that will host the section contents. */
	private ViewPager mViewPager;

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	private SectionsPagerAdapter mSectionsPagerAdapter;

	public SlidingMenuActivity(int layoutId) {
		this.mLayoutId = layoutId;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(NBApplication.THEMES_MENU_RES[NBApplication.THEME]);
		getWindow().setBackgroundDrawable(null);
		super.onCreate(savedInstanceState);
		setContentView(mLayoutId);

		mMenuDrawer = MenuDrawer.attach(this, MenuDrawer.MENU_DRAG_WINDOW);

		mSlidingMenuHelper = new SlidingMenuHelper(this);
		mSlidingMenuHelper.setupActionBar(getSupportActionBar());
		mSlidingMenuHelper.setupSlidingMenu(mMenuDrawer);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				getSupportActionBar().setSelectedNavigationItem(position);
			}
		});
	}

	@Override
	public void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mSlidingMenuHelper.onPostCreate(getIntent(), mMenuDrawer, savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		final Fragment fragment = getCurrentFragment();

		if (fragment instanceof CustomFragmentActions) {
			final CustomFragmentActions customListFragment = (CustomFragmentActions) fragment;
			customListFragment.onCreateOptionsMenu(menu);
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		final Fragment fragment = getCurrentFragment();

		if (DBG)
			Log.d(LOG_TAG, "onPrepareOptionsMenu " + fragment);

		if (fragment instanceof CustomFragmentActions) {
			final CustomFragmentActions customListFragment = (CustomFragmentActions) fragment;
			customListFragment.onPrepareOptionsMenu(menu);
		}

		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * Show the menu when home icon is clicked.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			mMenuDrawer.toggleMenu();
			return true;
		default:
			final Fragment fragment = getCurrentFragment();
			if (fragment instanceof CustomFragmentActions) {
				final CustomFragmentActions customListFragment = (CustomFragmentActions) fragment;
				return customListFragment.onOptionsItemSelected(item);
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(BUNDLE_TABS_CURRENT, getSupportActionBar().getSelectedNavigationIndex());
		outState.putIntArray(BUNDLE_TABS_TITLES, mTitles);
		outState.putStringArray(BUNDLE_TABS_CLASSES, mClasses);
		mSlidingMenuHelper.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState.containsKey(BUNDLE_TABS_TITLES)) {
			final int[] titles = savedInstanceState.getIntArray(BUNDLE_TABS_TITLES);
			final String[] classes = savedInstanceState.getStringArray(BUNDLE_TABS_CLASSES);
			final int selectedPosition = savedInstanceState.getInt(BUNDLE_TABS_CURRENT);

			addFragments(titles, classes);

			getSupportActionBar().setSelectedNavigationItem(selectedPosition > -1 ? selectedPosition : 0);
		}
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		// Gérer le changement de thème;
		if (hasFocus && (mCurrentTheme != NBApplication.THEME)) {
			final Intent intent = new Intent(this, this.getClass());
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			overridePendingTransition(0, 0);
			startActivity(intent);
			finish();
		}

		mSlidingMenuHelper.onWindowFocusChanged(hasFocus, mMenuDrawer);
	}

	/**
	 * Animates the sliding menu slightly open.
	 */
	protected void peekSlidingMenu() {
		mMenuDrawer.peekDrawer(DEFAULT_PEEK_START_DELAY, DEFAULT_PEEK_DELAY);
	}

	/**
	 * Show the menu when menu button pressed, hide it when back is pressed
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU || (mMenuDrawer.isMenuVisible() && keyCode == KeyEvent.KEYCODE_BACK)) {
			mMenuDrawer.toggleMenu();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		if (DBG)
			Log.d(LOG_TAG, "onTabSelected " + tab.getPosition());

		mViewPager.setCurrentItem(tab.getPosition());
		invalidateOptionsMenu();
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		if (DBG)
			Log.d(LOG_TAG, "onTabUnselected " + tab.getPosition());
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		if (DBG)
			Log.d(LOG_TAG, "onTabReselected " + tab.getPosition());
	}

	protected void addFragments(int[] titles, String[] classes) {
		if (DBG)
			Log.d(LOG_TAG, "addFragments " + titles.length);

		final ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		mClasses = classes;
		mTitles = titles;
		mFragmentsTags = new String[classes.length];

		for (int i = 0; i < titles.length; i++) {
			actionBar.addTab(actionBar.newTab().setText(titles[i]).setTabListener(this));
		}

		mViewPager.setOffscreenPageLimit(classes.length);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		invalidateOptionsMenu();
	}

	/**
	 * Ajouter les informations de fragments.
	 * 
	 * @param titles
	 *            Les titres (ressources).
	 * @param classes
	 *            Les classes des fragments.
	 */
	protected void addFragments(int[] titles, Class<?>[] classes) {
		mClasses = new String[classes.length];
		for (int i = 0; i < classes.length; i++) {
			mClasses[i] = classes[i].getName();
		}
		addFragments(titles, mClasses);
	}

	protected void addDelayedFragments(int[] titles, Class<?>[] classes) {
		mClasses = new String[classes.length];
		for (int i = 0; i < classes.length; i++) {
			mClasses[i] = classes[i].getName();
		}
		mTitles = titles;
	}

	protected void loadDelayedFragments() {
		addFragments(mTitles, mClasses);
	}

	protected void setSelectedTab(int position) {
		if (DBG)
			Log.d(LOG_TAG, "setSelectedTab " + position);
		getSupportActionBar().setSelectedNavigationItem(position);
	}

	/**
	 * Get the current Fragment.
	 * 
	 * @return the current Fragment, or <code>null</code> if we can't find it.
	 */
	private Fragment getCurrentFragment() {
		final Tab tab = getSupportActionBar().getSelectedTab();
		if (tab != null) {
			return getSupportFragmentManager().findFragmentByTag(mFragmentsTags[tab.getPosition()]);
		}
		return null;
	}

	/**
	 * Renvoyer la valeur du paramètre de l'intent
	 * 
	 * @param key
	 * @return
	 */
	protected Object getParamValue(IIntentParamKey key) {
		return getIntent().getSerializableExtra(key.toString());
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		private static final String LOG_TAG = SlidingMenuActivity.LOG_TAG + "$SectionsPagerAdapter";

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			final Fragment fragment = (Fragment) super.instantiateItem(container, position);
			mFragmentsTags[position] = fragment.getTag();
			return fragment;
		}

		@Override
		public Fragment getItem(int position) {
			if (DBG)
				Log.d(LOG_TAG, "getItem " + position);
			return Fragment.instantiate(SlidingMenuActivity.this, mClasses[position]);
		}

		@Override
		public int getCount() {
			return mClasses.length;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			if (DBG)
				Log.d(LOG_TAG, "getPageTitle " + position);

			return getString(mTitles[position]);
		}

		public String makeFragmentName(int viewId, long id) {
			return "android:switcher:" + viewId + ":" + id;
		}
	}

}
