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
import net.naonedbus.widget.adapter.SectionAdapter;
import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.TextView;

/**
 * Liste des commentaires avec sépérateurs
 * 
 * @author romain
 */
public class LignesArrayExpandableAdapter extends SectionAdapter<Ligne> implements ExpandableListAdapter {

	private static class ViewHolder {
		TextView icon;
		TextView sens1;
		TextView sens2;
	}

	private final Typeface robotoLight;

	public LignesArrayExpandableAdapter(Context context, List<Ligne> lignes) {
		super(context, R.layout.list_item_ligne, lignes);
		robotoLight = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
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

	@Override
	public int getGroupCount() {
		return 0;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return 0;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return null;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return null;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return 0;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		final View v;
		if (convertView == null) {
			v = newView(getContext(), groupPosition);
		} else {
			v = convertView;
		}
		bindSectionHeader(v, groupPosition);
		bindView(v, getContext(), groupPosition);

		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
			ViewGroup parent) {
		final View v;
		if (convertView == null) {
			v = newView(getContext(), childPosition);
		} else {
			v = convertView;
		}
		bindView(v, getContext(), childPosition);

		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	@Override
	public void onGroupExpanded(int groupPosition) {

	}

	@Override
	public void onGroupCollapsed(int groupPosition) {

	}

	@Override
	public long getCombinedChildId(long groupId, long childId) {
		return 0;
	}

	@Override
	public long getCombinedGroupId(long groupId) {
		return 0;
	}

}
