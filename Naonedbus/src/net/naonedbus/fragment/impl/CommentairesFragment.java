package net.naonedbus.fragment.impl;

import java.util.List;

import net.naonedbus.BuildConfig;
import net.naonedbus.R;
import net.naonedbus.activity.impl.CommentaireDetailActivity;
import net.naonedbus.bean.Commentaire;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.formatter.CommentaireFomatter;
import net.naonedbus.fragment.CustomListFragment;
import net.naonedbus.intent.ParamIntent;
import net.naonedbus.manager.impl.CommentaireManager;
import net.naonedbus.widget.adapter.impl.CommentaireArrayAdapter;
import net.naonedbus.widget.indexer.impl.CommentaireIndexer;

import org.joda.time.DateTime;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class CommentairesFragment extends CustomListFragment {

	private static final String LOG_TAG = "HorairesFragment";
	private static final boolean DBG = BuildConfig.DEBUG;

	private LoadTimeLineCache mLoaderCache;

	public CommentairesFragment() {
		super(R.string.title_fragment_en_direct, R.layout.fragment_listview_box);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public void onStart() {
		super.onStart();
		mLoaderCache = (LoadTimeLineCache) new LoadTimeLineCache().execute();
		loadContent();
	}

	@Override
	public void onDestroy() {
		mLoaderCache = null;
		super.onDestroy();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu) {
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_refresh:
			refreshContent();
			break;
		}
		return true;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		final Commentaire commentaire = (Commentaire) l.getItemAtPosition(position);
		final ParamIntent intent = new ParamIntent(getActivity(), CommentaireDetailActivity.class);
		intent.putExtraSerializable(CommentaireDetailActivity.Param.commentaire, commentaire);
		startActivity(intent);
	}

	@Override
	protected void onPreExecute() {
		getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
	}

	@Override
	protected void onPostExecute() {
		getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
	}

	@Override
	public Loader<AsyncResult<ListAdapter>> onCreateLoader(int arg0, Bundle arg1) {
		final Loader<AsyncResult<ListAdapter>> loader = new AsyncTaskLoader<AsyncResult<ListAdapter>>(getActivity()) {
			@Override
			public AsyncResult<ListAdapter> loadInBackground() {
				return loadContent(getActivity());
			}
		};

		onPreExecute();
		loader.forceLoad();

		return loader;
	}

	@Override
	protected AsyncResult<ListAdapter> loadContent(final Context context) {
		final AsyncResult<ListAdapter> result = new AsyncResult<ListAdapter>();
		if (DBG)
			Log.d(LOG_TAG, "Chargement depuis le web ...");

		try {
			final CommentaireManager manager = CommentaireManager.getInstance();
			final List<Commentaire> infoTraficLignes = manager.getFromWeb(context, null, null, null, new DateTime(0));

			final CommentaireFomatter fomatter = new CommentaireFomatter(context);
			final CommentaireArrayAdapter adapter = new CommentaireArrayAdapter(context);
			if (infoTraficLignes != null) {
				fomatter.appendToAdapter(adapter, infoTraficLignes);
			}
			adapter.setIndexer(new CommentaireIndexer());
			result.setResult(adapter);
		} catch (Exception e) {
			result.setException(e);
		}

		if (DBG)
			Log.d(LOG_TAG, "Chargé depuis le web.");

		return result;
	}

	/**
	 * Classe de chargement du cache des commentaires.
	 */
	private class LoadTimeLineCache extends AsyncTask<String, ListAdapter, CommentaireArrayAdapter> {

		public static final int PARAM_CODE_LIGNE = 0;
		public static final int PARAM_CODE_SENS = 1;
		public static final int PARAM_CODE_ARRET = 2;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (DBG)
				Log.d(LOG_TAG, "Chargement du cache...");
			getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
			showLoader();
		}

		@Override
		protected CommentaireArrayAdapter doInBackground(String... codes) {
			final CommentaireManager manager = CommentaireManager.getInstance();
			final Context context = getActivity();
			final String codeLigne = codes.length > PARAM_CODE_LIGNE ? codes[PARAM_CODE_LIGNE] : null;
			final String codeSens = codes.length > PARAM_CODE_SENS ? codes[PARAM_CODE_SENS] : null;
			final String codeArret = codes.length > PARAM_CODE_ARRET ? codes[PARAM_CODE_ARRET] : null;

			// Charger le cache
			final List<Commentaire> commentaires = manager.getFromCache(context, codeLigne, codeSens, codeArret);

			final CommentaireFomatter fomatter = new CommentaireFomatter(context);
			final CommentaireArrayAdapter adapter = new CommentaireArrayAdapter(context);
			if (commentaires != null) {
				setTweetId(commentaires);
				fomatter.appendToAdapter(adapter, commentaires);
			}
			adapter.setIndexer(new CommentaireIndexer());

			return adapter;
		}

		private void setTweetId(List<Commentaire> commentaires) {
			int id = -1;
			for (Commentaire commentaire : commentaires) {
				if (commentaire.getId() == null) {
					commentaire.setId(id--);
				}
			}
		}

		@Override
		protected void onPostExecute(CommentaireArrayAdapter result) {
			super.onPostExecute(result);

			if (DBG)
				Log.d(LOG_TAG, "Cache chargé : " + result.getCount());

			if (result.getCount() > 0) {
				setListAdapter(result);
				showContent();

				// // Le chargement depuis le web est en cours ?
				// if (loader != null && loader.getStatus() !=
				// AsyncTask.Status.FINISHED) {
				// addLoadingItem(adapter);
				// }

				// setListAdapter(adapter);
			}
		}
	}

}
