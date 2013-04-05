package net.naonedbus.fragment.impl;

import java.util.ArrayList;
import java.util.List;

import net.naonedbus.R;
import net.naonedbus.activity.impl.MapActivity;
import net.naonedbus.activity.impl.ParcoursActivity;
import net.naonedbus.bean.Equipement.Type;
import net.naonedbus.bean.TypeEquipement;
import net.naonedbus.fragment.CustomCursorFragment;
import net.naonedbus.intent.ParamIntent;
import net.naonedbus.manager.impl.EquipementManager;
import net.naonedbus.manager.impl.TypeEquipementManager;
import net.naonedbus.provider.impl.EquipementProvider;
import net.naonedbus.provider.table.EquipementTable;
import net.naonedbus.widget.ModalSearchView.OnQueryTextListener;
import net.naonedbus.widget.adapter.impl.EquipementCursorAdapter;
import net.naonedbus.widget.indexer.impl.EquipementCursorIndexer;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
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
		super(0, R.layout.fragment_listview_section);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mEquipementManager = EquipementManager.getInstance();
		final TypeEquipementManager typeEquipementManager = TypeEquipementManager.getInstance();
		final List<TypeEquipement> types = typeEquipementManager.getAll(getActivity().getContentResolver());
		final List<String> equipements = new ArrayList<String>();
		for (final TypeEquipement typeEquipement : types) {
			equipements.add(typeEquipement.nom);
		}

		mAdapter = new EquipementCursorAdapter(getActivity(), null);
		mAdapter.setIndexer(new EquipementCursorIndexer(null, equipements, EquipementTable.ID_TYPE));
		mAdapter.setFilterQueryProvider(this);

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
	public void onLoaderReset(final Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
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
