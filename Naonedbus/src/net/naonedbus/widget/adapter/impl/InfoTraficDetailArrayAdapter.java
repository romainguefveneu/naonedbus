/**
 *  Copyright (C) 2011 Romain Guefveneu
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
import net.naonedbus.bean.InfoTraficDetail;
import net.naonedbus.widget.adapter.SectionAdapter;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

public class InfoTraficDetailArrayAdapter extends SectionAdapter<InfoTraficDetail> {

	static class ViewHolder {
		TextView itemTitle = null;
		TextView itemTime = null;
		TextView itemDate = null;
	}

	public InfoTraficDetailArrayAdapter(Context context, List<InfoTraficDetail> objects) {
		super(context, R.layout.list_item_trafic_ligne, objects);
	}

	@Override
	public void bindView(View view, Context context, int position) {
		ViewHolder holder = (ViewHolder) view.getTag();
		InfoTraficDetail item = getItem(position);
		holder.itemTitle.setText(item.getTitre());
		holder.itemTime.setText(item.getType());
		holder.itemDate.setText(item.getPeriode());

	}

	@Override
	public void bindViewHolder(View view) {
		ViewHolder holder = new ViewHolder();
		holder.itemTitle = (TextView) view.findViewById(R.id.itemTitle);
		holder.itemTime = (TextView) view.findViewById(R.id.itemTime);
		holder.itemDate = (TextView) view.findViewById(R.id.itemDate);

		view.setTag(holder);
	}

}
