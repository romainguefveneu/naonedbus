package net.naonedbus.card.impl;

import java.io.IOException;
import java.util.List;

import net.naonedbus.R;
import net.naonedbus.activity.impl.InfoTraficDetailActivity;
import net.naonedbus.bean.InfoTrafic;
import net.naonedbus.bean.Ligne;
import net.naonedbus.card.Card;
import net.naonedbus.intent.ParamIntent;
import net.naonedbus.manager.impl.InfoTraficManager;

import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class TraficCard extends Card<List<InfoTrafic>> {

	private final Ligne mLigne;
	private ViewGroup mRoot;

	public TraficCard(final Context context, final LoaderManager loaderManager, final Ligne ligne) {
		super(context, loaderManager, R.string.card_trafic_title, R.layout.card_trafic);
		mLigne = ligne;
	}

	@Override
	protected Intent getMoreIntent() {
		return null;
	}

	@Override
	protected void bindView(final Context context, final View view) {
		mRoot = (ViewGroup) view;
		initLoader(null, this).forceLoad();
	}

	@Override
	public Loader<List<InfoTrafic>> onCreateLoader(final int arg0, final Bundle arg1) {
		return new LoaderTask(getContext(), mLigne);
	}

	@Override
	public void onLoadFinished(final Loader<List<InfoTrafic>> loader, final List<InfoTrafic> infoTrafics) {

		if (infoTrafics.isEmpty()) {
			showMessage(R.string.msg_nothing_info_trafic, R.drawable.ic_checkmark_holo_light);
		} else {
			final LayoutInflater inflater = LayoutInflater.from(getContext());

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
				final ParamIntent intent = new ParamIntent(getContext(), InfoTraficDetailActivity.class);
				intent.putExtra(InfoTraficDetailActivity.PARAM_INFO_TRAFIC, (Parcelable) infoTrafic);
				getContext().startActivity(intent);
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
				infoTrafics = manager.getByLigneCode(getContext(), mLigne.code);
			} catch (final IOException e) {
				e.printStackTrace();
			} catch (final JSONException e) {
				e.printStackTrace();
			}

			return infoTrafics;
		}
	}
}
