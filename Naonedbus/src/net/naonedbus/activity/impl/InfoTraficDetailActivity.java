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
import net.naonedbus.bean.InfoTrafic;
import net.naonedbus.fragment.impl.InfoTraficDetailFragment;
import android.os.Bundle;

public class InfoTraficDetailActivity extends OneFragmentActivity {

	public static final String PARAM_INFO_TRAFIC = "infoTrafic";

	public InfoTraficDetailActivity() {
		super(R.layout.activity_one_fragment);
	}

	private InfoTrafic mInfoTrafic;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mInfoTrafic = getIntent().getParcelableExtra(PARAM_INFO_TRAFIC);

		final Bundle bundle = new Bundle();
		bundle.putParcelable(InfoTraficDetailFragment.PARAM_INFO_TRAFIC, mInfoTrafic);

		if (savedInstanceState == null) {
			addFragment(InfoTraficDetailFragment.class, bundle);
		}
	}

}
