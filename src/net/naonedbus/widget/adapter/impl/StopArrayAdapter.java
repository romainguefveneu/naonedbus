/**
 * Copyright (C) 2013 Romain Guefveneu.
 *   
 *  This file is part of naonedbus.
 *   
 *  Naonedbus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  Naonedbus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.naonedbus.widget.adapter.impl;

import java.util.List;

import net.naonedbus.R;
import net.naonedbus.bean.Stop;
import net.naonedbus.utils.DistanceUtils;
import net.naonedbus.widget.StopStepView;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class StopArrayAdapter extends ArrayAdapter<Stop> {

	public static enum ViewType {
		TYPE_STANDARD, TYPE_METRO
	}

	private final LayoutInflater mLayoutInflater;
	private ViewType mViewType = ViewType.TYPE_STANDARD;
	private int mNearestPosition = -1;
	private final int mColor;

	public StopArrayAdapter(final Context context, final List<Stop> objects, final int color) {
		super(context, 0, objects);
		mLayoutInflater = LayoutInflater.from(getContext());
		mColor = color;
	}

	public void setViewType(final ViewType viewType) {
		this.mViewType = viewType;
	}

	@Override
	public View getView(final int position, View convertView, final ViewGroup parent) {
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.list_item_stop, null);
			bindViewHolder(convertView);
		}
		bindView(convertView, position);
		return convertView;
	}

	public void bindView(final View view, final int position) {
		final ViewHolder holder = (ViewHolder) view.getTag();
		final Stop stop = getItem(position);

		holder.stepView.setStyle(stop.getStepStyle());
		holder.stepView.setDepth(stop.getStepDepth());
		holder.stepView.setDepthsVisibility(stop.getStepDepthsVisibility());
		holder.stepView.setOrientationTop(stop.getStepOrientationTop());
		holder.stepView.setOrientationBottom(stop.getStepOrientationBottom());
		holder.stepView.setOtherLinesTopStyle(stop.getStepOtherLinesStyleTop());
		holder.stepView.setOtherLinesBottomStyle(stop.getStepOtherLinesStyleBottom());

		if (stop.getId() == -1) {
			holder.stepView.setTypeface(null, Typeface.BOLD);
			// holder.itemIcon.setVisibility(View.INVISIBLE);
		} else {
			holder.stepView.setTypeface(null, Typeface.NORMAL);
			// holder.itemIcon.setVisibility(View.VISIBLE);

			// Définir la distance.
			if (stop.getDistance() == null) {
				holder.itemDistance.setText("");
			} else {
				holder.itemDistance.setText(DistanceUtils.formatDist(stop.getDistance()));
			}

			bindDotPosition(holder, position);

			if (mViewType == ViewType.TYPE_METRO) {
				if (position == 0) {
					holder.itemMetroPoint.setBackgroundResource(R.drawable.ic_arret_first);
				} else if (position == getCount() - 1) {
					holder.itemMetroPoint.setBackgroundResource(R.drawable.ic_arret_last);
				} else {
					holder.itemMetroPoint.setBackgroundResource(R.drawable.ic_arret_step);
				}

				// holder.itemIcon.setVisibility(View.INVISIBLE);
				// holder.itemMetroPoint.setVisibility(View.VISIBLE);
			} else {
				// holder.itemIcon.setVisibility(View.VISIBLE);
				// holder.itemMetroPoint.setVisibility(View.INVISIBLE);
			}
		}

		holder.stepView.setText(stop.getName());

	}

	private void bindDotPosition(final ViewHolder holder, final int position) {
		final AnimationDrawable animationDrawable = (AnimationDrawable) holder.dotLocation.getDrawable();
		if (mNearestPosition == position) {
			holder.dotLocation.setVisibility(View.VISIBLE);
			if (!animationDrawable.isRunning()) {
				holder.dotLocation.post(new Runnable() {
					@Override
					public void run() {
						animationDrawable.start();
					}
				});
			}
		} else {
			holder.dotLocation.setVisibility(View.GONE);
			if (animationDrawable.isRunning()) {
				holder.dotLocation.post(new Runnable() {
					@Override
					public void run() {
						animationDrawable.stop();
					}
				});
			}
		}
	}

	public void bindViewHolder(final View view) {
		final ViewHolder holder;
		holder = new ViewHolder();
		holder.itemMetroPoint = (ImageView) view.findViewById(R.id.itemMetroPoint);
		holder.itemIcon = (ImageView) view.findViewById(R.id.itemIcon);
		holder.itemTitle = (TextView) view.findViewById(R.id.itemTitle);
		holder.itemDistance = (TextView) view.findViewById(R.id.itemDistance);
		holder.dotLocation = (ImageView) view.findViewById(R.id.dotLocation);
		holder.stepView = (StopStepView) view.findViewById(R.id.stepView);
		holder.stepView.setColor(mColor);

		view.setTag(holder);
	}

	public void setNearestPosition(final int position) {
		mNearestPosition = position;
	}

	private static class ViewHolder {
		ImageView itemMetroPoint;
		ImageView itemIcon;
		ImageView dotLocation;
		TextView itemTitle;
		TextView itemDistance;
		StopStepView stepView;
	}
}
