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
import net.naonedbus.provider.table.ArretTable;
import net.naonedbus.provider.table.EquipementTable;
import net.naonedbus.provider.table.FavoriTable;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

public class ArretCursorAdapter extends CursorAdapter implements Filterable {

	protected LayoutInflater layoutInflater;
	private Cursor favoris;
	private int layoutId = R.layout.list_item_arret;
	private Drawable iconBackgroundDrawable;

	public ArretCursorAdapter(Context context, Cursor arrets, Cursor favoris) {
		super(context, arrets, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		this.favoris = favoris;
		this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public ArretCursorAdapter(Context context, Cursor arrets, Cursor favoris, int colorBack) {
		this(context, arrets, favoris);
	}

	protected void setLayoutId(int layoutId) {
		this.layoutId = layoutId;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final ViewHolder holder = (ViewHolder) view.getTag();
		final String description = cursor.getString(cursor.getColumnIndex(EquipementTable.NOM));
		final int id = cursor.getInt(cursor.getColumnIndex(ArretTable._ID));

		holder.iconFavori.setVisibility((isFavori(id)) ? View.VISIBLE : View.GONE);
		holder.title.setText(description);
		if (iconBackgroundDrawable != null) {
			holder.icon.setBackgroundDrawable(iconBackgroundDrawable);
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final View v = layoutInflater.inflate(this.layoutId, null);

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

	private boolean isFavori(int idArret) {
		if (favoris == null)
			return false;

		final int colIndex = favoris.getColumnIndex(FavoriTable._ID);
		favoris.moveToFirst();
		while (favoris.isAfterLast() == false) {
			if (idArret == favoris.getInt(colIndex))
				return true;
			favoris.moveToNext();
		}
		return false;
	}

	public void setFavoris(Cursor favoris) {
		this.favoris = favoris;
	}

	@Override
	public Filter getFilter() {
		return mFilter;
	}

	private Filter mFilter = new Filter() {
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {

		}

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			return null;
		}
	};

}
