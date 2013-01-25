package net.naonedbus.fragment.impl;

import java.util.List;

import net.naonedbus.BuildConfig;
import net.naonedbus.R;
import net.naonedbus.activity.impl.ArretsActivity;
import net.naonedbus.activity.impl.PlanActivity;
import net.naonedbus.bean.Ligne;
import net.naonedbus.bean.TypeLigne;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.fragment.CustomFragmentActions;
import net.naonedbus.fragment.CustomListFragment;
import net.naonedbus.intent.ParamIntent;
import net.naonedbus.manager.impl.LigneManager;
import net.naonedbus.manager.impl.TypeLigneManager;
import net.naonedbus.widget.adapter.impl.LignesArrayAdapter;
import net.naonedbus.widget.indexer.impl.LigneIndexer;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;

public class LignesFragment extends CustomListFragment implements CustomFragmentActions, OnQueryTextListener {

	private static final String LOG_TAG = "LignesFragment";
	private static final boolean DBG = BuildConfig.DEBUG;

	public LignesFragment() {
		super(R.string.title_fragment_lignes, R.layout.fragment_listview_section);
		if (DBG)
			Log.i(LOG_TAG, "LignesFragment()");
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (DBG)
			Log.d(LOG_TAG, "onActivityCreated");

		registerForContextMenu(getListView());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (DBG)
			Log.d(LOG_TAG, "onCreateView");
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onStart() {
		super.onStart();
		if (DBG)
			Log.d(LOG_TAG, "onStart");

		loadContent();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (DBG)
			Log.d(LOG_TAG, "onResume");
	}

	@Override
	public void onCreateOptionsMenu(Menu menu) {
		final SherlockFragmentActivity activity = getSherlockActivity();
		if (activity != null) {
			final MenuInflater menuInflater = getSherlockActivity().getSupportMenuInflater();
			menuInflater.inflate(R.menu.fragment_lignes, menu);

			final SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
			searchView.setOnQueryTextListener(this);
			searchView.setQueryHint(getString(R.string.search_lignes_hint));

			final AutoCompleteTextView searchText = (AutoCompleteTextView) searchView
					.findViewById(R.id.abs__search_src_text);
			searchText.setHintTextColor(getResources().getColor(R.color.query_hint_color));
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		final AdapterView.AdapterContextMenuInfo cmi = (AdapterView.AdapterContextMenuInfo) menuInfo;

		final ListView listview = getListView();
		final Ligne ligne = (Ligne) listview.getItemAtPosition(cmi.position);

		final android.view.MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.fragment_lignes_contextual, menu);

		menu.setHeaderTitle(getString(R.string.dialog_title_menu_lignes, ligne.lettre));
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		final AdapterView.AdapterContextMenuInfo cmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		final Ligne ligne = (Ligne) getListView().getItemAtPosition(cmi.position);

		switch (item.getItemId()) {
		case R.id.menu_show_plan:
			menuShowPlan(ligne);
			break;
		default:
			break;
		}

		return true;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		final Ligne ligne = (Ligne) l.getItemAtPosition(position);
		final ParamIntent intent = new ParamIntent(getActivity(), ArretsActivity.class);
		intent.putExtra(ArretsActivity.Param.codeLigne, ligne.code);
		getActivity().startActivity(intent);
	}

	private void menuShowPlan(final Ligne ligne) {
		final ParamIntent intent = new ParamIntent(getActivity(), PlanActivity.class);
		intent.putExtra(PlanActivity.Param.codeLigne, ligne.code);
		startActivity(intent);
	}

	@Override
	protected AsyncResult<ListAdapter> loadContent(final Context context) {
		final AsyncResult<ListAdapter> result = new AsyncResult<ListAdapter>();
		try {
			final TypeLigneManager typeLigneManager = TypeLigneManager.getInstance();
			final LigneManager ligneManager = LigneManager.getInstance();
			final List<TypeLigne> typesLignes = typeLigneManager.getAll(context.getContentResolver(), null, null);
			final List<Ligne> items = ligneManager.getAll(context.getContentResolver(), null, null);
			final LignesArrayAdapter adapter = new LignesArrayAdapter(context, items);
			adapter.setIndexer(new LigneIndexer(typesLignes));

			result.setResult(adapter);
		} catch (Exception e) {
			result.setException(e);
		}
		return result;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		return false;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		Toast.makeText(getActivity(), newText, Toast.LENGTH_SHORT).show();
		return false;
	}

}
