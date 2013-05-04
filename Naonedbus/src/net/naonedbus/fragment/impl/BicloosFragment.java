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
import net.naonedbus.bean.Bicloo;
import net.naonedbus.bean.Equipement;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.comparator.BiclooComparator;
import net.naonedbus.fragment.CustomFragmentActions;
import net.naonedbus.fragment.EquipementFragment;
import net.naonedbus.manager.impl.BiclooManager;
import net.naonedbus.widget.adapter.impl.BiclooArrayAdapter;
import net.naonedbus.widget.indexer.impl.BiclooNomIndexer;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

public class BicloosFragment extends EquipementFragment implements CustomFragmentActions {

	public BicloosFragment() {
		super(R.string.title_fragment_bicloos, R.layout.fragment_listview_section, Equipement.Type.TYPE_BICLOO);
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
	}

	@Override
	protected AsyncResult<ListAdapter> loadContent(final Context context, final Bundle bundle) {
		final AsyncResult<ListAdapter> result = new AsyncResult<ListAdapter>();
		try {
			final BiclooManager biclooManager = BiclooManager.getInstance();
			final List<Bicloo> bicloos = biclooManager.getAll(context);

			final BiclooArrayAdapter adapter = new BiclooArrayAdapter(context, bicloos);
			adapter.sort(new BiclooComparator());
			adapter.setIndexer(new BiclooNomIndexer());

			result.setResult(adapter);

		} catch (final Exception e) {
			result.setException(e);
		}
		return result;
	}
}
