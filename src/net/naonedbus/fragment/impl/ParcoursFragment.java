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

import java.util.List;

import net.naonedbus.R;
import net.naonedbus.activity.impl.ArretDetailActivity;
import net.naonedbus.bean.Equipment;
import net.naonedbus.bean.Equipment.Type;
import net.naonedbus.bean.Stop;
import net.naonedbus.bean.StopPath;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.fragment.CustomListFragment;
import net.naonedbus.manager.impl.StopManager;
import net.naonedbus.manager.impl.EquipmentManager;
import net.naonedbus.manager.impl.ParcoursManager;
import net.naonedbus.widget.adapter.impl.ParkArrayAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

public class ParcoursFragment extends CustomListFragment {

	public static final String PARAM_ID_STATION = "idStation";

	private Equipment mStation;
	private final EquipmentManager mEquipementManager;

	public ParcoursFragment() {
		super(R.layout.fragment_listview);
		mEquipementManager = EquipmentManager.getInstance();
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		final int idStation = getArguments().getInt(PARAM_ID_STATION);
		mStation = mEquipementManager.getSingle(getActivity().getContentResolver(), Type.TYPE_STOP, idStation);

		getActivity().setTitle(mStation.getName());

		loadContent();
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
		super.onListItemClick(l, v, position, id);
		final StopPath item = (StopPath) l.getItemAtPosition(position);

		final StopManager arretManager = StopManager.getInstance();
		final Stop arret = arretManager.getSingle(getActivity().getContentResolver(), item.getId());

		final Intent intent = new Intent(getActivity(), ArretDetailActivity.class);
		intent.putExtra(ArretDetailActivity.PARAM_ARRET, arret);

		startActivity(intent);
	}

	@Override
	protected AsyncResult<ListAdapter> loadContent(final Context context, final Bundle bundle) {
		final ParcoursManager parcoursManager = ParcoursManager.getInstance();
		final List<StopPath> parcoursList = parcoursManager.getParcoursList(context.getContentResolver(),
				mStation.getNormalizedName());
		final ListAdapter adapter = new ParkArrayAdapter(context, parcoursList);
		final AsyncResult<ListAdapter> result = new AsyncResult<ListAdapter>();
		result.setResult(adapter);
		return result;
	}

}
