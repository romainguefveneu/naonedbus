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

import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.activity.MenuDrawerActivity;
import net.naonedbus.activity.map.overlay.TypeOverlayItem;
import net.naonedbus.fragment.header.UpdateFragmentHeader;
import net.naonedbus.fragment.impl.MapFragment;
import net.naonedbus.helper.FavorisHelper;
import net.naonedbus.intent.ParamIntent;
import net.naonedbus.manager.impl.UpdaterManager;
import net.naonedbus.manager.impl.UpdaterManager.UpdateType;
import net.naonedbus.provider.impl.MyLocationProvider;
import net.naonedbus.service.FavoriService;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;

public class MainActivity extends MenuDrawerActivity {

	private final MyLocationProvider mMyLocationProvider;

	public MainActivity() {
		mMyLocationProvider = NBApplication.getLocationProvider();
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		BugSenseHandler.initAndStartSession(this, getString(R.string.bugsense));

		handleSearchResult();
		handleFavoriExport();

		if (savedInstanceState == null) {
			final UpdaterManager updaterManager = new UpdaterManager();
			final UpdateType updateType = updaterManager.needUpdate(this);

			if (UpdateType.FIRST_LAUNCH.equals(updateType)) {
				hideActionBar();
				setBaseMenuVisible(false);
				setFragment(new UpdateFragmentHeader(), R.string.title_activity_main);
				showTutorial();
			} else if (UpdateType.UPGRADE.equals(updateType)) {
				hideActionBar();
				setBaseMenuVisible(false);
				setFragment(new UpdateFragmentHeader(), R.string.title_activity_main);
			} else {
				final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
				selectNavigationItem(Integer.parseInt(preferences.getString(NBApplication.PREF_NAVIGATION_HOME, "0")));
			}
		}

	}

	@Override
	protected void onStop() {
		mMyLocationProvider.stop();
		super.onStop();
	}

	private void hideActionBar() {
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(false);
		actionBar.setDisplayUseLogoEnabled(true);
		actionBar.setLogo(R.drawable.ic_logo);
	}

	private void showTutorial() {
		new Handler().post(new Runnable() {
			@Override
			public void run() {
				startActivity(new Intent(MainActivity.this, TutorialActivity.class));
				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
			}
		});
	}

	public void onUpgradeDone() {
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayUseLogoEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(true);

		setBaseMenuVisible(true);
		selectNavigationItem(0);
		openDrawer();
	}

	private void handleFavoriExport() {
		final Intent intent = getIntent();
		if (intent.hasExtra(FavoriService.INTENT_PARAM_KEY)) {
			final String key = intent.getStringExtra(FavoriService.INTENT_PARAM_KEY);
			final FavorisHelper favorisHelper = new FavorisHelper(this);
			favorisHelper.showExportKey(key);
			intent.removeExtra(FavoriService.INTENT_PARAM_KEY);
		}
	}

	private void handleSearchResult() {
		final Intent queryIntent = getIntent();
		final String queryAction = queryIntent.getAction();
		if (Intent.ACTION_SEARCH.equals(queryAction)) {
			if (queryIntent.getData() == null) {
				// Lancer la recherche
				final String query = queryIntent.getStringExtra(SearchManager.QUERY);
				Toast.makeText(this, query, Toast.LENGTH_SHORT).show();
			} else {
				final Integer selectedItemId = Integer.valueOf(queryIntent.getStringExtra(SearchManager.QUERY));
				final TypeOverlayItem selectedItemType = TypeOverlayItem.getById(Integer.valueOf(queryIntent
						.getStringExtra(SearchManager.EXTRA_DATA_KEY)));

				if (selectedItemType.equals(TypeOverlayItem.TYPE_STATION)) {
					// Afficher les parcours de l'arrêt sélectionné
					final ParamIntent intent = new ParamIntent(this, ParcoursActivity.class);
					intent.putExtra(ParcoursActivity.PARAM_ID_SATION, selectedItemId);
					startActivity(intent);
				} else {
					// Afficher l'élément sur la carte
					final Intent intent = new Intent(this, MapActivity.class);
					intent.putExtra(MapFragment.PARAM_ITEM_ID, selectedItemId);
					intent.putExtra(MapFragment.PARAM_ITEM_TYPE, selectedItemType.getId());
					startActivity(intent);
				}
			}
			queryIntent.setAction(null);
		}
	}

}
