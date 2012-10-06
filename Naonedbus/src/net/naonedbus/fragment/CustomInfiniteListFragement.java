package net.naonedbus.fragment;

import java.io.IOException;

import net.naonedbus.R;
import net.naonedbus.bean.async.AsyncResult;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListAdapter;
import android.widget.ListView;

public abstract class CustomInfiniteListFragement extends CustomListFragment {

	public CustomInfiniteListFragement(int titleId, int layoutId) {
		super(titleId, layoutId);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		final ListView listView = getListView();
		final View loaderView = LayoutInflater.from(getActivity()).inflate(R.layout.list_item_loader, null);
		loaderView.setEnabled(false);
		loaderView.setClickable(false);
		loaderView.setFocusable(false);
		loaderView.setFocusableInTouchMode(false);

		addOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				int lastInScreen = firstVisibleItem + visibleItemCount;
				if ((lastInScreen == totalItemCount) && listView.getAdapter() != null) {
					onLoadMoreItems();
				}
			}

		});

		listView.addFooterView(loaderView);
	}

	protected abstract void onLoadMoreItems();

	@Override
	public Loader<AsyncResult<ListAdapter>> onCreateLoader(int arg0, Bundle arg1) {
		final Loader<AsyncResult<ListAdapter>> loader = new AsyncTaskLoader<AsyncResult<ListAdapter>>(getActivity()) {
			@Override
			public AsyncResult<ListAdapter> loadInBackground() {
				return loadContent(getActivity());
			}
		};
		if (getListAdapter() == null || getListAdapter().getCount() == 0)
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
				if (getListAdapter() == null) {
					setListAdapter(result.getResult());
					showContent();
				}
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

}
