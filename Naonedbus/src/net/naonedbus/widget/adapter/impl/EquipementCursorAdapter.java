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

import net.naonedbus.R;
import net.naonedbus.bean.Equipement;
import net.naonedbus.manager.impl.EquipementManager.SousType;
import net.naonedbus.provider.table.EquipementTable;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.widget.adapter.CursorSectionAdapter;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class EquipementCursorAdapter extends CursorSectionAdapter {

	private int mColIdType;
	private int mColIdSousType;
	private int mColNom;
	private int mColAdresse;
	private int mColDetails;

	public EquipementCursorAdapter(Context context, Cursor c) {
		super(context, c, R.layout.list_item_equipement);
		if (c != null) {
			initColumns();
		}
	}

	@Override
	public void changeCursor(Cursor cursor) {
		super.changeCursor(cursor);
		if (cursor != null) {
			initColumns();
		}
	}

	private void initColumns() {
		final Cursor c = getCursor();
		mColIdType = c.getColumnIndex(EquipementTable.ID_TYPE);
		mColIdSousType = c.getColumnIndex(EquipementTable.ID_SOUS_TYPE);
		mColNom = c.getColumnIndex(EquipementTable.NOM);
		mColAdresse = c.getColumnIndex(EquipementTable.ADRESSE);
		mColDetails = c.getColumnIndex(EquipementTable.DETAILS);
	}

	@Override
	protected void bindViewHolder(View view) {
		final ViewHolder holder = new ViewHolder();
		holder.itemTitle = (TextView) view.findViewById(R.id.itemTitle);
		holder.itemDescription = (TextView) view.findViewById(R.id.itemDescription);
		holder.itemSymbole = (ImageView) view.findViewById(R.id.itemSymbole);
		holder.itemLignes = (ViewGroup) view.findViewById(R.id.itemLignes);
		view.setTag(holder);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		super.bindView(view, context, cursor);

		final ViewHolder holder = (ViewHolder) view.getTag();

		final String nom = cursor.getString(mColNom);
		final int typeId = cursor.getInt(mColIdType);
		final int sousTypeId = cursor.getInt(mColIdSousType);
		final String details = cursor.getString(mColDetails);
		final String adresse = cursor.getString(mColAdresse);

		final Equipement.Type type = Equipement.Type.getTypeById(typeId);

		holder.itemTitle.setText(nom);

		// Définir le fond de l'icone.
		if (sousTypeId != 0) {
			final SousType sousType = SousType.getTypeByValue(sousTypeId);
			holder.itemSymbole.setImageResource(sousType.getDrawableRes());
		} else {
			holder.itemSymbole.setImageResource(type.getDrawableRes());
		}
		holder.itemSymbole.setBackgroundDrawable(ColorUtils.getRoundedGradiant(context.getResources().getColor(
				type.getBackgroundColorRes())));

		// Détail ou adresse
		if (TextUtils.isEmpty(details) && TextUtils.isEmpty(adresse)) {
			holder.itemDescription.setVisibility(View.GONE);
		} else {
			holder.itemDescription.setText((details != null) ? details : adresse);
			holder.itemDescription.setVisibility(View.VISIBLE);
		}
		holder.itemLignes.setVisibility(View.GONE);

	}

	public int getColIdType() {
		return mColIdType;
	}

	public int getColIdSousType() {
		return mColIdSousType;
	}

	public int getColNom() {
		return mColNom;
	}

	public int getColAdresse() {
		return mColAdresse;
	}

	public int getColDetails() {
		return mColDetails;
	}

	private class ViewHolder {
		TextView itemTitle;
		TextView itemDescription;
		ViewGroup itemLignes;
		ImageView itemSymbole;
	}

}
