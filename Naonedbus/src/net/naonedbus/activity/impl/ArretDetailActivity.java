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
import net.naonedbus.bean.Arret;
import net.naonedbus.bean.Ligne;
import net.naonedbus.bean.Sens;
import net.naonedbus.fragment.impl.ArretDetailFragment;
import net.naonedbus.fragment.impl.ArretDetailFragment.OnSensChangeListener;
import net.naonedbus.helper.HeaderHelper;
import net.naonedbus.manager.impl.LigneManager;
import net.naonedbus.manager.impl.SensManager;
import net.naonedbus.utils.SymbolesUtils;
import android.annotation.SuppressLint;
import android.os.Bundle;

public class ArretDetailActivity extends OneFragmentActivity implements OnSensChangeListener {

	public static final String PARAM_LIGNE = "ligne";
	public static final String PARAM_SENS = "sens";
	public static final String PARAM_ARRET = "arret";

	public static final String BUNDLE_KEY_SENS = "net.naonedbus.activity.impl.ArretDetailActivity:sens";

	private HeaderHelper mHeaderHelper;
	private Sens mSens;

	public ArretDetailActivity() {
		super(R.layout.activity_horaires);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Arret arret = getIntent().getParcelableExtra(PARAM_ARRET);
		Ligne ligne = getIntent().getParcelableExtra(PARAM_LIGNE);
		Sens sens;
		if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_KEY_SENS)) {
			sens = savedInstanceState.getParcelable(BUNDLE_KEY_SENS);
		} else {
			sens = getIntent().getParcelableExtra(PARAM_SENS);
		}

		if (ligne == null) {
			final LigneManager ligneManager = LigneManager.getInstance();
			ligne = ligneManager.getSingle(getContentResolver(), arret.codeLigne);
		}
		if (sens == null) {
			final SensManager sensManager = SensManager.getInstance();
			sens = sensManager.getSingle(getContentResolver(), arret.codeLigne, arret.codeSens);
		}
		mSens = sens;

		if (savedInstanceState == null) {
			final Bundle bundle = new Bundle();
			bundle.putParcelable(ArretDetailFragment.PARAM_LIGNE, ligne);
			bundle.putParcelable(ArretDetailFragment.PARAM_SENS, sens);
			bundle.putParcelable(ArretDetailFragment.PARAM_ARRET, arret);

			addFragment(ArretDetailFragment.class, bundle);
		}

		mHeaderHelper = new HeaderHelper(this);
		mHeaderHelper.setBackgroundColor(ligne.couleurBackground, ligne.couleurTexte);
		mHeaderHelper.setCode(ligne.lettre);
		mHeaderHelper.setTitle(arret.nomArret);
		mHeaderHelper.setSubTitle(SymbolesUtils.formatSens(sens.text));
	}

	@Override
	public void onSensChange(final Sens newSens) {
		mHeaderHelper.setSubTitleAnimated(SymbolesUtils.formatSens(newSens.text));
		mSens = newSens;
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		outState.putParcelable(BUNDLE_KEY_SENS, mSens);
		super.onSaveInstanceState(outState);
	}

	@SuppressLint("NewApi")
	@Override
	protected void onResume() {
		super.onResume();
		invalidateOptionsMenu();
	}

}
