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
import net.naonedbus.loader.AddressLoader;
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
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;

public class AddressSearchFragment extends SherlockListFragment implements OnQueryTextListener,
		LoaderCallbacks<AsyncResult<List<AddressResult>>> {

	private AddressResultArrayAdapter mAdapter;

	private ModalSearchView mModalSearchView;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final ActionBar actionBar = ((SherlockFragmentActivity) getActivity()).getSupportActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setCustomView(R.layout.search_view);

		mModalSearchView = (ModalSearchView) actionBar.getCustomView();
		mModalSearchView.setOnQueryTextListener(this);

		final String[] types = getResources().getStringArray(R.array.types_equipements_recherche);
		final AddressResultArrayIndexer indexer = new AddressResultArrayIndexer(types);

		mAdapter = new AddressResultArrayAdapter(getActivity(), new ArrayList<AddressResult>());
		mAdapter.setIndexer(indexer);

		// Associate the (now empty) adapter with the ListView.
		setListAdapter(mAdapter);

		mModalSearchView.requestFocus();
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
		final AddressResult item = (AddressResult) l.getItemAtPosition(position);

		final Intent data = new Intent();
		data.putExtra("title", item.getTitle());
		data.putExtra("latitude", item.getLatitude());
		data.putExtra("longitude", item.getLongitude());

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

		mAdapter.clear();
		mAdapter.addAll(result.getResult());
		setListShown(true);
	}

	@Override
	public void onLoaderReset(final Loader<AsyncResult<List<AddressResult>>> loader) {

	}

	@Override
	public void onQueryTextChange(final String newText) {
		final Bundle bundle = new Bundle();
		bundle.putString(AddressLoader.PARAM_FILTER, newText);
		getLoaderManager().restartLoader(0, bundle, this);

		if (mAdapter.getCount() == 0)
			setListShown(false);
	}

}
