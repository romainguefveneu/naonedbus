package net.naonedbus.activity.impl;

import net.naonedbus.R;
import net.naonedbus.activity.SlidingMenuActivity;
import net.naonedbus.fragment.impl.FavorisFragment;
import net.naonedbus.fragment.impl.LignesFragment;
import net.naonedbus.fragment.impl.ProximiteFragment;
import net.naonedbus.manager.impl.FavoriManager;
import net.naonedbus.manager.impl.HoraireManager;
import net.naonedbus.manager.impl.UpdaterManager;
import net.naonedbus.provider.CustomContentProvider;
import net.naonedbus.provider.DatabaseActionListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends SlidingMenuActivity {

	private static int[] titles = new int[] { R.string.title_fragment_lignes, R.string.title_fragment_favoris,
			R.string.title_fragment_proximite };

	private static Class<?>[] classes = new Class<?>[] { LignesFragment.class, FavorisFragment.class,
			ProximiteFragment.class };

	private boolean mHasSetup = false;
	private boolean mContentLoaded = false;
	private boolean mIsFrontActivity = false;
	private boolean mFirstLaunch = false;

	private DatabaseActionListener mListener = new DatabaseActionListener() {

		@Override
		public void onUpgrade(final int oldVersion) {
			MainActivity.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mFirstLaunch = true;
					showSetupView(R.string.updating);
					if (oldVersion < 10)
						startActivity(new Intent(MainActivity.this, TutorialActivity.class));
				}
			});
		}

		@Override
		public void onCreate() {
			MainActivity.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mFirstLaunch = true;
					showSetupView(R.string.setup);
					startActivity(new Intent(MainActivity.this, TutorialActivity.class));
				}
			});
		}
	};

	public MainActivity() {
		super(R.layout.activity_main);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTitle(R.string.title_activity_main);
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			new UpdateAndCleanTask().execute();
		}
		addDelayedFragments(titles, classes);
	}

	@Override
	protected void onResume() {
		mIsFrontActivity = true;
		if (mHasSetup && mContentLoaded == false) {
			hideSetupView();
			loadContent();
		}
		super.onResume();

	}

	@Override
	protected void onStop() {
		mIsFrontActivity = false;
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
		}
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	private void showSetupView(int textResId) {
		findViewById(R.id.setupViewStub).setVisibility(View.VISIBLE);
		((TextView) findViewById(R.id.setupViewLabel)).setText(textResId);
	}

	private void hideSetupView() {
		View view;
		if ((view = findViewById(R.id.setupView)) != null) {
			view.setVisibility(View.GONE);
		}
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
		protected Void doInBackground(Void... params) {
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
		protected void onPostExecute(Void result) {
			afterUpdate();
		}

	}

}
