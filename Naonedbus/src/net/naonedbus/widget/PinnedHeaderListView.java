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

package net.naonedbus.widget;

import net.naonedbus.R;
import net.naonedbus.utils.ThemeUtils;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * A ListView that maintains a header pinned at the top of the list. The pinned
 * header can be pushed up and dissolved as needed.
 */
public class PinnedHeaderListView extends ListView {

	/**
	 * Adapter interface. The list adapter must implement this interface.
	 */
	public interface PinnedHeaderAdapter {

		/**
		 * Pinned header state: don't show the header.
		 */
		public static final int PINNED_HEADER_GONE = 0;

		/**
		 * Pinned header state: show the header at the top of the list.
		 */
		public static final int PINNED_HEADER_VISIBLE = 1;

		/**
		 * Pinned header state: show the header. If the header extends beyond
		 * the bottom of the first shown element, push it up and clip.
		 */
		public static final int PINNED_HEADER_PUSHED_UP = 2;

		/**
		 * Computes the desired state of the pinned header for the given
		 * position of the first visible list item. Allowed return values are
		 * {@link #PINNED_HEADER_GONE}, {@link #PINNED_HEADER_VISIBLE} or
		 * {@link #PINNED_HEADER_PUSHED_UP}.
		 */
		int getPinnedHeaderState(int position);

		/**
		 * Configures the pinned header view to match the first visible list
		 * item.
		 * 
		 * @param header
		 *            pinned header view.
		 * @param position
		 *            position of the first visible list item.
		 */
		void configurePinnedHeader(View header, int position);

		int getSectionForPosition(int position);
	}

	private PinnedHeaderAdapter mAdapter;
	private View mHeaderView;
	private boolean mHeaderViewVisible;

	private int mHeaderViewWidth;
	private int mHeaderViewHeight;

	public PinnedHeaderListView(Context context) {
		super(context);
	}

	public PinnedHeaderListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PinnedHeaderListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setPinnedHeaderView(View view) {
		mHeaderView = view;

		mHeaderViewHeight = getResources().getDimensionPixelSize(R.dimen.list_section_divider_min_height);
		mHeaderViewHeight += ThemeUtils.getDimensionPixelSize(getContext(), android.R.attr.dividerHeight);

		// Disable vertical fading when the pinned header is present
		// TODO change ListView to allow separate measures for top and bottom
		// fading edge;
		// in this particular case we would like to disable the top, but not the
		// bottom edge.
		if (mHeaderView != null) {
			setFadingEdgeLength(0);
			mHeaderView.findViewById(net.naonedbus.R.id.headerTitle).setVisibility(View.VISIBLE);
			mHeaderView.findViewById(net.naonedbus.R.id.headerDivider).setVisibility(View.GONE);
		}
		requestLayout();
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);
		mAdapter = (PinnedHeaderAdapter) adapter;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (mHeaderView != null) {
			measureChild(mHeaderView, widthMeasureSpec, heightMeasureSpec);
			mHeaderViewWidth = mHeaderView.getMeasuredWidth();
			mHeaderViewHeight = mHeaderView.getMeasuredHeight();
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (mHeaderView != null) {
			mHeaderView.layout(getHeaderLeft(), 0, mHeaderViewWidth + getHeaderLeft(), mHeaderViewHeight);
			configureHeaderView(getFirstVisiblePosition());
		}
	}

	@Override
	public void setSelection(int position) {
		if (mAdapter != null && mAdapter.getSectionForPosition(position) == 0) {
			setSelectionFromTop(position, mHeaderViewHeight - 1);
		} else {
			setSelectionFromTop(position, 0);
		}
	}

	public void configureHeaderView(int position) {
		if (mHeaderView == null || mAdapter == null) {
			return;
		}

		boolean hasHeader = false;

		final View firstView = getChildAt(0);
		final int top = (firstView == null) ? 0 : firstView.getTop();

		int nextChild = position - getFirstVisiblePosition();
		if (top < 0)
			nextChild += 1;
		if (nextChild < getChildCount()) {
			hasHeader = getChildAt(nextChild).findViewById(R.id.headerTitle).getVisibility() == View.VISIBLE;
		}

		int state = mAdapter.getPinnedHeaderState(position);
		if (state == PinnedHeaderAdapter.PINNED_HEADER_VISIBLE && top > 0 && hasHeader) {
			state = PinnedHeaderAdapter.PINNED_HEADER_PUSHED_UP;
		} else if (state == PinnedHeaderAdapter.PINNED_HEADER_PUSHED_UP && !hasHeader) {
			state = PinnedHeaderAdapter.PINNED_HEADER_VISIBLE;
		}

		switch (state) {
		case PinnedHeaderAdapter.PINNED_HEADER_GONE: {
			mHeaderViewVisible = false;
			break;
		}

		case PinnedHeaderAdapter.PINNED_HEADER_VISIBLE: {
			mAdapter.configurePinnedHeader(mHeaderView, position);
			if (mHeaderView.getTop() != 0) {
				mHeaderView.layout(getHeaderLeft(), 0, mHeaderViewWidth + getHeaderLeft(), mHeaderViewHeight);
			}
			mHeaderViewVisible = true;
			break;
		}

		case PinnedHeaderAdapter.PINNED_HEADER_PUSHED_UP: {
			if (firstView != null) {
				int y;
				int bottom = firstView.getBottom();
				int headerHeight = mHeaderView.getHeight();

				if (bottom < headerHeight) {
					y = bottom - headerHeight;
				} else {
					y = (top < 0) ? 0 : top;
				}
				mAdapter.configurePinnedHeader(mHeaderView, position);
				if (mHeaderView.getTop() != y) {
					mHeaderView.layout(getHeaderLeft(), y, mHeaderViewWidth + getHeaderLeft(), mHeaderViewHeight + y);
				}
				mHeaderViewVisible = true;
			}
			break;
		}
		}
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if (mHeaderViewVisible) {
			drawChild(canvas, mHeaderView, getDrawingTime());
		}
	}

	private int getHeaderLeft() {
		return (this.getWidth() - mHeaderViewWidth) / 2;
	}

}
