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
package net.naonedbus.widget.indexer;

import java.util.ArrayList;
import java.util.Arrays;

import android.database.Cursor;
import android.database.DataSetObserver;
import android.widget.SectionIndexer;

public abstract class CursorSectionIndexer extends DataSetObserver implements SectionIndexer {

	/** Cursor à sectionner. */
	protected Cursor mDataCursor;
	/** Liste des sections. */
	private String[] mSections;
	/** Positions des sections. */
	private int[] mPositions;
	/** Nom de la colonne servant à indexer le contenu. */
	private final String mColumnSectionName;
	/** Index de la colonne servant à identifier la section */
	protected int mColumnSection;

	private int mCount;

	/**
	 * Constructs the indexer.
	 * 
	 * @param cursor
	 *            the cursor containing the data set
	 * @param columnSection
	 */
	public CursorSectionIndexer(final Cursor cursor, final String columnSectionName, final int sectionsCount) {
		mDataCursor = cursor;
		mColumnSectionName = columnSectionName;

		changeCursor(cursor);
	}

	public void changeCursor(final Cursor cursor) {
		if (mDataCursor != null) {
			mDataCursor.unregisterDataSetObserver(this);
		}
		mDataCursor = cursor;
		if (cursor != null) {
			cursor.registerDataSetObserver(this);
			mColumnSection = cursor.getColumnIndex(mColumnSectionName);

			buildIndex();
		}
	}

	private void buildIndex() {
		final ArrayList<String> sectionsText = new ArrayList<String>();
		final ArrayList<Integer> sectionsCount = new ArrayList<Integer>();
		int lastSectionPosition;
		int lastSectionSize;
		int lastSection = -1;

		int currentSection;
		if (mDataCursor.moveToFirst()) {
			do {
				currentSection = mDataCursor.getInt(mColumnSection);
				if (lastSection == -1 || lastSection != currentSection) {
					// Nouvelle section

					lastSection = currentSection;
					sectionsCount.add(1);
					sectionsText.add(getSectionLabel(currentSection));

				} else {
					// Mettre à jour le nombre d'élément de la section courante

					if (sectionsCount.size() == 0) {
						lastSectionPosition = 0;
						lastSectionSize = 0;
					} else {
						lastSectionPosition = sectionsCount.size() - 1;
						lastSectionSize = sectionsCount.get(lastSectionPosition);
					}
					sectionsCount.set(lastSectionPosition, lastSectionSize + 1);

				}
			} while (mDataCursor.moveToNext());
		}

		mSections = new String[sectionsText.size()];
		sectionsText.toArray(this.mSections);

		mPositions = new int[sectionsCount.size()];
		int position = 0;
		for (int i = 0; i < sectionsCount.size(); i++) {
			if (mSections[i] == null) {
				mSections[i] = " ";
			}

			mPositions[i] = position;
			position += sectionsCount.get(i);
		}
		mCount = position;
	}

	/**
	 * @param section
	 *            L'index de la section
	 * @return Le libellé de la section.
	 */
	protected abstract String getSectionLabel(int section);

	@Override
	public int getPositionForSection(final int section) {
		if (section < 0 || section >= mSections.length) {
			return -1;
		}

		return mPositions[section];
	}

	@Override
	public int getSectionForPosition(final int position) {
		if (position < 0 || position >= mCount) {
			return -1;
		}

		final int index = Arrays.binarySearch(mPositions, position);

		/*
		 * Consider this example: section positions are 0, 3, 5; the supplied
		 * position is 4. The section corresponding to position 4 starts at
		 * position 3, so the expected return value is 1. Binary search will not
		 * find 4 in the array and thus will return -insertPosition-1, i.e. -3.
		 * To get from that number to the expected value of 1 we need to negate
		 * and subtract 2.
		 */
		return index >= 0 ? index : -index - 2;
	}

	@Override
	public Object[] getSections() {
		return mSections;
	}

}
