/**
 * Copyright (C) 2013 Romain Guefveneu.
 *   
 *  This file is part of naonedbus.
 *   
 *  Naonedbus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  Naonedbus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.naonedbus.fragment.impl;

import java.util.ArrayList;
import java.util.List;

import net.naonedbus.R;
import net.naonedbus.activity.impl.CommentaireActivity;
import net.naonedbus.bean.EmptyInfoTrafic;
import net.naonedbus.bean.InfoTrafic;
import net.naonedbus.bean.Ligne;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.fragment.CustomListFragment;
import net.naonedbus.manager.impl.InfoTraficManager;
import net.naonedbus.manager.impl.LigneManager;
import net.naonedbus.widget.adapter.ArraySectionAdapter;
import net.naonedbus.widget.adapter.impl.InfoTraficLigneArrayAdapter;
import net.naonedbus.widget.indexer.impl.InfoTraficIndexer;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class InfoTraficFragment extends CustomListFragment {

	public static final String PARAM_CODE_LIGNE = "codeLigne";

	private MenuItem mRefreshMenuItem;
	private String mCodeLigne;

	public InfoTraficFragment() {
		super(R.layout.fragment_listview_box);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (getArguments() != null) {
			mCodeLigne = getArguments().getString(PARAM_CODE_LIGNE);
		}

		loadContent();
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.activity_en_direct, menu);
		mRefreshMenuItem = menu.findItem(R.id.menu_refresh);

		if (getLoaderManager().hasRunningLoaders())
			showResfrehMenuLoader();
	}

	private void showResfrehMenuLoader() {
		if (mRefreshMenuItem != null) {
			mRefreshMenuItem.setActionView(R.layout.action_item_refresh);
		}
	}

	private void hideResfrehMenuLoader() {
		if (mRefreshMenuItem != null) {
			mRefreshMenuItem.setActionView(null);
		}
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
		super.onListItemClick(l, v, position, id);
		final InfoTrafic item = (InfoTrafic) getListAdapter().getItem(position);

		final Bundle bundle = new Bundle();
		bundle.putParcelable(InfoTraficDetailFragment.PARAM_INFO_TRAFIC, item);

		final DialogFragment dialogFragment = new InfoTraficDetailDialogFragment();
		dialogFragment.setArguments(bundle);
		dialogFragment.show(getChildFragmentManager(), "InfoTraficDetailDialogFragment");
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_refresh:
			refreshContent();
			break;
		case R.id.menu_comment:
			startActivity(new Intent(getActivity(), CommentaireActivity.class));
			return true;
		}
		return true;
	}

	@Override
	protected void onPreExecute() {
		showResfrehMenuLoader();
	}

	@Override
	protected void onPostExecute() {
		hideResfrehMenuLoader();
	}

	@Override
	protected AsyncResult<ListAdapter> loadContent(final Context context, final Bundle bundle) {
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

				for (final Ligne ligne : lignes) {
					infoTrafics = infoTraficManager.getByLigneCode(context, ligne.getCode());

					if (infoTrafics == null || infoTrafics.isEmpty()) {
						// Gérer les lignes sans travaux.
						final InfoTrafic emptyDetail = new EmptyInfoTrafic();
						emptyDetail.setSection(ligne);

						infoTraficDetails.add(emptyDetail);
					} else {
						for (final InfoTrafic infoTrafic : infoTrafics) {
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
					for (final InfoTrafic infoTrafic : infoTrafics) {
						infoTraficClone = infoTrafic.clone();
						infoTraficClone.setSection(ligne);
						infoTraficDetails.add(infoTraficClone);
					}
				}
			}

			final ArraySectionAdapter<InfoTrafic> adapter = new InfoTraficLigneArrayAdapter(context, infoTraficDetails);
			adapter.setIndexer(new InfoTraficIndexer());
			result.setResult(adapter);
		} catch (final Exception e) {
			result.setException(e);
		}
		return result;
	}

}
