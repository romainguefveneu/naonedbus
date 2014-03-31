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
package net.naonedbus.provider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import net.naonedbus.BuildConfig;
import net.naonedbus.R;
import net.naonedbus.helper.BulkLoaderHelper.BulkQuery;
import net.naonedbus.helper.CompressedQueriesHelper;
import net.naonedbus.utils.TimeLogUtils;

import org.apache.commons.io.IOUtils;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.util.Log;

import com.bugsense.trace.BugSenseHandler;

public abstract class CustomContentProvider extends ContentProvider {

	private static CoreDatabase database;
	private static DatabaseActionObserver databaseActionListener;

	@Override
	public boolean onCreate() {
		synchronized (this) {
			if (database == null) {
				database = new CoreDatabase(getContext());
			}
		}
		return true;
	}

	@Override
	public abstract int delete(Uri uri, String selection, String[] selectionArgs);

	@Override
	public abstract String getType(Uri uri);

	@Override
	public abstract Uri insert(Uri uri, ContentValues values);

	@Override
	public abstract Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder);

	protected SQLiteDatabase getReadableDatabase() {
		return database.getReadableDatabase();
	}

	protected SQLiteDatabase getWritableDatabase() {
		return database.getWritableDatabase();
	}

	public static synchronized void setDatabaseActionListener(final DatabaseActionObserver listener) {
		CustomContentProvider.databaseActionListener = listener;
	}

	@Override
	public abstract int update(Uri uri, ContentValues values, String selection, String[] selectionArgs);

	protected static class CoreDatabase extends SQLiteOpenHelper {
		private static final String LOG_TAG = "CoreDatabase";
		private static final boolean DBG = BuildConfig.DEBUG;

		private static final int DB_VERSION = DatabaseVersions.CURRENT;
		private static final String DB_NAME = "data.db";

		private final Context mContext;
		private final CompressedQueriesHelper mCompressedQueriesHelper;

		private boolean mSuccess = true;

		public CoreDatabase(final Context context) {
			super(context, DB_NAME, null, DB_VERSION);
			mContext = context;
			mCompressedQueriesHelper = new CompressedQueriesHelper(context);
			mCompressedQueriesHelper.setNewVersion(DB_VERSION);
		}

		@Override
		public void onOpen(final SQLiteDatabase db) {
			super.onOpen(db);
			if (!db.isReadOnly()) {
				// Enable foreign key constraints
				db.execSQL("PRAGMA foreign_keys=ON;");
			}
		}

		@Override
		public void onCreate(final SQLiteDatabase db) {
			if (DBG)
				Log.d(LOG_TAG, "Création de la base de données.");

			TimeLogUtils timeLogUtils;

			if (CustomContentProvider.databaseActionListener != null) {
				CustomContentProvider.databaseActionListener.dispatchCreate();
			}

			if (DBG) {
				timeLogUtils = new TimeLogUtils(LOG_TAG);
				timeLogUtils.start();
			}

			createDatabase(db);

			if (CustomContentProvider.databaseActionListener != null) {
				CustomContentProvider.databaseActionListener.dispatchUpgradeDone(mSuccess);
			}

			if (DBG) {
				timeLogUtils.step("Fin d'installation");
			}
		}

		private void createDatabase(final SQLiteDatabase db) {
			try {

				execute(db, R.raw.sql_create);
				executeBulk(db, R.raw.sql_data);

			} catch (final Exception e) {
				BugSenseHandler.sendExceptionMessage("Erreur lors de création de la base.", "", e);
				Log.e(LOG_TAG, "Erreur lors de création de la base.", e);
			}
		}

		@Override
		public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
			if (DBG)
				Log.d(LOG_TAG, "Mise à jour de la base de données.");

			mCompressedQueriesHelper.setOldVersion(oldVersion);

			TimeLogUtils timeLogUtils;

			if (CustomContentProvider.databaseActionListener != null) {
				CustomContentProvider.databaseActionListener.dispatchUpgrade(oldVersion);
			}

			if (DBG) {
				timeLogUtils = new TimeLogUtils(LOG_TAG);
				timeLogUtils.start();
			}

			try {

				execute(db, R.raw.sql_before_update, R.raw.sql_create);
				executeBulk(db, R.raw.sql_data);
				execute(db, R.raw.sql_after_update);

			} catch (final Exception e) {
				BugSenseHandler.sendExceptionMessage("Erreur lors de la mise à jour de la base.", "", e);
				Log.e(LOG_TAG, "Erreur lors de la mise à jour de la base.", e);
				handleUpgradeException();
			} finally {
				if (DBG)
					timeLogUtils.step("Fin de la mise à jour");
			}

		}

		private void handleUpgradeException() {
			if (DBG)
				Log.i(LOG_TAG, "Recréation de la base de donnée.");

			mSuccess = false;

			final File dbFile = mContext.getDatabasePath(DB_NAME);
			dbFile.delete();

			final SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbFile.getAbsolutePath(), null);
			db.beginTransaction();
			createDatabase(db);
			db.setTransactionSuccessful();
			db.endTransaction();
		}

		/**
		 * Executer des fichiers SQL sur la base de données.
		 * 
		 * @param db
		 *            La base de données.
		 * @param resIds
		 *            Les ids des scripts à exécuter, dans l'ordre.
		 * @throws IOException
		 */
		private void execute(final SQLiteDatabase db, final int... resIds) throws IOException {
			for (final int resId : resIds) {
				if (resId == 0)
					continue;

				if (DBG)
					Log.i(LOG_TAG, "Execution du script " + mContext.getResources().getResourceName(resId));

				final List<String> queries = mCompressedQueriesHelper.getQueries(resId);

				for (final String query : queries) {
					if (DBG)
						Log.d(LOG_TAG, "\t" + query);

					if (query.trim().length() > 0) {
						db.execSQL(query);
					}
				}

			}
		}

		/**
		 * Executer des fichiers SQL sur la base de données, en mode Bulk.
		 * 
		 * @param db
		 *            La base de données.
		 * @param resIds
		 *            Les ids des scripts à exécuter, dans l'ordre.
		 * @throws IOException
		 */
		private void executeBulk(final SQLiteDatabase db, final int... resIds) throws IOException {
			SQLiteStatement statement;
			List<BulkQuery> bulkQueries;

			for (final int resId : resIds) {
				if (DBG)
					Log.i(LOG_TAG, "Execution du script " + mContext.getResources().getResourceName(resId));

				bulkQueries = mCompressedQueriesHelper.getBulkQueries(resId);

				for (final BulkQuery bulkQuery : bulkQueries) {
					final List<String[]> statementValues = bulkQuery.getValues();
					statement = db.compileStatement(bulkQuery.getPattern());
					for (final String[] values : statementValues) {
						for (int i = 0; i < values.length; i++) {
							statement.bindString(i + 1, values[i]);
						}
						statement.execute();
					}
				}
			}

		}

	}

}
