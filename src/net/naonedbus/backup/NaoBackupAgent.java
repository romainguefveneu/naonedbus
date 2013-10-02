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
package net.naonedbus.backup;

import java.io.IOException;

import net.naonedbus.BuildConfig;
import net.naonedbus.manager.impl.FavoriManager;
import android.annotation.TargetApi;
import android.app.backup.BackupAgent;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.os.ParcelFileDescriptor;
import android.util.Log;

@TargetApi(8)
public class NaoBackupAgent extends BackupAgent {

	private static final String LOG_TAG = "NaoBackupAgent";
	private static final boolean DBG = BuildConfig.DEBUG;

	private static final String BACKUP_KEY = "favoris";

	@Override
	public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState)
			throws IOException {
		if (DBG)
			Log.i(LOG_TAG, "Backup en cours...");

		final FavoriManager favoriManager = FavoriManager.getInstance();
		final String favoriJson = favoriManager.toJson(getContentResolver());

		if (DBG)
			Log.i(LOG_TAG, "\t " + favoriJson);

		final byte[] favoriBytes = favoriJson.getBytes();
		data.writeEntityHeader(BACKUP_KEY, favoriBytes.length);
		data.writeEntityData(favoriBytes, favoriBytes.length);
	}

	@Override
	public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException {
		if (DBG)
			Log.i(LOG_TAG, "Restauration en cours...");

		final FavoriManager favoriManager = FavoriManager.getInstance();

		while (data.readNextHeader()) {
			if (data.getKey().equals(BACKUP_KEY)) {
				final int dataSize = data.getDataSize();
				final byte[] buffer = new byte[dataSize];
				data.readEntityData(buffer, 0, dataSize);

				final String favoriJson = new String(buffer);
				favoriManager.setRestoredFavoris(favoriJson);

				if (DBG)
					Log.i(LOG_TAG, "\t" + favoriJson);
			}
		}
	}

}
