package net.naonedbus.fragment.impl;

import java.util.List;

import net.naonedbus.R;
import net.naonedbus.activity.impl.ArretsActivity;
import net.naonedbus.activity.impl.CommentaireActivity;
import net.naonedbus.bean.Ligne;
import net.naonedbus.bean.TypeLigne;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.fragment.CustomFragmentActions;
import net.naonedbus.fragment.CustomListFragment;
import net.naonedbus.manager.impl.LigneManager;
import net.naonedbus.manager.impl.TypeLigneManager;
import net.naonedbus.widget.adapter.impl.LignesArrayAdapter;
import net.naonedbus.widget.indexer.impl.LigneIndexer;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class LignesFragment extends CustomListFragment implements CustomFragmentActions {

	public LignesFragment() {
		super(R.string.title_fragment_lignes, R.layout.fragment_listview_section);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu) {
		final MenuInflater menuInflater = getSherlockActivity().getSupportMenuInflater();
		menuInflater.inflate(R.menu.fragment_lignes, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_edit:
			startActivity(new Intent(getActivity(), CommentaireActivity.class));
			break;
		default:
			return false;
		}
		return true;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		final Intent intent = new Intent(getActivity(), ArretsActivity.class);
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
