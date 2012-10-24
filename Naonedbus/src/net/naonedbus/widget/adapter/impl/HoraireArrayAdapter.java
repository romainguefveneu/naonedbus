package net.naonedbus.widget.adapter.impl;

import java.text.DateFormat;
import java.util.List;

import net.naonedbus.R;
import net.naonedbus.bean.horaire.Horaire;
import net.naonedbus.widget.adapter.SectionAdapter;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class HoraireArrayAdapter extends SectionAdapter<Horaire> {

	final DateFormat timeFormat;

	public HoraireArrayAdapter(Context context, List<Horaire> objects) {
		super(context, R.layout.list_item_horaire, objects);
		timeFormat = android.text.format.DateFormat.getTimeFormat(context);
	}

	@Override
	public void bindView(View view, Context context, int position) {
		final ViewHolder holder = (ViewHolder) view.getTag();
		final Horaire item = getItem(position);

		holder.itemTitle.setText(timeFormat.format(item.getDate()));

		if (item.getTerminus() != null) {
			holder.itemDescription.setText(item.getTerminus());
			holder.itemDescription.setVisibility(View.VISIBLE);
		} else {
			holder.itemDescription.setVisibility(View.GONE);
		}

		if (item.getDelai() != null) {
			holder.itemTime.setText(item.getDelai());
			holder.itemTime.setVisibility(View.VISIBLE);
		} else {
			holder.itemTime.setVisibility(View.GONE);
		}

		if (item.isBeforeNow()) {
			holder.itemTitle.setEnabled(false);
			holder.itemDescription.setEnabled(false);
			holder.itemIcon.setImageResource(R.drawable.horraireselect_disabled);
		} else {
			holder.itemTitle.setEnabled(true);
			holder.itemDescription.setEnabled(true);
			holder.itemIcon.setImageResource(R.drawable.horraireselect);
		}
	}

	@Override
	public void bindViewHolder(View view) {
		final ViewHolder holder = new ViewHolder();
		holder.itemTitle = (TextView) view.findViewById(R.id.itemTitle);
		holder.itemDescription = (TextView) view.findViewById(R.id.itemDescription);
		holder.itemTime = (TextView) view.findViewById(R.id.itemTime);
		holder.itemIcon = (ImageView) view.findViewById(R.id.itemIcon);
		view.setTag(holder);
	}

	@Override
	public void customizeHeaderView(View view, int position) {
	}

	private static class ViewHolder {
		ImageView itemIcon;
		TextView itemTitle;
		TextView itemDescription;
		TextView itemTime;
	}

}
