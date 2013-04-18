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
package net.naonedbus.widget.adapter;

import net.naonedbus.R;
import net.naonedbus.widget.PinnedHeaderListView;
import net.naonedbus.widget.PinnedHeaderListView.PinnedHeaderAdapter;
import net.naonedbus.widget.indexer.CursorSectionIndexer;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.TextView;

public abstract class CursorSectionAdapter extends CursorAdapter implements OnScrollListener, PinnedHeaderAdapter {

	protected CursorSectionIndexer mIndexer;
	protected LayoutInflater mLayoutInflater;

	private int mLayoutId;

	public CursorSectionAdapter(Context context, Cursor c, int layoutId) {
		super(context, c, layoutId);

		mLayoutInflater = LayoutInflater.from(context);
		mLayoutId = layoutId;
	}

	public void setIndexer(CursorSectionIndexer indexer) {
		mIndexer = indexer;
	}

	@Override
	public void changeCursor(Cursor cursor) {
		super.changeCursor(cursor);
		if (mIndexer != null) {
			mIndexer.changeCursor(cursor);
		}
	}

	@Override
	public Cursor swapCursor(Cursor newCursor) {
		final Cursor oldCursor = super.swapCursor(newCursor);
		if (mIndexer != null) {
			mIndexer.changeCursor(newCursor);
		}
		return oldCursor;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final View v = mLayoutInflater.inflate(mLayoutId, null);
		bindViewHolder(v);
		return v;
	}

	/**
	 * Bind the view holder.
	 * 
	 * @param view
	 */
	protected abstract void bindViewHolder(View view);

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		bindSectionHeader(view, cursor.getPosition());
	}

	/**
	 * Remplir la section.
	 * 
	 * @param itemView
	 * @param position
	 */
	protected void bindSectionHeader(View itemView, int position) {
		final View headerView = itemView.findViewById(R.id.headerView);

		if (headerView != null) {
			final int section = getSectionForPosition(position);
			final TextView headerTextView = (TextView) itemView.findViewById(R.id.headerTitle);
			final View divider = itemView.findViewById(R.id.headerDivider);

			if (getPositionForSection(section) == position) {
				final String title = (String) mIndexer.getSections()[section];
				headerTextView.setText(title);
				headerTextView.setVisibility(View.VISIBLE);
				if (divider != null) {
					divider.setVisibility(View.GONE);
				}
			} else {
				headerTextView.setVisibility(View.GONE);
				if (divider != null) {
					divider.setVisibility(View.VISIBLE);
				}
			}

		}

	}

	@Override
	public int getPinnedHeaderState(int position) {
		if (mIndexer == null || getCount() == 0) {
			return PINNED_HEADER_GONE;
		}

		if (position < 0) {
			return PINNED_HEADER_GONE;
		}

		// The header should get pushed up if the top item shown
		// is the last item in a section for a particular letter.
		int section = getSectionForPosition(position);
		if (section == -1) {
			return PINNED_HEADER_GONE;
		}

		int nextSectionPosition = getPositionForSection(section + 1);
		if (nextSectionPosition != -1 && position == nextSectionPosition - 1) {
			return PINNED_HEADER_PUSHED_UP;
		}

		return PINNED_HEADER_VISIBLE;
	}

	@Override
	public void configurePinnedHeader(View v, int position) {
		final TextView header = (TextView) v.findViewById(R.id.headerTitle);
		final int section = getSectionForPosition(position);
		final String title = (String) getSections()[section];

		header.setText(title);
	}

	public Object[] getSections() {
		if (mIndexer == null) {
			return new String[] { " " };
		} else {
			return mIndexer.getSections();
		}
	}

	public int getPositionForSection(int sectionIndex) {
		if (mIndexer == null) {
			return -1;
		}

		return mIndexer.getPositionForSection(sectionIndex);
	}

	@Override
	public int getSectionForPosition(int position) {
		if (mIndexer == null) {
			return -1;
		}

		return mIndexer.getSectionForPosition(position);
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (view instanceof PinnedHeaderListView) {
			((PinnedHeaderListView) view).configureHeaderView(firstVisibleItem);
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}

}
