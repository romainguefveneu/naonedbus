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
package net.naonedbus.helper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import net.naonedbus.helper.BulkLoaderHelper.BulkQuery;
import net.naonedbus.sql.LiteScript;
import android.content.Context;

/**
 * @author romain.guefveneu
 * 
 */
public class CompressedQueriesHelper {

	private final Context mContext;
	private final LiteScript mLiteScript;

	public CompressedQueriesHelper(final Context context) {
		mContext = context;
		mLiteScript = new LiteScript();
	}

	/**
	 * @param newVersion
	 *            The new version of the database
	 */
	public void setOldVersion(final int oldVersion) {
		mLiteScript.setOldVersion(oldVersion);
	}

	/**
	 * @param newVersion
	 *            The new version of the database
	 */
	public void setNewVersion(final int newVersion) {
		mLiteScript.setNewVersion(newVersion);
	}

	/**
	 * Récupérer les requêtes d'un des fichiers de data.zip
	 * 
	 * @param resId
	 * @return La liste des requêtes.
	 */
	public List<String> getQueries(final int resId) throws IOException {
		final InputStream inputStream = mContext.getResources().openRawResource(resId);
		final List<String> queries = mLiteScript.getQueries(inputStream);
		inputStream.close();
		return queries;
	}

	/**
	 * Récupérer les requêtes Bulk d'un des fichiers de data.zip
	 * 
	 * @param resId
	 * @return La liste des requêtes Bulk
	 */
	public List<BulkQuery> getBulkQueries(final int resId) throws IOException {
		final InputStream inputStream = this.mContext.getResources().openRawResource(resId);
		final BulkLoaderHelper bulkLoaderHelper = new BulkLoaderHelper(inputStream);
		return bulkLoaderHelper.getQueries();
	}

}
