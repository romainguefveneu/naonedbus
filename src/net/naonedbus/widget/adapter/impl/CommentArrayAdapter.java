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
import net.naonedbus.bean.LiveNews;
import net.naonedbus.bean.Route;
import net.naonedbus.security.NaonedbusClient;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.utils.FontUtils;
import net.naonedbus.widget.adapter.ArraySectionAdapter;
import net.naonedbus.widget.adapter.impl.CommentArrayAdapter.ViewHolder;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.BufferType;

public class CommentArrayAdapter extends ArraySectionAdapter<LiveNews> {

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

	private final Typeface robotoMedium;
	private final Typeface robotoCondensed;

	public CommentArrayAdapter(final Context context) {
		super(context, R.layout.list_item_livenews);
		robotoMedium = FontUtils.getRobotoMedium(context);
		robotoCondensed = FontUtils.getRobotoBoldCondensed(context);
	}

	@Override
	public void bindView(final View view, final Context context, final int position) {
		final LiveNews item = getItem(position);
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

		holder.ligneCode.setTypeface(robotoCondensed);
		holder.itemDate.setTypeface(robotoMedium);

		view.setTag(holder);
	}

}

/**
 * Défini un Adapter gérant l'affichage d'une route de liveNews
 */
interface CommentaireAdapter {
	void setObject(View itemView, LiveNews item);
}

/**
 * route de liveNews de type Tweet @tan_trafic.
 */
class TanTraficCommentaireAdapter implements CommentaireAdapter {

	@Override
	public void setObject(final View itemView, final LiveNews item) {
		final ViewHolder holder = (ViewHolder) itemView.getTag();

		holder.ligneCodeBackground.setBackgroundResource(R.drawable.logo_tan);
		holder.ligneCodeBackground.setImageResource(0);
		holder.ligneCodeBackground.setVisibility(View.VISIBLE);
		holder.ligneCode.setVisibility(View.GONE);

		holder.itemDescription.setText(item.getMessage(), BufferType.SPANNABLE);
		holder.itemDate.setText(item.getDelay());

		holder.itemTitle.setVisibility(View.VISIBLE);
		holder.itemTitle.setText(R.string.commentaire_tan_info_trafic);
	}

}

/**
 * route de liveNews de type Tweet @tan_actus.
 */
class TanActusCommentaireAdapter implements CommentaireAdapter {

	@Override
	public void setObject(final View itemView, final LiveNews item) {
		final ViewHolder holder = (ViewHolder) itemView.getTag();

		holder.ligneCodeBackground.setBackgroundResource(R.drawable.logo_tan);
		holder.ligneCodeBackground.setImageResource(0);
		holder.ligneCodeBackground.setVisibility(View.VISIBLE);
		holder.ligneCode.setVisibility(View.GONE);

		holder.itemDescription.setText(item.getMessage(), BufferType.SPANNABLE);
		holder.itemDate.setText(item.getDelay());

		holder.itemTitle.setVisibility(View.VISIBLE);
		holder.itemTitle.setText(R.string.commentaire_tan_actus);
	}

}

/**
 * route de liveNews de type Tweet @TANinfos.
 */
class TanInfosCommentaireAdapter implements CommentaireAdapter {

	@Override
	public void setObject(final View itemView, final LiveNews item) {
		final ViewHolder holder = (ViewHolder) itemView.getTag();

		holder.ligneCodeBackground.setBackgroundResource(R.drawable.logo_taninfos_background);
		holder.ligneCodeBackground.setImageResource(R.drawable.logo_taninfos);
		holder.ligneCodeBackground.setVisibility(View.VISIBLE);
		holder.ligneCode.setVisibility(View.GONE);

		holder.itemDescription.setText(item.getMessage(), BufferType.SPANNABLE);
		holder.itemDate.setText(item.getDelay());

		holder.itemTitle.setVisibility(View.VISIBLE);
		holder.itemTitle.setText(R.string.commentaire_tan_infos);
	}

}

/**
 * route de liveNews de type Message de service naonedbus
 */
class MessageServiceCommentaireAdapter implements CommentaireAdapter {

	@Override
	public void setObject(final View itemView, final LiveNews item) {
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
 * route de liveNews de type standard
 */
class DefaultCommentaireAdapter implements CommentaireAdapter {

	@Override
	public void setObject(final View itemView, final LiveNews item) {
		final ViewHolder holder = (ViewHolder) itemView.getTag();

		String title = "";

		holder.ligneCodeBackground.setVisibility(View.GONE);
		holder.ligneCode.setVisibility(View.VISIBLE);

		if (item.getRoute() != null) {

			final Route route = item.getRoute();
			if (item.getBackground() == null) {
				item.setBackground(ColorUtils.getRoundedGradiant(route.getBackColor()));
			}

			holder.ligneCode.setText(route.getLetter());
			holder.ligneCode.setBackgroundDrawable(item.getBackground());
			holder.ligneCode.setTextColor(route.getFrontColor());

		} else {

			holder.ligneCode.setText(R.string.target_toutes_lignes_symbole);
			holder.ligneCode.setBackgroundDrawable(null);
			holder.ligneCode.setTextColor(Color.BLACK);

		}

		if (item.getStop() == null && item.getDirection() == null && item.getRoute() == null) {
			title = itemView.getContext().getString(R.string.commentaire_tout);
		} else {
			if (item.getStop() != null) {
				title = item.getStop().getName() + " ";
			}
			if (item.getDirection() != null) {
				title = title + "\u2192 " + item.getDirection().getName();
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
