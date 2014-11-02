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
import net.naonedbus.widget.item.impl.DrawerMenuItem;
import net.naonedbus.widget.item.impl.SettingMenuItem;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MainMenuAdapter extends ArrayAdapter<DrawerMenuItem> {

	private final LayoutInflater mLayoutInflater;
	private int mFirstSettingsPosition = 0;

	public MainMenuAdapter(final Context context, final List<DrawerMenuItem> objects) {
		super(context, 0, android.R.id.text1, objects);
		mLayoutInflater = LayoutInflater.from(context);
		indexSettingsPosition();
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		indexSettingsPosition();
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		final DrawerMenuItem item = getItem(position);
		final boolean isSetting = item instanceof SettingMenuItem;
		return isSetting ? 1 : 0;
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		final DrawerMenuItem item = getItem(position);

		final boolean isSetting = item instanceof SettingMenuItem;

		View view = convertView;
		if (view == null) {
			final int layoutId = isSetting ? R.layout.list_item_menu_setting : R.layout.list_item_menu;
			view = mLayoutInflater.inflate(layoutId, parent, false);
		}

		final TextView label = (TextView) view.findViewById(android.R.id.text1);
		label.setText(item.getTitle());
		if (item.getResIcon() != 0) {
			label.setCompoundDrawablesWithIntrinsicBounds(item.getResIcon(), 0, 0, 0);
		}

		if (isSetting) {
			final View dividerBottom = view.findViewById(R.id.dividerTop);
			if (dividerBottom != null) {
				if (position == mFirstSettingsPosition) {
					dividerBottom.setVisibility(View.VISIBLE);
				} else {
					dividerBottom.setVisibility(View.GONE);
				}
			}
		}

		return view;
	}

	private void indexSettingsPosition() {
		mFirstSettingsPosition = -1;
		for (int i = 0; i < getCount(); i++) {
			if (getItem(i) instanceof SettingMenuItem) {
				mFirstSettingsPosition = i;
				break;
			}
		}
	}

}
