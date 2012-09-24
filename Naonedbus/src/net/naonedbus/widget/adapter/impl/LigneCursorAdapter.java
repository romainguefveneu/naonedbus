package net.naonedbus.widget.adapter.impl;

import net.naonedbus.R;
import net.naonedbus.provider.table.LigneTable;
import net.naonedbus.utils.ColorUtils;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class LigneCursorAdapter extends CursorAdapter {

	private LayoutInflater layoutInflater;
	private int layoutId = R.layout.list_item_content_ligne;

	private int COL_LETTRE;
	private int COL_FRONT_COLOR;
	private int COL_SENS1;
	private int COL_SENS2;

	private final Typeface robotoLight;

	public LigneCursorAdapter(Context context, Cursor c) {
		super(context, c, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		robotoLight = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
		initColumns(c);
	}

	private void initColumns(Cursor c) {
		COL_LETTRE = c.getColumnIndex(LigneTable.LETTRE);
		COL_FRONT_COLOR = c.getColumnIndex(LigneTable.COULEUR);
		COL_SENS1 = c.getColumnIndex(LigneTable.DEPUIS);
		COL_SENS2 = c.getColumnIndex(LigneTable.VERS);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final ViewHolder holder = (ViewHolder) view.getTag();
		final String lettre = cursor.getString(COL_LETTRE);
		final String sens1 = cursor.getString(COL_SENS1);
		final String sens2 = cursor.getString(COL_SENS2);
		final int color = cursor.getInt(COL_FRONT_COLOR);

		holder.icon.setText(lettre);
		holder.icon.setTextColor(ColorUtils.isLightColor(color) ? Color.BLACK : Color.WHITE);
		holder.icon.setBackgroundDrawable(ColorUtils.getRoundedGradiant(color));
		holder.sens1.setText(sens1);
		holder.sens2.setText(sens2);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = layoutInflater.inflate(this.layoutId, null);

		ViewHolder holder = new ViewHolder();
		holder.icon = (TextView) view.findViewById(R.id.itemSymbole);
		holder.sens1 = (TextView) view.findViewById(R.id.ligneFrom);
		holder.sens2 = (TextView) view.findViewById(R.id.ligneTo);
		holder.icon.setTypeface(robotoLight);

		view.setTag(holder);
		return view;
	}

	private static class ViewHolder {
		TextView icon;
		TextView sens1;
		TextView sens2;
	}

}
