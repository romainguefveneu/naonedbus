package net.naonedbus.widget.adapter.impl;

import java.util.List;

import net.naonedbus.R;
import net.naonedbus.bean.Parcours;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.utils.FontUtils;
import net.naonedbus.utils.SymbolesUtils;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ParcoursAdapter extends ArrayAdapter<Parcours> {

	private Typeface mTypeface;

	public ParcoursAdapter(Context context, List<Parcours> objects) {
		super(context, 0, objects);
		mTypeface = FontUtils.getRobotoMedium(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_parcours, null);
			bindViewHolder(convertView);
		}
		bindView(convertView, position);
		return convertView;
	}

	protected void bindView(View view, int position) {
		final ViewHolder holder = (ViewHolder) view.getTag();
		final Parcours parcours = getItem(position);
		holder.itemSymbole.setText(parcours.lettre);
		holder.itemSymbole.setBackgroundDrawable(ColorUtils.getRoundedGradiant(parcours.couleur));
		holder.itemSymbole.setTextColor(ColorUtils.isLightColor(parcours.couleur) ? Color.BLACK : Color.WHITE);
		holder.itemTitle.setText(SymbolesUtils.formatSens(parcours.nomSens));
	}

	protected void bindViewHolder(View view) {
		final ViewHolder holder;
		holder = new ViewHolder();
		holder.itemSymbole = (TextView) view.findViewById(R.id.itemSymbole);
		holder.itemTitle = (TextView) view.findViewById(R.id.itemTitle);
		holder.itemSymbole.setTypeface(mTypeface);
		view.setTag(holder);
	}

	protected static class ViewHolder {
		TextView itemSymbole;
		TextView itemTitle;
	}
}
