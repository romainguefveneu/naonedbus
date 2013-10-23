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
package net.naonedbus.fragment.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.bean.Route;
import net.naonedbus.fragment.CustomFragment;
import net.naonedbus.manager.impl.RouteManager;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;
import com.polites.android.GestureImageView;

public class PlanFragment extends CustomFragment {
	public static final String PARAM_CODE_LIGNE = "routeCode";

	private static final String FILE_EXT = ".png";
	private static final String URL = "http://naonedbus.romain-guefveneu.net/plans/{route}" + FILE_EXT;

	private GestureImageView mGestureView;
	private ProgressBar mProgressBar;
	private RouteManager mRouteManager;
	private Route mRoute;
	private boolean mSaveToCache;

	public PlanFragment() {
		super(R.layout.fragment_plan);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final Context context = getActivity();
		final View view = getView();

		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		mSaveToCache = preferences.getBoolean(NBApplication.PREF_PLAN_CACHE, true);

		final String routeCode = getArguments().getString(PARAM_CODE_LIGNE);
		mRouteManager = RouteManager.getInstance();
		mRoute = mRouteManager.getSingle(context.getContentResolver(), routeCode);

		mGestureView = (GestureImageView) view.findViewById(R.id.planView);
		mProgressBar = (ProgressBar) view.findViewById(android.R.id.progress);

		if (isInCache(mRoute.getLetter())) {
			afterLoaded();
		} else {
			try {
				save(mRoute.getLetter());
			} catch (final MalformedURLException e) {
				onError(e);
			} catch (final IOException e) {
				onError(e);
			}
		}
	}

	/**
	 * Afficher le plan en cache
	 */
	private void afterLoaded() {
		mProgressBar.setVisibility(View.GONE);
		mGestureView.setVisibility(View.VISIBLE);
		mGestureView.setImageURI(Uri.parse(getLocalPath(mRoute.getCode())));
		if (mSaveToCache == false) {
			deleteFile(mRoute.getCode());
		}
	}

	/**
	 * Enregistrer le plan dans le cache
	 * 
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	private void save(final String routeCode) throws MalformedURLException, IOException {
		final DownloadFile downloadFile = new DownloadFile();
		downloadFile.execute(routeCode);
	}

	/**
	 * Indique si le plan est présent dans le cache
	 * 
	 * @return vrai|faux si le plan est dans le cache
	 */
	private boolean isInCache(final String routeCode) {
		final File cache = new File(getLocalPath(routeCode));
		return cache.exists();
	}

	/**
	 * Supprimer le fichier.
	 * 
	 * @param routeCode
	 */
	private void deleteFile(final String routeCode) {
		final File cache = new File(getLocalPath(routeCode));
		cache.delete();
	}

	/**
	 * @param routeCode
	 * @return L'adresse du fichier dans le cache
	 */

	private String getLocalPath(final String routeCode) {
		return getActivity().getCacheDir() + "/" + routeCode + FILE_EXT;
	}

	/**
	 * Construit l'url du plan
	 * 
	 * @param routeCode
	 *            : code de la route
	 * @return l'url du plan
	 */
	private String getRemoteUrl(final String routeCode) {
		return URL.replace("{route}", routeCode);
	}

	/**
	 * Gérer les erreurs qui surviennent et quitter l'activity
	 * 
	 * @param e
	 *            L'exception survenue
	 */
	private void onError(final Exception e) {
		final Activity activity = getActivity();
		if (isAdded() && isVisible() && activity != null) {
			Toast.makeText(activity, this.getString(R.string.plan_not_found), Toast.LENGTH_SHORT).show();
			activity.finish();
		}
	}

	/**
	 * Télécharger l'image
	 * 
	 * @author Romain
	 */
	private class DownloadFile extends AsyncTask<String, Integer, Boolean> {

		@Override
		protected Boolean doInBackground(final String... routeCode) {
			int count;
			long total = 0;
			int lenghtOfFile;
			final byte data[] = new byte[1024];

			try {
				final URL localUrl = new URL(getRemoteUrl(routeCode[0]));
				final URLConnection connexion = localUrl.openConnection();
				connexion.connect();
				lenghtOfFile = connexion.getContentLength();

				final File file = new File(getActivity().getCacheDir(), routeCode[0] + FILE_EXT);
				final InputStream input = new BufferedInputStream(localUrl.openStream());
				final OutputStream output = new FileOutputStream(file);

				while ((count = input.read(data)) != -1) {
					total += count;
					publishProgress((int) (total * 100 / lenghtOfFile));
					output.write(data, 0, count);
				}

				output.flush();
				output.close();
				input.close();
			} catch (final Exception e) {
				BugSenseHandler.sendExceptionMessage("Erreur lors de la récupération du plan.", null, e);
				return false;
			}
			return true;
		}

		@Override
		public void onProgressUpdate(final Integer... args) {
			mProgressBar.setIndeterminate(false);
			mProgressBar.setProgress(args[0]);
		}

		@Override
		protected void onPostExecute(final Boolean result) {
			if (getActivity() != null && isVisible()) {
				if (result) {
					afterLoaded();
				} else {
					onError(null);
				}
			}
		}
	}

	@Override
	protected void bindView(final View view, final Bundle savedInstanceState) {

	}
}
