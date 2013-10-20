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
import net.naonedbus.activity.impl.CommentaireActivity;
import net.naonedbus.bean.Comment;
import net.naonedbus.bean.Direction;
import net.naonedbus.bean.Route;
import net.naonedbus.bean.Stop;
import net.naonedbus.card.Card;
import net.naonedbus.formatter.CommentaireFomatter;
import net.naonedbus.fragment.impl.ArretDetailFragment.OnSensChangeListener;
import net.naonedbus.fragment.impl.CommentaireDetailDialogFragment;
import net.naonedbus.intent.ParamIntent;
import net.naonedbus.manager.impl.CommentManager;
import net.naonedbus.security.NaonedbusClient;
import net.naonedbus.utils.FontUtils;

import org.joda.time.DateMidnight;
import org.json.JSONException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
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
import android.widget.TextView.BufferType;

import com.bugsense.trace.BugSenseHandler;

public class CommentairesCard extends Card<List<Comment>> implements OnSensChangeListener {

	private static final String LOG_TAG = "CommentairesCard";
	private static final boolean DBG = BuildConfig.DEBUG;

	private static final int LIMIT = 3;
	private static final String BUNDLE_FORCE_UPDATE = "forceUpdate";

	private final static IntentFilter intentFilter;
	static {
		intentFilter = new IntentFilter();
		intentFilter.addAction(CommentaireActivity.ACTION_COMMENTAIRE_SENT);
	}

	/**
	 * Reçoit les intents de notre intentFilter
	 */
	private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			showLoader();
			final Bundle bundle = new Bundle();
			bundle.putBoolean(BUNDLE_FORCE_UPDATE, true);
			restartLoader(bundle, CommentairesCard.this).forceLoad();
		}
	};

	private Route mLigne;
	private Direction mSens;
	private Stop mArret;
	private ViewGroup mRoot;
	private final Typeface mRobotoMedium;

	public CommentairesCard(final Context context, final LoaderManager loaderManager,
			final FragmentManager fragmentManager) {
		super(context, loaderManager, fragmentManager, R.string.card_commentaires_title, R.layout.card_trafic);
		getContext().registerReceiver(mIntentReceiver, intentFilter);
		mRobotoMedium = FontUtils.getRobotoMedium(context);
	}

	public void setLigne(final Route ligne) {
		mLigne = ligne;
	}

	public void setSens(final Direction sens) {
		mSens = sens;
	}

	public void setArret(final Stop arret) {
		mArret = arret;
	}

	@Override
	public void setContext(final Context context) {
		if (!context.equals(getContext())) {
			getContext().unregisterReceiver(mIntentReceiver);
			super.setContext(context);
			getContext().registerReceiver(mIntentReceiver, intentFilter);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		initLoader(null, this).forceLoad();
	}

	@Override
	public void onDestroy() {
		getContext().unregisterReceiver(mIntentReceiver);
		super.onDestroy();
	}

	@Override
	protected void bindView(final Context context, final View base, final View view) {
		mRoot = (ViewGroup) view;
	}

	@Override
	protected Intent getMoreIntent() {
		final ParamIntent intent = new ParamIntent(getContext(), CommentaireActivity.class);
		intent.putExtra(CommentaireActivity.PARAM_LIGNE, mLigne);
		intent.putExtra(CommentaireActivity.PARAM_SENS, mSens);
		intent.putExtra(CommentaireActivity.PARAM_ARRET, mArret);

		intent.putExtra(Intent.EXTRA_TITLE, R.string.card_more_commenter);
		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, R.drawable.ic_card_send);
		return intent;
	}

	private View createView(final LayoutInflater inflater, final ViewGroup root, final Comment commentaire) {
		final View view = inflater.inflate(R.layout.card_item_commentaire, root, false);

		final TextView itemTitle = (TextView) view.findViewById(R.id.itemTitle);
		final TextView itemDate = (TextView) view.findViewById(R.id.itemTime);
		final TextView itemDescription = (TextView) view.findViewById(R.id.itemDescription);

		String title = "";

		if (NaonedbusClient.NAONEDBUS.name().equals(commentaire.getSource())) {
			if (commentaire.getStop() == null && commentaire.getDirection() == null && commentaire.getRoute() == null) {
				title = view.getContext().getString(R.string.commentaire_tout);
			} else {
				if (commentaire.getStop() != null) {
					title = commentaire.getStop().getNomArret() + " ";
				}
				if (commentaire.getDirection() != null) {
					title = title + "\u2192 " + commentaire.getDirection().getName();
				}
			}
		} else {
			title = getString(CommentaireFomatter.getTitleResId(commentaire.getSource()));
		}

		itemDescription.setText(commentaire.getMessage(), BufferType.SPANNABLE);
		itemDate.setText(commentaire.getDelay());
		itemDate.setTypeface(mRobotoMedium);

		if (title.trim().length() == 0) {
			itemTitle.setVisibility(View.GONE);
		} else {
			itemTitle.setVisibility(View.VISIBLE);
			itemTitle.setText(title.trim());
		}

		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				final Bundle bundle = new Bundle();
				bundle.putParcelable(CommentaireDetailDialogFragment.PARAM_COMMENTAIRE, commentaire);

				final DialogFragment dialogFragment = new CommentaireDetailDialogFragment();
				dialogFragment.setArguments(bundle);
				dialogFragment.show(getFragmentManager(), "CommentaireDetailFragment");
			}
		});

		return view;
	}

	@Override
	public Loader<List<Comment>> onCreateLoader(final int id, final Bundle bundle) {
		final boolean forceUpdate = (bundle != null && bundle.getBoolean(BUNDLE_FORCE_UPDATE, false));
		return new LoaderTask(getContext(), mLigne, mSens, forceUpdate);
	}

	@Override
	public void onLoadFinished(final Loader<List<Comment>> loader, final List<Comment> commentaires) {
		if (commentaires == null || commentaires.isEmpty()) {
			showMessage(R.string.msg_nothing_commentaires, R.drawable.ic_checkmark_holo_light);
		} else {
			final LayoutInflater inflater = LayoutInflater.from(getContext());

			mRoot.removeAllViews();
			for (final Comment commentaire : commentaires) {
				mRoot.addView(createView(inflater, mRoot, commentaire));
			}

			showContent();
		}

	}

	private static class LoaderTask extends AsyncTaskLoader<List<Comment>> {
		private final Route mLigne;
		private final Direction mSens;
		private final boolean mForceUpdate;
		private List<Comment> mCommentaires;

		public LoaderTask(final Context context, final Route ligne, final Direction sens, final boolean forceUpdate) {
			super(context);
			mLigne = ligne;
			mSens = sens;
			mForceUpdate = forceUpdate;
		}

		@Override
		public List<Comment> loadInBackground() {
			final CommentManager manager = CommentManager.getInstance();
			final CommentaireFomatter fomatter = new CommentaireFomatter(getContext());
			final long today = new DateMidnight().getMillis();

			List<Comment> commentaires = null;

			try {
				if (mForceUpdate) {
					manager.updateCache(getContext().getContentResolver());
				}
				commentaires = manager.getAll(getContext().getContentResolver(), mLigne.getCode(), mSens.getCode(),
						null);

				for (int i = commentaires.size() - 1; i > -1; i--) {
					final Comment commentaire = commentaires.get(i);

					if (commentaire.getTimestamp() > today) {
						fomatter.formatValues(commentaire);
					} else {
						commentaires.remove(i);
					}
				}

				commentaires = commentaires.subList(0, Math.min(LIMIT, commentaires.size()));
			} catch (final IOException e) {
				if (DBG)
					Log.e(LOG_TAG, "Erreur de chargement", e);
			} catch (final JSONException e) {
				if (DBG)
					Log.e(LOG_TAG, "Erreur de chargement", e);
				BugSenseHandler.sendExceptionMessage("Erreur de chargement", "", e);
			}
			return commentaires;
		}

		/**
		 * Called when there is new data to deliver to the client. The super
		 * class will take care of delivering it; the implementation here just
		 * adds a little more logic.
		 */
		@Override
		public void deliverResult(final List<Comment> commentaires) {
			mCommentaires = commentaires;

			if (isStarted()) {
				// If the Loader is currently started, we can immediately
				// deliver its results.
				super.deliverResult(mCommentaires);
			}
		}

		/**
		 * Handles a request to start the Loader.
		 */
		@Override
		protected void onStartLoading() {
			if (mCommentaires != null) {
				// If we currently have a result available, deliver it
				// immediately.
				deliverResult(mCommentaires);
			}

			if (takeContentChanged() || mCommentaires == null) {
				// If the data has changed since the last time it was loaded
				// or is not currently available, start a load.
				forceLoad();
			}
		}

	}

	@Override
	public void onSensChange(final Direction newSens) {
		mSens = newSens;
		showLoader();
		restartLoader(null, this);
	}

}