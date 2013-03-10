package net.naonedbus.fragment.impl;

import java.util.List;

import net.naonedbus.BuildConfig;
import net.naonedbus.R;
import net.naonedbus.activity.impl.CommentaireDetailActivity;
import net.naonedbus.bean.Commentaire;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.formatter.CommentaireFomatter;
import net.naonedbus.fragment.CustomFragmentActions;
import net.naonedbus.fragment.CustomListFragment;
import net.naonedbus.manager.impl.CommentaireManager;
import net.naonedbus.widget.adapter.impl.CommentaireArrayAdapter;
import net.naonedbus.widget.indexer.impl.CommentaireIndexer;

import org.joda.time.DateTime;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class CommentairesFragment extends CustomListFragment implements CustomFragmentActions {

	public static final String PARAM_CODE_LIGNE = "codeLigne";
	public static final String PARAM_CODE_SENS = "codeSens";
	public static final String PARAM_CODE_ARRET = "codeArret";

	private static final String LOG_TAG = "CommentairesFragment";
	private static final boolean DBG = BuildConfig.DEBUG;

	private MenuItem mRefreshMenuItem;
	private String mCodeLigne;
	private String mCodeSens;
	private String mCodeArret;

	public CommentairesFragment() {
		super(R.string.title_fragment_en_direct, R.layout.fragment_listview_box);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		if (DBG)
			Log.d(LOG_TAG, "onCreate");
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (DBG)
			Log.d(LOG_TAG, "onActivityCreated");

		if (getArguments() != null) {
			mCodeLigne = getArguments().getString(PARAM_CODE_LIGNE);
			mCodeSens = getArguments().getString(PARAM_CODE_SENS);
			mCodeArret = getArguments().getString(PARAM_CODE_ARRET);
		}

		new LoadTimeLineCache().execute(mCodeLigne, mCodeSens, mCodeArret);
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		if (DBG)
			Log.d(LOG_TAG, "onCreateOptionsMenu");
		mRefreshMenuItem = menu.findItem(R.id.menu_refresh);

		if (getLoaderManager().hasRunningLoaders())
			showResfrehMenuLoader();

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_refresh:
			refreshContent();
			break;
		}
		return true;
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
		final Commentaire commentaire = (Commentaire) l.getItemAtPosition(position);
		final Intent intent = new Intent(getActivity(), CommentaireDetailActivity.class);
		intent.putExtra(CommentaireDetailActivity.PARAM_COMMENTAIRE, (Parcelable) commentaire);
		startActivity(intent);
	}

	private void showResfrehMenuLoader() {
		if (mRefreshMenuItem != null) {
			mRefreshMenuItem.setActionView(R.layout.action_item_refresh);
		}
	}

	private void hideResfrehMenuLoader() {
		if (mRefreshMenuItem != null) {
			mRefreshMenuItem.setActionView(null);
		}
	}

	@Override
	protected void onPreExecute() {
		if (DBG)
			Log.d(LOG_TAG, "onPreExecute");
		if (getListAdapter() == null) {
			showLoader();
		}
		showResfrehMenuLoader();
	}

	@Override
	protected void onPostExecute() {
		if (DBG)
			Log.d(LOG_TAG, "onPostExecute");
		hideResfrehMenuLoader();
	}

	@Override
	public Loader<AsyncResult<ListAdapter>> onCreateLoader(final int arg0, final Bundle arg1) {
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
			final List<Commentaire> commentaires = manager.getFromWeb(context, mCodeLigne, mCodeSens, mCodeArret,
					new DateTime(0));

			final CommentaireFomatter fomatter = new CommentaireFomatter(context);
			final CommentaireArrayAdapter adapter = new CommentaireArrayAdapter(context);
			if (commentaires != null) {
				fomatter.appendToAdapter(adapter, commentaires);
			}
			adapter.setIndexer(new CommentaireIndexer());
			result.setResult(adapter);
		} catch (final Exception e) {
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
			showLoader();
			showResfrehMenuLoader();
		}

		@Override
		protected CommentaireArrayAdapter doInBackground(final String... codes) {
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

		private void setTweetId(final List<Commentaire> commentaires) {
			int id = -1;
			for (final Commentaire commentaire : commentaires) {
				if (commentaire.getId() == null) {
					commentaire.setId(id--);
				}
			}
		}

		@Override
		protected void onPostExecute(final CommentaireArrayAdapter result) {
			super.onPostExecute(result);

			if (DBG)
				Log.d(LOG_TAG, "Cache chargé : " + result.getCount());

			if (result.getCount() > 0) {
				setListAdapter(result);
				showContent();
			}

			// Démarrer la récupérer depuis le web
			refreshContent();

		}
	}

}
