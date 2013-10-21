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

import java.util.ArrayList;
import java.util.List;

import net.naonedbus.R;
import net.naonedbus.bean.Direction;
import net.naonedbus.widget.adapter.ArraySectionAdapter;
import net.naonedbus.widget.item.SectionItem;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class RouteDialogAdapter extends ArraySectionAdapter<SectionItem> {

	public RouteDialogAdapter(Context context, List<Direction> direction) {
		super(context, R.layout.list_item_icon_section, merge(direction));
	}

	private static List<SectionItem> merge(List<Direction> direction) {
		List<SectionItem> objects = new ArrayList<SectionItem>();
		for (Direction s : direction) {
			objects.add((SectionItem) s);
		}
		return objects;
	}

	@Override
	public void bindView(View view, Context context, int position) {
		final ViewHolder holder = (ViewHolder) view.getTag();
		final Direction direction = (Direction) getItem(position);
		holder.itemTitle.setText(direction.getName());
	}

	@Override
	public void bindViewHolder(View view) {
		final ViewHolder holder;
		holder = new ViewHolder();
		holder.itemIcon = (ImageView) view.findViewById(R.id.itemIcon);
		holder.itemTitle = (TextView) view.findViewById(R.id.itemTitle);
		holder.itemIcon.setImageResource(R.drawable.ic_action_forward_light);

		view.setTag(holder);
	}

	private static class ViewHolder {
		ImageView itemIcon;
		TextView itemTitle;
	}

}
