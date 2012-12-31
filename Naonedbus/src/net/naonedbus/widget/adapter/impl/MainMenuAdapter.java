package net.naonedbus.widget.adapter.impl;

import net.naonedbus.R;
import net.naonedbus.widget.adapter.SectionAdapter;
import net.naonedbus.widget.indexer.impl.MainMenuIndexer;
import net.naonedbus.widget.item.impl.MainMenuItem;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MainMenuAdapter extends SectionAdapter<MainMenuItem> {

	private Class<?> mCurrentClass;

	public MainMenuAdapter(Context context) {
		super(context, R.layout.list_item_menu);
		setIndexer(new MainMenuIndexer());
	}

	public void setCurrentClass(Class<?> currentClass) {
		mCurrentClass = currentClass;
	}

	@Override
	public void bindView(View view, Context context, int position) {
		final ViewHolder holder = (ViewHolder) view.getTag();
		final MainMenuItem item = getItem(position);

		holder.title.setText(context.getString(item.getTitle()));
		holder.icon.setImageResource(item.getResIcon());

		final Class<?> intentClass = item.getIntentClass();
		if (intentClass != null && intentClass.equals(mCurrentClass)) {
			holder.view.setBackgroundResource(R.color.menu_current);
		} else {
			holder.view.setBackgroundColor(Color.TRANSPARENT);
		}
	}

	@Override
	public void bindViewHolder(View view) {
		final ViewHolder holder = new ViewHolder();
		holder.view = view.findViewById(R.id.itemContent);
		holder.icon = (ImageView) view.findViewById(R.id.itemIcon);
		holder.title = (TextView) view.findViewById(R.id.itemTitle);

		view.setTag(holder);
	}

	static class ViewHolder {
		View view;
		ImageView icon;
		TextView title;
	}

}
