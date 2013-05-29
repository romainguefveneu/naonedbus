package net.naonedbus.widget.adapter.impl;

import java.util.List;

import net.naonedbus.R;
import net.naonedbus.bean.ItineraryWrapper;
import net.naonedbus.bean.Ligne;
import net.naonedbus.utils.ColorUtils;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.gridlayout.GridLayout;

public class ItineraryWrapperArrayAdapter extends ArrayAdapter<ItineraryWrapper> {

	private final LayoutInflater mLayoutInflater;

	public ItineraryWrapperArrayAdapter(final Context context, final List<ItineraryWrapper> objects) {
		super(context, 0, objects);
		mLayoutInflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		View view = convertView;
		ViewHolder viewHolder;
		if (view == null) {
			view = mLayoutInflater.inflate(R.layout.item_itineraire, parent, false);

			viewHolder = new ViewHolder();
			viewHolder.itemTitle = (TextView) view.findViewById(R.id.itemTitle);
			viewHolder.itemDate = (TextView) view.findViewById(R.id.itemDate);
			viewHolder.itemWalkTime = (TextView) view.findViewById(R.id.itemWalkTime);
			viewHolder.gridLayout = (GridLayout) view.findViewById(R.id.lignes);

			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}

		final ItineraryWrapper wrapper = getItem(position);

		viewHolder.itemTitle.setText(wrapper.getTitle());
		viewHolder.itemDate.setText(wrapper.getDate());
		viewHolder.itemWalkTime.setText(wrapper.getWalkTime());

		final List<Ligne> lignes = wrapper.getLignes();
		viewHolder.gridLayout.removeAllViews();
		for (final Ligne l : lignes) {
			final TextView textView = (TextView) mLayoutInflater.inflate(R.layout.ligne_code_item_medium,
					viewHolder.gridLayout, false);
			textView.setBackgroundDrawable(ColorUtils.getGradiant(l.getCouleur()));
			textView.setText(l.getLettre());
			textView.setTextColor(l.getCouleurTexte());

			viewHolder.gridLayout.addView(textView);
		}

		return view;
	}

	private static class ViewHolder {
		TextView itemTitle;
		TextView itemDate;
		TextView itemWalkTime;
		GridLayout gridLayout;
	}

}
