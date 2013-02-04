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
import net.naonedbus.bean.Ligne;
import net.naonedbus.fragment.CustomFragment;
import net.naonedbus.manager.impl.LigneManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;
import com.bugsense.trace.BugSenseHandler;
import com.polites.android.GestureImageView;

public class PlanFragment extends CustomFragment {
	public static final String PARAM_CODE_LIGNE = "codeLigne";

	private static final String FILE_EXT = ".png";
	private static final String URL = "http://naonedbus.romain-guefveneu.net/plans2/{ligne}" + FILE_EXT;

	private GestureImageView mGestureView;
	private ProgressBar mProgressBar;
	private LigneManager mLigneManager;
	private Ligne mLigne;
	private boolean mSaveToCache;

	public PlanFragment() {
		super(R.string.title_fragment_plan, R.layout.fragment_plan);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final Context context = getActivity();
		final View view = getView();

		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		mSaveToCache = preferences.getBoolean(NBApplication.PREF_PLAN_CACHE, true);

		final String codeLigne = getArguments().getString(PARAM_CODE_LIGNE);
		mLigneManager = LigneManager.getInstance();
		mLigne = mLigneManager.getSingle(context.getContentResolver(), codeLigne);

		mGestureView = (GestureImageView) view.findViewById(R.id.planView);
		mProgressBar = (ProgressBar) view.findViewById(android.R.id.progress);

		if (isInCache(mLigne.lettre)) {
			afterLoaded();
		} else {
			try {
				save(mLigne.lettre);
			} catch (MalformedURLException e) {
				onError(e);
			} catch (IOException e) {
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
		mGestureView.setImageURI(Uri.parse(getLocalPath(mLigne.code)));
		if (mSaveToCache == false) {
			deleteFile(mLigne.code);
		}
	}

	/**
	 * Enregistrer le plan dans le cache
	 * 
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	private void save(String codeLigne) throws MalformedURLException, IOException {
		final DownloadFile downloadFile = new DownloadFile();
		downloadFile.execute(codeLigne);
	}

	/**
	 * Indique si le plan est présent dans le cache
	 * 
	 * @return vrai|faux si le plan est dans le cache
	 */
	private boolean isInCache(final String codeLigne) {
		final File cache = new File(getLocalPath(codeLigne));
		return cache.exists();
	}

	/**
	 * Supprimer le fichier.
	 * 
	 * @param codeLigne
	 */
	private void deleteFile(final String codeLigne) {
		final File cache = new File(getLocalPath(codeLigne));
		cache.delete();
	}

	/**
	 * @param codeLigne
	 * @return L'adresse du fichier dans le cache
	 */

	private String getLocalPath(String codeLigne) {
		return getActivity().getCacheDir() + "/" + codeLigne + FILE_EXT;
	}

	/**
	 * Construit l'url du plan
	 * 
	 * @param codeLigne
	 *            : code de la ligne
	 * @return l'url du plan
	 */
	private String getRemoteUrl(String codeLigne) {
		return URL.replace("{ligne}", codeLigne);
	}

	/**
	 * Gérer les erreurs qui surviennent et quitter l'activity
	 * 
	 * @param e
	 *            L'exception survenue
	 */
	private void onError(Exception e) {
		Toast.makeText(getActivity(), this.getString(R.string.msg_plan_not_found), Toast.LENGTH_SHORT).show();
		getActivity().finish();
	}

	/**
	 * Télécharger l'image
	 * 
	 * @author Romain
	 */
	private class DownloadFile extends AsyncTask<String, Integer, Boolean> {

		@Override
		protected Boolean doInBackground(String... codeLigne) {
			int count;
			long total = 0;
			int lenghtOfFile;
			byte data[] = new byte[1024];

			try {
				final URL localUrl = new URL(getRemoteUrl(codeLigne[0]));
				final URLConnection connexion = localUrl.openConnection();
				connexion.connect();
				lenghtOfFile = connexion.getContentLength();

				final File file = new File(getActivity().getCacheDir(), codeLigne[0] + FILE_EXT);
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
			} catch (Exception e) {
				BugSenseHandler.sendExceptionMessage("Erreur lors de la récupération du plan.", null, e);
				return false;
			}
			return true;
		}

		@Override
		public void onProgressUpdate(Integer... args) {
			mProgressBar.setIndeterminate(false);
			mProgressBar.setProgress(args[0]);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result) {
				afterLoaded();
			} else {
				onError(null);
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return false;
	}

	@Override
	protected void bindView(View view, Bundle savedInstanceState) {

	}
}
