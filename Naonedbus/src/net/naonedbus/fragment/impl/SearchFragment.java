package net.naonedbus.fragment.impl;

import java.util.ArrayList;
import java.util.List;

import net.naonedbus.BuildConfig;
import net.naonedbus.R;
import net.naonedbus.activity.impl.MapActivity;
import net.naonedbus.activity.impl.ParcoursActivity;
import net.naonedbus.bean.Equipement.Type;
import net.naonedbus.bean.TypeEquipement;
import net.naonedbus.intent.ParamIntent;
import net.naonedbus.manager.impl.EquipementManager;
import net.naonedbus.manager.impl.TypeEquipementManager;
import net.naonedbus.provider.impl.EquipementProvider;
import net.naonedbus.provider.table.EquipementTable;
import net.naonedbus.widget.PinnedHeaderListView;
import net.naonedbus.widget.ModalSearchView.OnQueryTextListener;
import net.naonedbus.widget.adapter.impl.EquipementCursorAdapter;
import net.naonedbus.widget.indexer.impl.EquipementCursorIndexer;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.DataSetObserver;
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
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;

public class SearchFragment extends SherlockListFragment implements LoaderCallbacks<Cursor>, OnQueryTextListener,
		FilterQueryProvider {

	private static final String LOG_TAG = "SearchFragment";
	private static final boolean DBG = BuildConfig.DEBUG;

	private static final int LOADER_INIT = 0;

	private EquipementCursorAdapter mAdapter;
	private EquipementManager mEquipementManager;
	private ViewGroup fragmentView;
	private TextView messageTextView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mEquipementManager = EquipementManager.getInstance();
		final TypeEquipementManager typeEquipementManager = TypeEquipementManager.getInstance();
		final List<TypeEquipement> types = typeEquipementManager.getAll(getActivity().getContentResolver());
		final List<String> equipements = new ArrayList<String>();
		for (TypeEquipement typeEquipement : types) {
			equipements.add(typeEquipement.nom);
		}

		mAdapter = new EquipementCursorAdapter(getActivity(), null);
		mAdapter.setIndexer(new EquipementCursorIndexer(null, equipements, EquipementTable.ID_TYPE));
		mAdapter.setFilterQueryProvider(this);

		mAdapter.registerDataSetObserver(new DataSetObserver() {
			@Override
			public void onChanged() {
				if (mAdapter.getCount() == 0) {
					messageTextView.setVisibility(View.VISIBLE);
				} else {
					messageTextView.setVisibility(View.GONE);
				}
			}
		});

		// Associate the (now empty) adapter with the ListView.
		setListAdapter(mAdapter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (container == null) // must put this in
			return null;

		fragmentView = (ViewGroup) inflater.inflate(R.layout.fragment_base, container, false);
		final View view = inflater.inflate(R.layout.fragment_search, container, false);
		view.setId(R.id.fragmentContent);

		messageTextView = (TextView) view.findViewById(android.R.id.title);

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
	public void onListItemClick(ListView l, View v, int position, long id) {
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
		mAdapter.swapCursor(null);
	}

	@Override
	public void onQueryTextChange(String newText) {
		mAdapter.getFilter().filter(newText);
	}

	@Override
	public Cursor runQuery(CharSequence constraint) {
		return mEquipementManager.getEquipementsCursorByName(getActivity().getContentResolver(), null,
				constraint.toString());
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

}
