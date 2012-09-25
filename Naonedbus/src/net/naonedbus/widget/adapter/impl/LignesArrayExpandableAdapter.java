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
import net.naonedbus.bean.Sens;
import net.naonedbus.utils.ColorUtils;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.PorterDuff.Mode;
import android.graphics.Typeface;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Liste des commentaires avec sépérateurs
 * 
 * @author romain
 */
public class LignesArrayExpandableAdapter implements ExpandableListAdapter {

	private static class GroupViewHolder {
		TextView icon;
		ImageView arrowBottom;
		TextView sens1;
		TextView sens2;
	}

	private static class ChildViewHolder {
		ImageView icon;
		TextView text;
	}

	private final Typeface mRobotoLight;

	private Context mContext;
	private LayoutInflater mLayoutInflater;

	private List<Ligne> mLignes;
	private SparseArray<List<Sens>> mSens;

	public LignesArrayExpandableAdapter(Context context, List<Ligne> lignes, SparseArray<List<Sens>> sens) {
		mContext = context;
		mLayoutInflater = LayoutInflater.from(context);
		mRobotoLight = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
		mLignes = lignes;
		mSens = sens;
	}

	private Context getContext() {
		return mContext;
	}

	public void bindGroupView(View view, Context context, int groupPosition) {
		final GroupViewHolder holder = (GroupViewHolder) view.getTag();
		final Ligne ligne = (Ligne) getGroup(groupPosition);
		holder.icon.setText(ligne.lettre);
		holder.icon.setTextColor(ligne.couleurTexte);
		if (ligne.couleurBackground == 0) {
			holder.icon.setBackgroundResource(R.drawable.item_symbole_back);
		} else {
			holder.icon.setBackgroundDrawable(ColorUtils.getRoundedGradiant(ligne.couleurBackground));
			holder.arrowBottom.setColorFilter(ColorUtils.getDarkerColor(ligne.couleurBackground), Mode.MULTIPLY);
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

	public void bindChildView(View view, Context context, int groupPosition, int childPosition) {
		final ChildViewHolder holder = (ChildViewHolder) view.getTag();
		final Ligne ligne = (Ligne) getGroup(groupPosition);
		final Sens sens = (Sens) getChild(groupPosition, childPosition);
		holder.icon.setBackgroundDrawable(ColorUtils.getRoundedGradiant(ligne.couleurBackground));
		holder.icon.setColorFilter(ligne.couleurTexte, Mode.MULTIPLY);
		holder.text.setText(sens.text);
	}

	public void bindGroupViewHolder(View view) {
		final GroupViewHolder holder;
		holder = new GroupViewHolder();
		holder.icon = (TextView) view.findViewById(R.id.itemSymbole);
		holder.sens1 = (TextView) view.findViewById(R.id.ligneFrom);
		holder.sens2 = (TextView) view.findViewById(R.id.ligneTo);
		holder.icon.setTypeface(mRobotoLight);

		holder.arrowBottom = (ImageView) view.findViewById(R.id.arrowBottom);

		view.findViewById(R.id.header_view).setVisibility(View.GONE);

		view.setTag(holder);
	}

	public void bindChildViewHolder(View view) {
		final ChildViewHolder holder;
		holder = new ChildViewHolder();
		holder.text = (TextView) view.findViewById(R.id.itemTitle);
		holder.icon = (ImageView) view.findViewById(R.id.itemIcon);
		holder.icon.setImageResource(R.drawable.ic_action_forward_light);

		view.setTag(holder);
	}

	@Override
	public int getGroupCount() {
		return this.mLignes.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return this.mSens.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return this.mLignes.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return this.mSens.get(groupPosition).get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return this.mLignes.get(groupPosition)._id;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return this.mSens.get(groupPosition).get(childPosition)._id;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.list_item_ligne, null);
			bindGroupViewHolder(convertView);
		}
		bindGroupView(convertView, getContext(), groupPosition);

		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
			ViewGroup parent) {
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.list_item_icon, null);
			bindChildViewHolder(convertView);
		}
		bindChildView(convertView, getContext(), groupPosition, childPosition);

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

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isEmpty() {
		return mLignes.isEmpty();
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {

	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {

	}

}
