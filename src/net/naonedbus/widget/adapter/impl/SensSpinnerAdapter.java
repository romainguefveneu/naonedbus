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
import net.naonedbus.bean.Sens;
import net.naonedbus.utils.FormatUtils;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class SensSpinnerAdapter extends SensArrayAdapter implements SpinnerAdapter {

	private int mTextColor;

	public SensSpinnerAdapter(Context context, List<Sens> objects, int textColor) {
		super(context, R.layout.list_item_sens_spinner, objects);
		mTextColor = textColor;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final TextView view = (TextView) super.getView(position, convertView, parent);
		final Sens sens = getItem(position);
		view.setText(FormatUtils.formatSens(sens.text));
		view.setTextColor(mTextColor);
		return view;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		final TextView view = (TextView) super.getView(position, convertView, parent);
		final Sens sens = getItem(position);
		view.setText(FormatUtils.formatSens(sens.text));
		return view;
	}
}
