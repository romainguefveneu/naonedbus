package net.naonedbus.widget.adapter.impl;

import net.naonedbus.R;
import net.naonedbus.bean.Favori;
import net.naonedbus.manager.impl.FavoriManager;
import net.naonedbus.utils.ColorUtils;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

public class FavoriCursorAdapter extends CursorAdapter {

	private LayoutInflater mLayoutInflater;
	private FavoriManager mFavoriManager;

	public FavoriCursorAdapter(Context context, Cursor c) {
		super(context, c, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		mLayoutInflater = LayoutInflater.from(context);
		mFavoriManager = FavoriManager.getInstance();
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final ViewHolder holder = (ViewHolder) view.getTag();
		final Favori item = mFavoriManager.getSingleFromCursor(cursor);

		if (item.background == null) {
			final GradientDrawable background = (GradientDrawable) ColorUtils
					.getRoundedGradiant(item.couleurBackground);
			item.background = background;
			item.couleurTexte = (ColorUtils.isLightColor(item.couleurBackground) ? Color.BLACK : Color.WHITE);
		}

		holder.ligneCode.setText(item.lettre);
		holder.ligneCode.setBackgroundDrawable(item.background);
		holder.ligneCode.setTextColor(item.couleurTexte);

		if (item.nomFavori == null) {
			holder.itemTitle.setText(item.nomArret);
			holder.itemDescription.setText("\u2192 " + item.nomSens);
		} else {
			holder.itemTitle.setText(item.nomFavori);
			holder.itemDescription.setText(item.nomArret + " \u2192 " + item.nomSens);
		}

		if (item.delay == null) {
			holder.nextHoraire.setVisibility(View.GONE);
			holder.progressBar.setVisibility(View.VISIBLE);
		} else {
			holder.progressBar.setVisibility(View.GONE);
			holder.nextHoraire.setText(item.delay);
			holder.nextHoraire.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup root) {
		final View view = mLayoutInflater.inflate(R.layout.list_item_favori, root, false);

		final ViewHolder holder = new ViewHolder();
		holder.ligneCode = (TextView) view.findViewById(R.id.itemSymbole);
		holder.itemTitle = (TextView) view.findViewById(R.id.itemTitle);
		holder.itemDescription = (TextView) view.findViewById(R.id.itemDescription);
		holder.progressBar = (ProgressBar) view.findViewById(R.id.loading);
		holder.nextHoraire = (TextView) view.findViewById(R.id.itemTime);

		view.setTag(holder);

		return view;
	}

	private static class ViewHolder {
		TextView ligneCode;
		TextView itemTitle;
		TextView itemDescription;
		ProgressBar progressBar;
		TextView nextHoraire;
	}
}
