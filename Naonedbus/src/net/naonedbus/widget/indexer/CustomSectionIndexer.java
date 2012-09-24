/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.naonedbus.widget.indexer;

import java.util.ArrayList;
import java.util.Arrays;

import net.naonedbus.widget.item.SectionItem;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.SectionIndexer;

/**
 * A section indexer that is configured with precomputed section titles and
 * their respective counts.
 */
public abstract class CustomSectionIndexer<T extends SectionItem> implements SectionIndexer {

	private String[] mSections;
	private int[] mPositions;
	private int mCount;

	/**
	 * @param sections
	 *            a non-null array
	 * @param counts
	 *            a non-null array of the same size as <code>sections</code>
	 */
	public void buildIndex(Context context, ArrayAdapter<T> adapter) {
		final ArrayList<String> sectionsText = new ArrayList<String>();
		final ArrayList<Integer> sectionsCount = new ArrayList<Integer>();
		int lastSectionPosition;
		int lastSectionSize;
		Object lastSection = null;

		for (int i = 0; i < adapter.getCount(); i++) {
			final T item = adapter.getItem(i);
			prepareSection(item);

			if (lastSection == null || !lastSection.equals(item.getSection())) {
				// Nouvelle section

				lastSection = item.getSection();
				sectionsCount.add(1);
				sectionsText.add(getSectionLabel(context, item));

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
		}

		this.mSections = new String[sectionsText.size()];
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
	 * Affecter la section à un élément.
	 */
	protected abstract void prepareSection(T item);

	/**
	 * @param item
	 * @return Le libellé de la section.
	 */
	protected abstract String getSectionLabel(Context context, T item);

	@Override
	public Object[] getSections() {
		return mSections;
	}

	@Override
	public int getPositionForSection(int section) {
		if (section < 0 || section >= mSections.length) {
			return -1;
		}

		return mPositions[section];
	}

	@Override
	public int getSectionForPosition(int position) {
		if (position < 0 || position >= mCount) {
			return -1;
		}

		int index = Arrays.binarySearch(mPositions, position);

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
}