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
import net.naonedbus.utils.FontUtils;
import net.naonedbus.widget.item.impl.MainMenuItem;
import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MainMenuAdapter extends ArrayAdapter<MainMenuItem> {

	private final Typeface mRoboto;

	public MainMenuAdapter(final Context context, final List<MainMenuItem> items) {
		super(context, R.layout.list_item_menu, items);
		mRoboto = FontUtils.getRobotoLight(context);
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		final TextView textview = (TextView) super.getView(position, convertView, parent);

		final MainMenuItem item = getItem(position);
		textview.setText(getContext().getString(item.getTitle()));
		textview.setCompoundDrawablesWithIntrinsicBounds(item.getResIcon(), 0, 0, 0);
		textview.setTypeface(mRoboto);

		return textview;
	}

}
