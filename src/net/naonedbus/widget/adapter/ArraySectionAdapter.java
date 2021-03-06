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

import java.util.List;

import net.naonedbus.R;
import net.naonedbus.widget.PinnedHeaderListView;
import net.naonedbus.widget.PinnedHeaderListView.PinnedHeaderAdapter;
import net.naonedbus.widget.indexer.ArraySectionIndexer;
import net.naonedbus.widget.item.SectionItem;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Adapter gérant les sections.
 * 
 * @author romain
 * 
 */
public abstract class ArraySectionAdapter<T extends SectionItem> extends ArrayAdapter<T> implements OnScrollListener,
		PinnedHeaderAdapter {

	protected ArraySectionIndexer<T> mIndexer;
	protected LayoutInflater mLayoutInflater;

	private int mLayoutId;

	public ArraySectionAdapter(final Context context, final int layoutId) {
		super(context, layoutId);
		init(context, layoutId);
	}

	public ArraySectionAdapter(final Context context, final int layoutId, final List<T> objects) {
		super(context, layoutId, objects);
		init(context, layoutId);
	}

	private void init(final Context context, final int layoutId) {
		mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mLayoutId = layoutId;
	}

	public void setIndexer(final ArraySectionIndexer<T> indexer) {
		this.mIndexer = indexer;
		if (indexer != null) {
			this.mIndexer.buildIndex(getContext(), this);
		}
	}

	public View newView(final Context context, final int position) {
		final View v = mLayoutInflater.inflate(mLayoutId, null);
		bindViewHolder(v);
		return v;
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		if (mIndexer != null) {
			mIndexer.buildIndex(getContext(), this);
		}
	}

	/**
	 * @see android.widget.ListAdapter#getView(int, View, ViewGroup)
	 */
	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		final View v;
		if (convertView == null) {
			v = newView(getContext(), position);
		} else {
			v = convertView;
		}
		bindSectionHeader(v, position);
		bindView(v, getContext(), position);
		return v;
	}

	/**
	 * Remplir la vue.
	 * 
	 * @param view
	 * @param context
	 * @param position
	 */
	public abstract void bindView(View view, Context context, int position);

	/**
	 * Remplir le tag de la vue avec un ViewHolder.
	 * 
	 * @param view
	 */
	public abstract void bindViewHolder(View view);

	/**
	 * Remplir la section.
	 * 
	 * @param itemView
	 * @param position
	 */
	protected void bindSectionHeader(final View itemView, final int position) {
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
					divider.setVisibility(position != 0 ? View.VISIBLE : View.GONE);
				}
			}

		}

	}

	public int getPositionForSection(final int sectionIndex) {
		if (mIndexer == null) {
			return -1;
		}

		return mIndexer.getPositionForSection(sectionIndex);
	}

	@Override
	public int getSectionForPosition(final int position) {
		if (mIndexer == null) {
			return -1;
		}

		return mIndexer.getSectionForPosition(position);
	}

	public Object[] getSections() {
		if (mIndexer == null) {
			return new String[] { " " };
		} else {
			return mIndexer.getSections();
		}
	}

	@Override
	public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount,
			final int totalItemCount) {
		if (view instanceof PinnedHeaderListView) {
			((PinnedHeaderListView) view).configureHeaderView(firstVisibleItem);
		}
	}

	@Override
	public void onScrollStateChanged(final AbsListView arg0, final int arg1) {
	}

	@Override
	public int getPinnedHeaderState(final int position) {
		if (mIndexer == null || getCount() == 0) {
			return PINNED_HEADER_GONE;
		}

		if (position < 0) {
			return PINNED_HEADER_GONE;
		}

		// The header should get pushed up if the top item shown
		// is the last item in a section for a particular letter.
		final int section = getSectionForPosition(position);
		if (section == -1) {
			return PINNED_HEADER_GONE;
		}

		final int nextSectionPosition = getPositionForSection(section + 1);
		if (nextSectionPosition != -1 && position == nextSectionPosition - 1) {
			return PINNED_HEADER_PUSHED_UP;
		}

		return PINNED_HEADER_VISIBLE;
	}

	@Override
	public void configurePinnedHeader(final View v, final int position) {
		final TextView header = (TextView) v.findViewById(R.id.headerTitle);
		final int section = getSectionForPosition(position);
		final String title = (String) getSections()[section];

		header.setText(title);
	}

}
