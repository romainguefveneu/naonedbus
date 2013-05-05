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
import net.naonedbus.bean.Parcours;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.utils.FontUtils;
import net.naonedbus.utils.FormatUtils;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ParcoursAdapter extends ArrayAdapter<Parcours> {

	private Typeface mTypeface;

	public ParcoursAdapter(Context context, List<Parcours> objects) {
		super(context, 0, objects);
		mTypeface = FontUtils.getRobotoMedium(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_parcours, null);
			bindViewHolder(convertView);
		}
		bindView(convertView, position);
		return convertView;
	}

	protected void bindView(View view, int position) {
		final ViewHolder holder = (ViewHolder) view.getTag();
		final Parcours parcours = getItem(position);
		holder.itemSymbole.setText(parcours.lettre);
		holder.itemSymbole.setBackgroundDrawable(ColorUtils.getRoundedGradiant(parcours.couleur));
		holder.itemSymbole.setTextColor(ColorUtils.isLightColor(parcours.couleur) ? Color.BLACK : Color.WHITE);
		holder.itemTitle.setText(FormatUtils.formatSens(parcours.nomSens));
	}

	protected void bindViewHolder(View view) {
		final ViewHolder holder;
		holder = new ViewHolder();
		holder.itemSymbole = (TextView) view.findViewById(R.id.itemSymbole);
		holder.itemTitle = (TextView) view.findViewById(R.id.itemTitle);
		holder.itemSymbole.setTypeface(mTypeface);
		view.setTag(holder);
	}

	protected static class ViewHolder {
		TextView itemSymbole;
		TextView itemTitle;
	}
}
