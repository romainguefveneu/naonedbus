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

import net.naonedbus.BuildConfig;
import net.naonedbus.R;
import net.naonedbus.activity.impl.ArretsActivity;
import net.naonedbus.activity.impl.CommentaireActivity;
import net.naonedbus.activity.impl.PlanActivity;
import net.naonedbus.bean.Route;
import net.naonedbus.fragment.CustomCursorFragment;
import net.naonedbus.intent.ParamIntent;
import net.naonedbus.manager.impl.LigneManager;
import net.naonedbus.provider.impl.RouteProvider;
import net.naonedbus.provider.table.RouteTable;
import net.naonedbus.widget.adapter.impl.RouteCursorAdapter;
import net.naonedbus.widget.indexer.impl.RouteCursorIndexer;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnActionExpandListener;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;

public class LignesFragment extends CustomCursorFragment implements OnQueryTextListener {

	private static final String LOG_TAG = "LignesFragment";
	private static final boolean DBG = BuildConfig.DEBUG;

	private RouteCursorAdapter mAdapter;
	private LigneManager mLigneManager;

	public LignesFragment() {
		super(R.layout.fragment_listview_section);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		mLigneManager = LigneManager.getInstance();
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		registerForContextMenu(getListView());
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_lignes, menu);

		final MenuItem searchItem = menu.findItem(R.id.menu_search);
		final SearchView searchView = (SearchView) searchItem.getActionView();
		searchItem.setOnActionExpandListener(new OnActionExpandListener() {

			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				searchView.setOnQueryTextListener(LignesFragment.this);
				return true;
			}

			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				searchView.setOnQueryTextListener(null);
				return true;
			}
		});
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		final AdapterView.AdapterContextMenuInfo cmi = (AdapterView.AdapterContextMenuInfo) menuInfo;

		final CursorWrapper ligne = (CursorWrapper) getListAdapter().getItem(cmi.position);
		final String lettreLigne = ligne.getString(ligne.getColumnIndex(RouteTable.LETTER));

		final android.view.MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.fragment_lignes_contextual, menu);

		menu.setHeaderTitle(getString(R.string.dialog_title_menu_lignes, lettreLigne));
	}

	@Override
	public boolean onContextItemSelected(final android.view.MenuItem item) {
		final AdapterView.AdapterContextMenuInfo cmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		final CursorWrapper cursor = (CursorWrapper) getListAdapter().getItem(cmi.position);
		final Route ligne = mLigneManager.getSingleFromCursor(cursor);

		switch (item.getItemId()) {
		case R.id.menu_show_plan:
			menuShowPlan(ligne);
			break;
		case R.id.menu_comment:
			menuComment(ligne);
			break;
		default:
			break;
		}

		return true;
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
		super.onListItemClick(l, v, position, id);
		final CursorWrapper cursorWrapper = (CursorWrapper) getListAdapter().getItem(position);

		final LigneManager ligneManager = LigneManager.getInstance();
		final Route ligne = ligneManager.getSingleFromCursor(cursorWrapper);

		final ParamIntent intent = new ParamIntent(getActivity(), ArretsActivity.class);
		intent.putExtra(ArretsActivity.PARAM_LIGNE, ligne);
		getActivity().startActivity(intent);
	}

	private void menuShowPlan(final Route ligne) {
		final Intent intent = new Intent(getActivity(), PlanActivity.class);
		intent.putExtra(PlanActivity.PARAM_CODE_LIGNE, ligne.getCode());
		startActivity(intent);
	}

	private void menuComment(final Route ligne) {
		final Intent intent = new Intent(getActivity(), CommentaireActivity.class);
		intent.putExtra(CommentaireActivity.PARAM_LIGNE, ligne);
		startActivity(intent);
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int loaderId, final Bundle bundle) {
		if (DBG)
			Log.d(LOG_TAG, "onCreateLoader " + loaderId);

		return new CursorLoader(getActivity(), RouteProvider.CONTENT_URI, null, null, null, null);
	}

	@Override
	protected CursorAdapter getCursorAdapter(final Context context) {
		final String[] types = context.getResources().getStringArray(R.array.types_lignes);

		mAdapter = new RouteCursorAdapter(getActivity(), null);
		mAdapter.setIndexer(new RouteCursorIndexer(null, types, RouteTable.TYPE_ID));

		return mAdapter;
	}

	@Override
	public boolean onQueryTextSubmit(final String query) {
		return false;
	}

	@Override
	public boolean onQueryTextChange(final String newText) {
		if (DBG)
			Log.d(LOG_TAG, "onQueryTextChange '" + newText + "'");

		mAdapter.getFilter().filter(newText);
		return true;
	}

}
