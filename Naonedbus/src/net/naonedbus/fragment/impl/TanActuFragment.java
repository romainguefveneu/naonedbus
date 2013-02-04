package net.naonedbus.fragment.impl;

import java.util.ArrayList;
import java.util.List;

import net.naonedbus.R;
import net.naonedbus.activity.impl.InfoTraficDetailActivity;
import net.naonedbus.bean.EmptyInfoTrafic;
import net.naonedbus.bean.InfoTrafic;
import net.naonedbus.bean.Ligne;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.fragment.CustomListFragment;
import net.naonedbus.intent.ParamIntent;
import net.naonedbus.manager.impl.InfoTraficManager;
import net.naonedbus.manager.impl.LigneManager;
import net.naonedbus.widget.adapter.ArraySectionAdapter;
import net.naonedbus.widget.adapter.impl.InfoTraficLigneArrayAdapter;
import net.naonedbus.widget.indexer.impl.InfoTraficIndexer;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.actionbarsherlock.view.MenuItem;

public class TanActuFragment extends CustomListFragment {

	public TanActuFragment() {
		super(R.string.title_fragment_tan_actu, R.layout.fragment_listview_box);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public void onStart() {
		super.onStart();
		loadContent();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		final InfoTrafic item = (InfoTrafic) getListAdapter().getItem(position);

		final ParamIntent intent = new ParamIntent(getActivity(), InfoTraficDetailActivity.class);
		intent.putExtra(InfoTraficDetailActivity.Param.codeInfoTrafic, item.getCode());
		startActivity(intent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_refresh:
			refreshContent();
			break;
		}
		return true;
	}

	@Override
	protected AsyncResult<ListAdapter> loadContent(final Context context) {
		final AsyncResult<ListAdapter> result = new AsyncResult<ListAdapter>();

		try {
			final List<InfoTrafic> infoTraficDetails = new ArrayList<InfoTrafic>();

			final InfoTraficManager infoTraficManager = InfoTraficManager.getInstance();
			final LigneManager ligneManager = LigneManager.getInstance();
			final List<Ligne> lignes = ligneManager.getAll(context.getContentResolver());

			List<InfoTrafic> infoTrafics;
			InfoTrafic infoTraficClone;
			for (Ligne ligne : lignes) {
				infoTrafics = infoTraficManager.getByLigneCode(context, ligne.code);

				if (infoTrafics.size() == 0) {
					// Gérer les lignes sans travaux.
					final InfoTrafic emptyDetail = new EmptyInfoTrafic();
					emptyDetail.setSection(ligne);

					infoTraficDetails.add(emptyDetail);
				} else {
					for (InfoTrafic infoTrafic : infoTrafics) {
						infoTraficClone = infoTrafic.clone();
						infoTraficClone.setSection(ligne);
						infoTraficDetails.add(infoTraficClone);
					}
				}
			}

			final ArraySectionAdapter<InfoTrafic> adapter = new InfoTraficLigneArrayAdapter(context, infoTraficDetails);
			adapter.setIndexer(new InfoTraficIndexer());
			result.setResult(adapter);
		} catch (Exception e) {
			result.setException(e);
		}
		return result;
	}

}
