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
import net.naonedbus.activity.SlidingMenuActivity;
import net.naonedbus.fragment.impl.BicloosFragment;
import net.naonedbus.fragment.impl.CoVoituragesFragment;
import net.naonedbus.fragment.impl.LilasFragment;
import net.naonedbus.fragment.impl.MargueritesFragment;
import android.os.Bundle;

public class EquipementsActivity extends SlidingMenuActivity {

	private static int[] titles = new int[] { R.string.title_fragment_bicloos, R.string.title_fragment_marguerites,
			R.string.title_fragment_covoiturage, R.string.title_fragment_lila };

	private static Class<?>[] classes = new Class<?>[] { BicloosFragment.class, MargueritesFragment.class,
			CoVoituragesFragment.class, LilasFragment.class };

	public EquipementsActivity() {
		super(R.layout.activity_tabs);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			addFragments(titles, classes);
		}
	}

}
