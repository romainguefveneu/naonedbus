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
import net.naonedbus.activity.impl.ArretDetailActivity;
import net.naonedbus.bean.Arret;
import net.naonedbus.bean.Equipement;
import net.naonedbus.bean.Equipement.Type;
import net.naonedbus.bean.Parcours;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.bean.horaire.Attente;
import net.naonedbus.fragment.CustomListFragment;
import net.naonedbus.loader.AttenteLoader;
import net.naonedbus.manager.impl.ArretManager;
import net.naonedbus.manager.impl.EquipementManager;
import net.naonedbus.manager.impl.ParcoursManager;
import net.naonedbus.widget.adapter.impl.ParcoursAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

public class ParcoursFragment extends CustomListFragment {

	public static final String PARAM_ID_STATION = "idStation";

	private LoaderCallbacks<List<Attente>> mAttenteLoader = new LoaderCallbacks<List<Attente>>() {

		@Override
		public Loader<List<Attente>> onCreateLoader(int arg0, Bundle args) {
			getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
			return new AttenteLoader(getActivity(), args);
		}

		@Override
		public void onLoadFinished(Loader<List<Attente>> arg0, List<Attente> data) {
			mAttentes.clear();
			mAttentes.addAll(data);

			mParcoursAdapter.notifyDataSetChanged();

			getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
			resetNextUpdate();
		}

		@Override
		public void onLoaderReset(Loader<List<Attente>> arg0) {
		}

	};

	private final static IntentFilter intentFilter;
	static {
		intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_TIME_TICK);
		intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
	}

	private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			getLoaderManager().restartLoader(1, AttenteLoader.create(mStation), mAttenteLoader).forceLoad();
		}
	};

	private Equipement mStation;
	private List<Parcours> mParcoursList;
	private List<Attente> mAttentes;

	private ParcoursAdapter mParcoursAdapter;
	private final EquipementManager mEquipementManager;

	public ParcoursFragment() {
		super(R.layout.fragment_listview);
		mEquipementManager = EquipementManager.getInstance();
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		final int idStation = getArguments().getInt(PARAM_ID_STATION);
		mStation = mEquipementManager.getSingle(getActivity().getContentResolver(), Type.TYPE_ARRET, idStation);

		getActivity().setTitle(mStation.getNom());

		setTimeToLive(1);

		loadContent();
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		mParcoursList = new ArrayList<Parcours>();
		mAttentes = new ArrayList<Attente>();

		mParcoursAdapter = new ParcoursAdapter(getActivity(), mParcoursList, mAttentes);
	}

	@Override
	public void onResume() {
		super.onResume();
		getActivity().registerReceiver(mIntentReceiver, intentFilter);
		if (mAttentes.isEmpty()) {
			getLoaderManager().initLoader(1, AttenteLoader.create(mStation), mAttenteLoader);
		} else {
			if (isNotUpToDate())
				getLoaderManager().restartLoader(1, AttenteLoader.create(mStation), mAttenteLoader).forceLoad();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(mIntentReceiver);
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
		super.onListItemClick(l, v, position, id);
		final Parcours item = (Parcours) l.getItemAtPosition(position);

		final ArretManager arretManager = ArretManager.getInstance();
		final Arret arret = arretManager.getSingle(getActivity().getContentResolver(), item._id);

		final Intent intent = new Intent(getActivity(), ArretDetailActivity.class);
		intent.putExtra(ArretDetailActivity.PARAM_ARRET, arret);

		startActivity(intent);
	}

	@Override
	protected AsyncResult<ListAdapter> loadContent(final Context context, final Bundle bundle) {
		final ParcoursManager parcoursManager = ParcoursManager.getInstance();
		final List<Parcours> parcoursList = parcoursManager.getParcoursList(context.getContentResolver(),
				mStation.getNormalizedNom());

		mParcoursList.clear();
		mParcoursList.addAll(parcoursList);

		mParcoursAdapter.notifyDataSetChanged();

		AsyncResult<ListAdapter> result = new AsyncResult<ListAdapter>();
		result.setResult(mParcoursAdapter);

		return result;
	}

}
