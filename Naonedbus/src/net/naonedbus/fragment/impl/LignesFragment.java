package net.naonedbus.fragment.impl;

import java.util.ArrayList;
import java.util.List;

import net.naonedbus.R;
import net.naonedbus.activity.impl.ArretsActivity;
import net.naonedbus.activity.impl.PlanActivity;
import net.naonedbus.bean.TypeLigne;
import net.naonedbus.fragment.CustomCursorFragment;
import net.naonedbus.fragment.CustomFragmentActions;
import net.naonedbus.helper.StateHelper;
import net.naonedbus.intent.ParamIntent;
import net.naonedbus.manager.impl.TypeLigneManager;
import net.naonedbus.provider.impl.LigneProvider;
import net.naonedbus.provider.table.LigneTable;
import net.naonedbus.widget.adapter.impl.LigneCursorAdapter;
import net.naonedbus.widget.indexer.impl.LigneCursorIndexer;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.SparseIntArray;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class LignesFragment extends CustomCursorFragment implements CustomFragmentActions {

	// private static final String LOG_TAG = "LignesFragment";
	// private static final boolean DBG = BuildConfig.DEBUG;

	private final static int FILTER_ALL = 0;;
	private final static int FILTER_FAVORIS = 1;
	private final static SparseIntArray MENU_MAPPING = new SparseIntArray();
	static {
		MENU_MAPPING.append(FILTER_ALL, R.id.menu_filter_all);
		MENU_MAPPING.append(FILTER_FAVORIS, R.id.menu_filter_favoris);
	}

	private StateHelper mStateHelper;
	private LigneCursorAdapter mAdapter;
	private int mCurrentFilter = FILTER_ALL;

	public LignesFragment() {
		super(R.string.title_fragment_lignes, R.layout.fragment_listview_section);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Gestion du tri par défaut
		mStateHelper = new StateHelper(getActivity());
		mCurrentFilter = mStateHelper.getSortType(this, FILTER_ALL);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		registerForContextMenu(getListView());
	}

	@Override
	public void onPause() {
		mStateHelper.setSortType(this, mCurrentFilter);
		super.onPause();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu) {
		final SherlockFragmentActivity activity = getSherlockActivity();
		if (activity != null) {
			final MenuInflater menuInflater = getSherlockActivity().getSupportMenuInflater();
			menuInflater.inflate(R.menu.fragment_lignes, menu);
			menu.findItem(MENU_MAPPING.get(mCurrentFilter)).setChecked(true);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		final AdapterView.AdapterContextMenuInfo cmi = (AdapterView.AdapterContextMenuInfo) menuInfo;

		final CursorWrapper ligne = (CursorWrapper) getListAdapter().getItem(cmi.position);
		final String lettreLigne = ligne.getString(ligne.getColumnIndex(LigneTable.LETTRE));

		final android.view.MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.fragment_lignes_contextual, menu);

		menu.setHeaderTitle(getString(R.string.dialog_title_menu_lignes, lettreLigne));
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		final AdapterView.AdapterContextMenuInfo cmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		final CursorWrapper ligne = (CursorWrapper) getListAdapter().getItem(cmi.position);
		final String codeLigne = ligne.getString(ligne.getColumnIndex(LigneTable.CODE));

		switch (item.getItemId()) {
		case R.id.menu_show_plan:
			menuShowPlan(codeLigne);
			break;
		default:
			break;
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_filter_all:
			item.setChecked(true);
			mCurrentFilter = FILTER_ALL;
			refreshContent();
			break;
		case R.id.menu_filter_favoris:
			item.setChecked(true);
			mCurrentFilter = FILTER_FAVORIS;
			refreshContent();
			break;
		default:
			return false;
		}

		return true;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		final CursorWrapper ligne = (CursorWrapper) getListAdapter().getItem(position);
		final String codeLigne = ligne.getString(ligne.getColumnIndex(LigneTable.CODE));

		final ParamIntent intent = new ParamIntent(getActivity(), ArretsActivity.class);
		intent.putExtra(ArretsActivity.Param.codeLigne, codeLigne);
		getActivity().startActivity(intent);
	}

	private void menuShowPlan(final String codeLigne) {
		final ParamIntent intent = new ParamIntent(getActivity(), PlanActivity.class);
		intent.putExtra(PlanActivity.Param.codeLigne, codeLigne);
		startActivity(intent);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
		Uri uri = LigneProvider.CONTENT_URI;
		if (mCurrentFilter == FILTER_FAVORIS) {
			uri = uri.buildUpon().path(LigneProvider.LIGNE_FAVORIS_URI_PATH_QUERY).build();
		}

		final CursorLoader cursorLoader = new CursorLoader(getActivity(), uri, null, null, null, null);
		return cursorLoader;
	}

	@Override
	protected CursorAdapter getCursorAdapter(Context context) {
		mAdapter = new LigneCursorAdapter(getActivity(), null);

		final TypeLigneManager typeLigneManager = TypeLigneManager.getInstance();
		final List<TypeLigne> typesLignes = typeLigneManager.getAll(getActivity().getContentResolver());
		final List<String> types = new ArrayList<String>();
		for (TypeLigne type : typesLignes) {
			types.add(type.nom);
		}

		mAdapter.setIndexer(new LigneCursorIndexer(null, types, LigneTable.TYPE));

		return mAdapter;
	}

}
