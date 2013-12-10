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
import net.naonedbus.fragment.impl.SchedulesFragment;
import net.naonedbus.fragment.impl.SchedulesFragment.OnDirectionChangedListener;
import net.naonedbus.helper.HeaderHelper;
import net.naonedbus.manager.impl.DirectionManager;
import net.naonedbus.manager.impl.RouteManager;
import net.naonedbus.utils.FormatUtils;
import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.view.MenuItem;

public class SchedulesActivity extends OneFragmentActivity implements OnDirectionChangedListener {

	public static final String PARAM_LIGNE = "route";
	public static final String PARAM_SENS = "direction";
	public static final String PARAM_ARRET = "stop";
	public static final String PARAM_FROM_WIDGET = "fromWidget";

	private HeaderHelper mHeaderHelper;
	private boolean mFromWidget;

	public SchedulesActivity() {
		super(R.layout.activity_schedules);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		final Intent intent = getIntent();
		mFromWidget = intent.getBooleanExtra(PARAM_FROM_WIDGET, false);

		final Stop stop = intent.getParcelableExtra(PARAM_ARRET);
		Route route = intent.getParcelableExtra(PARAM_LIGNE);
		Direction direction = intent.getParcelableExtra(PARAM_SENS);

		if (route == null) {
			final RouteManager ligneManager = RouteManager.getInstance();
			route = ligneManager.getSingle(getContentResolver(), stop.getCodeLigne());
		}
		if (direction == null) {
			final DirectionManager sensManager = DirectionManager.getInstance();
			direction = sensManager.getSingle(getContentResolver(), stop.getCodeLigne(), stop.getCodeSens());
		}

		if (savedInstanceState == null) {
			final Bundle bundle = new Bundle();
			bundle.putParcelable(SchedulesFragment.PARAM_ROUTE, route);
			bundle.putParcelable(SchedulesFragment.PARAM_DIRECTION, direction);
			bundle.putParcelable(SchedulesFragment.PARAM_STOP, stop);

			addFragment(SchedulesFragment.class, bundle);
		}

		mHeaderHelper = new HeaderHelper(this);
		mHeaderHelper.setColor(route.getBackColor(), route.getFrontColor());
		mHeaderHelper.setTitle(stop.getName() + " " + stop.getCodeArret());
		mHeaderHelper.setSubTitle(FormatUtils.formatSens(route.getLetter(), direction.getName()));
	}

	@Override
	public void onDirectionChanged(final Direction newDirection) {
		mHeaderHelper.setSubTitleAnimated(FormatUtils.formatSens(newDirection.getName()));
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
