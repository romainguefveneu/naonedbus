package net.naonedbus.widget.adapter.impl;

import java.util.List;

import net.naonedbus.R;
import net.naonedbus.bean.Sens;
import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SensArrayAdapter extends ArrayAdapter<Sens> {

	private int mLayoutId;
	private Integer mTextColor;
	private Typeface mTypeface;

	public SensArrayAdapter(Context context, List<Sens> objects) {
		super(context, 0, objects);
		mLayoutId = R.layout.list_item_icon;
	}

	public SensArrayAdapter(Context context, int layoutId, List<Sens> objects) {
		super(context, 0, objects);
		mLayoutId = layoutId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(mLayoutId, null);
			bindViewHolder(convertView);
		}
		bindView(convertView, position, mTextColor, mTypeface);
		return convertView;
	}

	protected void bindView(View view, int position) {
		bindView(view, position, null, null);
	}

	protected void bindView(View view, int position, Integer textColor, Typeface typeface) {
		final ViewHolder holder = (ViewHolder) view.getTag();
		final Sens sens = getItem(position);
		holder.itemTitle.setText(sens.text);
		if (textColor != null)
			holder.itemTitle.setTextColor(textColor);
		if (typeface != null)
			holder.itemTitle.setTypeface(typeface);

		Log.d("SensArrayAdapter", "Color : " + textColor);
	}

	protected void bindViewHolder(View view) {
		final ViewHolder holder;
		holder = new ViewHolder();
		holder.itemIcon = (ImageView) view.findViewById(R.id.itemIcon);
		holder.itemTitle = (TextView) view.findViewById(R.id.itemTitle);
		if (holder.itemIcon != null)
			holder.itemIcon.setImageResource(R.drawable.ic_action_forward_light);

		view.setTag(holder);
	}

	public void setTextColor(int textColor) {
		mTextColor = textColor;
	}

	public void setTypeface(Typeface typeface) {
		mTypeface = typeface;
	}

	protected static class ViewHolder {
		ImageView itemIcon;
		TextView itemTitle;
	}
}
