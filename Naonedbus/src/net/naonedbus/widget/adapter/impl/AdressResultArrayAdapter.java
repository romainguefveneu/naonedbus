package net.naonedbus.widget.adapter.impl;

import java.util.List;

import net.naonedbus.R;
import net.naonedbus.bean.AddressResult;
import net.naonedbus.utils.ColorUtils;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AdressResultArrayAdapter extends ArrayAdapter<AddressResult> {

	private final LayoutInflater mInflater;

	public AdressResultArrayAdapter(final Context context, final List<AddressResult> objects) {
		super(context, 0, objects);

		mInflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		View view = convertView;
		final ViewHolder holder;
		if (view == null) {
			view = mInflater.inflate(R.layout.list_item_equipement, parent, false);

			holder = new ViewHolder();
			holder.itemTitle = (TextView) view.findViewById(R.id.itemTitle);
			holder.itemDescription = (TextView) view.findViewById(R.id.itemDescription);
			holder.itemSymbole = (ImageView) view.findViewById(R.id.itemSymbole);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		final AddressResult addressResult = getItem(position);
		holder.itemTitle.setText(addressResult.getTitle());
		holder.itemDescription.setText(addressResult.getDescription());
		holder.itemSymbole.setImageResource(addressResult.getIcon());
		holder.itemSymbole.setBackgroundDrawable(ColorUtils.getRoundedGradiant(addressResult.getColor()));

		return view;
	}

	private class ViewHolder {
		TextView itemTitle;
		TextView itemDescription;
		ImageView itemSymbole;
	}

}
