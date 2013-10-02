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
package net.naonedbus;

import net.naonedbus.provider.impl.MyLocationProvider;
import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class NBApplication extends Application {

	public static final String LOG_TAG = "naonedbus";

	public static final String PREF_PARKINGS_SORT = "parkings.sort";
	public static final String PREF_FAVORIS_SORT = "favoris.sort";
	public static final String PREF_FAVORIS_IMPORT = "favoris.import";
	public static final String PREF_FAVORIS_EXPORT = "favoris.export";
	public static final String PREF_CALENDRIER_DEFAUT = "calendrier.defaut";
	public static final String PREF_MAP_SATELLITE = "map.satellite";
	public static final String PREF_PLAN_CACHE = "plan.cache";
	public static final String PREF_NAVIGATION_HOME = "navigation.home";

	private static MyLocationProvider sMyLocationProvider;
	private static SharedPreferences sPreferences;

	private static boolean sIsSetup;

	@Override
	public void onCreate() {
		super.onCreate();
		// Accélérer le chargement des local et réduire la conso mémoire
		System.setProperty("org.joda.time.DateTimeZone.Provider",
				"net.naonedbus.provider.impl.FastDateTimeZoneProvider");

		if (sMyLocationProvider == null) {
			sMyLocationProvider = new MyLocationProvider(getApplicationContext());
		}

		if (sPreferences == null) {
			sPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		}

	}

	public static MyLocationProvider getLocationProvider() {
		return sMyLocationProvider;
	}

	public static SharedPreferences getPreferences() {
		return sPreferences;
	}

	public static void setSetup(final boolean setup) {
		sIsSetup = setup;
	}

	public static boolean isSetup() {
		return sIsSetup;
	}

}
