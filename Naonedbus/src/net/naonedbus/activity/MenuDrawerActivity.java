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
import net.naonedbus.fragment.impl.BicloosRootFragment;
import net.naonedbus.fragment.impl.EquipementsRootFragment;
import net.naonedbus.fragment.impl.InfoTraficRootFragment;
import net.naonedbus.fragment.impl.ItineraireFragment;
import net.naonedbus.fragment.impl.MainRootFragment;
import net.naonedbus.fragment.impl.MapFragment;
import net.naonedbus.fragment.impl.ParkingsRootFragment;
import net.naonedbus.fragment.impl.SearchFragment;
import net.naonedbus.widget.adapter.impl.MainMenuAdapter;
import net.naonedbus.widget.item.impl.MainMenuItem;
import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

@SuppressLint("NewApi")
public abstract class MenuDrawerActivity extends SherlockFragmentActivity {

	private static final String LOG_TAG = "MenuDrawerActivity";
	private static final boolean DBG = BuildConfig.DEBUG;

	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private ListView mDrawerList;
	private CharSequence mTitle;
	private MainMenuAdapter mAdapter;

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
		getActionBar().setTitle(mTitle);
	}

	private MainMenuAdapter buildMainMenuAdapter() {
		final List<MainMenuItem> items = new ArrayList<MainMenuItem>();
		// @formatter:off
		items.add(new MainMenuItem(R.string.title_activity_main, MainRootFragment.class, R.drawable.ic_action_home, 0));
		items.add(new MainMenuItem(R.string.title_activity_infos_trafic, InfoTraficRootFragment.class,R.drawable.ic_action_warning, 0));
		items.add(new MainMenuItem(R.string.title_activity_bicloo, BicloosRootFragment.class, R.drawable.ic_action_bicloo, 0));
		items.add(new MainMenuItem(R.string.title_activity_itineraire, ItineraireFragment.class, R.drawable.ic_action_direction, 0));
		items.add(new MainMenuItem(R.string.title_activity_parkings, ParkingsRootFragment.class, R.drawable.ic_action_parking, 0));
		items.add(new MainMenuItem(R.string.title_activity_equipements, EquipementsRootFragment.class, R.drawable.ic_action_place, 0));
		items.add(new MainMenuItem(R.string.title_activity_recherche, SearchFragment.class, R.drawable.ic_action_search, 0));
		items.add(new MainMenuItem(R.string.title_activity_carte, MapFragment.class, R.drawable.ic_action_map, 0));
//		items.add(new MainMenuItem(R.string.title_activity_parametres, (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) ? OldSettingsFragment.class : SettingsFragment.class, R.drawable.ic_action_settings, 1));
//		items.add(new MainMenuItem(R.string.title_activity_about, AboutRootFragment.class, R.drawable.ic_action_info, 1));
//		items.add(new MainMenuItem(R.string.title_activity_donate, DonateFragment.class, R.drawable.ic_action_favourite, 1));
		// @formatter:on
		return new MainMenuAdapter(this, items);
	}

	private void selectItem(final int position) {
		final MainMenuItem item = mAdapter.getItem(position);

		mDrawerList.setItemChecked(position, true);

		setFragment(item.getFragmentClass(), item.getTitle());

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				// update selected item and title, then close the drawer
				mDrawerLayout.closeDrawer(mDrawerList);
			}
		}, 100);
	}

	protected void setFragment(final Class<? extends Fragment> fragementClass, final int title) {
		setTitle(title);

		final Fragment fragment = Fragment.instantiate(this, fragementClass.getName());
		final FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
	}

}
