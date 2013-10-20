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
import net.naonedbus.bean.Route;
import net.naonedbus.utils.FontUtils;
import net.naonedbus.widget.adapter.ArraySectionAdapter;
import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

public class RouteArrayAdapter extends ArraySectionAdapter<Route> {

	private static class ViewHolder {
		TextView icon;
		TextView sens1;
		TextView sens2;
	}

	private final Typeface robotoMedium;
	private boolean mHideDivider;

	public RouteArrayAdapter(final Context context, final List<Route> lignes) {
		super(context, R.layout.list_item_ligne, lignes);
		robotoMedium = FontUtils.getRobotoBoldCondensed(context);
	}

	public void setHideDivider(final boolean hide) {
		mHideDivider = hide;
	}

	@Override
	public void bindView(final View view, final Context context, final int position) {
		final ViewHolder holder = (ViewHolder) view.getTag();
		final Route ligne = getItem(position);

		holder.icon.setText(ligne.getLetter());
		holder.icon.setTextColor(ligne.getFrontColor());

		if (ligne.getId() == -1) {
			holder.icon.setBackgroundDrawable(null);
			holder.sens1.setTypeface(null, Typeface.BOLD);
		} else {
			holder.sens1.setTypeface(null, Typeface.NORMAL);

			if (ligne.getBackColor() == 0) {
				holder.icon.setBackgroundResource(R.drawable.item_symbole_back);
			} else {
				holder.icon.setBackgroundColor(ligne.getBackColor());
			}
		}

		if ((ligne.getHeadsigneFrom() == null || ligne.getHeadsigneFrom().length() == 0 || ligne.getHeadsigneFrom()
				.equals(ligne.getHeadsignTo()))) {
			holder.sens1.setText(ligne.getName());
			holder.sens2.setVisibility(View.GONE);
		} else {
			holder.sens1.setText(ligne.getHeadsigneFrom());
			holder.sens2.setText(ligne.getHeadsignTo());
			holder.sens2.setVisibility(View.VISIBLE);
		}

		if (mHideDivider) {
			view.findViewById(R.id.headerDivider).setVisibility(View.GONE);
		}
	}

	@Override
	public void bindViewHolder(final View view) {
		final ViewHolder holder;
		holder = new ViewHolder();
		holder.icon = (TextView) view.findViewById(R.id.itemSymbole);
		holder.sens1 = (TextView) view.findViewById(R.id.ligneFrom);
		holder.sens2 = (TextView) view.findViewById(R.id.ligneTo);
		holder.icon.setTypeface(robotoMedium);

		view.setTag(holder);
	}

}
