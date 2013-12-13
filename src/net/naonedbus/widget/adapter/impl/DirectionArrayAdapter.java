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

import java.util.List;

import net.naonedbus.R;
import net.naonedbus.bean.Direction;
import net.naonedbus.utils.FormatUtils;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DirectionArrayAdapter extends ArrayAdapter<Direction> {

	private final int mLayoutId;
	private Integer mTextColor;
	private Typeface mTypeface;

	public DirectionArrayAdapter(final Context context, final List<Direction> objects) {
		super(context, 0, objects);
		mLayoutId = R.layout.list_item_icon;
	}

	public DirectionArrayAdapter(final Context context, final int layoutId, final List<Direction> objects) {
		super(context, 0, objects);
		mLayoutId = layoutId;
	}

	@Override
	public View getView(final int position, View convertView, final ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(mLayoutId, null);
			bindViewHolder(convertView);
		}
		bindView(convertView, position, mTextColor, mTypeface);
		return convertView;
	}

	protected void bindView(final View view, final int position) {
		bindView(view, position, null, null);
	}

	protected void bindView(final View view, final int position, final Integer textColor, final Typeface typeface) {
		final ViewHolder holder = (ViewHolder) view.getTag();
		final Direction direction = getItem(position);

		if (direction.getId() == -1) {
			holder.itemTitle.setTypeface(null, Typeface.BOLD);
			if (holder.itemIcon != null)
				holder.itemIcon.setVisibility(View.INVISIBLE);
		} else {
			holder.itemTitle.setTypeface(null, Typeface.NORMAL);
			if (holder.itemIcon != null)
				holder.itemIcon.setVisibility(View.VISIBLE);
		}

		holder.itemTitle.setText(direction.getServiceId() + "/" + direction.getCode() + " "
				+ FormatUtils.formatSens(direction.getName()));
		if (textColor != null)
			holder.itemTitle.setTextColor(textColor);
		if (typeface != null)
			holder.itemTitle.setTypeface(typeface);
	}

	protected void bindViewHolder(final View view) {
		final ViewHolder holder;
		holder = new ViewHolder();
		holder.itemIcon = (ImageView) view.findViewById(R.id.itemIcon);
		holder.itemTitle = (TextView) view.findViewById(R.id.itemTitle);
		if (holder.itemIcon != null)
			holder.itemIcon.setImageResource(R.drawable.ic_action_forward_light);

		view.setTag(holder);
	}

	public void setTextColor(final int textColor) {
		mTextColor = textColor;
	}

	public void setTypeface(final Typeface typeface) {
		mTypeface = typeface;
	}

	protected static class ViewHolder {
		ImageView itemIcon;
		TextView itemTitle;
	}
}
