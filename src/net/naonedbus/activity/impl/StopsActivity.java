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
import net.naonedbus.bean.Route;
import net.naonedbus.bean.Direction;
import net.naonedbus.fragment.impl.StopsFragment;
import net.naonedbus.helper.StateHelper;
import net.naonedbus.manager.impl.DirectionManager;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.utils.FontUtils;
import net.naonedbus.widget.adapter.impl.DirectionSpinnerAdapter;
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

public class StopsActivity extends OneFragmentActivity {

	public static final String PARAM_ROUTE = "route";

	public static interface OnDirectionChanged {
		void onDirectionChanged(Direction direction);
	}

	private StateHelper mStateHelper;
	private OnDirectionChanged mOnDirectionChanged;
	private Route mRoute;
	private Direction mDirection;

	public StopsActivity() {
		super(R.layout.activity_arrets);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		final DirectionManager directionManager = DirectionManager.getInstance();

		mStateHelper = new StateHelper(getApplicationContext());

		mRoute = getIntent().getParcelableExtra(PARAM_ROUTE);

		final List<Direction> directions = directionManager.getAll(getContentResolver(), mRoute.getCode());
		final int directionId = mStateHelper.getSens(mRoute.getCode(), directions.get(0).getId());

		if (savedInstanceState == null) {
			final Bundle bundle = new Bundle();
			bundle.putParcelable(StopsFragment.PARAM_ROUTE, mRoute);

			addFragment(StopsFragment.class, bundle);
		}

		final Typeface robotoBold = FontUtils.getRobotoBoldCondensed(getApplicationContext());

		final View header = findViewById(R.id.headerView);
		header.setBackgroundDrawable(ColorUtils.getGradiant(mRoute.getBackColor()));

		final TextView code = (TextView) findViewById(R.id.itemCode);
		code.setText(mRoute.getLetter());
		code.setTextColor(mRoute.getFrontColor());
		code.setTypeface(robotoBold);

		final Spinner directionTitle = (Spinner) findViewById(R.id.itemTitle);
		directionTitle.setAdapter(new DirectionSpinnerAdapter(this, directions, mRoute.getFrontColor(), robotoBold));
		directionTitle.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(final AdapterView<?> arg0, final View arg1, final int location, final long arg3) {
				final Direction direction = directions.get(location);
				mOnDirectionChanged.onDirectionChanged(direction);
				mDirection = direction;
			}

			@Override
			public void onNothingSelected(final AdapterView<?> arg0) {

			}
		});
		directionTitle.setSelection(getDirectionPosition(directions, directionId));

		final ShowcaseController showcaseController = new ShowcaseController(this,
				(ViewStub) findViewById(R.id.showCaseViewStub), directionTitle);
		showcaseController.setShowcaseButtonResId(R.id.showCaseButton);
		showcaseController.setShotType(ShotType.ONE_SHOT);
		showcaseController.show();

		directionTitle.setOnTouchListener(new OnTouchListener() {
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
		mOnDirectionChanged = (OnDirectionChanged) getCurrentFragment();
	}

	@Override
	protected void onStop() {
		if (mDirection != null)
			mStateHelper.setSens(mRoute.getCode(), mDirection.getId());
		super.onStop();
	}

	private int getDirectionPosition(final List<Direction> sensList, final int id) {
		Direction direction;
		for (int i = 0; i < sensList.size(); i++) {
			direction = sensList.get(i);
			if (direction.getId() == id)
				return i;
		}
		return 0;
	}

}
