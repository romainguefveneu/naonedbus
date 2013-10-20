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
import net.naonedbus.activity.OneFragmentActivity;
import net.naonedbus.bean.Route;
import net.naonedbus.fragment.impl.PlanFragment;
import net.naonedbus.manager.impl.LigneManager;
import android.os.Bundle;

public class PlanActivity extends OneFragmentActivity {

	public static final String PARAM_CODE_LIGNE = "codeLigne";

	public PlanActivity() {
		super(R.layout.activity_one_fragment);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setHomeButtonEnabled(true);

		final String codeLigne = getIntent().getStringExtra(PARAM_CODE_LIGNE);
		final Route ligne = LigneManager.getInstance().getSingle(getContentResolver(), codeLigne);

		setTitle(getString(R.string.title_activity_plan, ligne.getLetter(), ligne.getName()));

		final Bundle bundle = new Bundle();
		bundle.putString(PlanFragment.PARAM_CODE_LIGNE, codeLigne);

		if (savedInstanceState == null) {
			addFragment(PlanFragment.class, bundle);
		}

	}
}
