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
import net.naonedbus.fragment.impl.MainRootFragment;
import net.naonedbus.helper.FavorisHelper;
import net.naonedbus.manager.impl.HoraireManager;
import net.naonedbus.manager.impl.UpdaterManager;
import net.naonedbus.provider.CustomContentProvider;
import net.naonedbus.provider.DatabaseActionObserver;
import net.naonedbus.provider.DatabaseVersions;
import net.naonedbus.provider.impl.MyLocationProvider;
import net.naonedbus.service.FavoriService;
import net.naonedbus.utils.FontUtils;
import net.naonedbus.utils.InfoDialogUtils;
import net.naonedbus.utils.VersionUtils;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.bugsense.trace.BugSenseHandler;

public class MainActivity extends MenuDrawerActivity {

	private boolean mHasSetup;
	private boolean mContentLoaded;
	private boolean mIsFrontActivity;
	private boolean mFirstLaunch;
	private boolean mUpgradeError;
	private boolean mUpgrade;
	private final MyLocationProvider mMyLocationProvider;
	private UpdateCard mUpdateCard;

	private final DatabaseActionObserver mListener = new DatabaseActionObserver(new Handler()) {

		@Override
		public void onUpgrade(final int oldVersion) {
			showUpdateView();
			mUpgrade = true;
			if (oldVersion < DatabaseVersions.ACAPULCO) {
				mFirstLaunch = true;
				showTutorial();
			}
		}

		@Override
		public void onCreate() {
			mFirstLaunch = true;
			showSetupView();
			showTutorial();
		}

		@Override
		public void onUpgradeError() {
			mUpgradeError = true;
		}
	};

	public MainActivity() {
		mMyLocationProvider = NBApplication.getLocationProvider();
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		BugSenseHandler.initAndStartSession(this, getString(R.string.bugsense));

		if (savedInstanceState == null) {
			new UpdateAndCleanTask().execute();
		}
	}

	@Override
	protected void onResume() {
		mIsFrontActivity = true;
		mMyLocationProvider.start();
		if (mHasSetup && mContentLoaded == false) {
			hideSetupView();
		}
		super.onResume();
	}

	@Override
	protected void onStop() {
		mIsFrontActivity = false;
		mMyLocationProvider.stop();
		super.onStop();
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

			setFragment(MainRootFragment.class, R.string.title_activity_main);

			hideSetupView();

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

			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	private void showSetupView() {
		final View view;
		if ((view = findViewById(R.id.setupViewStub)) != null) {
			view.setVisibility(View.VISIBLE);
		}
	}

	private void showUpdateView() {
		mUpdateCard = new UpdateCard(this, getWindow().getDecorView(), new OnClickListener() {
			@Override
			public void onClick(final View v) {
				afterUpdate();
			}
		});
		mUpdateCard.show();
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
			if (mUpgrade) {
				mUpdateCard.setComplete();
			} else {
				afterUpdate();
			}
		}

	}

	private static class UpdateCard {

		private final Context mContext;
		private final View mParentView;
		private final OnClickListener mOnNextClickListener;
		private View mUpdateView;

		public UpdateCard(final Context context, final View parentView, final OnClickListener onNextClickListener) {
			mContext = context;
			mParentView = parentView;
			mOnNextClickListener = onNextClickListener;
		}

		public void show() {
			if ((mUpdateView = mParentView.findViewById(R.id.updateViewStub)) != null) {
				mUpdateView.setVisibility(View.VISIBLE);

				final Typeface robotoTypeface = FontUtils.getRobotoLight(mContext);
				((TextView) mParentView.findViewById(android.R.id.title)).setTypeface(robotoTypeface);
				((TextView) mParentView.findViewById(R.id.codename)).setTypeface(robotoTypeface);

				final String version = mContext.getString(R.string.version_number, VersionUtils.getVersion(mContext));
				((TextView) mParentView.findViewById(R.id.version)).setText(version);

				final String versionNotes = VersionUtils.getCurrentVersionNotes(mContext);
				((TextView) mParentView.findViewById(R.id.versionNotes)).setText(versionNotes);

				mParentView.findViewById(R.id.nextButton).setOnClickListener(mOnNextClickListener);
				mParentView.findViewById(R.id.nextButton).setEnabled(false);
			}
		}

		public void setComplete() {
			if (mUpdateView != null) {
				((TextView) mParentView.findViewById(android.R.id.title)).setText(R.string.updating_complete);
				mParentView.findViewById(android.R.id.progress).setVisibility(View.GONE);
				mParentView.findViewById(R.id.nextButton).setEnabled(true);
			}
		}

		public void hide() {
			View view;
			if ((view = mParentView.findViewById(R.id.setupView)) != null) {
				view.setVisibility(View.GONE);
			}
		}

	}
}
