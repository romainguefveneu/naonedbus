/**
 *  Copyright (C) 2011 Romain Guefveneu
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
package net.naonedbus.activity.widgetconfigure;

import java.util.ArrayList;
import java.util.List;

import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.appwidget.HoraireWidgetProvider;
import net.naonedbus.bean.Favori;
import net.naonedbus.manager.impl.FavoriManager;
import net.naonedbus.widget.adapter.impl.FavoriArrayAdapter;
import android.app.ListActivity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

/**
 * @author romain.guefveneu
 * 
 */
public abstract class WidgetConfigureActivity extends ListActivity {

	private static final String PREFS_NAME = "net.naonedbus.activity.WidgetProvider";
	private static final String PREF_PREFIX_KEY = "widgetFavoriId";

	private FavoriManager favoriManager;
	private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	private LoadFavoris loadFavoris = null;

	public WidgetConfigureActivity() {
		favoriManager = FavoriManager.getInstance();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// If the user closes window, don't create the widget
		setResult(RESULT_CANCELED);

		// Find widget id from launching intent
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		// If they gave us an intent without the widget id, just bail.
		if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			finish();
		}

		doLoadFavoris();
	}

	/**
	 * Configures the created widget
	 * 
	 * @param context
	 */
	public void configureWidget(Context context) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		getWidgetProvider().updateAppWidget(context, appWidgetManager, appWidgetId);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		// Récupérer l'id du favori sélectionné
		Favori item = (Favori) l.getItemAtPosition(position);

		// Stocker dans les préférences l'id du favori associé à l'id du widget
		SharedPreferences.Editor prefs = this.getSharedPreferences(PREFS_NAME, 0).edit();
		prefs.putInt(PREF_PREFIX_KEY + appWidgetId, item._id);
		prefs.commit();

		// Valider la création du widget
		Intent resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		setResult(RESULT_OK, resultValue);

		configureWidget(getApplicationContext());

		finish();

	}

	/**
	 * Lancer le chargement des favoris
	 */
	protected void doLoadFavoris() {
		if (loadFavoris == null || loadFavoris.getStatus().equals(AsyncTask.Status.FINISHED)) {
			loadFavoris = (LoadFavoris) new LoadFavoris().execute();
		}
	}

	/**
	 * Classe de chargement des favoris
	 * 
	 * @author romain.guefveneu
	 * 
	 */
	private class LoadFavoris extends AsyncTask<Void, Void, FavoriArrayAdapter> {

		@Override
		protected FavoriArrayAdapter doInBackground(Void... params) {
			final List<Favori> items = new ArrayList<Favori>();
			final Context context = getApplicationContext();
			context.setTheme(R.style.Theme_Acapulco_Dark);
			final FavoriArrayAdapter adapter = new FavoriArrayAdapter(context, items);

			items.addAll(favoriManager.getAll(getApplicationContext().getContentResolver(), null, null));

			if (items.isEmpty()) {
			} else {
				for (Favori favoriItem : items) {
					favoriItem.delay = ""; // Cacher le loader
				}
			}

			return adapter;
		}

		@Override
		protected void onPostExecute(FavoriArrayAdapter result) {
			super.onPostExecute(result);
			setListAdapter(result);
		}

	}

	/**
	 * Récupérer l'id d'un favori à partir d'un id de widget
	 * 
	 * @param context
	 * @param appWidgetId
	 * @return
	 */
	public static int getFavoriIdFromWidget(Context context, int appWidgetId) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		int id = prefs.getInt(PREF_PREFIX_KEY + appWidgetId, -1);
		return id;
	}

	/**
	 * Supprimer la configuration d'un widget
	 * 
	 * @param context
	 * @param appWidgetId
	 */
	public static void removeWidgetId(Context context, int appWidgetId) {
		Log.d(NBApplication.LOG_TAG, "removeWidgetId(" + appWidgetId + ")");
		SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
		prefs.remove(PREF_PREFIX_KEY + appWidgetId);
		prefs.commit();
	}

	/**
	 * Doit retourner le bon widgetprovider
	 * 
	 * @return
	 */
	protected abstract HoraireWidgetProvider getWidgetProvider();
}
