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
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class DirectionSpinnerAdapter extends DirectionArrayAdapter implements SpinnerAdapter {

	public DirectionSpinnerAdapter(Context context, List<Direction> objects, int textColor, Typeface typeface) {
		super(context, R.layout.list_item_sens_spinner, objects);
		setTypeface(typeface);
		setTextColor(textColor);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final TextView view = (TextView) super.getView(position, convertView, parent);
		final Direction sens = getItem(position);
		view.setText(FormatUtils.formatSens(sens.getName()));
		return view;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_icon, null);
			bindViewHolder(convertView);
		}
		bindView(convertView, position);
		return convertView;
	}
}
