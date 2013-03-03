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
import android.os.Parcelable;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.actionbarsherlock.view.MenuItem;

public class TanActuFragment extends CustomListFragment {

	public static final String PARAM_CODE_LIGNE = "codeLigne";

	private String mCodeLigne;

	public TanActuFragment() {
		super(R.string.title_fragment_tan_actu, R.layout.fragment_listview_box);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (getArguments() != null) {
			mCodeLigne = getArguments().getString(PARAM_CODE_LIGNE);
		}

		loadContent();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		final InfoTrafic item = (InfoTrafic) getListAdapter().getItem(position);

		final ParamIntent intent = new ParamIntent(getActivity(), InfoTraficDetailActivity.class);
		intent.putExtra(InfoTraficDetailActivity.PARAM_INFO_TRAFIC, (Parcelable) item);
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

			List<InfoTrafic> infoTrafics;
			InfoTrafic infoTraficClone;

			// TODO : Faire plus propre
			if (mCodeLigne == null) {
				final List<Ligne> lignes = ligneManager.getAll(context.getContentResolver());

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
			} else {
				final Ligne ligne = ligneManager.getSingle(context.getContentResolver(), mCodeLigne);
				infoTrafics = infoTraficManager.getByLigneCode(context, mCodeLigne);

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
