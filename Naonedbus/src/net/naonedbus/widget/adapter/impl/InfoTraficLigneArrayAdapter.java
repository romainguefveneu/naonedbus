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
import net.naonedbus.bean.EmptyInfoTrafic;
import net.naonedbus.bean.InfoTrafic;
import net.naonedbus.bean.Ligne;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.widget.adapter.ArraySectionAdapter;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

public class InfoTraficLigneArrayAdapter extends ArraySectionAdapter<InfoTrafic> {

	static class ViewHolder {
		TextView itemTitle;
		TextView itemDate;
		View itemSymbole;
	}

	public InfoTraficLigneArrayAdapter(Context context, List<InfoTrafic> objects) {
		super(context, R.layout.list_item_trafic_ligne, objects);
	}

	@Override
	public void bindView(View view, Context context, int position) {
		final ViewHolder holder = (ViewHolder) view.getTag();
		final InfoTrafic item = getItem(position);
		final Ligne ligne = (Ligne) item.getSection();

		if (item instanceof EmptyInfoTrafic) {
			view.findViewById(R.id.contentView).setVisibility(View.GONE);

			view.setEnabled(true);
			view.setClickable(true);
			view.setFocusable(true);
			view.setFocusableInTouchMode(true);
		} else {
			view.findViewById(R.id.contentView).setVisibility(View.VISIBLE);

			holder.itemTitle.setText(item.getIntitule());
			holder.itemDate.setText(item.getDateFormated());
			holder.itemSymbole.setBackgroundDrawable(ColorUtils.getGradiant(ligne.couleurBackground));

			if (isCurrent(item)) {
				holder.itemDate.setCompoundDrawablesWithIntrinsicBounds(R.drawable.info_trafic_on, 0, 0, 0);
			} else {
				holder.itemDate.setCompoundDrawablesWithIntrinsicBounds(R.drawable.info_trafic_off, 0, 0, 0);
			}

			view.setEnabled(false);
			view.setClickable(false);
			view.setFocusable(false);
			view.setFocusableInTouchMode(false);
		}

	}

	@Override
	public void bindViewHolder(View view) {
		final ViewHolder holder = new ViewHolder();
		holder.itemTitle = (TextView) view.findViewById(R.id.itemTitle);
		holder.itemDate = (TextView) view.findViewById(R.id.itemDate);
		holder.itemSymbole = view.findViewById(R.id.itemSymbole);

		view.setTag(holder);
	}

	/**
	 * Déterminer si l'infotrafic est en cours.
	 * 
	 * @param item
	 * @return <code>true</code> si l'infotrafic est en cours,
	 *         <code>false</code> sinon.
	 */
	private static boolean isCurrent(InfoTrafic infoTrafic) {
		return (infoTrafic.getDateDebut() != null && infoTrafic.getDateDebut().isBeforeNow() && (infoTrafic
				.getDateFin() == null || infoTrafic.getDateFin().isAfterNow()));
	}

}
