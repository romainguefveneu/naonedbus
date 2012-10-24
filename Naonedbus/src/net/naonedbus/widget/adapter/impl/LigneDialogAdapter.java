package net.naonedbus.widget.adapter.impl;

import java.util.ArrayList;
import java.util.List;

import net.naonedbus.R;
import net.naonedbus.bean.Sens;
import net.naonedbus.widget.adapter.SectionAdapter;
import net.naonedbus.widget.item.SectionItem;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class LigneDialogAdapter extends SectionAdapter<SectionItem> {

	public LigneDialogAdapter(Context context, List<Sens> sens) {
		super(context, R.layout.list_item_icon_section, merge(sens));
	}

	private static List<SectionItem> merge(List<Sens> sens) {
		List<SectionItem> objects = new ArrayList<SectionItem>();
		for (Sens s : sens) {
			objects.add((SectionItem) s);
		}
		return objects;
	}

	@Override
	public void bindView(View view, Context context, int position) {
		final ViewHolder holder = (ViewHolder) view.getTag();
		final Sens sens = (Sens) getItem(position);
		holder.itemTitle.setText(sens.text);
	}

	@Override
	public void bindViewHolder(View view) {
		final ViewHolder holder;
		holder = new ViewHolder();
		holder.itemIcon = (ImageView) view.findViewById(R.id.itemIcon);
		holder.itemTitle = (TextView) view.findViewById(R.id.itemTitle);
		holder.itemIcon.setImageResource(R.drawable.ic_action_forward_light);

		view.setTag(holder);
	}

	private static class ViewHolder {
		ImageView itemIcon;
		TextView itemTitle;
	}

	@Override
	public void customizeHeaderView(View view, int position) {
	}

}
