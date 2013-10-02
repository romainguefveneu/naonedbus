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
package net.naonedbus.manager.impl;

import net.naonedbus.provider.DatabaseVersions;
import net.naonedbus.provider.impl.UpdaterProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;

/**
 * @author romain.guefveneu
 * 
 */
public class UpdaterManager {

	public enum UpdateType {
		FIRST_LAUNCH, UPGRADE, UP_TO_DATE;
	}

	private static final String LAST_DATABASE_VERSION = "lastDataBaseVersion";
	private static final String SHARED_PREF_NAME = "updateInfo";

	public UpdateType needUpdate(final Context context) {
		final SharedPreferences preferences = context.getSharedPreferences(SHARED_PREF_NAME, 0);
		final int newDatabaseVersion = DatabaseVersions.CURRENT;
		final int oldDatabaseVersion = preferences.getInt(LAST_DATABASE_VERSION, 0);

		if (oldDatabaseVersion < DatabaseVersions.ACAPULCO) {
			return UpdateType.FIRST_LAUNCH;
		} else if (oldDatabaseVersion < newDatabaseVersion) {
			return UpdateType.UPGRADE;
		} else {
			return UpdateType.UP_TO_DATE;
		}
	}

	/**
	 * Déclencher la mise à jour de la base de données si nécessaire.
	 * 
	 * @param contentResolver
	 */
	public void triggerUpdate(final Context context) {
		final Cursor c = context.getContentResolver().query(UpdaterProvider.CONTENT_URI, null, null, null, null);
		if (c != null) {
			c.close();
		}

		final SharedPreferences preferences = context.getSharedPreferences(SHARED_PREF_NAME, 0);
		preferences.edit().putInt(LAST_DATABASE_VERSION, DatabaseVersions.CURRENT).commit();
	}

}
