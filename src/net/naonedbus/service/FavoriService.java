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
package net.naonedbus.service;

import net.naonedbus.R;
import net.naonedbus.activity.impl.MainActivity;
import net.naonedbus.manager.impl.StopBookmarkManager;
import net.naonedbus.rest.controller.impl.FavoriController;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.bugsense.trace.BugSenseHandler;

public class FavoriService extends IntentService {

	public static final String INTENT_ACTION_IMPORT = "import";
	public static final String INTENT_ACTION_EXPORT = "export";
	public static final String INTENT_PARAM_KEY = "key";

	public static final String ACTION_EXPORTED = "net.naonedbus.action.FAVORIS_EXPORTED";

	private static final String LOG_TAG = "FavoriService";

	private static final int NOTIFICATION_ID = 0;

	private NotificationCompat.Builder mNotification;
	private NotificationManager mNotificationManager;

	public FavoriService() {
		super("FavoriService");
	}

	@Override
	protected void attachBaseContext(final Context base) {
		super.attachBaseContext(base);
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	}

	@Override
	protected void onHandleIntent(final Intent intent) {

		final String key;
		if (INTENT_ACTION_IMPORT.equals(intent.getAction())) {
			key = intent.getStringExtra(INTENT_PARAM_KEY);
			importFavoris(key);
		} else if (INTENT_ACTION_EXPORT.equals(intent.getAction())) {
			exportFavoris();
		}
	}

	private void importFavoris(final String key) {
		final StopBookmarkManager favoriManager = StopBookmarkManager.getInstance();

		showNotification(R.string.bookmarks_import, R.string.import_ongoing);

		try {
			favoriManager.importFavoris(getContentResolver(), key);
		} catch (final Exception e) {
			BugSenseHandler.sendExceptionMessage("Erreur lors de l'import des favoris", null, e);
			Log.e(LOG_TAG, "Erreur lors de l'import des favoris", e);
			showNotification(R.string.bookmarks_import, R.string.bookmarks_import_fail);
			return;
		}

		showNotification(R.string.bookmarks_import, R.string.bookmarks_import_success);
		cancelNotification();
	}

	private void exportFavoris() {
		final StopBookmarkManager favoriManager = StopBookmarkManager.getInstance();
		final FavoriController favoriController = new FavoriController();
		String key = null;

		boolean succeed;

		showNotification(R.string.bookmarks_export, R.string.export_ongoing);
		try {
			final String content = favoriManager.toJson(getContentResolver());
			key = favoriController.post(content);
			succeed = (key != null);
		} catch (final Exception e) {
			BugSenseHandler.sendExceptionMessage("Erreur lors de l'export des favoris", null, e);
			Log.e(LOG_TAG, "Erreur lors de l'export des favoris", e);
			showNotification(R.string.bookmarks_export, R.string.bookmarks_export_fail);
			succeed = false;
		}

		if (succeed) {
			showResultNotification(R.string.bookmarks_export, R.string.bookmarks_export_success, key);

			final Intent broadcast = new Intent(ACTION_EXPORTED);
			broadcast.putExtra(INTENT_PARAM_KEY, key);
			getApplicationContext().sendBroadcast(broadcast);
		}
	}

	private void showNotification(final int title, final int message) {
		if (mNotification == null) {
			createNotification(title, message, null);
		} else {
			mNotification.setContentTitle(getString(title)).setContentText(getString(message))
					.setTicker(getString(message));
			mNotificationManager.notify(NOTIFICATION_ID, mNotification.build());
		}
	}

	private void showResultNotification(final int title, final int message, final String key) {
		cancelNotification();
		createNotification(title, message, key);
	}

	private void createNotification(final int title, final int message, final String key) {
		final Intent resultIntent = new Intent(this, MainActivity.class);
		if (key != null) {
			resultIntent.putExtra(INTENT_PARAM_KEY, key);
		}

		final TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(MainActivity.class);
		stackBuilder.addNextIntent(resultIntent);

		final PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

		final String titleString = getString(title);
		final String messageString = getString(message);
		final NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_notification).setContentTitle(titleString).setContentText(messageString)
				.setTicker(messageString).setAutoCancel(true);

		builder.setContentIntent(resultPendingIntent);

		mNotification = builder;

		mNotificationManager.cancel(NOTIFICATION_ID);
		mNotificationManager.notify(NOTIFICATION_ID, builder.build());
	}

	private void cancelNotification() {
		mNotificationManager.cancel(NOTIFICATION_ID);
	}

}
