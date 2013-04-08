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
package net.naonedbus.provider;

import java.io.IOException;
import java.util.List;

import net.naonedbus.BuildConfig;
import net.naonedbus.R;
import net.naonedbus.helper.BulkLoaderHelper.BulkQuery;
import net.naonedbus.helper.CompressedQueriesHelper;
import net.naonedbus.utils.TimeLogUtils;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.util.Log;

public abstract class CustomContentProvider extends ContentProvider {

	protected static final String AUTHORITY = "naonedbus";

	private static CoreDatabase mDB;

	private static DatabaseActionListener databaseActionListener;

	@Override
	public boolean onCreate() {
		if (mDB == null) {
			mDB = new CoreDatabase(getContext());
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
		return mDB.getReadableDatabase();
	}

	protected SQLiteDatabase getWritableDatabase() {
		return mDB.getWritableDatabase();
	}

	public static synchronized void setDatabaseActionListener(final DatabaseActionListener listener) {
		CustomContentProvider.databaseActionListener = listener;
	}

	@Override
	public abstract int update(Uri uri, ContentValues values, String selection, String[] selectionArgs);

	protected static class CoreDatabase extends SQLiteOpenHelper {
		private static final String LOG_TAG = "CoreDatabase";
		private static final boolean DBG = BuildConfig.DEBUG;

		private static final int DB_VERSION = 11;
		private static final String DB_NAME = "data.db";

		private final CompressedQueriesHelper mCompressedQueriesHelper;

		public CoreDatabase(final Context context) {
			super(context, DB_NAME, null, DB_VERSION);
			mCompressedQueriesHelper = new CompressedQueriesHelper(context);
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
				CustomContentProvider.databaseActionListener.onCreate();
			}

			if (DBG) {
				timeLogUtils = new TimeLogUtils(LOG_TAG);
				timeLogUtils.start();
			}

			execute(db, R.raw.sql_create);
			executeBulk(db, R.raw.sql_data);

			if (DBG) {
				timeLogUtils.step("Fin d'installation");
			}
		}

		@Override
		public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
			if (DBG)
				Log.d(LOG_TAG, "Mise à jour de la base de données.");

			TimeLogUtils timeLogUtils;

			if (CustomContentProvider.databaseActionListener != null) {
				CustomContentProvider.databaseActionListener.onUpgrade(oldVersion);
			}

			if (DBG) {
				timeLogUtils = new TimeLogUtils(LOG_TAG);
				timeLogUtils.start();
			}
			execute(db, R.raw.sql_before_update, R.raw.sql_create);
			executeBulk(db, R.raw.sql_data);
			execute(db, R.raw.sql_after_update);

			if (DBG)
				timeLogUtils.step("Fin de la mise à jour");

		}

		/**
		 * Executer des fichiers SQL sur la base de données.
		 * 
		 * @param db
		 *            La base de données.
		 * @param resIds
		 *            Les ids des scripts à exécuter, dans l'ordre.
		 */
		private void execute(final SQLiteDatabase db, final int... resIds) {
			try {
				for (final int resId : resIds) {

					if (DBG)
						Log.i(LOG_TAG, "Execution du script " + resId);

					final String[] queries = mCompressedQueriesHelper.getQueries(resId);

					for (final String query : queries) {
						if (query.trim().length() > 0) {
							db.execSQL(query);
						}
					}

				}
			} catch (final IOException e) {
				Log.e(LOG_TAG, "Erreur à l'execution", e);
			}
		}

		/**
		 * Executer des fichiers SQL sur la base de données, en mode Bulk.
		 * 
		 * @param db
		 *            La base de données.
		 * @param resIds
		 *            Les ids des scripts à exécuter, dans l'ordre.
		 */
		private void executeBulk(final SQLiteDatabase db, final int... resIds) {
			SQLiteStatement statement;
			List<BulkQuery> bulkQueries;

			try {

				for (final int resId : resIds) {
					if (DBG)
						Log.i(LOG_TAG, "Execution du script " + resId);

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

			} catch (final IOException e) {
				Log.e(LOG_TAG, "Erreur à l'execution", e);
			}
		}
	}

}
