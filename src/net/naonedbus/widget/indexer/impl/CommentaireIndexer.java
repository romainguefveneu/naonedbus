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
import net.naonedbus.bean.Commentaire;
import net.naonedbus.widget.indexer.ArraySectionIndexer;
import android.content.Context;
import android.util.SparseArray;

public class CommentaireIndexer extends ArraySectionIndexer<Commentaire> {

	public static final Integer SECTION_FUTURE = 0;
	public static final Integer SECTION_NOW = 1;
	public static final Integer SECTION_YESTERDAY = 2;
	public static final Integer SECTION_PAST = 3;

	private static final SparseArray<Integer> SECTIONS_LABELS = new SparseArray<Integer>();
	static {
		SECTIONS_LABELS.put(SECTION_FUTURE, R.string.time_line_after);
		SECTIONS_LABELS.put(SECTION_NOW, R.string.time_line_now);
		SECTIONS_LABELS.put(SECTION_YESTERDAY, R.string.time_line_yesterday);
		SECTIONS_LABELS.put(SECTION_PAST, R.string.time_line_before);
	}

	@Override
	protected String getSectionLabel(Context context, Commentaire item) {
		return context.getString(SECTIONS_LABELS.get((Integer) item.getSection()));
	}

	@Override
	protected void prepareSection(Commentaire item) {
	}

}
