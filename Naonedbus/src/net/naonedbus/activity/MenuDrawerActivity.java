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
import net.naonedbus.fragment.impl.ItineraireFragment;
import net.naonedbus.fragment.impl.SearchFragment;
import net.naonedbus.fragment.impl.nested.BicloosFavorisFragment;
import net.naonedbus.fragment.impl.nested.BicloosFragment;
import net.naonedbus.fragment.impl.nested.CoVoituragesFragment;
import net.naonedbus.fragment.impl.nested.CommentairesFragment;
import net.naonedbus.fragment.impl.nested.FavorisFragment;
import net.naonedbus.fragment.impl.nested.LignesFragment;
import net.naonedbus.fragment.impl.nested.LilasFragment;
import net.naonedbus.fragment.impl.nested.MargueritesFragment;
import net.naonedbus.fragment.impl.nested.ParkingsPublicsFragment;
import net.naonedbus.fragment.impl.nested.ParkingsRelaisFragment;
import net.naonedbus.fragment.impl.nested.ProximiteFragment;
import net.naonedbus.fragment.impl.nested.TanActuFragment;
import net.naonedbus.widget.adapter.impl.MainMenuAdapter;
import net.naonedbus.widget.item.impl.MainMenuItem;
import android.annotation.SuppressLint;
import android.content.res.Configuration;
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
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;

@SuppressLint("NewApi")
public abstract class MenuDrawerActivity extends SherlockFragmentActivity implements TabListener {

	private static final String LOG_TAG = "MenuDrawerActivity";
	private static final boolean DBG = BuildConfig.DEBUG;

	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private ListView mDrawerList;
	private CharSequence mTitle;
	private MainMenuAdapter mAdapter;
	private List<Class<?>[]> mFragmentsClasses;
	private List<Integer[]> mFragmentsTitles;

	/** Titres des fragments. */
	private Integer[] mTitles;
	/** Classes des fragments */
	private String[] mClasses = new String[0];
	/** Fragments tags. */
	private String[] mFragmentsTags;

	private PagerSlidingTabStrip mTabs;
	private ViewPager mViewPager;
	private SectionsPagerAdapter mSectionsPagerAdapter;

	private final OnItemClickListener mOnMenuItemCliclListener = new OnItemClickListener() {
		@Override
		public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
			selectItem(position);
		}
	};

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_drawer_base);

		buildFragmentsClasses();
		buildFragmentsTitles();

		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

		mAdapter = buildMainMenuAdapter();
		mDrawerList.setAdapter(mAdapter);
		mDrawerList.setOnItemClickListener(mOnMenuItemCliclListener);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open,
				R.string.drawer_close) {
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

		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

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

	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

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

	@SuppressLint("NewApi")
	@Override
	public void onTabSelected(final Tab tab, final FragmentTransaction ft) {
		if (DBG)
			Log.d(LOG_TAG, "onTabSelected " + tab.getPosition());

		mViewPager.setCurrentItem(tab.getPosition());
		invalidateOptionsMenu();
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

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
				mDrawerLayout.closeDrawer(GravityCompat.START);
			} else {
				mDrawerLayout.openDrawer(GravityCompat.START);
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void setTitle(final CharSequence title) {
		mTitle = title;
		getSupportActionBar().setTitle(mTitle);
	}

	private MainMenuAdapter buildMainMenuAdapter() {
		final List<MainMenuItem> items = new ArrayList<MainMenuItem>();
		items.add(new MainMenuItem(R.string.title_activity_main, R.drawable.ic_action_home));
		items.add(new MainMenuItem(R.string.title_activity_infos_trafic, R.drawable.ic_action_warning));
		items.add(new MainMenuItem(R.string.title_activity_bicloo, R.drawable.ic_action_bicloo));
		items.add(new MainMenuItem(R.string.title_activity_itineraire, R.drawable.ic_action_direction));
		items.add(new MainMenuItem(R.string.title_activity_parkings, R.drawable.ic_action_parking));
		items.add(new MainMenuItem(R.string.title_activity_equipements, R.drawable.ic_action_place));
		items.add(new MainMenuItem(R.string.title_activity_recherche, R.drawable.ic_action_search));
		items.add(new MainMenuItem(R.string.title_activity_carte, R.drawable.ic_action_map));
		return new MainMenuAdapter(this, items);
	}

	private void buildFragmentsClasses() {
		mFragmentsClasses = new ArrayList<Class<?>[]>();
		mFragmentsClasses.add(new Class<?>[] { LignesFragment.class, FavorisFragment.class, ProximiteFragment.class });
		mFragmentsClasses.add(new Class<?>[] { CommentairesFragment.class, TanActuFragment.class });
		mFragmentsClasses.add(new Class<?>[] { BicloosFragment.class, BicloosFavorisFragment.class });
		mFragmentsClasses.add(new Class<?>[] { ItineraireFragment.class });
		mFragmentsClasses.add(new Class<?>[] { ParkingsPublicsFragment.class, ParkingsRelaisFragment.class });
		mFragmentsClasses.add(new Class<?>[] { MargueritesFragment.class, CoVoituragesFragment.class,
				LilasFragment.class });
		mFragmentsClasses.add(new Class<?>[] { SearchFragment.class });
	}

	private void buildFragmentsTitles() {
		mFragmentsTitles = new ArrayList<Integer[]>();
		mFragmentsTitles.add(new Integer[] { R.string.title_fragment_lignes, R.string.title_fragment_favoris,
				R.string.title_fragment_proximite });
		mFragmentsTitles.add(new Integer[] { R.string.title_fragment_en_direct, R.string.title_fragment_tan_actu });
		mFragmentsTitles.add(new Integer[] { R.string.title_fragment_bicloos, R.string.title_fragment_favoris });
		mFragmentsTitles.add(new Integer[] { R.string.title_activity_itineraire });
		mFragmentsTitles.add(new Integer[] { R.string.title_fragment_parkings_publics,
				R.string.title_fragment_parkings_relais });
		mFragmentsTitles.add(new Integer[] { R.string.title_fragment_marguerites, R.string.title_fragment_covoiturage,
				R.string.title_fragment_lila });
		mFragmentsTitles.add(new Integer[] { R.string.title_activity_recherche });
	}

	private void selectItem(final int position) {
		final MainMenuItem item = mAdapter.getItem(position);

		mDrawerList.setItemChecked(position, true);

		setFragment(mFragmentsClasses.get(position), mFragmentsTitles.get(position), item.getTitle());

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				mDrawerLayout.closeDrawer(mDrawerList);
			}
		}, 100);
	}

	protected void setSelectedTab(final int position) {
		if (DBG)
			Log.d(LOG_TAG, "setSelectedTab " + position);
	}

	protected void setFragment(final Class<?>[] fragementsClasses, final Integer[] fragmentsTitles, final int title) {
		setTitle(title);

		mTitles = fragmentsTitles;
		mClasses = new String[fragementsClasses.length];
		for (int i = 0; i < fragementsClasses.length; i++) {
			mClasses[i] = fragementsClasses[i].getName();
		}
		mFragmentsTags = new String[fragementsClasses.length];

		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOffscreenPageLimit(mClasses.length);
		mTabs.notifyDataSetChanged();
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		private static final String LOG_TAG = MenuDrawerActivity.LOG_TAG + "$SectionsPagerAdapter";

		public SectionsPagerAdapter(final FragmentManager fm) {
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

			return getString(mTitles[position]);
		}

		public String makeFragmentName(final int viewId, final long id) {
			return "android:switcher:" + viewId + ":" + id;
		}
	}

}
