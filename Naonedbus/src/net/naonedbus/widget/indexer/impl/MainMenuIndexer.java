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
import net.naonedbus.widget.indexer.ArraySectionIndexer;
import net.naonedbus.widget.item.impl.MainMenuItem;
import android.content.Context;
import android.util.SparseArray;

public class MainMenuIndexer extends ArraySectionIndexer<MainMenuItem> {

	private final SparseArray<String> mSectionTitles;

	public MainMenuIndexer(final Context context) {
		mSectionTitles = new SparseArray<String>();
		mSectionTitles.append(0, context.getString(R.string.menu_section_naonedbus));
		mSectionTitles.append(1, context.getString(R.string.menu_section_options));
	}

	@Override
	protected String getSectionLabel(final Context context, final MainMenuItem item) {
		return mSectionTitles.get((Integer) item.getSection());
	}

	@Override
	protected void prepareSection(final MainMenuItem item) {
	}

}
