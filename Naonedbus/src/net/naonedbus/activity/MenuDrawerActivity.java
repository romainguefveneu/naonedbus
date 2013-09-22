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

import java.util.ArrayList;
import java.util.List;

import net.naonedbus.BuildConfig;
import net.naonedbus.R;
import net.naonedbus.activity.impl.AboutActivity;
import net.naonedbus.activity.impl.DonateActivity;
import net.naonedbus.activity.impl.OldSettingsActivity;
import net.naonedbus.activity.impl.SettingsActivity;
import net.naonedbus.fragment.header.BicloosFragmentHeader;
import net.naonedbus.fragment.header.EquipementsFragmentHeader;
import net.naonedbus.fragment.header.FragmentHeader;
import net.naonedbus.fragment.header.InfoTraficFragmentHeader;
import net.naonedbus.fragment.header.ItineraireFragmentHeader;
import net.naonedbus.fragment.header.MainFragmentHeader;
import net.naonedbus.fragment.header.MapFragmentHeader;
import net.naonedbus.fragment.header.ParkingsFragmentHeader;
import net.naonedbus.fragment.header.SearchFragmentHeader;
import net.naonedbus.widget.adapter.impl.MainMenuAdapter;
import net.naonedbus.widget.item.impl.MainMenuItem;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;

@SuppressLint("NewApi")
public abstract class MenuDrawerActivity extends SherlockFragmentActivity {

	private static final String LOG_TAG = "MenuDrawerActivity";
	private static final boolean DBG = BuildConfig.DEBUG;

	private static final String BUNDLE_MENU_POSITION = "MenuDrawerActivity:menuPosition";
	private static final String BUNDLE_FRAGMENTS_TAGS = "MenuDrawerActivity:fragmentsTags";

	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private ListView mDrawerList;
	private CharSequence mTitle;
	private MainMenuAdapter mAdapter;

	private boolean mBaseMenuVisible = true;

	/** Titres des fragments. */
	private int[] mTitles;
	/** Classes des fragments */
	private String[] mClasses = new String[0];
	/** Fragments tags. */
	private String[] mFragmentsTags = new String[0];

	private int mCurrentMenuItem;
	private PagerSlidingTabStrip mTabs;
	private ViewPager mViewPager;
	private TabsAdapter mSectionsPagerAdapter;
	private FrameLayout mSingleFragmentContent;

	private final OnItemClickListener mOnMenuItemCliclListener = new OnItemClickListener() {
		@Override
		public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
			selectNavigationItem(position);
		}
	};

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_drawer_base);

		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);

		mTitle = getTitle();
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

		mAdapter = buildMainMenuAdapter();
		mDrawerList.setAdapter(mAdapter);
		mDrawerList.setOnItemClickListener(mOnMenuItemCliclListener);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, 0, 0) {
			@Override
			public void onDrawerClosed(final View view) {
				getSupportActionBar().setTitle(mTitle);
				invalidateOptionsMenu();
			}

			@Override
			public void onDrawerOpened(final View drawerView) {
				getSupportActionBar().setTitle(R.string.app_name);
				invalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		mSectionsPagerAdapter = new TabsAdapter(getSupportFragmentManager());

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(final int position) {
				getSupportActionBar().setSelectedNavigationItem(position);
			}
		});

		mTabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		mTabs.setViewPager(mViewPager);
		mTabs.setShouldExpand(!getResources().getBoolean(R.bool.isTablet));

		mSingleFragmentContent = (FrameLayout) findViewById(R.id.singleFragmentContent);

		if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_MENU_POSITION)) {
			final String[] fragmentsTags = savedInstanceState.getStringArray(BUNDLE_FRAGMENTS_TAGS);
			final boolean singleFragmentMode = fragmentsTags.length == 1;
			if (singleFragmentMode) {
				mFragmentsTags = fragmentsTags;
			}
			mCurrentMenuItem = savedInstanceState.getInt(BUNDLE_MENU_POSITION);
			selectNavigationItem(mCurrentMenuItem, !singleFragmentMode);
		}
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		outState.putInt(BUNDLE_MENU_POSITION, mCurrentMenuItem);
		outState.putStringArray(BUNDLE_FRAGMENTS_TAGS, mFragmentsTags);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onPostCreate(final Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(final Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		if (mBaseMenuVisible) {
			getSupportMenuInflater().inflate(R.menu.activity_base, menu);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu menu) {
		final boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		final int itemCount = menu.size();
		for (int i = 0; i < itemCount; i++) {
			final MenuItem item = menu.getItem(i);
			final int itemId = item.getItemId();
			final boolean isGlobalMenuItem = (itemId == R.id.menu_about || itemId == R.id.menu_settings || itemId == R.id.menu_donate);

			item.setVisible(!drawerOpen || isGlobalMenuItem);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
				mDrawerLayout.closeDrawer(mDrawerList);
			} else {
				mDrawerLayout.openDrawer(mDrawerList);
			}
			return true;
		case R.id.menu_settings:
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				startActivity(new Intent(this, SettingsActivity.class));
			} else {
				startActivity(new Intent(this, OldSettingsActivity.class));
			}
			return true;
		case R.id.menu_about:
			startActivity(new Intent(this, AboutActivity.class));
			return true;
		case R.id.menu_donate:
			startActivity(new Intent(this, DonateActivity.class));
			return true;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void setTitle(final CharSequence title) {
		mTitle = title;
		getSupportActionBar().setTitle(mTitle);
	}

	private MainMenuAdapter buildMainMenuAdapter() {
		return new MainMenuAdapter(this, getMainMenuItems());
	}

	protected void selectNavigationItem(final int position) {
		selectNavigationItem(position, true);
	}

	private void selectNavigationItem(final int position, final boolean setFragment) {
		if (DBG)
			Log.d(LOG_TAG, "selectItem " + position);

		mCurrentMenuItem = position;

		final MainMenuItem item = mAdapter.getItem(position);
		final FragmentHeader fragmentHeader = item.getFragmentHeader();

		mDrawerList.setItemChecked(position, true);
		mAdapter.setCurrentItem(position);

		if (setFragment) {
			setFragment(fragmentHeader, item.getTitle());
		}

		if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					mDrawerLayout.closeDrawer(mDrawerList);
				}
			}, 100);
		}
	}

	protected void openDrawer() {
		mDrawerLayout.openDrawer(mDrawerList);
	}

	protected void setFragment(final FragmentHeader fragmentHeader, final int title) {
		if (DBG)
			Log.d(LOG_TAG, "setFragment " + getString(title));

		clearFragments();
		setTitle(title);

		final Class<?>[] classes = fragmentHeader.getFragmentsClasses();
		mTitles = fragmentHeader.getFragmentsTitles();
		mClasses = new String[classes.length];
		for (int i = 0; i < classes.length; i++) {
			mClasses[i] = classes[i].getName();
		}
		mFragmentsTags = new String[classes.length];

		if (classes.length == 1) {
			setSingleFragment();
		} else {
			setMultipleFragments(fragmentHeader.getSelectedPosition(this));
		}
	}

	private void setSingleFragment() {
		final String fragmentTag = mClasses[0];
		final FragmentManager fragmentManager = getSupportFragmentManager();
		final FragmentTransaction transaction = fragmentManager.beginTransaction();
		transaction.add(R.id.singleFragmentContent, Fragment.instantiate(MenuDrawerActivity.this, mClasses[0]),
				fragmentTag);
		transaction.commit();

		mFragmentsTags[0] = fragmentTag;

		mTabs.setVisibility(View.GONE);
		mViewPager.setVisibility(View.GONE);
		mSingleFragmentContent.setVisibility(View.VISIBLE);
	}

	private void setMultipleFragments(final int selectedPosition) {
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOffscreenPageLimit(mClasses.length);

		if (mTitles.length == 1) {
			mTabs.setVisibility(View.GONE);
		} else {
			mTabs.setVisibility(View.VISIBLE);
		}

		mTabs.notifyDataSetChanged();
		mViewPager.setCurrentItem(selectedPosition);
		mViewPager.setVisibility(View.VISIBLE);
		mSingleFragmentContent.setVisibility(View.GONE);
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

	public void setBaseMenuVisible(final boolean baseMenuVisible) {
		mBaseMenuVisible = baseMenuVisible;
	}

	public static List<MainMenuItem> getMainMenuItems() {
		final List<MainMenuItem> items = new ArrayList<MainMenuItem>();
		items.add(new MainMenuItem(R.string.title_activity_main, R.drawable.ic_action_home, new MainFragmentHeader()));
		items.add(new MainMenuItem(R.string.title_activity_infos_trafic, R.drawable.ic_action_warning,
				new InfoTraficFragmentHeader()));
		items.add(new MainMenuItem(R.string.title_activity_itineraire, R.drawable.ic_action_direction,
				new ItineraireFragmentHeader()));
		items.add(new MainMenuItem(R.string.title_activity_bicloo, R.drawable.ic_action_bicloo,
				new BicloosFragmentHeader()));
		items.add(new MainMenuItem(R.string.title_activity_parkings, R.drawable.ic_action_parking,
				new ParkingsFragmentHeader()));
		items.add(new MainMenuItem(R.string.title_activity_equipements, R.drawable.ic_action_place,
				new EquipementsFragmentHeader()));
		items.add(new MainMenuItem(R.string.title_activity_recherche, R.drawable.ic_action_search,
				new SearchFragmentHeader()));
		items.add(new MainMenuItem(R.string.title_activity_carte, R.drawable.ic_action_map, new MapFragmentHeader()));

		return items;
	}

	public class TabsAdapter extends FragmentPagerAdapter {
		private static final String LOG_TAG = MenuDrawerActivity.LOG_TAG + "$TabsAdapter";

		public TabsAdapter(final FragmentManager fm) {
			super(fm);
		}

		@Override
		public Object instantiateItem(final ViewGroup container, final int position) {
			if (DBG)
				Log.d(LOG_TAG, "instantiateItem " + position);

			final Fragment fragment = (Fragment) super.instantiateItem(container, position);
			fragment.setRetainInstance(true);
			mFragmentsTags[position] = fragment.getTag();
			return fragment;
		}

		@Override
		public Fragment getItem(final int position) {
			if (DBG)
				Log.d(LOG_TAG, "getItem " + position + " : " + mClasses[position]);

			final Fragment fragment = Fragment.instantiate(MenuDrawerActivity.this, mClasses[position]);
			fragment.setRetainInstance(true);
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

			if (mTitles[position] == 0) {
				return "";
			} else {
				return getString(mTitles[position]);
			}
		}
	}

}
