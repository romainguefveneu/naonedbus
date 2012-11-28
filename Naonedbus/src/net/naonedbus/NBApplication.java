/**
 *  Copyright (C) 2011 Romain Guefveneu
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

	public static final int THEME_LIGHT = 0;
	public static final int THEME_DARK = 1;
	public static final int[] THEMES_RES = new int[] { R.style.Theme_Acapulco_Light, R.style.Theme_Acapulco_Dark };
	public static final int[] THEMES_MENU_RES = new int[] { R.style.Theme_Acapulco_Light_HomeAsMenu,
			R.style.Theme_Acapulco_Dark_HomeAsMenu };

	public static int THEME = R.style.Theme_Acapulco_Light_HomeAsMenu;

	public static final String PREF_THEME = "theme";
	public static final String PREF_PARKINGS_SORT = "parkings.sort";
	public static final String PREF_FAVORIS_SORT = "favoris.sort";
	public static final String PREF_FAVORIS_IMPORT = "favoris.import";
	public static final String PREF_FAVORIS_EXPORT = "favoris.export";
	public static final String PREF_CALENDRIER_DEFAUT = "calendrier.defaut";
	public static final String PREF_MAP_SATELLITE = "map.satellite";
	public static final String PREF_PLAN_CACHE = "plan.cache";

	private static MyLocationProvider myLocationProvider;
	private static SharedPreferences preferences;

	@Override
	public void onCreate() {
		super.onCreate();
		// Accélérer le chargement des local et réduire la conso mémoire
		System.setProperty("org.joda.time.DateTimeZone.Provider",
				"net.naonedbus.provider.impl.FastDateTimeZoneProvider");

		if (myLocationProvider == null) {
			myLocationProvider = new MyLocationProvider(getApplicationContext());
		}

		if (preferences == null) {
			preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		}

		// Définir le thème
		NBApplication.THEME = Integer.valueOf(preferences.getString(PREF_THEME, "0"));
	}

	public static MyLocationProvider getLocationProvider() {
		return myLocationProvider;
	}

	public static SharedPreferences getPreferences() {
		return preferences;
	}

}
