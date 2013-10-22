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
import net.naonedbus.bean.Direction;
import net.naonedbus.bean.Route;
import net.naonedbus.bean.Stop;
import net.naonedbus.fragment.impl.StopDetailFragment;
import net.naonedbus.fragment.impl.StopDetailFragment.OnDirectionChangedListener;
import net.naonedbus.helper.HeaderHelper;
import net.naonedbus.manager.impl.DirectionManager;
import net.naonedbus.manager.impl.RouteManager;
import net.naonedbus.utils.FormatUtils;
import android.annotation.SuppressLint;
import android.os.Bundle;

public class StopDetailActivity extends OneFragmentActivity implements OnDirectionChangedListener {

	public static final String PARAM_LIGNE = "route";
	public static final String PARAM_SENS = "direction";
	public static final String PARAM_ARRET = "stop";

	public static final String BUNDLE_KEY_SENS = "net.naonedbus.activity.impl.ArretDetailActivity:direction";

	private HeaderHelper mHeaderHelper;
	private Route mRoute;
	private Direction mDirection;

	public StopDetailActivity() {
		super(R.layout.activity_schedules);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Stop stop = getIntent().getParcelableExtra(PARAM_ARRET);
		mRoute = getIntent().getParcelableExtra(PARAM_LIGNE);

		Direction direction;
		if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_KEY_SENS)) {
			direction = savedInstanceState.getParcelable(BUNDLE_KEY_SENS);
		} else {
			direction = getIntent().getParcelableExtra(PARAM_SENS);
		}

		if (mRoute == null) {
			final RouteManager ligneManager = RouteManager.getInstance();
			mRoute = ligneManager.getSingle(getContentResolver(), stop.getCodeLigne());
		}
		if (direction == null) {
			final DirectionManager sensManager = DirectionManager.getInstance();
			direction = sensManager.getSingle(getContentResolver(), stop.getCodeLigne(), stop.getCodeSens());
		}
		mDirection = direction;

		if (savedInstanceState == null) {
			final Bundle bundle = new Bundle();
			bundle.putParcelable(StopDetailFragment.PARAM_ROUTE, mRoute);
			bundle.putParcelable(StopDetailFragment.PARAM_DIRECTION, direction);
			bundle.putParcelable(StopDetailFragment.PARAM_STOP, stop);

			addFragment(StopDetailFragment.class, bundle);
		}

		mHeaderHelper = new HeaderHelper(this);
		mHeaderHelper.setColor(mRoute.getBackColor(), mRoute.getFrontColor());
		mHeaderHelper.setTitle(stop.getName());
		mHeaderHelper.setSubTitle(FormatUtils.formatSens(mRoute.getLetter(), direction.getName()));
	}

	@Override
	public void onDirectionChanged(final Direction newDirection) {
		mHeaderHelper.setSubTitleAnimated(FormatUtils.formatSens(mRoute.getLetter(), newDirection.getName()));
		mDirection = newDirection;
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		outState.putParcelable(BUNDLE_KEY_SENS, mDirection);
		super.onSaveInstanceState(outState);
	}

	@SuppressLint("NewApi")
	@Override
	protected void onResume() {
		super.onResume();
		invalidateOptionsMenu();
	}

}
