package net.naonedbus.widget.adapter.impl;

import net.naonedbus.R;
import net.naonedbus.utils.FontUtils;
import net.naonedbus.widget.adapter.ArraySectionAdapter;
import net.naonedbus.widget.indexer.impl.MainMenuIndexer;
import net.naonedbus.widget.item.impl.MainMenuItem;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MainMenuAdapter extends ArraySectionAdapter<MainMenuItem> {

	private final Typeface mRoboto;
	private final int mSelectedColor;

	private Class<?> mCurrentClass;

	public MainMenuAdapter(final Context context) {
		super(context, R.layout.list_item_menu);
		setIndexer(new MainMenuIndexer(context));
		mRoboto = FontUtils.getRobotoLight(context);
		mSelectedColor = context.getResources().getColor(R.color.menu_current);
	}

	public void setCurrentClass(final Class<?> currentClass) {
		mCurrentClass = currentClass;
	}

	@Override
	public void bindView(final View view, final Context context, final int position) {
		final ViewHolder holder = (ViewHolder) view.getTag();
		final MainMenuItem item = getItem(position);

		holder.title.setText(context.getString(item.getTitle()));
		holder.icon.setImageResource(item.getResIcon());

		final Class<?> intentClass = item.getIntentClass();
		if (intentClass != null && intentClass.equals(mCurrentClass)) {
			holder.view.setBackgroundColor(mSelectedColor);
		} else {
			holder.view.setBackgroundColor(Color.TRANSPARENT);
		}
	}

	@Override
	public void bindViewHolder(final View view) {
		final ViewHolder holder = new ViewHolder();
		holder.view = view.findViewById(R.id.itemContent);
		holder.icon = (ImageView) view.findViewById(R.id.itemIcon);
		holder.title = (TextView) view.findViewById(R.id.itemTitle);
		holder.title.setTypeface(mRoboto);

		view.setTag(holder);
	}

	static class ViewHolder {
		View view;
		ImageView icon;
		TextView title;
	}

}
