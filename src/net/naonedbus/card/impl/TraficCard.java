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
import net.naonedbus.bean.InfoTrafic;
import net.naonedbus.bean.Ligne;
import net.naonedbus.card.Card;
import net.naonedbus.fragment.impl.InfoTraficDetailDialogFragment;
import net.naonedbus.fragment.impl.InfoTraficDetailFragment;
import net.naonedbus.manager.impl.InfoTraficManager;

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

public class TraficCard extends Card<List<InfoTrafic>> {

	private static final String LOG_TAG = "TraficCard";
	private static final boolean DBG = BuildConfig.DEBUG;

	private final Ligne mLigne;
	private ViewGroup mRoot;

	public TraficCard(final Context context, final LoaderManager loaderManager, final FragmentManager fragmentManager,
			final Ligne ligne) {
		super(context, loaderManager, fragmentManager, R.string.card_trafic_title, R.layout.card_trafic);
		mLigne = ligne;
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
	public Loader<List<InfoTrafic>> onCreateLoader(final int arg0, final Bundle arg1) {
		return new LoaderTask(getContext(), mLigne);
	}

	@Override
	public void onLoadFinished(final Loader<List<InfoTrafic>> loader, final List<InfoTrafic> infoTrafics) {

		if (infoTrafics == null || infoTrafics.isEmpty()) {
			showMessage(R.string.msg_nothing_info_trafic, R.drawable.ic_checkmark_holo_light);
		} else {
			final LayoutInflater inflater = LayoutInflater.from(getContext());

			mRoot.removeAllViews();
			for (final InfoTrafic infoTrafic : infoTrafics) {
				mRoot.addView(createView(inflater, mRoot, infoTrafic));
			}

			showContent();
		}

	}

	private View createView(final LayoutInflater inflater, final ViewGroup root, final InfoTrafic infoTrafic) {
		final View view = inflater.inflate(R.layout.card_item_trafic_ligne, root, false);

		final TextView itemTitle = (TextView) view.findViewById(R.id.itemTitle);
		final TextView itemDate = (TextView) view.findViewById(R.id.itemDate);

		itemTitle.setText(infoTrafic.getIntitule());
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
				bundle.putParcelable(InfoTraficDetailFragment.PARAM_INFO_TRAFIC, infoTrafic);

				final DialogFragment dialogFragment = new InfoTraficDetailDialogFragment();
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
	private static boolean isCurrent(final InfoTrafic infoTrafic) {
		return (infoTrafic.getDateDebut() != null && infoTrafic.getDateDebut().isBeforeNow() && (infoTrafic
				.getDateFin() == null || infoTrafic.getDateFin().isAfterNow()));
	}

	private static class LoaderTask extends AsyncTaskLoader<List<InfoTrafic>> {
		private final Ligne mLigne;

		public LoaderTask(final Context context, final Ligne ligne) {
			super(context);
			mLigne = ligne;
		}

		@Override
		public List<InfoTrafic> loadInBackground() {
			final InfoTraficManager manager = InfoTraficManager.getInstance();

			List<InfoTrafic> infoTrafics = null;
			try {
				infoTrafics = manager.getByLigneCode(getContext(), mLigne.getCode());
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
