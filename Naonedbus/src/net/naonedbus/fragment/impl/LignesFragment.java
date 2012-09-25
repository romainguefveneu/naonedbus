package net.naonedbus.fragment.impl;

import java.util.List;

import net.naonedbus.R;
import net.naonedbus.activity.impl.ArretsActivity;
import net.naonedbus.activity.impl.CommentaireActivity;
import net.naonedbus.bean.Ligne;
import net.naonedbus.bean.Sens;
import net.naonedbus.bean.TypeLigne;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.fragment.CustomExpandableListFragment;
import net.naonedbus.fragment.CustomFragmentActions;
import net.naonedbus.manager.impl.LigneManager;
import net.naonedbus.manager.impl.SensManager;
import net.naonedbus.manager.impl.TypeLigneManager;
import net.naonedbus.utils.DpiUtils;
import net.naonedbus.widget.adapter.impl.LignesArrayExpandableAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class LignesFragment extends CustomExpandableListFragment<LignesArrayExpandableAdapter> implements
		CustomFragmentActions {

	public LignesFragment() {
		super(R.string.title_fragment_lignes, R.layout.fragment_expandablelistview_section);
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
	public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
		final Intent intent = new Intent(getActivity(), ArretsActivity.class);
		startActivity(intent);
	}

	@Override
	protected AsyncResult<LignesArrayExpandableAdapter> loadContent(final Context context) {
		final AsyncResult<LignesArrayExpandableAdapter> result = new AsyncResult<LignesArrayExpandableAdapter>();
		try {
			final TypeLigneManager typeLigneManager = TypeLigneManager.getInstance();
			final LigneManager ligneManager = LigneManager.getInstance();
			final SensManager sensManager = SensManager.getInstance();

			final List<TypeLigne> typesLignes = typeLigneManager.getAll(context.getContentResolver(), null, null);
			final List<Ligne> lignes = ligneManager.getAll(context.getContentResolver(), null, null);

			final SparseArray<List<Sens>> sens = new SparseArray<List<Sens>>();
			int i = 0;
			for (Ligne ligne : lignes) {
				sens.append(i++, sensManager.getAll(context.getContentResolver(), ligne.code));
			}

			final LignesArrayExpandableAdapter adapter = new LignesArrayExpandableAdapter(context, lignes, sens);

			result.setResult(adapter);
		} catch (Exception e) {
			result.setException(e);
		}
		return result;
	}

}
