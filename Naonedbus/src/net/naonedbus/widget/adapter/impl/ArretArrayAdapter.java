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

	public static enum ViewType {
		TYPE_STANDARD, TYPE_METRO
	}

	private ViewType mViewType = ViewType.TYPE_METRO;

	public ArretArrayAdapter(Context context, List<Arret> objects) {
		super(context, 0, objects);
	}

	public void setViewType(ViewType viewType) {
		this.mViewType = viewType;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_arret, null);
			bindViewHolder(convertView);
		}
		bindView(convertView, position);
		return convertView;
	}

	public void bindView(View view, int position) {
		final ViewHolder holder = (ViewHolder) view.getTag();
		final Arret arret = getItem(position);
		holder.itemTitle.setText(arret.nom);

		if (mViewType == ViewType.TYPE_METRO) {
			if (position == 0) {
				holder.itemMetroPoint.setBackgroundResource(R.drawable.ic_arret_first);
			} else if (position == getCount() - 1) {
				holder.itemMetroPoint.setBackgroundResource(R.drawable.ic_arret_last);
			} else {
				holder.itemMetroPoint.setBackgroundResource(R.drawable.ic_arret_step);
			}

			holder.itemIcon.setVisibility(View.INVISIBLE);
			holder.itemMetroPoint.setVisibility(View.VISIBLE);
		} else {
			holder.itemIcon.setVisibility(View.VISIBLE);
			holder.itemMetroPoint.setVisibility(View.INVISIBLE);
		}

	}

	public void bindViewHolder(View view) {
		final ViewHolder holder;
		holder = new ViewHolder();
		holder.itemMetroPoint = (ImageView) view.findViewById(R.id.itemMetroPoint);
		holder.itemIcon = (ImageView) view.findViewById(R.id.itemIcon);
		holder.itemTitle = (TextView) view.findViewById(R.id.itemTitle);

		view.setTag(holder);
	}

	private static class ViewHolder {
		ImageView itemMetroPoint;
		ImageView itemIcon;
		TextView itemTitle;
	}
}
