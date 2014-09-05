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
import net.naonedbus.bean.Parcours;
import net.naonedbus.bean.horaire.Attente;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.utils.FontUtils;
import net.naonedbus.utils.FormatUtils;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ParcoursAdapter extends ArrayAdapter<Parcours> {

	private final Typeface mTypeface;
	private List<Attente> mAttentes;

	public ParcoursAdapter(final Context context, final List<Parcours> objects, List<Attente> attentes) {
		super(context, 0, objects);
		mTypeface = FontUtils.getRobotoBoldCondensed(context);
		mAttentes = attentes;
	}

	@Override
	public View getView(final int position, View convertView, final ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_parcours, null);
			bindViewHolder(convertView);
		}
		bindView(convertView, position);
		return convertView;
	}

	protected void bindView(final View view, final int position) {
		final ViewHolder holder = (ViewHolder) view.getTag();
		final Parcours parcours = getItem(position);
		holder.itemSymbole.setText(parcours.lettre);
		holder.itemSymbole.setBackgroundDrawable(ColorUtils.getRoundedGradiant(parcours.couleurBack));
		holder.itemSymbole.setTextColor(parcours.couleurFront);
		holder.itemTitle.setText(FormatUtils.formatSens(parcours.nomSens));

		String temps = getAttenteTemps(parcours);
		if (temps != null) {
			holder.itemTime.setText(temps);
			holder.itemTime.setVisibility(View.VISIBLE);
		} else {
			holder.itemTime.setVisibility(View.GONE);
		}
	}

	protected void bindViewHolder(final View view) {
		final ViewHolder holder;
		holder = new ViewHolder();
		holder.itemSymbole = (TextView) view.findViewById(R.id.itemSymbole);
		holder.itemTitle = (TextView) view.findViewById(R.id.itemTitle);
		holder.itemTime = (TextView) view.findViewById(R.id.itemTime);

		holder.itemSymbole.setTypeface(mTypeface);
		view.setTag(holder);
	}

	private String getAttenteTemps(Parcours parcours) {
		if (mAttentes == null)
			return null;

		for (Attente attente : mAttentes) {
			if (attente.getCodeLigne().equals(parcours.codeLigne)
					&& attente.getCodeSens().equals(attente.getCodeSens())
					&& attente.getCodeArret().equals(parcours.codeArret))
				return attente.getTemps();
		}

		return null;
	}

	protected static class ViewHolder {
		TextView itemSymbole;
		TextView itemTitle;
		TextView itemTime;
	}
}
