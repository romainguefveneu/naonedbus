package net.naonedbus.fragment.impl;

import net.naonedbus.provider.impl.EquipementProvider;
import net.naonedbus.provider.table.EquipementTable;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;

import com.actionbarsherlock.app.SherlockListFragment;

public class SearchFragment extends SherlockListFragment implements LoaderCallbacks<Cursor> {

	private static final int LOADER_INIT = 0;
	private static final int LOADER_REFRESH = 1;

	private SimpleCursorAdapter mAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final String[] dataColumns = { EquipementTable.NOM, EquipementTable.ID_TYPE };
		final int[] viewIDs = { android.R.id.text1, android.R.id.text2 };

		mAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_2, null, dataColumns,
				viewIDs, 0);

		// Associate the (now empty) adapter with the ListView.
		setListAdapter(mAdapter);
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

}
