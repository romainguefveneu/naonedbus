package net.naonedbus.card.impl;

import java.util.List;

import net.naonedbus.R;
import net.naonedbus.bean.Commentaire;
import net.naonedbus.bean.Ligne;
import net.naonedbus.card.Card;
import net.naonedbus.manager.impl.CommentaireManager;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.LoaderManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class CommentaireCard extends Card {

	public static interface OnItemClickListener {
		void onItemClickListener(Commentaire commentaire);
	}

	private final Ligne mLigne;

	public CommentaireCard(final Context context, final LoaderManager loaderManager, final Ligne ligne) {
		super(context, loaderManager, R.string.card_trafic_title, R.layout.card_trafic);
		mLigne = ligne;
	}

	@Override
	protected void bindView(final Context context, final View view) {
		new Loader(context, (ViewGroup) view).execute();
	}

	private class Loader extends AsyncTask<Void, Void, List<Commentaire>> {

		private final Context mContext;
		private final ViewGroup mRoot;

		public Loader(final Context context, final ViewGroup root) {
			mContext = context;
			mRoot = root;
		}

		@Override
		protected List<Commentaire> doInBackground(final Void... params) {
			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
			final CommentaireManager manager = CommentaireManager.getInstance();

			List<Commentaire> commentaires = null;
			commentaires = manager.getFromCache(mContext, mLigne.code, null, null);

			return commentaires;
		}

		@Override
		protected void onPostExecute(final List<Commentaire> commentaires) {
			final LayoutInflater inflater = LayoutInflater.from(mContext);

			for (final Commentaire commentaire : commentaires) {
				mRoot.addView(createView(inflater, mRoot, commentaire));
			}

			showContent();

		}
	}

	private View createView(final LayoutInflater inflater, final ViewGroup root, final Commentaire commentaire) {
		final View view = inflater.inflate(R.layout.card_item_content_trafic_ligne, root, false);

		final TextView ligneCode = (TextView) view.findViewById(R.id.itemSymbole);
		final ImageView ligneCodeBackground = (ImageView) view.findViewById(R.id.itemIcon);
		final TextView itemTitle = (TextView) view.findViewById(R.id.itemTitle);
		final TextView itemDate = (TextView) view.findViewById(R.id.itemTime);
		final TextView itemDescription = (TextView) view.findViewById(R.id.itemDescription);

		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
			}
		});

		return view;
	}

}
