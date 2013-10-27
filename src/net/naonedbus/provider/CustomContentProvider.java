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

import net.naonedbus.BuildConfig;
import net.naonedbus.manager.impl.UpdaterManager;
import net.naonedbus.manager.impl.UpdaterManager.UpdateType;
import net.naonedbus.utils.TimeLogUtils;

import org.apache.commons.io.IOUtils;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public abstract class CustomContentProvider extends ContentProvider {
	private static CoreDatabase sDatabase;

	@Override
	public boolean onCreate() {
		synchronized (this) {
			if (sDatabase == null) {
				sDatabase = new CoreDatabase(getContext());
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
		return sDatabase.getReadableDatabase();
	}

	protected SQLiteDatabase getWritableDatabase() {
		return sDatabase.getWritableDatabase();
	}

	@Override
	public abstract int update(Uri uri, ContentValues values, String selection, String[] selectionArgs);

	protected static class CoreDatabase extends SQLiteOpenHelper {
		private static final String LOG_TAG = "CoreDatabase";
		private static final boolean DBG = BuildConfig.DEBUG;

		private static final int DB_VERSION = DatabaseVersions.CURRENT;
		private static final String DB_NAME = "data.db";

		private final Context mContext;

		public CoreDatabase(final Context context) {
			super(context, DB_NAME, null, DB_VERSION);
			mContext = context;
			createDatabase();
		}

		@Override
		public void onOpen(final SQLiteDatabase db) {
			super.onOpen(db);
			if (!db.isReadOnly()) {
				// Enable foreign key constraints
				db.execSQL("PRAGMA foreign_keys=ON;");
			}
		}

		public void createDatabase() {
			final File dbFile = mContext.getDatabasePath(DB_NAME);

			final UpdaterManager updaterManager = new UpdaterManager();
			final UpdateType updateType = updaterManager.needUpdate(mContext);

			if (!dbFile.exists() || updateType.equals(UpdateType.UPGRADE)) {
				copyDatabase(dbFile);
			}

			updaterManager.saveCurrentVersion(mContext);
		}

		public void copyDatabase(File dbFile) {
			try {
				TimeLogUtils timeLogUtils;

				if (DBG) {
					timeLogUtils = new TimeLogUtils(LOG_TAG);
					timeLogUtils.start();
				}

				dbFile.getParentFile().mkdirs();
				dbFile.createNewFile();
				final OutputStream outputStream = new FileOutputStream(mContext.getDatabasePath(DB_NAME));
				IOUtils.copy(mContext.getAssets().open(DB_NAME), outputStream);
				IOUtils.closeQuietly(outputStream);

				if (DBG) {
					timeLogUtils.step("Fin d'installation");
				}
			} catch (final IOException e) {
				throw new RuntimeException("Error creating source database", e);
			}
		}

		@Override
		public void onCreate(final SQLiteDatabase db) {
		}

		@Override
		public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
		}

	}

}
