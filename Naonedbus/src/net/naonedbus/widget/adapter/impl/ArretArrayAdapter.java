package net.naonedbus.widget.adapter.impl;

import java.util.List;

import net.naonedbus.R;
import net.naonedbus.bean.Arret;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ArretArrayAdapter extends ArrayAdapter<Arret> {

	public ArretArrayAdapter(Context context, List<Arret> objects) {
		super(context, 0, objects);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_icon, null);
			bindViewHolder(convertView);
		}
		bindView(convertView, position);
		return convertView;
	}

	public void bindView(View view, int position) {
		final ViewHolder holder = (ViewHolder) view.getTag();
		final Arret arret = getItem(position);
		holder.itemTitle.setText(arret.text);
	}

	public void bindViewHolder(View view) {
		final ViewHolder holder;
		holder = new ViewHolder();
		holder.itemIcon = (ImageView) view.findViewById(R.id.itemIcon);
		holder.itemTitle = (TextView) view.findViewById(R.id.itemTitle);
		holder.itemIcon.setImageResource(R.drawable.map_layer_arret);

		view.setTag(holder);
	}

	private static class ViewHolder {
		ImageView itemIcon;
		TextView itemTitle;
	}
}