package net.naonedbus.widget.adapter.impl;

import java.util.List;

import net.naonedbus.R;
import net.naonedbus.bean.AddressResult;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.widget.adapter.ArraySectionAdapter;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class AddressResultArrayAdapter extends ArraySectionAdapter<AddressResult> {

	public AddressResultArrayAdapter(final Context context, final List<AddressResult> objects) {
		super(context, R.layout.list_item_equipment, objects);
	}

	@Override
	public void bindView(final View view, final Context context, final int position) {
		final ViewHolder holder = (ViewHolder) view.getTag();

		final AddressResult addressResult = getItem(position);
		holder.itemTitle.setText(addressResult.getTitle());
		if (TextUtils.isEmpty(addressResult.getDescription())) {
			holder.itemDescription.setVisibility(View.GONE);
		} else {
			holder.itemDescription.setText(addressResult.getDescription());
			holder.itemDescription.setVisibility(View.VISIBLE);
		}
		holder.itemSymbole.setImageResource(addressResult.getIcon());
		holder.itemSymbole.setBackgroundDrawable(ColorUtils.getRoundedGradiant(addressResult.getColor()));
	}

	@Override
	public void bindViewHolder(final View view) {
		final ViewHolder holder = new ViewHolder();
		holder.itemTitle = (TextView) view.findViewById(R.id.itemTitle);
		holder.itemDescription = (TextView) view.findViewById(R.id.itemDescription);
		holder.itemSymbole = (ImageView) view.findViewById(R.id.itemSymbole);
		view.setTag(holder);
	}

	private class ViewHolder {
		TextView itemTitle;
		TextView itemDescription;
		ImageView itemSymbole;
	}

}
