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

import java.util.HashMap;
import java.util.Map;

import net.naonedbus.R;
import net.naonedbus.bean.Commentaire;
import net.naonedbus.bean.Ligne;
import net.naonedbus.security.NaonedbusClient;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.widget.adapter.ArraySectionAdapter;
import net.naonedbus.widget.adapter.impl.CommentaireArrayAdapter.ViewHolder;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.BufferType;

public class CommentaireArrayAdapter extends ArraySectionAdapter<Commentaire> {

	private static final Map<String, CommentaireAdapter> adapterMap = new HashMap<String, CommentaireAdapter>();
	static {
		final DefaultCommentaireAdapter defaultCommentaireAdapter = new DefaultCommentaireAdapter();
		adapterMap.put(null, defaultCommentaireAdapter);
		adapterMap.put(NaonedbusClient.NAONEDBUS.name(), defaultCommentaireAdapter);
		adapterMap.put(NaonedbusClient.SIMPLETAN.name(), defaultCommentaireAdapter);
		adapterMap.put(NaonedbusClient.TWITTER_TAN_TRAFIC.name(), new TanTraficCommentaireAdapter());
		adapterMap.put(NaonedbusClient.TWITTER_TAN_ACTUS.name(), new TanActusCommentaireAdapter());
		adapterMap.put(NaonedbusClient.TWITTER_TAN_INFOS.name(), new TanInfosCommentaireAdapter());
		adapterMap.put(NaonedbusClient.NAONEDBUS_SERVICE.name(), new MessageServiceCommentaireAdapter());
	}

	static class ViewHolder {
		TextView itemTitle;
		TextView itemDate;
		TextView itemDescription;
		ImageView ligneCodeBackground;
		TextView ligneCode;
	}

	public CommentaireArrayAdapter(final Context context) {
		super(context, R.layout.list_item_commentaire);
	}

	@Override
	public void bindView(final View view, final Context context, final int position) {
		final Commentaire item = getItem(position);
		CommentaireAdapter adapter;

		if (adapterMap.containsKey(item.getSource())) {
			adapter = adapterMap.get(item.getSource());
		} else {
			adapter = adapterMap.get(null);
		}

		adapter.setObject(view, item);
	}

	@Override
	public void bindViewHolder(final View view) {
		final ViewHolder holder = new ViewHolder();
		holder.ligneCode = (TextView) view.findViewById(R.id.itemSymbole);
		holder.ligneCodeBackground = (ImageView) view.findViewById(R.id.itemIcon);
		holder.itemTitle = (TextView) view.findViewById(R.id.itemTitle);
		holder.itemDate = (TextView) view.findViewById(R.id.itemTime);
		holder.itemDescription = (TextView) view.findViewById(R.id.itemDescription);

		view.setTag(holder);
	}

}

/**
 * Défini un Adapter gérant l'affichage d'une ligne de commentaire
 */
interface CommentaireAdapter {
	void setObject(View itemView, Commentaire item);
}

/**
 * Ligne de commentaire de type Tweet @infotrafic_tan.
 */
class TanTraficCommentaireAdapter implements CommentaireAdapter {

	@Override
	public void setObject(final View itemView, final Commentaire item) {
		final ViewHolder holder = (ViewHolder) itemView.getTag();

		holder.ligneCodeBackground.setBackgroundResource(R.drawable.item_tan_back);
		holder.ligneCodeBackground.setImageResource(R.drawable.ic_tan);
		holder.ligneCodeBackground.setVisibility(View.VISIBLE);
		holder.ligneCode.setVisibility(View.GONE);

		holder.itemDescription.setText(item.getMessage(), BufferType.SPANNABLE);
		holder.itemDate.setText(item.getDelay());

		holder.itemTitle.setVisibility(View.VISIBLE);
		holder.itemTitle.setText(R.string.commentaire_tan_info_trafic);
	}

}

/**
 * Ligne de commentaire de type Tweet @reseau_tan.
 */
class TanActusCommentaireAdapter implements CommentaireAdapter {

	@Override
	public void setObject(final View itemView, final Commentaire item) {
		final ViewHolder holder = (ViewHolder) itemView.getTag();

		holder.ligneCodeBackground.setBackgroundResource(R.drawable.item_tan_back);
		holder.ligneCodeBackground.setImageResource(R.drawable.ic_tan);
		holder.ligneCodeBackground.setVisibility(View.VISIBLE);
		holder.ligneCode.setVisibility(View.GONE);

		holder.itemDescription.setText(item.getMessage(), BufferType.SPANNABLE);
		holder.itemDate.setText(item.getDelay());

		holder.itemTitle.setVisibility(View.VISIBLE);
		holder.itemTitle.setText(R.string.commentaire_tan_actus);
	}

}

/**
 * Ligne de commentaire de type Tweet @TANinfos.
 */
class TanInfosCommentaireAdapter implements CommentaireAdapter {

	@Override
	public void setObject(final View itemView, final Commentaire item) {
		final ViewHolder holder = (ViewHolder) itemView.getTag();

		holder.ligneCodeBackground.setBackgroundResource(R.drawable.item_tan_infos_back);
		holder.ligneCodeBackground.setImageResource(R.drawable.ic_tan_infos);
		holder.ligneCodeBackground.setVisibility(View.VISIBLE);
		holder.ligneCode.setVisibility(View.GONE);

		holder.itemDescription.setText(item.getMessage(), BufferType.SPANNABLE);
		holder.itemDate.setText(item.getDelay());

		holder.itemTitle.setVisibility(View.VISIBLE);
		holder.itemTitle.setText(R.string.commentaire_tan_infos);
	}

}

/**
 * Ligne de commentaire de type Message de service naonedbus
 */
class MessageServiceCommentaireAdapter implements CommentaireAdapter {

	@Override
	public void setObject(final View itemView, final Commentaire item) {
		final ViewHolder holder = (ViewHolder) itemView.getTag();
		holder.ligneCodeBackground.setBackgroundResource(R.drawable.ic_launcher);
		holder.ligneCodeBackground.setImageResource(0);
		holder.ligneCodeBackground.setVisibility(View.VISIBLE);
		holder.ligneCode.setVisibility(View.GONE);

		holder.itemDescription.setText(item.getMessage(), BufferType.SPANNABLE);
		holder.itemDate.setText(item.getDelay());

		holder.itemTitle.setVisibility(View.VISIBLE);
		holder.itemTitle.setText(R.string.commentaire_message_service);
	}

}

/**
 * Ligne de commentaire de type standard
 */
class DefaultCommentaireAdapter implements CommentaireAdapter {

	@Override
	public void setObject(final View itemView, final Commentaire item) {
		final ViewHolder holder = (ViewHolder) itemView.getTag();

		String title = "";

		holder.ligneCodeBackground.setVisibility(View.GONE);
		holder.ligneCode.setVisibility(View.VISIBLE);

		if (item.getLigne() != null) {

			final Ligne ligne = item.getLigne();
			if (item.getBackground() == null) {
				item.setBackground(ColorUtils.getCircle(ligne.getCouleur()));
			}

			holder.ligneCode.setText(ligne.getLettre());
			holder.ligneCode.setBackgroundDrawable(item.getBackground());
			holder.ligneCode.setTextColor(ligne.getCouleurTexte());

		} else {

			holder.ligneCode.setText(R.string.target_toutes_lignes_symbole);
			holder.ligneCode.setBackgroundDrawable(null);
			holder.ligneCode.setTextColor(Color.BLACK);

		}

		if (item.getArret() == null && item.getSens() == null && item.getLigne() == null) {
			title = itemView.getContext().getString(R.string.commentaire_tout);
		} else {
			if (item.getArret() != null) {
				title = item.getArret().getNomArret() + " ";
			}
			if (item.getSens() != null) {
				title = title + "\u2192 " + item.getSens().text;
			}
		}

		holder.itemDescription.setText(item.getMessage(), BufferType.SPANNABLE);
		holder.itemDate.setText(item.getDelay());

		if (title.trim().length() == 0) {
			holder.itemTitle.setVisibility(View.GONE);
		} else {
			holder.itemTitle.setVisibility(View.VISIBLE);
			holder.itemTitle.setText(title.trim());
		}
	}

}
