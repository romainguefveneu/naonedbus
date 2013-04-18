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
import net.naonedbus.provider.table.SensTable;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class SensCursorAdapter extends CursorAdapter {

	private LayoutInflater layoutInflater;
	private int layoutId = R.layout.list_item_icon;
	private Drawable icon;
	private Drawable iconBackgroundDrawable;

	public SensCursorAdapter(Context context, Cursor c) {
		super(context, c, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		icon = context.getResources().getDrawable(R.drawable.ic_action_forward).mutate();
	}

	public SensCursorAdapter(Context context, Cursor c, int colorBack) {
		this(context, c);
	}

	protected void setLayoutId(int layoutId) {
		this.layoutId = layoutId;
	}

	protected void setIconDrawable(Drawable drawable) {
		this.icon = drawable;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();
		String description = cursor.getString(cursor.getColumnIndex(SensTable.NOM));

		holder.icon.setImageDrawable(this.icon);
		holder.title.setText(description);
		if (iconBackgroundDrawable != null) {
			holder.icon.setBackgroundDrawable(iconBackgroundDrawable);
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View v = layoutInflater.inflate(this.layoutId, null);

		ViewHolder holder = new ViewHolder();

		holder.icon = (ImageView) v.findViewById(R.id.itemIcon);
		holder.title = (TextView) v.findViewById(R.id.itemTitle);

		v.setTag(holder);
		return v;
	}

	static class ViewHolder {
		ImageView icon = null;
		TextView title = null;
	}

}
