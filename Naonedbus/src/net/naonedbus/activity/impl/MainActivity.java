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
import net.naonedbus.activity.SlidingMenuActivity;
import net.naonedbus.fragment.impl.FavorisFragment;
import net.naonedbus.fragment.impl.LignesFragment;
import net.naonedbus.fragment.impl.ProximiteFragment;
import net.naonedbus.helper.FavorisHelper;
import net.naonedbus.manager.impl.FavoriManager;
import net.naonedbus.manager.impl.HoraireManager;
import net.naonedbus.manager.impl.UpdaterManager;
import net.naonedbus.provider.CustomContentProvider;
import net.naonedbus.provider.DatabaseActionObserver;
import net.naonedbus.provider.DatabaseVersions;
import net.naonedbus.provider.impl.MyLocationProvider;
import net.naonedbus.service.FavoriService;
import net.naonedbus.utils.InfoDialogUtils;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.bugsense.trace.BugSenseHandler;

public class MainActivity extends SlidingMenuActivity {

	private static int[] titles = new int[] { R.string.title_fragment_lignes, R.string.title_fragment_favoris,
			R.string.title_fragment_proximite };

	private static Class<?>[] classes = new Class<?>[] { LignesFragment.class, FavorisFragment.class,
			ProximiteFragment.class };

	private boolean mHasSetup;
	private boolean mContentLoaded;
	private boolean mIsFrontActivity;
	private boolean mFirstLaunch;
	private boolean mUpgradeError;
	private final MyLocationProvider mMyLocationProvider;

	private final DatabaseActionObserver mListener = new DatabaseActionObserver(new Handler()) {

		@Override
		public void onUpgrade(final int oldVersion) {
			showSetupView(R.string.updating);
			if (oldVersion < DatabaseVersions.ACAPULCO) {
				mFirstLaunch = true;
				showTutorial();
			}
		}

		@Override
		public void onCreate() {
			mFirstLaunch = true;
			showSetupView(R.string.setup);
			showTutorial();
		}

		@Override
		public void onUpgradeError() {
			mUpgradeError = true;
		}
	};

	public MainActivity() {
		super(R.layout.activity_main);
		mMyLocationProvider = NBApplication.getLocationProvider();
	}

	@SuppressWarnings("unused")
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		setTitle(R.string.title_activity_main);
		super.onCreate(savedInstanceState);

		BugSenseHandler.initAndStartSession(this, getString(R.string.bugsense));

		if (savedInstanceState == null) {
			new UpdateAndCleanTask().execute();
		}
		addDelayedFragments(titles, classes);
	}

	@Override
	protected void onResume() {
		mIsFrontActivity = true;
		mMyLocationProvider.start();
		if (mHasSetup && mContentLoaded == false) {
			hideSetupView();
			loadContent();
		}
		super.onResume();
	}

	@Override
	protected void onStop() {
		mIsFrontActivity = false;
		mMyLocationProvider.stop();
		super.onStop();
	}

	private void loadContent() {
		if (mFirstLaunch) {
			peekSlidingMenu();
		}

		loadDelayedFragments();

		final FavoriManager favoriManager = FavoriManager.getInstance();
		final int count = favoriManager.getAll(getContentResolver()).size();
		if (count > 0) {
			setSelectedTab(1);
		}

		mContentLoaded = true;
	}

	/**
	 * Effectuer les actions avant de déclencher la mise à jour.
	 */
	private void beforeUpdate() {
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
	}

	/**
	 * Effectuer les actions après l'éventuelle mise à jour.
	 */
	private void afterUpdate() {
		mHasSetup = true;
		if (mIsFrontActivity && mContentLoaded == false) {
			hideSetupView();
			loadContent();

			final Intent intent = getIntent();

			if (intent.hasExtra(FavoriService.INTENT_PARAM_KEY)) {
				final String key = intent.getStringExtra(FavoriService.INTENT_PARAM_KEY);
				final FavorisHelper favorisHelper = new FavorisHelper(this);
				favorisHelper.showExportKey(key);
				intent.removeExtra(FavoriService.INTENT_PARAM_KEY);
			}

			if (mUpgradeError) {
				InfoDialogUtils.show(this, R.string.error_title_upgrade, R.string.error_summary_upgrade);
			}
		}
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		showTutorial();
	}

	private void showSetupView(final int textResId) {
		final View view;
		if ((view = findViewById(R.id.setupViewStub)) != null) {
			view.setVisibility(View.VISIBLE);
			((TextView) findViewById(R.id.setupViewLabel)).setText(textResId);
		}
	}

	private void hideSetupView() {
		View view;
		if ((view = findViewById(R.id.setupView)) != null) {
			view.setVisibility(View.GONE);
		}
	}

	private void showTutorial() {
		startActivity(new Intent(MainActivity.this, TutorialActivity.class));
		overridePendingTransition(0, 0);
	}

	/**
	 * Task chargée de déclencher une éventuelle mise à jour et de vider le
	 * cache horaire.
	 * 
	 * @author romain.guefveneu
	 * 
	 */
	private class UpdateAndCleanTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			beforeUpdate();
		}

		@Override
		protected Void doInBackground(final Void... params) {
			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
			CustomContentProvider.setDatabaseActionListener(mListener);

			// Déclencher une éventuelle mise à jour
			final UpdaterManager updaterManager = new UpdaterManager();
			updaterManager.triggerUpdate(getContentResolver());

			// Vider les anciens horaires
			final HoraireManager horaireManager = HoraireManager.getInstance();
			horaireManager.clearOldHoraires(getContentResolver());

			CustomContentProvider.setDatabaseActionListener(null);
			return null;
		}

		@Override
		protected void onPostExecute(final Void result) {
			afterUpdate();
		}

	}

}
