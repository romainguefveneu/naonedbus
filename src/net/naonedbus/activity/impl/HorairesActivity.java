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
import net.naonedbus.fragment.impl.HorairesFragment;
import net.naonedbus.fragment.impl.HorairesFragment.OnSensChangeListener;
import net.naonedbus.helper.HeaderHelper;
import net.naonedbus.manager.impl.LigneManager;
import net.naonedbus.manager.impl.SensManager;
import net.naonedbus.utils.FormatUtils;
import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.view.MenuItem;

public class HorairesActivity extends OneFragmentActivity implements OnSensChangeListener {

	public static final String PARAM_LIGNE = "ligne";
	public static final String PARAM_SENS = "sens";
	public static final String PARAM_ARRET = "arret";
	public static final String PARAM_FROM_WIDGET = "fromWidget";

	private HeaderHelper mHeaderHelper;
	private boolean mFromWidget;

	public HorairesActivity() {
		super(R.layout.activity_horaires);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		final Intent intent = getIntent();
		mFromWidget = intent.getBooleanExtra(PARAM_FROM_WIDGET, false);

		final Arret arret = intent.getParcelableExtra(PARAM_ARRET);
		Ligne ligne = intent.getParcelableExtra(PARAM_LIGNE);
		Sens sens = intent.getParcelableExtra(PARAM_SENS);

		if (ligne == null) {
			final LigneManager ligneManager = LigneManager.getInstance();
			ligne = ligneManager.getSingle(getContentResolver(), arret.getCodeLigne());
		}
		if (sens == null) {
			final SensManager sensManager = SensManager.getInstance();
			sens = sensManager.getSingle(getContentResolver(), arret.getCodeLigne(), arret.getCodeSens());
		}

		if (savedInstanceState == null) {
			final Bundle bundle = new Bundle();
			bundle.putParcelable(HorairesFragment.PARAM_LIGNE, ligne);
			bundle.putParcelable(HorairesFragment.PARAM_SENS, sens);
			bundle.putParcelable(HorairesFragment.PARAM_ARRET, arret);

			addFragment(HorairesFragment.class, bundle);
		}

		mHeaderHelper = new HeaderHelper(this);
		mHeaderHelper.setColor(ligne.getCouleur(), ligne.getCouleurTexte());
		mHeaderHelper.setTitle(arret.getNomArret());
		mHeaderHelper.setSubTitle(FormatUtils.formatSens(ligne.getLettre(), sens.text));
	}

	@Override
	public void onSensChange(final Sens newSens) {
		mHeaderHelper.setSubTitleAnimated(FormatUtils.formatSens(newSens.text));
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (mFromWidget) {
			switch (item.getItemId()) {
			case android.R.id.home:
				final Intent parentActivityIntent = new Intent(this, MainActivity.class);
				parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(parentActivityIntent);
				finish();
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}
}
