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
import net.naonedbus.bean.LiveNews;
import net.naonedbus.widget.indexer.ArraySectionIndexer;
import android.content.Context;
import android.util.SparseIntArray;

public class CommentsIndexer extends ArraySectionIndexer<LiveNews> {

	public static final int SECTION_FUTURE = 0;
	public static final int SECTION_NOW = 1;
	public static final int SECTION_YESTERDAY = 2;
	public static final int SECTION_PAST = 3;

	private static final SparseIntArray SECTIONS_LABELS = new SparseIntArray();
	static {
		SECTIONS_LABELS.put(SECTION_FUTURE, R.string.coming_up);
		SECTIONS_LABELS.put(SECTION_NOW, R.string.today);
		SECTIONS_LABELS.put(SECTION_YESTERDAY, R.string.yesterday);
		SECTIONS_LABELS.put(SECTION_PAST, R.string.previously);
	}

	@Override
	protected String getSectionLabel(final Context context, final LiveNews item) {
		return context.getString(SECTIONS_LABELS.get((Integer) item.getSection()));
	}

	@Override
	protected void prepareSection(final LiveNews item) {
	}

}
