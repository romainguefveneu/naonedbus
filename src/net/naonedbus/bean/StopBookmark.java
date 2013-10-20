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
package net.naonedbus.bean;

import net.naonedbus.widget.item.SectionItem;
import android.graphics.drawable.Drawable;

public class StopBookmark extends Stop implements SectionItem {

	public static class Builder extends Stop.Builder {
		private String mBookmarkName;
		private String mDirectionName;
		private int mGroupId;
		private String mGroupName;
		private Integer mNextSchedule;
		private int mBackColor;
		private int mFrontColor;
		private Integer mSection;

		public Builder setGroupId(final int groupId) {
			mGroupId = groupId;
			return this;
		}

		public Builder setBookmarkName(final String bookmarkName) {
			mBookmarkName = bookmarkName;
			return this;
		}

		public Builder setDirectionName(final String directionName) {
			mDirectionName = directionName;
			return this;
		}

		public Builder setGroupName(final String groupName) {
			mGroupName = groupName;
			return this;
		}

		public Builder setNextSchedule(final Integer nextSchedule) {
			mNextSchedule = nextSchedule;
			return this;
		}

		public Builder setBackColor(final int backColor) {
			mBackColor = backColor;
			return this;
		}

		public Builder setFrontColor(final int frontColor) {
			mFrontColor = frontColor;
			return this;
		}

		public Builder setSection(final Integer section) {
			mSection = section;
			return this;
		}

		@Override
		public StopBookmark build() {
			return new StopBookmark(this);
		}

	}

	private final String mDirectionName;
	private final int mGroupId;
	private final String mGroupName;

	private final Integer mNextSchedule;
	private final int mBackColor;
	private final int mFrontColor;

	private Drawable mBackground;
	private String mDelay;

	private String mBookmarkName;
	private Integer mSection;

	private StopBookmark(final Builder builder) {
		super(builder);
		mBookmarkName = builder.mBookmarkName;
		mDirectionName = builder.mDirectionName;
		mGroupId = builder.mGroupId;
		mGroupName = builder.mGroupName;
		mNextSchedule = builder.mNextSchedule;
		mBackColor = builder.mBackColor;
		mFrontColor = builder.mFrontColor;
		mSection = builder.mSection;
	}

	public String getBookmarkName() {
		return mBookmarkName;
	}

	public String getDirectionName() {
		return mDirectionName;
	}

	public int getGroupeId() {
		return mGroupId;
	}

	public String getGroupName() {
		return mGroupName;
	}

	public Integer getNextSchedule() {
		return mNextSchedule;
	}

	public int getBackColor() {
		return mBackColor;
	}

	public int getFrontColor() {
		return mFrontColor;
	}

	public Drawable getBackground() {
		return mBackground;
	}

	public String getDelay() {
		return mDelay;
	}

	public void setBackground(final Drawable background) {
		mBackground = background;
	}

	public void setDelay(final String delay) {
		mDelay = delay;
	}

	public void setSection(final Integer section) {
		mSection = section;
	}

	public void setBookmarkName(final String bookmarkName) {
		mBookmarkName = bookmarkName;
	}

	@Override
	public Object getSection() {
		return mSection;
	}

}
