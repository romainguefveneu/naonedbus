package net.naonedbus.fragment.impl;

import java.util.ArrayList;
import java.util.List;

import net.naonedbus.R;
import net.naonedbus.activity.impl.ArretsActivity;
import net.naonedbus.activity.impl.CommentaireActivity;
import net.naonedbus.activity.impl.PlanActivity;
import net.naonedbus.bean.Ligne;
import net.naonedbus.bean.TypeLigne;
import net.naonedbus.fragment.CustomCursorFragment;
import net.naonedbus.fragment.CustomFragmentActions;
import net.naonedbus.helper.StateHelper;
import net.naonedbus.intent.ParamIntent;
import net.naonedbus.manager.impl.FavoriManager;
import net.naonedbus.manager.impl.FavoriManager.OnFavoriActionListener;
import net.naonedbus.manager.impl.LigneManager;
import net.naonedbus.manager.impl.TypeLigneManager;
import net.naonedbus.provider.impl.LigneProvider;
import net.naonedbus.provider.table.LigneTable;
import net.naonedbus.widget.adapter.impl.LigneCursorAdapter;
import net.naonedbus.widget.indexer.impl.LigneCursorIndexer;
import android.content.Context;
import android.content.Intent;
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

	private FavoriManager mFavoriManager;

	private StateHelper mStateHelper;
	private LigneCursorAdapter mAdapter;
	private LigneManager mLigneManager;
	private int mCurrentFilter = FILTER_ALL;

	/**
	 * Action sur les favoris.
	 */
	private final OnFavoriActionListener mOnFavoriActionListener = new OnFavoriActionListener() {
		@Override
		public void onUpdate() {
			if (mCurrentFilter == FILTER_FAVORIS) {
				refreshContent();
			}
		};
	};

	public LignesFragment() {
		super(R.string.title_fragment_lignes, R.layout.fragment_listview_section);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		// Gestion du tri par défaut
		mStateHelper = new StateHelper(getActivity());
		mCurrentFilter = mStateHelper.getFilterType(this, FILTER_ALL);

		mFavoriManager = FavoriManager.getInstance();
		mFavoriManager.addActionListener(mOnFavoriActionListener);

		mLigneManager = LigneManager.getInstance();
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		registerForContextMenu(getListView());
	}

	@Override
	public void onPause() {
		mStateHelper.setFilterType(this, mCurrentFilter);
		super.onPause();
	}

	@Override
	public void onDestroy() {
		mFavoriManager.removeActionListener(mOnFavoriActionListener);
		super.onDestroy();
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_lignes, menu);
		menu.findItem(MENU_MAPPING.get(mCurrentFilter)).setChecked(true);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		final AdapterView.AdapterContextMenuInfo cmi = (AdapterView.AdapterContextMenuInfo) menuInfo;

		final CursorWrapper ligne = (CursorWrapper) getListAdapter().getItem(cmi.position);
		final String lettreLigne = ligne.getString(ligne.getColumnIndex(LigneTable.LETTRE));

		final android.view.MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.fragment_lignes_contextual, menu);

		menu.setHeaderTitle(getString(R.string.dialog_title_menu_lignes, lettreLigne));
	}

	@Override
	public boolean onContextItemSelected(final android.view.MenuItem item) {
		final AdapterView.AdapterContextMenuInfo cmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		final CursorWrapper cursor = (CursorWrapper) getListAdapter().getItem(cmi.position);
		final Ligne ligne = mLigneManager.getSingleFromCursor(cursor);

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
	public boolean onOptionsItemSelected(final MenuItem item) {

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
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
		super.onListItemClick(l, v, position, id);
		final CursorWrapper cursorWrapper = (CursorWrapper) getListAdapter().getItem(position);

		final LigneManager ligneManager = LigneManager.getInstance();
		final Ligne ligne = ligneManager.getSingleFromCursor(cursorWrapper);

		final ParamIntent intent = new ParamIntent(getActivity(), ArretsActivity.class);
		intent.putExtra(ArretsActivity.PARAM_LIGNE, ligne);
		getActivity().startActivity(intent);
	}

	private void menuShowPlan(final Ligne ligne) {
		final Intent intent = new Intent(getActivity(), PlanActivity.class);
		intent.putExtra(PlanActivity.PARAM_LIGNE, ligne);
		startActivity(intent);
	}

	private void menuComment(final Ligne ligne) {
		final Intent intent = new Intent(getActivity(), CommentaireActivity.class);
		intent.putExtra(CommentaireActivity.PARAM_LIGNE, ligne);
		startActivity(intent);
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int loaderId, final Bundle bundle) {
		Uri uri = LigneProvider.CONTENT_URI;
		if (mCurrentFilter == FILTER_FAVORIS) {
			uri = uri.buildUpon().path(LigneProvider.LIGNE_FAVORIS_URI_PATH_QUERY).build();
		}

		final CursorLoader cursorLoader = new CursorLoader(getActivity(), uri, null, null, null, null);
		return cursorLoader;
	}

	@Override
	protected CursorAdapter getCursorAdapter(final Context context) {
		mAdapter = new LigneCursorAdapter(getActivity(), null);

		final TypeLigneManager typeLigneManager = TypeLigneManager.getInstance();
		final List<TypeLigne> typesLignes = typeLigneManager.getAll(getActivity().getContentResolver());
		final List<String> types = new ArrayList<String>();
		for (final TypeLigne type : typesLignes) {
			types.add(type.nom);
		}

		mAdapter.setIndexer(new LigneCursorIndexer(null, types, LigneTable.TYPE));

		return mAdapter;
	}

}
