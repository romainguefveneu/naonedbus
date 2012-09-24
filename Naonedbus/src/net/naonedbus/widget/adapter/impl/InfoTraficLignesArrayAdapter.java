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
import net.naonedbus.bean.InfoTraficLigne;
import net.naonedbus.widget.adapter.SectionAdapter;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class InfoTraficLignesArrayAdapter extends SectionAdapter<InfoTraficLigne> {

	private Drawable icoInfoTraficCurrent;

	static class ViewHolder {
		TextView itemTitle = null;
		TextView description = null;
		TextView date = null;
		LinearLayout zoneTitle = null;
	}

	public InfoTraficLignesArrayAdapter(Context context, List<InfoTraficLigne> objects) {
		super(context, R.layout.list_item_trafic, objects);
		this.icoInfoTraficCurrent = getContext().getResources().getDrawable(R.drawable.info_trafic_on);
	}

	@Override
	public void bindView(View view, Context context, int position) {
		ViewHolder holder = (ViewHolder) view.getTag();
		InfoTraficLigne item = getItem(position);
		holder.itemTitle.setText(item.getNumLigne() + " " + item.getLibelleTrafic());
		holder.description.setText(String.valueOf(item.getEtatTrafic()));
	}

	@Override
	public void bindViewHolder(View view) {
		ViewHolder holder = new ViewHolder();
		holder.itemTitle = (TextView) view.findViewById(R.id.itemTitle);
		holder.description = (TextView) view.findViewById(R.id.itemDescription);
		holder.date = (TextView) view.findViewById(R.id.itemTime);
		holder.zoneTitle = (LinearLayout) view.findViewById(R.id.zoneTitle);

		view.setTag(holder);
	}

}
