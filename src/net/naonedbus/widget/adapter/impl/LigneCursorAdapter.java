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

import net.naonedbus.BuildConfig;
import net.naonedbus.R;
import net.naonedbus.manager.impl.LigneManager;
import net.naonedbus.provider.table.LigneTable;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.utils.FontUtils;
import net.naonedbus.widget.adapter.CursorSectionAdapter;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class LigneCursorAdapter extends CursorSectionAdapter {

	private static final String LOG_TAG = "LigneCursorAdapter";
	private static final boolean DBG = BuildConfig.DEBUG;

	private final LigneManager mLigneManager;
	private final Typeface mRoboto;
	private boolean mHideDivider;

	private int mColLettre;
	private int mColBackColor;
	private int mColFrontColor;
	private int mColSens1;
	private int mColSens2;

	public LigneCursorAdapter(final Context context, final Cursor c) {
		super(context, c, R.layout.list_item_ligne);
		mRoboto = FontUtils.getRobotoBoldCondensed(context);
		mLigneManager = LigneManager.getInstance();
		if (c != null) {
			initColumns(c);
		}
	}

	public void setHideDivider(final boolean hide) {
		mHideDivider = hide;
	}

	@Override
	public void changeCursor(final Cursor cursor) {
		if (DBG)
			Log.d(LOG_TAG, "changeCursor " + cursor);

		super.changeCursor(cursor);
		if (cursor != null) {
			initColumns(cursor);
		}
	}

	@Override
	public Cursor swapCursor(final Cursor newCursor) {
		if (DBG)
			Log.d(LOG_TAG, "swapCursor " + newCursor);

		final Cursor oldCursor = super.swapCursor(newCursor);
		if (newCursor != null) {
			initColumns(newCursor);
		}
		return oldCursor;
	}

	private void initColumns(final Cursor c) {
		mColLettre = c.getColumnIndex(LigneTable.LETTRE);
		mColBackColor = c.getColumnIndex(LigneTable.COULEUR_BACK);
		mColFrontColor = c.getColumnIndex(LigneTable.COULEUR_FRONT);
		mColSens1 = c.getColumnIndex(LigneTable.DEPUIS);
		mColSens2 = c.getColumnIndex(LigneTable.VERS);
	}

	@Override
	public void bindView(final View view, final Context context, final Cursor cursor) {
		super.bindView(view, context, cursor);

		final ViewHolder holder = (ViewHolder) view.getTag();
		final String lettre = cursor.getString(mColLettre);
		final String depuis = cursor.getString(mColSens1);
		final String vers = cursor.getString(mColSens2);
		final int color = cursor.getInt(mColBackColor);
		final int colorFront = cursor.getInt(mColFrontColor);

		holder.icon.setText(lettre);

		if (color == 0) {
			holder.icon.setBackgroundResource(R.drawable.item_symbole_back);
			holder.icon.setTextColor(Color.WHITE);
		} else {
			holder.icon.setBackgroundDrawable(ColorUtils.getRoundedGradiant(color));
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
	public CharSequence convertToString(final Cursor cursor) {
		return cursor.getString(mColLettre);
	}

	@Override
	public Cursor runQueryOnBackgroundThread(final CharSequence constraint) {
		if (DBG)
			Log.d(LOG_TAG, "runQueryOnBackgroundThread '" + constraint + "'");

		if (!TextUtils.isEmpty(constraint)) {
			return mLigneManager.getLignesSearch(mContext.getContentResolver(), constraint.toString());
		} else {
			return mLigneManager.getCursor(mContext.getContentResolver());
		}
	}

	@Override
	protected void bindViewHolder(final View view) {
		final ViewHolder holder = new ViewHolder();
		holder.icon = (TextView) view.findViewById(R.id.itemSymbole);
		holder.sens1 = (TextView) view.findViewById(R.id.ligneFrom);
		holder.sens2 = (TextView) view.findViewById(R.id.ligneTo);
		holder.icon.setTypeface(mRoboto);

		view.setTag(holder);
	}

	private static class ViewHolder {
		TextView icon;
		TextView sens1;
		TextView sens2;
	}

}
