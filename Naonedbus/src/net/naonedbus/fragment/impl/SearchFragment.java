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

import net.naonedbus.R;
import net.naonedbus.activity.impl.MapActivity;
import net.naonedbus.activity.impl.ParcoursActivity;
import net.naonedbus.bean.Equipement.Type;
import net.naonedbus.fragment.CustomCursorFragment;
import net.naonedbus.intent.ParamIntent;
import net.naonedbus.manager.impl.EquipementManager;
import net.naonedbus.provider.impl.EquipementProvider;
import net.naonedbus.provider.table.EquipementTable;
import net.naonedbus.widget.ModalSearchView.OnQueryTextListener;
import net.naonedbus.widget.adapter.impl.EquipementCursorAdapter;
import net.naonedbus.widget.indexer.impl.EquipementCursorIndexer;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.widget.FilterQueryProvider;
import android.widget.ListView;

public class SearchFragment extends CustomCursorFragment implements OnQueryTextListener, FilterQueryProvider {

	private EquipementCursorAdapter mAdapter;
	private EquipementManager mEquipementManager;

	public SearchFragment() {
		super(R.layout.fragment_listview_section);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mEquipementManager = EquipementManager.getInstance();
		final String[] types = getResources().getStringArray(R.array.types_equipements);

		mAdapter = new EquipementCursorAdapter(getActivity(), null);
		mAdapter.setIndexer(new EquipementCursorIndexer(null, types, EquipementTable.ID_TYPE));
		mAdapter.setFilterQueryProvider(this);

		mAdapter.registerDataSetObserver(new DataSetObserver() {
			@Override
			public void onChanged() {
				if (mAdapter.getCount() == 0) {
					showMessage();
				} else {
					showContent();
				}
			}
		});

		// Associate the (now empty) adapter with the ListView.
		setListAdapter(mAdapter);
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
		final ParamIntent intent;
		final CursorWrapper equipement = (CursorWrapper) getListAdapter().getItem(position);
		final int idType = equipement.getInt(equipement.getColumnIndex(EquipementTable.ID_TYPE));

		if (idType == Type.TYPE_ARRET.getId()) {
			intent = new ParamIntent(getActivity(), ParcoursActivity.class);
			intent.putExtra(ParcoursActivity.Param.idStation, (int) id);
		} else {
			intent = new ParamIntent(getActivity(), MapActivity.class);
			intent.putExtra(MapActivity.Param.itemId, (int) id);
			intent.putExtra(MapActivity.Param.itemType, idType);
		}
		startActivity(intent);
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int loaderId, final Bundle bundle) {
		final CursorLoader cursorLoader = new CursorLoader(getActivity(), EquipementProvider.CONTENT_URI, null, null,
				null, null);
		return cursorLoader;
	}

	@Override
	protected CursorAdapter getCursorAdapter(final Context context) {
		return mAdapter;
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
		mAdapter.changeCursor(cursor);
		super.onLoadFinished(loader, cursor);
	}

	@Override
	public void onQueryTextChange(final String newText) {
		mAdapter.getFilter().filter(newText);
	}

	@Override
	public Cursor runQuery(final CharSequence constraint) {
		return mEquipementManager.getEquipementsCursorByName(getActivity().getContentResolver(), null,
				constraint.toString());
	}

}
