package net.naonedbus.backup;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import net.naonedbus.manager.impl.FavoriManager;

import org.apache.commons.io.IOUtils;

import android.annotation.TargetApi;
import android.app.backup.BackupAgent;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.os.ParcelFileDescriptor;
import android.util.Log;

@TargetApi(8)
public class NaoBackupAgent extends BackupAgent {

	private static final String LOG_TAG = "NaoBackupAgent";

	@Override
	public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState)
			throws IOException {
		Log.i(LOG_TAG, "Backup en cours...");
		final FavoriManager favoriManager = FavoriManager.getInstance();
		final String favoriJson = favoriManager.toJson(getContentResolver());

		Log.i(LOG_TAG, "\t " + favoriJson);

		FileOutputStream output = null;
		try {
			output = new FileOutputStream(newState.getFileDescriptor());
			IOUtils.write(favoriJson, output);
		} catch (IOException e) {
			Log.e(LOG_TAG, "Erreur lors du backup", e);
		} finally {
			IOUtils.closeQuietly(output);
		}

	}

	@Override
	public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException {
		Log.i(LOG_TAG, "Restoration en cours...");
		final FavoriManager favoriManager = FavoriManager.getInstance();

		FileInputStream input = null;
		try {
			input = new FileInputStream(newState.getFileDescriptor());
			final String favoriJson = IOUtils.toString(input);
			Log.i(LOG_TAG, "\t" + favoriJson);
			favoriManager.fromJson(getContentResolver(), favoriJson);
		} catch (IOException e) {
			Log.e(LOG_TAG, "Erreur lors de la restoration", e);
		} finally {
			IOUtils.closeQuietly(input);
		}
	}

}
