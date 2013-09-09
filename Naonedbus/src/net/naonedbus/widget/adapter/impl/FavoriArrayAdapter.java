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
import net.naonedbus.bean.Favori;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.utils.FontUtils;
import net.naonedbus.utils.FormatUtils;
import net.naonedbus.widget.adapter.ArraySectionAdapter;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class FavoriArrayAdapter extends ArraySectionAdapter<Favori> {

	private final Typeface mRobotoBold;
	private final boolean mShowDelay;
	private SparseBooleanArray mCheckedItemPositions = new SparseBooleanArray();

	public FavoriArrayAdapter(final Context context, final List<Favori> objects, final boolean showDelay) {
		super(context, R.layout.list_item_favori, objects);
		mShowDelay = showDelay;
		mRobotoBold = FontUtils.getRobotoBoldCondensed(context);
	}

	public FavoriArrayAdapter(final Context context, final List<Favori> objects) {
		this(context, objects, true);
	}

	@Override
	public void bindView(final View view, final Context context, final int position) {
		final ViewHolder holder = (ViewHolder) view.getTag();
		final Favori item = getItem(position);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			if (mCheckedItemPositions.get(position)) {
				view.setBackgroundResource(R.color.holo_blue_selected);
			} else {
				view.setBackgroundResource(android.R.color.transparent);
			}
		}

		if (item.getBackground() == null) {
			final GradientDrawable background = ColorUtils.getRoundedGradiant(item.getCouleurBackground());
			item.setBackground(background);
		}

		holder.ligneCode.setText(item.getLettre());
		holder.ligneCode.setBackgroundDrawable(item.getBackground());
		holder.ligneCode.setTextColor(item.getCouleurTexte());

		if (item.getNomFavori() == null) {
			holder.itemTitle.setText(item.getNomArret());
			holder.itemDescription.setText(FormatUtils.formatSens(item.getNomSens()));
		} else {
			holder.itemTitle.setText(item.getNomFavori());
			holder.itemDescription.setText(FormatUtils.formatArretSens(item.getNomArret(), item.getNomSens()));
		}

		if (mShowDelay == false) {
			holder.progressBar.setVisibility(View.GONE);
		} else {
			if (item.getDelay() == null) {
				holder.nextHoraire.setVisibility(View.GONE);
				holder.progressBar.setVisibility(View.VISIBLE);
			} else {
				holder.progressBar.setVisibility(View.GONE);
				holder.nextHoraire.setText(item.getDelay());
				holder.nextHoraire.setVisibility(View.VISIBLE);
			}
		}
	}

	@Override
	public void bindViewHolder(final View view) {
		final ViewHolder holder = new ViewHolder();
		holder.ligneCode = (TextView) view.findViewById(R.id.itemSymbole);
		holder.itemTitle = (TextView) view.findViewById(R.id.itemTitle);
		holder.itemDescription = (TextView) view.findViewById(R.id.itemDescription);
		holder.progressBar = (ProgressBar) view.findViewById(R.id.loading);
		holder.nextHoraire = (TextView) view.findViewById(R.id.itemTime);
		holder.ligneCode.setTypeface(mRobotoBold);

		view.setTag(holder);
	}

	public void setCheckedItemPositions(final SparseBooleanArray checkedItemPositions) {
		mCheckedItemPositions = checkedItemPositions;
	}

	public void clearCheckedItemPositions() {
		mCheckedItemPositions.clear();
	}

	private static class ViewHolder {
		TextView ligneCode;
		TextView itemTitle;
		TextView itemDescription;
		ProgressBar progressBar;
		TextView nextHoraire;
	}

}
