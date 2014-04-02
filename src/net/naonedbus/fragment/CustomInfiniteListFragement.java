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
package net.naonedbus.fragment;

import java.io.IOException;

import net.naonedbus.R;
import net.naonedbus.bean.async.AsyncResult;

import org.json.JSONException;

import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public abstract class CustomInfiniteListFragement extends CustomListFragment {

	private Crouton mLastCrouton;
	private boolean mHasError = false;
	private View mLoaderView;

	public CustomInfiniteListFragement(final int layoutId) {
		super(layoutId);
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		final ListView listView = getListView();
		mLoaderView = LayoutInflater.from(getActivity()).inflate(R.layout.list_item_loader, null);
		mLoaderView.setEnabled(false);
		mLoaderView.setClickable(false);
		mLoaderView.setFocusable(false);
		mLoaderView.setFocusableInTouchMode(false);

		addOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(final AbsListView view, final int scrollState) {
			}

			@Override
			public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount,
					final int totalItemCount) {
				final int lastInScreen = firstVisibleItem + visibleItemCount;
				if ((lastInScreen == totalItemCount) && listView.getAdapter() != null && !mHasError) {
					onLoadMoreItems();
				} else if (lastInScreen < totalItemCount) {
					mHasError = false;
					mLoaderView.setVisibility(View.VISIBLE);
				}
			}

		});

		listView.addFooterView(mLoaderView);
	}

	protected abstract void onLoadMoreItems();

	@Override
	public Loader<AsyncResult<ListAdapter>> onCreateLoader(final int arg0, final Bundle bundle) {
		final Loader<AsyncResult<ListAdapter>> loader = new AsyncTaskLoader<AsyncResult<ListAdapter>>(getActivity()) {
			@Override
			public AsyncResult<ListAdapter> loadInBackground() {
				return loadContent(getActivity(), bundle);
			}
		};

		if (getListAdapter() == null || getListAdapter().getCount() == 0)
			showLoader();
		loader.forceLoad();

		return loader;
	}

	@Override
	public void onLoadFinished(final Loader<AsyncResult<ListAdapter>> loader, final AsyncResult<ListAdapter> result) {

		if (result == null) {
			showMessage(mMessageEmptyTitleId, mMessageEmptySummaryId, mMessageEmptyDrawableId);
			return;
		}

		final Exception exception = result.getException();

		if (exception == null) {
			if (result.getResult() == null || result.getResult().getCount() == 0) {
				showMessage(mMessageEmptyTitleId, mMessageEmptySummaryId, mMessageEmptyDrawableId);
			} else {
				if (getListAdapter() == null) {
					setListAdapter(result.getResult());
					showContent();
				}
				resetNextUpdate();
			}
		} else {
			Log.e(getClass().getSimpleName(), "Erreur de chargement.", exception);

			int titleRes = R.string.error_title;
			int messageRes = R.string.error_summary;
			int drawableRes = R.drawable.warning;

			// Erreur réseau ou interne ?
			if (exception instanceof IOException) {
				titleRes = R.string.error_title_network;
				messageRes = R.string.error_summary_network;
				drawableRes = R.drawable.orage;
			} else if (exception instanceof JSONException) {
				titleRes = R.string.error_title_webservice;
				messageRes = R.string.error_summary_webservice;
			}

			if (getListAdapter() == null || getListAdapter().isEmpty()) {
				showMessage(titleRes, messageRes, drawableRes);
			} else {
				if (mLastCrouton != null) {
					mLastCrouton.cancel();
				}
				mLastCrouton = Crouton.makeText(getActivity(), titleRes, Style.ALERT, (ViewGroup) getView());
				mLastCrouton.show();
			}

			mHasError = true;
			mLoaderView.setVisibility(View.GONE);
		}

		onPostExecute();
	}

}
