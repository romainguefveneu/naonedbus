package net.naonedbus.activity.impl;

import net.naonedbus.R;
import net.naonedbus.activity.RootActivity;
import net.naonedbus.fragment.impl.FavorisFragment;
import net.naonedbus.fragment.impl.LignesFragment;
import net.naonedbus.fragment.impl.ProximiteFragment;
import net.naonedbus.manager.impl.UpdaterManager;
import net.naonedbus.provider.CustomContentProvider;
import net.naonedbus.provider.DatabaseActionListener;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;

public class MainActivity extends RootActivity {

	private static int[] titles = new int[] { R.string.title_fragment_lignes, R.string.title_fragment_favoris,
			R.string.title_fragment_proximite };

	private static Class<?>[] classes = new Class<?>[] { LignesFragment.class, FavorisFragment.class,
			ProximiteFragment.class };

	private ProgressDialog progressDialog;

	private DatabaseActionListener listener = new DatabaseActionListener() {

		@Override
		public void onUpgrade() {
			MainActivity.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					progressDialog = new ProgressDialog(MainActivity.this);
					progressDialog.setMessage("Mise à jour en cours...");
					progressDialog.show();
				}
			});
		}

		@Override
		public void onCreate() {
			MainActivity.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					progressDialog = new ProgressDialog(MainActivity.this);
					progressDialog.setMessage("Installation en cours...");
					progressDialog.show();
				}
			});
		}
	};

	public MainActivity() {
		super(R.layout.activity_main);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.title_activity_main);
		if (savedInstanceState == null) {
			new UpdateAndCleanTask().execute();
		}
	}

	/**
	 * Effectuer les actions avant de déclencher la mise à jour.
	 */
	private void beforeUpdate() {

	}

	/**
	 * Effectuer les actions après l'éventuelle mise à jour.
	 */
	private void afterUpdate() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
		addFragments(titles, classes);
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
			CustomContentProvider.setDatabaseActionListener(listener);

			// Déclencher une éventuelle mise à jour
			final UpdaterManager updaterManager = UpdaterManager.getInstance();
			updaterManager.triggerUpdate(getContentResolver());

			// TODO: Vider les anciens horaires
			// final HoraireManager horaireManager =
			// HoraireManager.getInstance();
			// horaireManager.clearOldHoraires(getContentResolver());

			CustomContentProvider.setDatabaseActionListener(null);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			afterUpdate();
		}

	}

}