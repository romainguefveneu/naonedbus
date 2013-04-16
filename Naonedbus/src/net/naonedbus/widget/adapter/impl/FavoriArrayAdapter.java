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
import net.naonedbus.utils.SymbolesUtils;
import net.naonedbus.widget.adapter.ArraySectionAdapter;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class FavoriArrayAdapter extends ArraySectionAdapter<Favori> {

	private final Typeface robotoBold;

	private SparseBooleanArray checkedItemPositions = new SparseBooleanArray();

	public FavoriArrayAdapter(Context context, List<Favori> objects) {
		super(context, R.layout.list_item_favori, objects);
		robotoBold = FontUtils.getRobotoBoldCondensed(context);
	}

	@Override
	public void bindView(View view, Context context, int position) {
		final ViewHolder holder = (ViewHolder) view.getTag();
		final Favori item = (Favori) getItem(position);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			if (this.checkedItemPositions.get(position)) {
				view.setBackgroundResource(R.color.holo_blue_selected);
			} else {
				view.setBackgroundResource(android.R.color.transparent);
			}
		}

		if (item.background == null) {
			final GradientDrawable background = (GradientDrawable) ColorUtils
					.getRoundedGradiant(item.couleurBackground);
			item.background = background;
			item.couleurTexte = (ColorUtils.isLightColor(item.couleurBackground) ? Color.BLACK : Color.WHITE);
		}

		holder.ligneCode.setText(item.lettre);
		holder.ligneCode.setBackgroundDrawable(item.background);
		holder.ligneCode.setTextColor(item.couleurTexte);

		if (item.nomFavori == null) {
			holder.itemTitle.setText(item.nomArret);
			holder.itemDescription.setText(SymbolesUtils.formatSens(item.nomSens));
		} else {
			holder.itemTitle.setText(item.nomFavori);
			holder.itemDescription.setText(SymbolesUtils.formatArretSens(item.nomArret, item.nomSens));
		}

		if (item.delay == null) {
			holder.nextHoraire.setVisibility(View.GONE);
			holder.progressBar.setVisibility(View.VISIBLE);
		} else {
			holder.progressBar.setVisibility(View.GONE);
			holder.nextHoraire.setText(item.delay);
			holder.nextHoraire.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void bindViewHolder(View view) {
		final ViewHolder holder = new ViewHolder();
		holder.ligneCode = (TextView) view.findViewById(R.id.itemSymbole);
		holder.itemTitle = (TextView) view.findViewById(R.id.itemTitle);
		holder.itemDescription = (TextView) view.findViewById(R.id.itemDescription);
		holder.progressBar = (ProgressBar) view.findViewById(R.id.loading);
		holder.nextHoraire = (TextView) view.findViewById(R.id.itemTime);
		holder.ligneCode.setTypeface(robotoBold);

		view.setTag(holder);
	}

	public void setCheckedItemPositions(SparseBooleanArray checkedItemPositions) {
		this.checkedItemPositions = checkedItemPositions;
	}

	public void clearCheckedItemPositions() {
		this.checkedItemPositions.clear();
	}

	private static class ViewHolder {
		TextView ligneCode;
		TextView itemTitle;
		TextView itemDescription;
		ProgressBar progressBar;
		TextView nextHoraire;
	}

}
