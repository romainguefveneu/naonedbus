package net.naonedbus.fragment.impl;

import net.naonedbus.manager.impl.EquipementManager;
import net.naonedbus.provider.table.EquipementTable;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;

import com.actionbarsherlock.app.SherlockListFragment;

public class SearchFragment extends SherlockListFragment implements LoaderCallbacks<Cursor> {

	private SimpleCursorAdapter mAdapter;
	private EquipementManager mEquipementManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mEquipementManager = EquipementManager.getInstance();

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
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
		return null;// mEquipementManager.getCursor(getActivity().getContentResolver());
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

	}

}
