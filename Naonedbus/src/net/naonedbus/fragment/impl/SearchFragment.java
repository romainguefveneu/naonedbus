package net.naonedbus.fragment.impl;

import java.util.ArrayList;
import java.util.List;

import net.naonedbus.R;
import net.naonedbus.bean.TypeEquipement;
import net.naonedbus.manager.impl.TypeEquipementManager;
import net.naonedbus.provider.impl.EquipementProvider;
import net.naonedbus.provider.table.EquipementTable;
import net.naonedbus.widget.PinnedHeaderListView;
import net.naonedbus.widget.SearchView.OnQueryTextListener;
import net.naonedbus.widget.adapter.impl.EquipementCursorAdapter;
import net.naonedbus.widget.indexer.impl.EquipementCursorIndexer;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Adapter;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;

public class SearchFragment extends SherlockListFragment implements LoaderCallbacks<Cursor>, OnQueryTextListener {

	private static final int LOADER_INIT = 0;
	private static final int LOADER_REFRESH = 1;

	private EquipementCursorAdapter mAdapter;

	protected ViewGroup fragmentView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final TypeEquipementManager typeEquipementManager = TypeEquipementManager.getInstance();
		final List<TypeEquipement> types = typeEquipementManager.getAll(getActivity().getContentResolver());
		final List<String> equipements = new ArrayList<String>();
		for (TypeEquipement typeEquipement : types) {
			equipements.add(typeEquipement.nom);
		}

		mAdapter = new EquipementCursorAdapter(getActivity(), null);
		mAdapter.setIndexer(new EquipementCursorIndexer(null, equipements, EquipementTable.ID_TYPE));

		// Associate the (now empty) adapter with the ListView.
		setListAdapter(mAdapter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (container == null) // must put this in
			return null;

		fragmentView = (ViewGroup) inflater.inflate(R.layout.fragment_base, container, false);
		final View view = inflater.inflate(R.layout.fragment_listview_section, container, false);
		view.setId(R.id.fragmentContent);

		fragmentView.addView(view);

		setupListView(inflater, fragmentView);

		return fragmentView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getLoaderManager().initLoader(LOADER_INIT, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
		final CursorLoader cursorLoader = new CursorLoader(getActivity(), EquipementProvider.CONTENT_URI, null, null,
				null, null);
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mAdapter.changeCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

	}

	private void setupListView(LayoutInflater inflater, View view) {
		final ListView listView = (ListView) fragmentView.findViewById(android.R.id.list);

		if (listView instanceof PinnedHeaderListView) {
			final PinnedHeaderListView pinnedListView = (PinnedHeaderListView) listView;
			pinnedListView.setPinnedHeaderView(inflater.inflate(R.layout.list_item_header, pinnedListView, false));
			pinnedListView.setOnScrollListener(new OnScrollListener() {

				@Override
				public void onScrollStateChanged(AbsListView view, int scrollState) {

				}

				@Override
				public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
					final Adapter adapter = getListAdapter();
					if (adapter != null && adapter instanceof OnScrollListener) {
						final OnScrollListener sectionAdapter = (OnScrollListener) adapter;
						sectionAdapter.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
					}
				}
			});
		}
	}

	@Override
	public void onQueryTextChange(String newText) {
		Toast.makeText(getActivity(), newText, Toast.LENGTH_SHORT).show();
	}

}
