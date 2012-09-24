package net.naonedbus.fragment;

import java.io.IOException;

import net.naonedbus.R;
import net.naonedbus.bean.async.AsyncResult;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

public abstract class CustomListFragment extends ListFragment implements LoaderCallbacks<AsyncResult<ListAdapter>> {

	private static final int LOADER_INIT = 0;
	private static final int LOADER_REFRESH = 1;

	private static final String STATE_POSITION = "position";
	private static final String STATE_TOP = "top";

	private int messageEmptyTitleId = R.string.error_title_empty;
	private int messageEmptySummaryId = R.string.error_summary_empty;
	private int messageEmptyDrawableId = R.drawable.sad_face;

	private int mListViewStatePosition;
	private int mListViewStateTop;

	public CustomListFragment(final int titleId, final int layoutId) {
		super(titleId, layoutId);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setRetainInstance(true);
		if (getListAdapter() == null) {
			getLoaderManager().initLoader(LOADER_INIT, null, this);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (container == null) // must put this in
			return null;

		if (savedInstanceState != null) {
			mListViewStatePosition = savedInstanceState.getInt(STATE_POSITION, -1);
			mListViewStateTop = savedInstanceState.getInt(STATE_TOP, 0);
		} else {
			mListViewStatePosition = -1;
			mListViewStateTop = 0;
		}

		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (isAdded()) {
			View v = getListView().getChildAt(0);
			int top = (v == null) ? 0 : v.getTop();
			outState.putInt(STATE_POSITION, getListView().getFirstVisiblePosition());
			outState.putInt(STATE_TOP, top);
		}
		super.onSaveInstanceState(outState);
	}

	public void refreshContent() {
		getLoaderManager().restartLoader(LOADER_REFRESH, null, this);
	}

	public void cancelLoading() {
		getLoaderManager().destroyLoader(LOADER_INIT);
		getLoaderManager().destroyLoader(LOADER_REFRESH);
	}

	/**
	 * Charger le contenu en background.
	 * 
	 * @return AsyncResult du resultat.
	 */
	protected abstract AsyncResult<ListAdapter> loadContent(final Context context);

	/**
	 * Après le chargement.
	 */
	protected void onPostExecute() {
	}

	@Override
	public Loader<AsyncResult<ListAdapter>> onCreateLoader(int arg0, Bundle arg1) {
		final Loader<AsyncResult<ListAdapter>> loader = new AsyncTaskLoader<AsyncResult<ListAdapter>>(getActivity()) {
			@Override
			public AsyncResult<ListAdapter> loadInBackground() {
				return loadContent(getActivity());
			}
		};
		showLoader();
		loader.forceLoad();

		return loader;
	}

	@Override
	public void onLoadFinished(Loader<AsyncResult<ListAdapter>> loader, AsyncResult<ListAdapter> result) {

		if (result == null) {
			showMessage(messageEmptyTitleId, messageEmptySummaryId, messageEmptyDrawableId);
			return;
		}

		final Exception exception = result.getException();

		if (exception == null) {
			if (result.getResult() == null || result.getResult().getCount() == 0) {
				showMessage(messageEmptyTitleId, messageEmptySummaryId, messageEmptyDrawableId);
			} else {
				setListAdapter(result.getResult());
				if (mListViewStatePosition != -1 && isAdded()) {
					getListView().setSelectionFromTop(mListViewStatePosition, mListViewStateTop);
					mListViewStatePosition = -1;
				}
				showContent();
				resetNextUpdate();
			}
		} else {
			Log.e(getClass().getSimpleName(), "Erreur de chargement.", exception);

			// Erreur réseau ou interne ?
			if (exception instanceof IOException) {
				showMessage(R.string.error_title_network, R.string.error_summary_network, R.drawable.orage);
			} else {
				showError(R.string.error_title, R.string.error_summary);
			}
		}

		onPostExecute();
	}

	@Override
	public void onLoaderReset(Loader<AsyncResult<ListAdapter>> arg0) {

	}

}
