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
import net.naonedbus.bean.EmptyInfoTrafic;
import net.naonedbus.bean.InfoTrafic;
import net.naonedbus.bean.Ligne;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.widget.adapter.SectionAdapter;
import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;

public class InfoTraficLigneArrayAdapter extends SectionAdapter<InfoTrafic> {

	static class ViewHolder {
		TextView itemTitle = null;
		TextView itemDate = null;
	}

	private SparseArray<Integer> mTraficImages;

	private static final int ETAT_TRAFIC_OK = 1;
	private static final int ETAT_TRAFIC_PERTURBATION = 2;

	public InfoTraficLigneArrayAdapter(Context context, List<InfoTrafic> objects) {
		super(context, R.layout.list_item_trafic_ligne, objects);

		mTraficImages = new SparseArray<Integer>();
		mTraficImages.put(ETAT_TRAFIC_OK, R.drawable.trafic_ok);
		mTraficImages.put(ETAT_TRAFIC_PERTURBATION, R.drawable.trafic_perturbation);
	}

	@Override
	public void bindView(View view, Context context, int position) {
		final ViewHolder holder = (ViewHolder) view.getTag();
		final InfoTrafic item = getItem(position);

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

			if (isCurrent(item)) {
				holder.itemDate.setCompoundDrawablesWithIntrinsicBounds(R.drawable.trafic_perturbation, 0, 0, 0);
			} else {
				holder.itemDate.setCompoundDrawablesWithIntrinsicBounds(R.drawable.trafic_unknown, 0, 0, 0);
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

		view.setTag(holder);
	}

	/**
	 * Remplir la section.
	 * 
	 * @param view
	 * @param position
	 */
	protected void bindSectionHeader(View view, int position) {
		final View headerView = view.findViewById(R.id.headerView);

		if (headerView != null) {
			final int section = getSectionForPosition(position);
			final TextView headerTextView = (TextView) view.findViewById(R.id.headerTitle);
			final View headerUnderline = view.findViewById(R.id.headerUnderline);

			if (getPositionForSection(section) == position) {
				final String title = (String) mIndexer.getSections()[section];
				headerTextView.setText(title);
				headerTextView.setVisibility(View.VISIBLE);
				headerUnderline.setVisibility(View.VISIBLE);
				customizeHeaderView(headerView, position);
			} else {
				headerUnderline.setVisibility(View.GONE);
				headerTextView.setVisibility(View.GONE);
			}

		}

	}

	@Override
	public void customizeHeaderView(View view, int position) {
		final InfoTrafic item = getItem(position);
		final Ligne ligne = (Ligne) item.getSection();

		final TextView headerTitle = (TextView) view.findViewById(R.id.headerTitle);
		final View headerUnderline = view.findViewById(R.id.headerUnderline);

		final int color = ColorUtils.isLightColor(ligne.couleurBackground) ? ColorUtils
				.getDarkerColor(ligne.couleurBackground) : ligne.couleurBackground;

		headerTitle.setTextColor(color);
		headerUnderline.setBackgroundColor(color);
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
