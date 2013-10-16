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

import java.util.List;

import net.naonedbus.R;
import net.naonedbus.activity.OneFragmentActivity;
import net.naonedbus.bean.Ligne;
import net.naonedbus.bean.Sens;
import net.naonedbus.fragment.impl.ArretsFragment;
import net.naonedbus.helper.StateHelper;
import net.naonedbus.manager.impl.SensManager;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.utils.FontUtils;
import net.naonedbus.widget.adapter.impl.SensSpinnerAdapter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.espian.showcaseview.ShowcaseController;
import com.espian.showcaseview.ShowcaseController.ShotType;

public class ArretsActivity extends OneFragmentActivity {

	public static final String PARAM_LIGNE = "ligne";

	public static interface OnChangeSens {
		void onChangeSens(Sens sens);
	}

	private StateHelper mStateHelper;
	private OnChangeSens mOnChangeSens;
	private Ligne mLigne;
	private Sens mCurrentSens;

	public ArretsActivity() {
		super(R.layout.activity_arrets);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		final SensManager sensManager = SensManager.getInstance();

		mStateHelper = new StateHelper(getApplicationContext());

		mLigne = getIntent().getParcelableExtra(PARAM_LIGNE);

		final List<Sens> sensList = sensManager.getAll(getContentResolver(), mLigne.getCode());
		final int idSens = mStateHelper.getSens(mLigne.getCode(), sensList.get(0)._id);

		if (savedInstanceState == null) {
			final Bundle bundle = new Bundle();
			bundle.putParcelable(ArretsFragment.PARAM_LIGNE, mLigne);

			addFragment(ArretsFragment.class, bundle);
		}

		final Typeface robotoBold = FontUtils.getRobotoBoldCondensed(getApplicationContext());

		final View header = findViewById(R.id.headerView);
		header.setBackgroundDrawable(ColorUtils.getGradiant(mLigne.getCouleur()));

		final TextView code = (TextView) findViewById(R.id.itemCode);
		code.setText(mLigne.getLettre());
		code.setTextColor(mLigne.getCouleurTexte());
		code.setTypeface(robotoBold);

		final Spinner sensTitle = (Spinner) findViewById(R.id.itemTitle);
		sensTitle.setAdapter(new SensSpinnerAdapter(this, sensList, mLigne.getCouleurTexte(), robotoBold));
		sensTitle.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(final AdapterView<?> arg0, final View arg1, final int location, final long arg3) {
				final Sens sens = sensList.get(location);
				mOnChangeSens.onChangeSens(sens);
				mCurrentSens = sens;
			}

			@Override
			public void onNothingSelected(final AdapterView<?> arg0) {

			}
		});
		sensTitle.setSelection(getSensPosition(sensList, idSens));

		final ShowcaseController showcaseController = new ShowcaseController(this,
				(ViewStub) findViewById(R.id.showCaseViewStub), sensTitle);
		showcaseController.setShowcaseButtonResId(R.id.showCaseButton);
		showcaseController.setShotType(ShotType.NO_LIMIT);
		showcaseController.show();

		sensTitle.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(final View v, final MotionEvent event) {
				showcaseController.hide();
				return false;
			}
		});
	}

	@Override
	protected void onPostCreate(final Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mOnChangeSens = (OnChangeSens) getCurrentFragment();
	}

	@Override
	protected void onStop() {
		if (mCurrentSens != null)
			mStateHelper.setSens(mLigne.getCode(), mCurrentSens._id);
		super.onStop();
	}

	private int getSensPosition(final List<Sens> sensList, final int id) {
		Sens sens;
		for (int i = 0; i < sensList.size(); i++) {
			sens = sensList.get(i);
			if (sens._id == id)
				return i;
		}
		return 0;
	}

}
