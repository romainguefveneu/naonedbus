package net.naonedbus.card.impl;

import java.io.IOException;
import java.util.List;

import net.naonedbus.R;
import net.naonedbus.bean.InfoTrafic;
import net.naonedbus.bean.Ligne;
import net.naonedbus.card.Card;
import net.naonedbus.manager.impl.InfoTraficManager;

import org.json.JSONException;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class TraficCard extends Card {

	public static interface OnInfoTraficClickListener {
		void onInfoTraficClickListener(InfoTrafic infoTrafic);
	}

	private final Ligne mLigne;
	private final OnInfoTraficClickListener mOnInfoTraficClickListener;

	public TraficCard(final Ligne ligne, final OnInfoTraficClickListener onInfoTraficClickListener) {
		super(R.string.card_trafic_title, R.layout.card_trafic);
		mLigne = ligne;
		mOnInfoTraficClickListener = onInfoTraficClickListener;
	}

	@Override
	protected void bindView(final Context context, final View view) {
		new Loader(context, (ViewGroup) view).execute();
	}

	private class Loader extends AsyncTask<Void, Void, List<InfoTrafic>> {

		private final Context mContext;
		private final ViewGroup mRoot;

		public Loader(final Context context, final ViewGroup root) {
			mContext = context;
			mRoot = root;
		}

		@Override
		protected List<InfoTrafic> doInBackground(final Void... params) {
			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
			final InfoTraficManager manager = InfoTraficManager.getInstance();

			List<InfoTrafic> infoTrafics = null;
			try {
				infoTrafics = manager.getByLigneCode(mContext, mLigne.code);
			} catch (final IOException e) {
				e.printStackTrace();
			} catch (final JSONException e) {
				e.printStackTrace();
			}

			return infoTrafics;
		}

		@Override
		protected void onPostExecute(final List<InfoTrafic> infoTrafics) {
			final LayoutInflater inflater = LayoutInflater.from(mContext);

			for (final InfoTrafic infoTrafic : infoTrafics) {
				mRoot.addView(createView(inflater, mRoot, infoTrafic));
			}

			showContent();

		}
	}

	private View createView(final LayoutInflater inflater, final ViewGroup root, final InfoTrafic infoTrafic) {
		final View view = inflater.inflate(R.layout.card_item_content_trafic_ligne, root, false);

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
				mOnInfoTraficClickListener.onInfoTraficClickListener(infoTrafic);
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
}
