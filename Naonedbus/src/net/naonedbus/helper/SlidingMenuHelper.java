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

import java.util.ArrayList;
import java.util.List;

import net.naonedbus.R;
import net.naonedbus.activity.impl.AboutActivity;
import net.naonedbus.activity.impl.BicloosActivity;
import net.naonedbus.activity.impl.DonateActivity;
import net.naonedbus.activity.impl.InfosTraficActivity;
import net.naonedbus.activity.impl.ItineraireActivity;
import net.naonedbus.activity.impl.MainActivity;
import net.naonedbus.activity.impl.MapActivity;
import net.naonedbus.activity.impl.OldSettingsActivity;
import net.naonedbus.activity.impl.ParkingsActivity;
import net.naonedbus.activity.impl.SearchActivity;
import net.naonedbus.activity.impl.SettingsActivity;
import net.naonedbus.widget.adapter.impl.MainMenuAdapter;
import net.naonedbus.widget.indexer.impl.MainMenuIndexer;
import net.naonedbus.widget.item.impl.MainMenuItem;
import net.simonvt.menudrawer.MenuDrawer;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;

public class SlidingMenuHelper {

	private static MainMenuAdapter sAdapter;
	private static int sSavedPosition = -1;
	private static int sSavedListTop;

	/** Menu général. */
	private ListView mMenuListView;

	/**
	 * Contenu du menu général.
	 */
	private static final List<MainMenuItem> MENU_ITEMS;
	static {
		// @formatter:off
		MENU_ITEMS = new ArrayList<MainMenuItem>();
		MENU_ITEMS.add(new MainMenuItem(R.string.title_activity_main, MainActivity.class, R.drawable.ic_action_view_as_grid, 0));
		MENU_ITEMS.add(new MainMenuItem(R.string.title_activity_infos_trafic, InfosTraficActivity.class,R.drawable.ic_action_warning, 0));
		MENU_ITEMS.add(new MainMenuItem(R.string.title_activity_bicloo, BicloosActivity.class, R.drawable.ic_action_bicloo, 0));
		MENU_ITEMS.add(new MainMenuItem(R.string.title_activity_itineraire, ItineraireActivity.class, R.drawable.ic_action_direction, 0));
		MENU_ITEMS.add(new MainMenuItem(R.string.title_activity_parkings, ParkingsActivity.class, R.drawable.ic_action_parking, 0));
//		MENU_ITEMS.add(new MainMenuItem(R.string.title_activity_equipements, EquipementsActivity.class, R.drawable.ic_action_place, 0));
		MENU_ITEMS.add(new MainMenuItem(R.string.title_activity_recherche, SearchActivity.class, R.drawable.ic_action_search, 0));
		MENU_ITEMS.add(new MainMenuItem(R.string.title_activity_carte, MapActivity.class, R.drawable.ic_action_map, 0));
		MENU_ITEMS.add(new MainMenuItem(R.string.title_activity_parametres, (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) ? OldSettingsActivity.class : SettingsActivity.class, R.drawable.ic_action_settings, 1));
		MENU_ITEMS.add(new MainMenuItem(R.string.title_activity_about, AboutActivity.class, R.drawable.ic_action_info, 1));
		MENU_ITEMS.add(new MainMenuItem(R.string.title_activity_donate, DonateActivity.class, R.drawable.ic_action_favourite, 1));
		// @formatter:on
	}

	private final Activity mActivity;

	public SlidingMenuHelper(final Activity activity) {
		mActivity = activity;
	}

	public void onPostCreate(final Intent intent, final MenuDrawer slidingMenu, final Bundle savedInstanceState) {
		if (intent.getBooleanExtra("fromMenu", false)
				&& (savedInstanceState == null || !savedInstanceState.containsKey("menuConsumed"))) {
			// Afficher le menu au démarrage, pour la transition.
			slidingMenu.openMenu(false);

			// // Masquer le menu.
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					slidingMenu.closeMenu(true);
				}
			}, 400);

			// Consommer l'affichage du menu pour ne pas réafficher en cas de
			// rotation.
			intent.putExtra("fromMenu", false);
		}
	}

	public void onSaveInstanceState(final Bundle outState) {
		outState.putBoolean("menuConsumed", true);
	}

	public void onWindowFocusChanged(final boolean hasFocus, final MenuDrawer slidingMenu) {
		// Gérer le masquage de menu
		if (hasFocus == false && slidingMenu.isMenuVisible()) {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					slidingMenu.closeMenu(false);
				}
			}, 800);
		}

		if (hasFocus) {
			// Nouvelle activité
			sAdapter.setCurrentClass(mActivity.getClass());
			sAdapter.notifyDataSetChanged();
		}

	}

	public void setupSlidingMenu(final MenuDrawer slidingMenu) {
		slidingMenu.setMenuView(R.layout.menu);
		slidingMenu.setDropShadow(R.drawable.shadow);
		slidingMenu.setSlideDrawable(R.drawable.ic_drawer);
		slidingMenu.setDrawerIndicatorEnabled(true);

		mMenuListView = (ListView) slidingMenu.findViewById(android.R.id.list);
		if (sAdapter == null) {
			sAdapter = new MainMenuAdapter(mActivity);
			for (final MainMenuItem item : MENU_ITEMS) {
				sAdapter.add(item);
			}
			final MainMenuIndexer indexer = new MainMenuIndexer(mActivity);
			indexer.buildIndex(mActivity, sAdapter);
			sAdapter.setIndexer(indexer);
		}

		final View headerView = LayoutInflater.from(mActivity).inflate(R.layout.list_menu_header, mMenuListView, false);
		mMenuListView.addHeaderView(headerView);

		sAdapter.setCurrentClass(mActivity.getClass());
		mMenuListView.setAdapter(sAdapter);

		if (sSavedPosition >= 0) { // initialized to -1
			mMenuListView.setSelectionFromTop(sSavedPosition, sSavedListTop);
		}

		mMenuListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
				// Ne pas permettre à l'utilisateur de cliquer n'importe où...
				mMenuListView.setClickable(false);

				// Sauvegarder l'état de la listview
				sSavedPosition = mMenuListView.getFirstVisiblePosition();
				final View firstVisibleView = mMenuListView.getChildAt(0);
				sSavedListTop = (firstVisibleView == null) ? 0 : firstVisibleView.getTop();

				final MainMenuItem item = (MainMenuItem) mMenuListView.getItemAtPosition(position);
				if (item != null) {
					if (mActivity.getClass().equals(item.getIntentClass())) {
						// Même activité
						slidingMenu.closeMenu();
						mMenuListView.setClickable(true);
					} else {
						// Nouvelle activité
						sAdapter.setCurrentClass(item.getIntentClass());
						sAdapter.notifyDataSetChanged();

						final Intent intent = new Intent(mActivity, item.getIntentClass());
						intent.putExtra("fromMenu", true);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						mActivity.startActivity(intent);
						mActivity.overridePendingTransition(0, 0);
					}
				}
			}
		});

	}

	public void setupActionBar(final ActionBar actionBar) {
		actionBar.setDisplayHomeAsUpEnabled(true);
	}
}
