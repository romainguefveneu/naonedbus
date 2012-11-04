package net.naonedbus.widget.adapter.impl;

import java.text.DateFormat;
import java.util.List;

import net.naonedbus.R;
import net.naonedbus.bean.horaire.Horaire;
import net.naonedbus.utils.DrawableUtils;
import net.naonedbus.widget.adapter.SectionAdapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class HoraireArrayAdapter extends SectionAdapter<Horaire> {

	final DateFormat timeFormat;
	final float mClockHandWidth;
	final float mClockHandHeight;
	final int mClockSize;

	public HoraireArrayAdapter(Context context, List<Horaire> objects) {
		super(context, R.layout.list_item_horaire, objects);
		timeFormat = android.text.format.DateFormat.getTimeFormat(context);

		mClockHandWidth = context.getResources().getDimension(R.dimen.clock_hand_width);
		mClockHandHeight = context.getResources().getDimension(R.dimen.clock_hand_height);
		mClockSize = context.getResources().getDimensionPixelSize(R.dimen.clock_icon_size);
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

		final Bitmap bitmap = Bitmap.createBitmap(mClockSize, mClockSize, Bitmap.Config.ARGB_8888);
		int color;

		if (item.isBeforeNow()) {
			holder.itemTitle.setEnabled(false);
			holder.itemDescription.setEnabled(false);
			color = Color.GRAY;
		} else {
			holder.itemTitle.setEnabled(true);
			holder.itemDescription.setEnabled(true);
			color = Color.BLACK;
		}
		DrawableUtils.drawClockBitmap(bitmap, color, mClockHandWidth, mClockHandHeight, item.getDate());
		holder.itemIcon.setImageBitmap(bitmap);
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

	private static class ViewHolder {
		ImageView itemIcon;
		TextView itemTitle;
		TextView itemDescription;
		TextView itemTime;
	}

}
