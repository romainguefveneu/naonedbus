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
import net.naonedbus.provider.table.EquipmentTable;
import net.naonedbus.provider.table.StopBookmarkTable;
import net.naonedbus.provider.table.StopTable;
import net.naonedbus.utils.ColorUtils;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class StopCursorAdapter extends CursorAdapter {

	protected LayoutInflater mLayoutInflater;
	private int mLayoutId = R.layout.list_item_stop;
	private Cursor mFavoris;
	private Drawable mIconBackgroundDrawable;

	public StopCursorAdapter(final Context context, final Cursor arrets, final Cursor favoris) {
		super(context, arrets, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		mFavoris = favoris;
		mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	protected void setLayoutId(final int layoutId) {
		this.mLayoutId = layoutId;
	}

	@Override
	public void bindView(final View view, final Context context, final Cursor cursor) {
		final ViewHolder holder = (ViewHolder) view.getTag();
		final String description = cursor.getString(cursor.getColumnIndex(EquipmentTable.EQUIPMENT_NAME));
		final int id = cursor.getInt(cursor.getColumnIndex(StopTable._ID));

		holder.iconFavori.setVisibility((isFavori(id)) ? View.VISIBLE : View.GONE);
		holder.title.setText(description);
		if (mIconBackgroundDrawable != null) {
			ColorUtils.setBackground(holder.icon, mIconBackgroundDrawable);
		}
	}

	@Override
	public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
		final View v = mLayoutInflater.inflate(this.mLayoutId, null);

		final ViewHolder holder = new ViewHolder();
		holder.iconFavori = (ImageView) v.findViewById(R.id.itemFavori);
		holder.icon = (ImageView) v.findViewById(R.id.itemIcon);
		holder.title = (TextView) v.findViewById(R.id.itemTitle);

		v.setTag(holder);
		return v;
	}

	static class ViewHolder {
		ImageView iconFavori = null;
		ImageView icon = null;
		TextView title = null;
	}

	private boolean isFavori(final int stopId) {
		if (mFavoris == null)
			return false;

		final int colIndex = mFavoris.getColumnIndex(StopBookmarkTable._ID);
		mFavoris.moveToFirst();
		while (mFavoris.isAfterLast() == false) {
			if (stopId == mFavoris.getInt(colIndex))
				return true;
			mFavoris.moveToNext();
		}
		return false;
	}

	public void setFavoris(final Cursor favoris) {
		this.mFavoris = favoris;
	}

}
