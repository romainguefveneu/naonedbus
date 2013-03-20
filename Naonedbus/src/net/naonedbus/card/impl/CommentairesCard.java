package net.naonedbus.card.impl;

import java.io.IOException;
import java.util.List;

import net.naonedbus.R;
import net.naonedbus.activity.impl.CommentaireActivity;
import net.naonedbus.bean.Arret;
import net.naonedbus.bean.Commentaire;
import net.naonedbus.bean.Ligne;
import net.naonedbus.bean.Sens;
import net.naonedbus.card.Card;
import net.naonedbus.formatter.CommentaireFomatter;
import net.naonedbus.intent.ParamIntent;
import net.naonedbus.manager.impl.CommentaireManager;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.TextView.BufferType;

public class CommentairesCard extends Card<List<Commentaire>> {

	private static final int LIMIT = 3;

	private Ligne mLigne;
	private Sens mSens;
	private Arret mArret;
	private ViewGroup mRoot;

	public CommentairesCard(final Context context, final LoaderManager loaderManager) {
		super(context, loaderManager, R.string.card_commentaires_title, R.layout.card_trafic);
	}

	public void setLigne(final Ligne ligne) {
		mLigne = ligne;
	}

	public void setSens(final Sens sens) {
		mSens = sens;
	}

	public void setArret(final Arret arret) {
		mArret = arret;
	}

	@Override
	protected void bindView(final Context context, final View view) {
		mRoot = (ViewGroup) view;
		initLoader(null, this).forceLoad();
	}

	@Override
	protected Intent getMoreIntent() {
		final ParamIntent intent = new ParamIntent(getContext(), CommentaireActivity.class);
		intent.putExtra(CommentaireActivity.Param.idLigne, mLigne._id);
		intent.putExtra(CommentaireActivity.Param.idSens, mSens._id);
		intent.putExtra(CommentaireActivity.Param.idArret, mArret._id);

		intent.putExtra(Intent.EXTRA_TITLE, R.string.card_more_commenter);
		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, R.drawable.ic_card_send);
		return intent;
	}

	private View createView(final LayoutInflater inflater, final ViewGroup root, final Commentaire commentaire) {
		final View view = inflater.inflate(R.layout.card_item_commentaire, root, false);

		final TextView itemTitle = (TextView) view.findViewById(R.id.itemTitle);
		final TextView itemDate = (TextView) view.findViewById(R.id.itemTime);
		final TextView itemDescription = (TextView) view.findViewById(R.id.itemDescription);

		String title = "";

		if (commentaire.getArret() == null && commentaire.getSens() == null && commentaire.getLigne() == null) {
			title = view.getContext().getString(R.string.commentaire_tout);
		} else {
			if (commentaire.getArret() != null) {
				title = commentaire.getArret().nomArret + " ";
			}
			if (commentaire.getSens() != null) {
				title = title + "\u2192 " + commentaire.getSens().text;
			}
		}

		itemDescription.setText(commentaire.getMessage(), BufferType.SPANNABLE);
		itemDate.setText(commentaire.getDelay());

		if (title.trim().length() == 0) {
			itemTitle.setVisibility(View.GONE);
		} else {
			itemTitle.setVisibility(View.VISIBLE);
			itemTitle.setText(title.trim());
		}

		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
			}
		});

		return view;
	}

	@Override
	public Loader<List<Commentaire>> onCreateLoader(final int arg0, final Bundle arg1) {
		return new LoaderTask(getContext(), mLigne);
	}

	@Override
	public void onLoadFinished(final Loader<List<Commentaire>> loader, final List<Commentaire> commentaires) {
		if (commentaires == null || commentaires.isEmpty()) {
			showMessage(R.string.msg_nothing_commentaires, R.drawable.ic_checkmark_holo_light);
		} else {
			final LayoutInflater inflater = LayoutInflater.from(getContext());

			for (final Commentaire commentaire : commentaires) {
				mRoot.addView(createView(inflater, mRoot, commentaire));
			}

			showContent();
		}

	}

	private static class LoaderTask extends AsyncTaskLoader<List<Commentaire>> {
		private final Ligne mLigne;

		public LoaderTask(final Context context, final Ligne ligne) {
			super(context);
			mLigne = ligne;
		}

		@Override
		public List<Commentaire> loadInBackground() {
			final CommentaireManager manager = CommentaireManager.getInstance();
			final CommentaireFomatter fomatter = new CommentaireFomatter(getContext());
			final long today = new DateMidnight().getMillis();

			List<Commentaire> commentaires = null;

			try {
				commentaires = manager.getAll(getContext(), mLigne.code, null, null, new DateTime(0));

				for (int i = commentaires.size() - 1; i > -1; i--) {
					final Commentaire commentaire = commentaires.get(i);

					if (commentaire.getTimestamp() > today) {
						fomatter.formatValues(commentaire);
					} else {
						commentaires.remove(i);
					}
				}

				commentaires = commentaires.subList(0, Math.min(LIMIT, commentaires.size()));
			} catch (final IOException e) {
				e.printStackTrace();
			} catch (final JSONException e) {
				e.printStackTrace();
			}
			return commentaires;
		}

	}

}
