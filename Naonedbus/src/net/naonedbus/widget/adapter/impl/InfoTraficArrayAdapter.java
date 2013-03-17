/**
 *  Copyright (C) 2011 Romain Guefveneu
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
import net.naonedbus.bean.InfoTrafic;
import net.naonedbus.helper.DateTimeFormatHelper;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class InfoTraficArrayAdapter extends ArrayAdapter<InfoTrafic> {

	private final DateTimeFormatHelper dateTimeFormatHelper;

	static class ViewHolder {
		TextView itemTitle = null;
		TextView description = null;
		TextView date = null;
		LinearLayout zoneTitle = null;
	}

	public InfoTraficArrayAdapter(final Context context, final List<InfoTrafic> objects) {
		super(context, R.layout.list_item_trafic, objects);
		this.dateTimeFormatHelper = new DateTimeFormatHelper(context);
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		LinearLayout newView;
		final InfoTrafic item = getItem(position);

		ViewHolder holder;

		if (convertView == null) {
			newView = new LinearLayout(getContext());
			final String inflater = Context.LAYOUT_INFLATER_SERVICE;
			final LayoutInflater vi = (LayoutInflater) getContext().getSystemService(inflater);
			vi.inflate(R.layout.list_item_trafic, newView, true);

			holder = new ViewHolder();
			holder.itemTitle = (TextView) newView.findViewById(R.id.itemTitle);
			holder.description = (TextView) newView.findViewById(R.id.itemDescription);
			holder.date = (TextView) newView.findViewById(R.id.itemTime);
			holder.zoneTitle = (LinearLayout) newView.findViewById(R.id.zoneTitle);

			newView.setTag(holder);
		} else {
			newView = (LinearLayout) convertView;
			holder = (ViewHolder) convertView.getTag();
		}

		holder.itemTitle.setText(item.getIntitule());

		if (item.getResume() != null) {
			holder.description.setText(Html.fromHtml(item.getResume()));
		} else if (item.getTexteVocal() != null) {
			holder.description.setText("Texte Vocal : \u00AB " + item.getTexteVocal() + " \u00BB");
		}

		holder.date.setText(dateTimeFormatHelper.formatDuree(item.getDateDebut(), item.getDateFin()));

		return newView;
	}

	/**
	 * Déterminer si l'infotrafic est en cours.
	 * 
	 * @param item
	 * @return <code>true</code> si l'infotrafic est en cours,
	 *         <code>false</code> sinon.
	 */
	private static boolean isCurrent(final InfoTrafic infoTrafic) {
		return (infoTrafic.getDateDebut() != null && infoTrafic.getDateDebut().isBeforeNow() && (infoTrafic
				.getDateFin() == null || infoTrafic.getDateFin().isAfterNow()));
	}
}
