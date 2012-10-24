package net.naonedbus.widget.adapter.impl;

import net.naonedbus.R;
import net.naonedbus.widget.adapter.SectionAdapter;
import net.naonedbus.widget.indexer.impl.MainMenuIndexer;
import net.naonedbus.widget.item.impl.MainMenuItem;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MainMenuAdapter extends SectionAdapter<MainMenuItem> {

	public MainMenuAdapter(Context context) {
		super(context, R.layout.list_item_menu);
		setIndexer(new MainMenuIndexer());
	}

	@Override
	public void bindView(View view, Context context, int position) {
		final ViewHolder holder = (ViewHolder) view.getTag();
		final MainMenuItem item = getItem(position);

		holder.title.setText(context.getString(item.getTitle()));
		holder.icon.setImageResource(item.getResIcon());
	}

	@Override
	public void bindViewHolder(View view) {
		final ViewHolder holder = new ViewHolder();
		holder.icon = (ImageView) view.findViewById(R.id.itemIcon);
		holder.title = (TextView) view.findViewById(R.id.itemTitle);

		view.setTag(holder);
	}

	static class ViewHolder {
		ImageView icon = null;
		TextView title = null;
	}

	@Override
	public void customizeHeaderView(View view, int position) {
	}

}
