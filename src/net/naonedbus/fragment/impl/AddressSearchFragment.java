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

import java.util.ArrayList;
import java.util.List;

import net.naonedbus.R;
import net.naonedbus.bean.AddressResult;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.fragment.AbstractListFragment;
import net.naonedbus.loader.AddressLoader;
import net.naonedbus.task.AddressResolverTask;
import net.naonedbus.task.AddressResolverTask.AddressTaskListener;
import net.naonedbus.widget.ModalSearchView;
import net.naonedbus.widget.ModalSearchView.OnQueryTextListener;
import net.naonedbus.widget.adapter.impl.AddressResultArrayAdapter;
import net.naonedbus.widget.indexer.impl.AddressResultArrayIndexer;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class AddressSearchFragment extends AbstractListFragment implements OnQueryTextListener,
		LoaderCallbacks<AsyncResult<List<AddressResult>>> {

	public static final String PARAM_QUERY = "query";

	private static final int LOADER_EQUIPEMENTS = 0;
	private static final int LOADER_FULL = 1;

	private AddressResultArrayAdapter mAdapter;
	private final ArrayList<AddressResult> mResults = new ArrayList<AddressResult>();
	private ModalSearchView mModalSearchView;

	private ProgressBar mProgressBar;
	private String mCurrentFilter;

	public AddressSearchFragment() {
		super(R.layout.fragment_address_search);
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final ActionBar actionBar = ((SherlockFragmentActivity) getActivity()).getSupportActionBar();

		mModalSearchView = (ModalSearchView) actionBar.getCustomView();
		mModalSearchView.setOnQueryTextListener(this);
		mModalSearchView.requestFocus();

		final String query = getArguments().getString(PARAM_QUERY);
		if (query != null) {
			mModalSearchView.setText(query);
			mModalSearchView.setSelected(true);
			mModalSearchView.selectAll();
		}
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final String[] types = getResources().getStringArray(R.array.types_equipements_recherche);
		final AddressResultArrayIndexer indexer = new AddressResultArrayIndexer(types);

		mAdapter = new AddressResultArrayAdapter(getActivity(), mResults);
		mAdapter.setIndexer(indexer);

		setListAdapter(mAdapter);

		final Bundle bundle = new Bundle();
		bundle.putBoolean(AddressLoader.PARAM_LOAD_ADDRESS, true);
		getLoaderManager().initLoader(LOADER_EQUIPEMENTS, bundle, this);
	}

	@Override
	protected void bindView(final View view, final Bundle savedInstanceState) {
		super.bindView(view, savedInstanceState);

		mProgressBar = (ProgressBar) view.findViewById(android.R.id.progress);
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
		final AddressResult item = mAdapter.getItem(position);

		final Intent data = new Intent();
		data.putExtra("address", item.getAddress());
		data.putExtra("latitude", item.getLatitude());
		data.putExtra("longitude", item.getLongitude());
		data.putExtra("type", item.getType());
		data.putExtra("isCurrentLocation", item.isCurrentLocation());

		final Activity activity = getActivity();

		activity.setResult(1, data);
		activity.finish();
	}

	@Override
	public Loader<AsyncResult<List<AddressResult>>> onCreateLoader(final int loaderId, final Bundle bundle) {
		return new AddressLoader(getActivity(), bundle);
	}

	@Override
	public void onLoadFinished(final Loader<AsyncResult<List<AddressResult>>> loader,
			final AsyncResult<List<AddressResult>> result) {

		mResults.clear();
		mResults.addAll(result.getResult());
		mAdapter.notifyDataSetChanged();

		if (mResults.size() == 0) {
			showMessage();
		} else {
			showContent();
		}

		getActivity().setProgressBarIndeterminateVisibility(false);

		if (loader.getId() == LOADER_EQUIPEMENTS) {
			final Bundle bundle = new Bundle();
			bundle.putBoolean(AddressLoader.PARAM_LOAD_ADDRESS, true);
			bundle.putString(AddressLoader.PARAM_FILTER, mCurrentFilter);
			getLoaderManager().restartLoader(LOADER_FULL, bundle, this);
		} else {
			mProgressBar.setVisibility(View.GONE);
			mProgressBar.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out));
		}
	}

	@Override
	public void onLoaderReset(final Loader<AsyncResult<List<AddressResult>>> loader) {

	}

	@Override
	public void onQueryTextChange(final String newText) {
		mCurrentFilter = newText;

		final Bundle bundle = new Bundle();
		bundle.putString(AddressLoader.PARAM_FILTER, newText);
		getLoaderManager().restartLoader(LOADER_EQUIPEMENTS, bundle, this);

		mProgressBar.setVisibility(View.VISIBLE);

		if (mAdapter.getCount() == 0)
			showLoader();
	}

}
