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
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.bugsense.trace.BugSenseHandler;

public class PlanFragment extends CustomFragment {
	public static final String PARAM_CODE_LIGNE = "codeLigne";

	private static final String FILE_EXT = ".jpg";
	private static final String URL = "http://naonedbus.romain-guefveneu.net/plans/L{ligne}" + FILE_EXT;

	private WebView mWebView;
	private ProgressBar mProgressBar;
	private LinearLayout mLoaderView;
	private LigneManager mLigneManager;
	private Ligne mLigne;

	public PlanFragment() {
		super(R.string.title_fragment_plan, R.layout.fragment_plan);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final Context context = getActivity();
		final View view = getView();

		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		final Boolean planCache = preferences.getBoolean(NBApplication.PREF_PLAN_CACHE, true);

		final String codeLigne = getArguments().getString(PARAM_CODE_LIGNE);
		mLigneManager = LigneManager.getInstance();
		mLigne = mLigneManager.getSingle(context.getContentResolver(), codeLigne);

		mWebView = (WebView) view.findViewById(R.id.webView);
		mProgressBar = (ProgressBar) view.findViewById(android.R.id.progress);
		mLoaderView = (LinearLayout) view.findViewById(R.id.loader);

		mWebView.getSettings().setBuiltInZoomControls(true);

		if (isInCache(mLigne.lettre)) {
			afterLoaded();
		} else {

			if (planCache) {
				try {
					save(mLigne.lettre);
				} catch (MalformedURLException e) {
					onError(e);
				} catch (IOException e) {
					onError(e);
				}
			} else {
				loadFromWeb(mLigne.lettre);
			}

		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu) {

	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return false;
	}

	@Override
	protected void bindView(View view, Bundle savedInstanceState) {

	}

	/**
	 * Afficher le plan en cache
	 */
	private void afterLoaded() {
		mWebView.loadUrl(getLocalUrl(mLigne.lettre));
		mProgressBar.setVisibility(View.GONE);
		mWebView.setVisibility(View.VISIBLE);
	}

	/**
	 * Enregistrer le plan dans le cache
	 * 
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	private void save(String codeLigne) throws MalformedURLException, IOException {
		DownloadFile downloadFile = new DownloadFile();
		downloadFile.execute(codeLigne);
	}

	private void loadFromWeb(String codeLigne) {
		mWebView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				mProgressBar.setProgress(newProgress);
				if (newProgress == 100) {
					mLoaderView.setVisibility(View.GONE);
					mWebView.setVisibility(View.VISIBLE);
				}
			}
		});

		mWebView.loadUrl(getRemoteUrl(codeLigne));
	}

	/**
	 * Indique si le plan est présent dans le cache
	 * 
	 * @return vrai|faux si le plan est dans le cache
	 */
	private boolean isInCache(final String codeLigne) {
		File cache = new File(getLocalPath(codeLigne));
		return cache.exists();
	}

	/**
	 * @param codeLigne
	 * @return L'url du plan dans le cache
	 */
	private String getLocalUrl(String codeLigne) {
		return "file://" + getLocalPath(codeLigne);
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
}
