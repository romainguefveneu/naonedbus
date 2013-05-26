package net.naonedbus.widget.adapter.impl;

import java.util.List;

import net.naonedbus.R;
import net.naonedbus.utils.FormatUtils;
import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import fr.ybo.opentripplanner.client.modele.Leg;
import fr.ybo.opentripplanner.client.modele.Place;

public class LegArrayAdapter extends ArrayAdapter<Leg> {

	private final LayoutInflater mLayoutInflater;

	public LegArrayAdapter(final Context context, final List<Leg> objects) {
		super(context, 0, objects);
		mLayoutInflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		View view = convertView;
		ViewHolder viewHolder;
		if (view == null) {
			view = mLayoutInflater.inflate(R.layout.list_item_leg, parent, false);

			viewHolder = new ViewHolder();
			viewHolder.itemSymbole = (TextView) view.findViewById(R.id.itemSymbole);
			viewHolder.itemTitle = (TextView) view.findViewById(R.id.itemTitle);
			viewHolder.itemTime = (TextView) view.findViewById(R.id.itemTime);
			viewHolder.fromTime = (TextView) view.findViewById(R.id.fromTime);
			viewHolder.fromPlace = (TextView) view.findViewById(R.id.fromPlace);
			viewHolder.toTime = (TextView) view.findViewById(R.id.toTime);
			viewHolder.toPlace = (TextView) view.findViewById(R.id.toPlace);

			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}

		final Leg leg = getItem(position);
		final long startTime = leg.startTime.getTime();
		final long endTime = leg.endTime.getTime();

		viewHolder.itemTitle.setText(FormatUtils.formatSens(leg.headsign));
		viewHolder.itemSymbole.setText(leg.route);
		viewHolder.itemTime.setText(FormatUtils.formatMinutes(getContext(), endTime - startTime));

		final Place from = leg.from;
		viewHolder.fromPlace.setText(from.name);
		viewHolder.fromTime.setText(DateUtils.formatDateTime(getContext(), startTime, DateUtils.FORMAT_SHOW_TIME));

		final Place to = leg.to;
		viewHolder.toPlace.setText(to.name);
		viewHolder.toTime.setText(DateUtils.formatDateTime(getContext(), endTime, DateUtils.FORMAT_SHOW_TIME));

		return view;
	}

	private static class ViewHolder {
		TextView itemSymbole;
		TextView itemTitle;
		TextView itemTime;
		TextView fromTime;
		TextView fromPlace;
		TextView toTime;
		TextView toPlace;
	}

}
