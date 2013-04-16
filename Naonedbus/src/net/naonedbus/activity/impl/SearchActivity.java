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
package net.naonedbus.activity.impl;

import net.naonedbus.R;
import net.naonedbus.activity.OneFragmentSlidingActivity;
import net.naonedbus.activity.map.overlay.TypeOverlayItem;
import net.naonedbus.fragment.impl.SearchFragment;
import net.naonedbus.intent.ParamIntent;
import net.naonedbus.widget.ModalSearchView;
import net.simonvt.menudrawer.MenuDrawer;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;

public class SearchActivity extends OneFragmentSlidingActivity {

	private ModalSearchView mModalSearchView;

	public SearchActivity() {
		super(R.layout.activity_one_fragment);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			addFragment(SearchFragment.class);
		}

		final SearchFragment searchFragment = (SearchFragment) getCurrentFragment();
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setCustomView(R.layout.search_view);

		mModalSearchView = (ModalSearchView) actionBar.getCustomView();
		mModalSearchView.setOnQueryTextListener(searchFragment);

		final Intent queryIntent = getIntent();
		final String queryAction = queryIntent.getAction();
		if (Intent.ACTION_SEARCH.equals(queryAction)) {
			if (queryIntent.getData() == null) {
				// Lancer la recherche
				final String query = queryIntent.getStringExtra(SearchManager.QUERY);
				mModalSearchView.setText(query);
				Toast.makeText(this, query, Toast.LENGTH_SHORT).show();
			} else {
				final Integer selectedItemId = Integer.valueOf(queryIntent.getStringExtra(SearchManager.QUERY));
				final TypeOverlayItem selectedItemType = TypeOverlayItem.getById(Integer.valueOf(queryIntent
						.getStringExtra(SearchManager.EXTRA_DATA_KEY)));

				if (selectedItemType.equals(TypeOverlayItem.TYPE_STATION)) {
					// Afficher les parcours de l'arrêt sélectionné
					final ParamIntent intent = new ParamIntent(this, ParcoursActivity.class);
					intent.putExtra(ParcoursActivity.Param.idStation, selectedItemId);
					startActivity(intent);
				} else {
					// Afficher l'élément sur la carte
					final ParamIntent intent = new ParamIntent(this, MapActivity.class);
					intent.putExtra(MapActivity.Param.itemId, selectedItemId);
					intent.putExtra(MapActivity.Param.itemType, selectedItemType.getId());
					startActivity(intent);
				}
				finish();
			}
		}
	}

	@Override
	public void onDrawerStateChange(final int oldState, final int newState) {
		if (newState == MenuDrawer.STATE_CLOSED) {
			mModalSearchView.requestFocus();
			final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

}
