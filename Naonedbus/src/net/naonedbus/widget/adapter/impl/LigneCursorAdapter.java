package net.naonedbus.widget.adapter.impl;

import net.naonedbus.R;
import net.naonedbus.provider.table.LigneTable;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.utils.FontUtils;
import net.naonedbus.widget.adapter.CursorSectionAdapter;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

public class LigneCursorAdapter extends CursorSectionAdapter {

	private int COL_LETTRE;
	private int COL_COLOR;
	private int COL_SENS1;
	private int COL_SENS2;

	private final Typeface mRobotoMedium;
	private boolean mHideDivider;

	public LigneCursorAdapter(Context context, Cursor c) {
		super(context, c, R.layout.list_item_ligne);
		mRobotoMedium = FontUtils.getRobotoMedium(context);
		if (c != null) {
			initColumns(c);
		}
	}

	public void setHideDivider(boolean hide) {
		mHideDivider = hide;
	}

	@Override
	public void changeCursor(Cursor cursor) {
		super.changeCursor(cursor);
		if (cursor != null) {
			initColumns(cursor);
		}
	}

	@Override
	public Cursor swapCursor(Cursor newCursor) {
		final Cursor oldCursor = super.swapCursor(newCursor);
		if (newCursor != null) {
			initColumns(newCursor);
		}
		return oldCursor;
	}

	private void initColumns(Cursor c) {
		COL_LETTRE = c.getColumnIndex(LigneTable.LETTRE);
		COL_COLOR = c.getColumnIndex(LigneTable.COULEUR);
		COL_SENS1 = c.getColumnIndex(LigneTable.DEPUIS);
		COL_SENS2 = c.getColumnIndex(LigneTable.VERS);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		super.bindView(view, context, cursor);

		final ViewHolder holder = (ViewHolder) view.getTag();
		final String lettre = cursor.getString(COL_LETTRE);
		final String depuis = cursor.getString(COL_SENS1);
		final String vers = cursor.getString(COL_SENS2);
		final int color = cursor.getInt(COL_COLOR);

		holder.icon.setText(lettre);

		if (color == 0) {
			holder.icon.setBackgroundResource(R.drawable.item_symbole_back);
			holder.icon.setTextColor(Color.WHITE);
		} else {
			holder.icon.setBackgroundDrawable(ColorUtils.getRoundedGradiant(color));
			holder.icon.setTextColor(ColorUtils.isLightColor(color) ? Color.BLACK : Color.WHITE);
		}
		if ((depuis == null || depuis.length() == 0 || depuis.equals(vers))) {
			holder.sens1.setText(depuis + " \u2194 " + vers);
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
	protected void bindViewHolder(View view) {
		final ViewHolder holder = new ViewHolder();
		holder.icon = (TextView) view.findViewById(R.id.itemSymbole);
		holder.sens1 = (TextView) view.findViewById(R.id.ligneFrom);
		holder.sens2 = (TextView) view.findViewById(R.id.ligneTo);
		holder.icon.setTypeface(mRobotoMedium);

		view.setTag(holder);
	}

	private static class ViewHolder {
		TextView icon;
		TextView sens1;
		TextView sens2;
	}

}
