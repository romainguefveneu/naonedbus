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
package net.naonedbus.widget.adapter.impl;

import net.naonedbus.R;
import net.naonedbus.utils.FontUtils;
import net.naonedbus.widget.adapter.ArraySectionAdapter;
import net.naonedbus.widget.item.impl.MainMenuItem;
import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MainMenuAdapter extends ArraySectionAdapter<MainMenuItem> {

	private final Typeface mRoboto;
	private final int mSelectedColor;
	private final int mOptionsColor;

	private Class<?> mCurrentClass;

	public MainMenuAdapter(final Context context) {
		super(context, R.layout.list_item_menu);
		mRoboto = FontUtils.getRobotoLight(context);
		mSelectedColor = context.getResources().getColor(R.color.menu_current);
		mOptionsColor = context.getResources().getColor(R.color.menu_option_background);
	}

	public void setCurrentClass(final Class<?> currentClass) {
		mCurrentClass = currentClass;
	}

	@Override
	public void bindView(final View view, final Context context, final int position) {
		final ViewHolder holder = (ViewHolder) view.getTag();
		final MainMenuItem item = getItem(position);

		holder.title.setText(context.getString(item.getTitle()));
		holder.icon.setImageResource(item.getResIcon());

		final Class<?> intentClass = item.getIntentClass();
		if (intentClass != null && intentClass.equals(mCurrentClass)) {
			holder.view.setBackgroundColor(mSelectedColor);
		} else {
			if (item.getSection().equals(0)) {
				setViewBackgroundWithoutResettingPadding(holder.view, R.drawable.item_background_holo_light);
			} else {
				setViewBackgroundWithoutResettingPadding(holder.view, R.drawable.item_background_options_holo_light);
			}
		}
	}

	public static void setViewBackgroundWithoutResettingPadding(final View v, final int backgroundResId) {
		final int paddingBottom = v.getPaddingBottom(), paddingLeft = v.getPaddingLeft();
		final int paddingRight = v.getPaddingRight(), paddingTop = v.getPaddingTop();
		v.setBackgroundResource(backgroundResId);
		v.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
	}

	@Override
	public void bindViewHolder(final View view) {
		final ViewHolder holder = new ViewHolder();
		holder.view = view.findViewById(R.id.itemContent);
		holder.icon = (ImageView) view.findViewById(R.id.itemIcon);
		holder.title = (TextView) view.findViewById(R.id.itemTitle);
		holder.title.setTypeface(mRoboto);

		view.setTag(holder);
	}

	@Override
	protected void bindSectionHeader(final View itemView, final int position) {
		final View headerView = itemView.findViewById(R.id.headerView);
		if (headerView != null) {
			final int section = getSectionForPosition(position);
			if (section == 1 && getPositionForSection(section) == position) {
				headerView.setVisibility(View.VISIBLE);
			} else {
				headerView.setVisibility(View.GONE);
			}
		}
	}

	static class ViewHolder {
		View view;
		ImageView icon;
		TextView title;
	}

}
