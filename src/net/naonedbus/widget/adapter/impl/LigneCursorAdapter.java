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
import net.naonedbus.provider.table.LigneTable;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.widget.adapter.CursorSectionAdapter;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

public class LigneCursorAdapter extends CursorSectionAdapter {

	private int COL_LETTRE;
	private int COL_COLOR_BACK;
	private int COL_COLOR_FRONT;
	private int COL_SENS1;
	private int COL_SENS2;

	private boolean mHideDivider;

	public LigneCursorAdapter(final Context context, final Cursor c) {
		super(context, c, R.layout.list_item_ligne);
		if (c != null) {
			initColumns(c);
		}
	}

	public void setHideDivider(final boolean hide) {
		mHideDivider = hide;
	}

	@Override
	public void changeCursor(final Cursor cursor) {
		super.changeCursor(cursor);
		if (cursor != null) {
			initColumns(cursor);
		}
	}

	@Override
	public Cursor swapCursor(final Cursor newCursor) {
		final Cursor oldCursor = super.swapCursor(newCursor);
		if (newCursor != null) {
			initColumns(newCursor);
		}
		return oldCursor;
	}

	private void initColumns(final Cursor c) {
		COL_LETTRE = c.getColumnIndex(LigneTable.LETTRE);
		COL_COLOR_BACK = c.getColumnIndex(LigneTable.COULEUR_BACK);
		COL_COLOR_FRONT = c.getColumnIndex(LigneTable.COULEUR_FRONT);
		COL_SENS1 = c.getColumnIndex(LigneTable.DEPUIS);
		COL_SENS2 = c.getColumnIndex(LigneTable.VERS);
	}

	@Override
	public void bindView(final View view, final Context context, final Cursor cursor) {
		super.bindView(view, context, cursor);

		final ViewHolder holder = (ViewHolder) view.getTag();
		final String lettre = cursor.getString(COL_LETTRE);
		final String depuis = cursor.getString(COL_SENS1);
		final String vers = cursor.getString(COL_SENS2);
		final int color = cursor.getInt(COL_COLOR_BACK);
		final int colorFront = cursor.getInt(COL_COLOR_FRONT);

		holder.icon.setText(lettre);

		if (color == 0) {
			holder.icon.setBackgroundResource(R.drawable.item_symbole_back);
			holder.icon.setTextColor(Color.WHITE);
		} else {
			holder.icon.setBackgroundDrawable(ColorUtils.getCircle(color));
			holder.icon.setTextColor(colorFront);
		}
		if ((depuis == null || depuis.length() == 0 || depuis.equals(vers))) {
			holder.sens1.setText(vers);
			holder.sens2.setVisibility(View.GONE);
		} else {
			holder.sens1.setText(depuis);
			holder.sens2.setText(vers);
			holder.sens2.setVisibility(View.VISIBLE);
		}

		if (mHideDivider) {
			view.findViewById(R.id.headerDivider).setVisibility(View.GONE);
		}
	}

	@Override
	protected void bindViewHolder(final View view) {
		final ViewHolder holder = new ViewHolder();
		holder.icon = (TextView) view.findViewById(R.id.itemSymbole);
		holder.sens1 = (TextView) view.findViewById(R.id.ligneFrom);
		holder.sens2 = (TextView) view.findViewById(R.id.ligneTo);

		view.setTag(holder);
	}

	private static class ViewHolder {
		TextView icon;
		TextView sens1;
		TextView sens2;
	}

}
