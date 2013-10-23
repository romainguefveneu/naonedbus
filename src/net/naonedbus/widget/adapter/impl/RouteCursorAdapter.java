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
import net.naonedbus.manager.impl.RouteManager;
import net.naonedbus.provider.table.RouteTable;
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

public class RouteCursorAdapter extends CursorSectionAdapter {

	private static final String LOG_TAG = "RouteCursorAdapter";
	private static final boolean DBG = BuildConfig.DEBUG;

	private final RouteManager mRouteManager;
	private final Typeface mRoboto;
	private boolean mHideDivider;

	private int mColLettre;
	private int mColBackColor;
	private int mColFrontColor;
	private int mColHeadsignFrom;
	private int mColHeadsignTo;

	public RouteCursorAdapter(final Context context, final Cursor c) {
		super(context, c, R.layout.list_item_ligne);
		mRoboto = FontUtils.getRobotoBoldCondensed(context);
		mRouteManager = RouteManager.getInstance();
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
		mColLettre = c.getColumnIndex(RouteTable.LETTER);
		mColBackColor = c.getColumnIndex(RouteTable.BACK_COLOR);
		mColFrontColor = c.getColumnIndex(RouteTable.FRONT_COLOR);
		mColHeadsignFrom = c.getColumnIndex(RouteTable.HEADSIGN_FROM);
		mColHeadsignTo = c.getColumnIndex(RouteTable.HEADSIGN_TO);
	}

	@Override
	public void bindView(final View view, final Context context, final Cursor cursor) {
		super.bindView(view, context, cursor);

		final ViewHolder holder = (ViewHolder) view.getTag();
		final String letter = cursor.getString(mColLettre);
		final String headsignFrom = cursor.getString(mColHeadsignFrom);
		final String headsignTo = cursor.getString(mColHeadsignTo);
		final int backColor = cursor.getInt(mColBackColor);
		final int frontColor = cursor.getInt(mColFrontColor);

		holder.icon.setText(letter);

		if (backColor == 0) {
			holder.icon.setBackgroundResource(R.drawable.item_symbole_back);
			holder.icon.setTextColor(Color.WHITE);
		} else {
			holder.icon.setBackgroundDrawable(ColorUtils.getRoundedGradiant(backColor));
			holder.icon.setTextColor(frontColor);
		}
		if ((headsignFrom == null || headsignFrom.length() == 0 || headsignFrom.equals(headsignTo))) {
			holder.headsignFrom.setText(headsignTo);
			holder.headsignTo.setVisibility(View.GONE);
		} else {
			holder.headsignFrom.setText(headsignFrom);
			holder.headsignTo.setText(headsignTo);
			holder.headsignTo.setVisibility(View.VISIBLE);
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
			return mRouteManager.getRoutes(mContext.getContentResolver(), constraint.toString());
		} else {
			return mRouteManager.getCursor(mContext.getContentResolver());
		}
	}

	@Override
	protected void bindViewHolder(final View view) {
		final ViewHolder holder = new ViewHolder();
		holder.icon = (TextView) view.findViewById(R.id.itemSymbole);
		holder.headsignFrom = (TextView) view.findViewById(R.id.ligneFrom);
		holder.headsignTo = (TextView) view.findViewById(R.id.ligneTo);
		holder.icon.setTypeface(mRoboto);

		view.setTag(holder);
	}

	private static class ViewHolder {
		TextView icon;
		TextView headsignFrom;
		TextView headsignTo;
	}

}
