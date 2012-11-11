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
import net.naonedbus.bean.Ligne;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.utils.FontUtils;
import net.naonedbus.widget.adapter.SectionAdapter;
import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

/**
 * Liste des commentaires avec sépérateurs
 * 
 * @author romain
 */
public class LignesArrayAdapter extends SectionAdapter<Ligne> {

	private static class ViewHolder {
		TextView icon;
		TextView sens1;
		TextView sens2;
	}

	private final Typeface robotoLight;

	public LignesArrayAdapter(Context context, List<Ligne> lignes) {
		super(context, R.layout.list_item_ligne, lignes);
		robotoLight = FontUtils.getRobotoLight(context);
	}

	@Override
	public void bindView(View view, Context context, int position) {
		final ViewHolder holder = (ViewHolder) view.getTag();
		final Ligne ligne = getItem(position);
		holder.icon.setText(ligne.lettre);
		holder.icon.setTextColor(ligne.couleurTexte);
		if (ligne.couleurBackground == 0) {
			holder.icon.setBackgroundResource(R.drawable.item_symbole_back);
		} else {
			holder.icon.setBackgroundDrawable(ColorUtils.getRoundedGradiant(ligne.couleurBackground));
		}
		if ((ligne.depuis == null || ligne.depuis.length() == 0)) {
			holder.sens1.setText(ligne.nom);
			holder.sens2.setVisibility(View.GONE);
		} else {
			holder.sens1.setText(ligne.depuis);
			holder.sens2.setText(ligne.vers);
			holder.sens2.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void bindViewHolder(View view) {
		final ViewHolder holder;
		holder = new ViewHolder();
		holder.icon = (TextView) view.findViewById(R.id.itemSymbole);
		holder.sens1 = (TextView) view.findViewById(R.id.ligneFrom);
		holder.sens2 = (TextView) view.findViewById(R.id.ligneTo);
		holder.icon.setTypeface(robotoLight);

		view.setTag(holder);
	}

}
