package net.naonedbus.fragment.impl;

import java.util.List;

import net.naonedbus.R;
import net.naonedbus.activity.impl.ArretsActivity;
import net.naonedbus.activity.impl.CommentaireActivity;
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
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class LignesFragment extends CustomListFragment implements CustomFragmentActions {

	public LignesFragment() {
		super(R.string.title_fragment_lignes, R.layout.fragment_listview_section);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		registerForContextMenu(getListView());

		loadContent();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu) {
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
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_edit:
			startActivity(new Intent(getActivity(), CommentaireActivity.class));
			break;
		}
		return false;
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

}
