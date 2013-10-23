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
package net.naonedbus.card.impl;

import java.io.IOException;
import java.util.List;

import net.naonedbus.BuildConfig;
import net.naonedbus.R;
import net.naonedbus.bean.TanNews;
import net.naonedbus.bean.Route;
import net.naonedbus.card.Card;
import net.naonedbus.fragment.impl.TanNewsDetailDialogFragment;
import net.naonedbus.fragment.impl.TanNewsDetailFragment;
import net.naonedbus.manager.impl.TanNewsManager;

import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class TraficCard extends Card<List<TanNews>> {

	private static final String LOG_TAG = "TraficCard";
	private static final boolean DBG = BuildConfig.DEBUG;

	private final Route mRoute;
	private ViewGroup mRoot;

	public TraficCard(final Context context, final LoaderManager loaderManager, final FragmentManager fragmentManager,
			final Route route) {
		super(context, loaderManager, fragmentManager, R.string.card_trafic_title, R.layout.card_news);
		mRoute = route;
	}

	@Override
	public void onResume() {
		super.onResume();
		initLoader(null, this).forceLoad();
	}

	@Override
	protected Intent getMoreIntent() {
		return null;
	}

	@Override
	protected void bindView(final Context context, final View base, final View view) {
		mRoot = (ViewGroup) view;
	}

	@Override
	public Loader<List<TanNews>> onCreateLoader(final int arg0, final Bundle arg1) {
		return new LoaderTask(getContext(), mRoute);
	}

	@Override
	public void onLoadFinished(final Loader<List<TanNews>> loader, final List<TanNews> infoTrafics) {

		if (infoTrafics == null || infoTrafics.isEmpty()) {
			showMessage(R.string.no_tannews, R.drawable.ic_checkmark_holo_light);
		} else {
			final LayoutInflater inflater = LayoutInflater.from(getContext());

			mRoot.removeAllViews();
			for (final TanNews infoTrafic : infoTrafics) {
				mRoot.addView(createView(inflater, mRoot, infoTrafic));
			}

			showContent();
		}

	}

	private View createView(final LayoutInflater inflater, final ViewGroup root, final TanNews infoTrafic) {
		final View view = inflater.inflate(R.layout.card_item_trafic_ligne, root, false);

		final TextView itemTitle = (TextView) view.findViewById(R.id.itemTitle);
		final TextView itemDate = (TextView) view.findViewById(R.id.itemDate);

		itemTitle.setText(infoTrafic.getTitle());
		itemDate.setText(infoTrafic.getDateFormated());

		if (isCurrent(infoTrafic)) {
			itemDate.setCompoundDrawablesWithIntrinsicBounds(R.drawable.info_trafic_on, 0, 0, 0);
		} else {
			itemDate.setCompoundDrawablesWithIntrinsicBounds(R.drawable.info_trafic_off, 0, 0, 0);
		}

		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				final Bundle bundle = new Bundle();
				bundle.putParcelable(TanNewsDetailFragment.PARAM_TAN_NEWS, infoTrafic);

				final DialogFragment dialogFragment = new TanNewsDetailDialogFragment();
				dialogFragment.setArguments(bundle);
				dialogFragment.show(getFragmentManager(), "InfoTraficDetailDialogFragment");
			}
		});

		return view;
	}

	/**
	 * Déterminer si l'infotrafic est en cours.
	 * 
	 * @param item
	 * @return <code>true</code> si l'infotrafic est en cours,
	 *         <code>false</code> sinon.
	 */
	private static boolean isCurrent(final TanNews infoTrafic) {
		return (infoTrafic.getStartDate() != null && infoTrafic.getStartDate().isBeforeNow() && (infoTrafic
				.getEndDate() == null || infoTrafic.getEndDate().isAfterNow()));
	}

	private static class LoaderTask extends AsyncTaskLoader<List<TanNews>> {
		private final Route mRoute;

		public LoaderTask(final Context context, final Route route) {
			super(context);
			mRoute = route;
		}

		@Override
		public List<TanNews> loadInBackground() {
			final TanNewsManager manager = TanNewsManager.getInstance();

			List<TanNews> infoTrafics = null;
			try {
				infoTrafics = manager.getByRouteCode(getContext(), mRoute.getCode());
			} catch (final IOException e) {
				if (DBG)
					Log.e(LOG_TAG, "Erreur de récupération des infos trafic.", e);
			} catch (final JSONException e) {
				if (DBG)
					Log.e(LOG_TAG, "Erreur de récupération des infos trafic.", e);
			}

			return infoTrafics;
		}
	}
}
