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
import net.naonedbus.activity.impl.MapActivity;
import net.naonedbus.activity.impl.ParcoursActivity;
import net.naonedbus.activity.map.overlay.TypeOverlayItem;
import net.naonedbus.bean.AddressResult;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.intent.ParamIntent;
import net.naonedbus.loader.AddressLoader;
import net.naonedbus.manager.impl.EquipementManager;
import net.naonedbus.widget.ModalSearchView;
import net.naonedbus.widget.ModalSearchView.OnQueryTextListener;
import net.naonedbus.widget.adapter.impl.AdressResultArrayAdapter;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;

public class AddressSearchFragment extends SherlockListFragment implements OnQueryTextListener, FilterQueryProvider,
		LoaderCallbacks<AsyncResult<List<AddressResult>>> {

	private AdressResultArrayAdapter mAdapter;
	private EquipementManager mEquipementManager;

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

		final Intent queryIntent = getActivity().getIntent();
		final String queryAction = queryIntent.getAction();
		if (Intent.ACTION_SEARCH.equals(queryAction)) {
			if (queryIntent.getData() == null) {
				// Lancer la recherche
				final String query = queryIntent.getStringExtra(SearchManager.QUERY);
				mModalSearchView.setText(query);
				Toast.makeText(getActivity(), query, Toast.LENGTH_SHORT).show();
			} else {
				final Integer selectedItemId = Integer.valueOf(queryIntent.getStringExtra(SearchManager.QUERY));
				final TypeOverlayItem selectedItemType = TypeOverlayItem.getById(Integer.valueOf(queryIntent
						.getStringExtra(SearchManager.EXTRA_DATA_KEY)));

				if (selectedItemType.equals(TypeOverlayItem.TYPE_STATION)) {
					// Afficher les parcours de l'arrêt sélectionné
					final Intent intent = new Intent(getActivity(), ParcoursActivity.class);
					intent.putExtra(ParcoursActivity.PARAM_ID_SATION, selectedItemId);
					startActivity(intent);
				} else {
					// Afficher l'élément sur la carte
					final ParamIntent intent = new ParamIntent(getActivity(), MapActivity.class);
					intent.putExtra(MapActivity.Param.itemId, selectedItemId);
					intent.putExtra(MapActivity.Param.itemType, selectedItemType.getId());
					startActivity(intent);
				}
				getActivity().finish();
			}
		}

		mEquipementManager = EquipementManager.getInstance();
		final String[] types = getResources().getStringArray(R.array.types_equipements);

		mAdapter = new AdressResultArrayAdapter(getActivity(), new ArrayList<AddressResult>());

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
	}

	@Override
	public void onLoaderReset(final Loader<AsyncResult<List<AddressResult>>> loader) {

	}

	@Override
	public void onQueryTextChange(final String newText) {
		final Bundle bundle = new Bundle();
		bundle.putString(AddressLoader.PARAM_FILTER, newText);
		getLoaderManager().restartLoader(0, bundle, this);
	}

	@Override
	public Cursor runQuery(final CharSequence constraint) {
		return mEquipementManager.getEquipementsCursorByName(getActivity().getContentResolver(), null,
				constraint.toString());
	}

}
