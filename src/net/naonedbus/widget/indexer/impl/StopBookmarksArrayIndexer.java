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
package net.naonedbus.widget.indexer.impl;

import net.naonedbus.R;
import net.naonedbus.bean.StopBookmark;
import net.naonedbus.widget.indexer.ArraySectionIndexer;
import android.content.Context;
import android.util.SparseArray;

public class StopBookmarksArrayIndexer extends ArraySectionIndexer<StopBookmark> {

	private final SparseArray<String> mGroupes;

	public StopBookmarksArrayIndexer(final SparseArray<String> groupes) {
		mGroupes = groupes;
	}

	@Override
	protected void prepareSection(final StopBookmark item) {
	}

	@Override
	protected String getSectionLabel(final Context context, final StopBookmark item) {
		final Integer section = (Integer) item.getSection();
		if (section == -1) {
			return context.getString(R.string.no_group);
		} else {
			return mGroupes.get(section);
		}
	}

}
