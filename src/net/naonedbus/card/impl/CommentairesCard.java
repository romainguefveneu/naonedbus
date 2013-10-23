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
import net.naonedbus.activity.impl.SendNewsActivity;
import net.naonedbus.bean.LiveNews;
import net.naonedbus.bean.Direction;
import net.naonedbus.bean.Route;
import net.naonedbus.bean.Stop;
import net.naonedbus.card.Card;
import net.naonedbus.formatter.LiveNewsFomatter;
import net.naonedbus.fragment.impl.StopDetailFragment.OnDirectionChangedListener;
import net.naonedbus.fragment.impl.LiveNewsDetailDialogFragment;
import net.naonedbus.intent.ParamIntent;
import net.naonedbus.manager.impl.LiveNewsManager;
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

public class CommentairesCard extends Card<List<LiveNews>> implements OnDirectionChangedListener {

	private static final String LOG_TAG = "CommentairesCard";
	private static final boolean DBG = BuildConfig.DEBUG;

	private static final int LIMIT = 3;
	private static final String BUNDLE_FORCE_UPDATE = "forceUpdate";

	private final static IntentFilter intentFilter;
	static {
		intentFilter = new IntentFilter();
		intentFilter.addAction(SendNewsActivity.ACTION_COMMENTAIRE_SENT);
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

	private Route mRoute;
	private Direction mDirection;
	private Stop mStop;
	private ViewGroup mRoot;
	private final Typeface mRobotoMedium;

	public CommentairesCard(final Context context, final LoaderManager loaderManager,
			final FragmentManager fragmentManager) {
		super(context, loaderManager, fragmentManager, R.string.card_commentaires_title, R.layout.card_news);
		getContext().registerReceiver(mIntentReceiver, intentFilter);
		mRobotoMedium = FontUtils.getRobotoMedium(context);
	}

	public void setLigne(final Route route) {
		mRoute = route;
	}

	public void setSens(final Direction direction) {
		mDirection = direction;
	}

	public void setArret(final Stop stop) {
		mStop = stop;
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
		final ParamIntent intent = new ParamIntent(getContext(), SendNewsActivity.class);
		intent.putExtra(SendNewsActivity.PARAM_LIGNE, mRoute);
		intent.putExtra(SendNewsActivity.PARAM_SENS, mDirection);
		intent.putExtra(SendNewsActivity.PARAM_ARRET, mStop);

		intent.putExtra(Intent.EXTRA_TITLE, R.string.card_more_commenter);
		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, R.drawable.ic_card_send);
		return intent;
	}

	private View createView(final LayoutInflater inflater, final ViewGroup root, final LiveNews liveNews) {
		final View view = inflater.inflate(R.layout.card_item_commentaire, root, false);

		final TextView itemTitle = (TextView) view.findViewById(R.id.itemTitle);
		final TextView itemDate = (TextView) view.findViewById(R.id.itemTime);
		final TextView itemDescription = (TextView) view.findViewById(R.id.itemDescription);

		String title = "";

		if (NaonedbusClient.NAONEDBUS.name().equals(liveNews.getSource())) {
			if (liveNews.getStop() == null && liveNews.getDirection() == null && liveNews.getRoute() == null) {
				title = view.getContext().getString(R.string.entire_network);
			} else {
				if (liveNews.getStop() != null) {
					title = liveNews.getStop().getName() + " ";
				}
				if (liveNews.getDirection() != null) {
					title = title + "\u2192 " + liveNews.getDirection().getName();
				}
			}
		} else {
			title = getString(LiveNewsFomatter.getTitleResId(liveNews.getSource()));
		}

		itemDescription.setText(liveNews.getMessage(), BufferType.SPANNABLE);
		itemDate.setText(liveNews.getDelay());
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
				bundle.putParcelable(LiveNewsDetailDialogFragment.PARAM_LIVENEWS, liveNews);

				final DialogFragment dialogFragment = new LiveNewsDetailDialogFragment();
				dialogFragment.setArguments(bundle);
				dialogFragment.show(getFragmentManager(), "CommentaireDetailFragment");
			}
		});

		return view;
	}

	@Override
	public Loader<List<LiveNews>> onCreateLoader(final int id, final Bundle bundle) {
		final boolean forceUpdate = (bundle != null && bundle.getBoolean(BUNDLE_FORCE_UPDATE, false));
		return new LoaderTask(getContext(), mRoute, mDirection, forceUpdate);
	}

	@Override
	public void onLoadFinished(final Loader<List<LiveNews>> loader, final List<LiveNews> commentaires) {
		if (commentaires == null || commentaires.isEmpty()) {
			showMessage(R.string.no_livenews, R.drawable.ic_checkmark_holo_light);
		} else {
			final LayoutInflater inflater = LayoutInflater.from(getContext());

			mRoot.removeAllViews();
			for (final LiveNews liveNews : commentaires) {
				mRoot.addView(createView(inflater, mRoot, liveNews));
			}

			showContent();
		}

	}

	private static class LoaderTask extends AsyncTaskLoader<List<LiveNews>> {
		private final Route mRoute;
		private final Direction mDirection;
		private final boolean mForceUpdate;
		private List<LiveNews> mCommentaires;

		public LoaderTask(final Context context, final Route route, final Direction direction, final boolean forceUpdate) {
			super(context);
			mRoute = route;
			mDirection = direction;
			mForceUpdate = forceUpdate;
		}

		@Override
		public List<LiveNews> loadInBackground() {
			final LiveNewsManager manager = LiveNewsManager.getInstance();
			final LiveNewsFomatter fomatter = new LiveNewsFomatter(getContext());
			final long today = new DateMidnight().getMillis();

			List<LiveNews> commentaires = null;

			try {
				if (mForceUpdate) {
					manager.updateCache(getContext().getContentResolver());
				}
				commentaires = manager.getAll(getContext().getContentResolver(), mRoute.getCode(), mDirection.getCode(),
						null);

				for (int i = commentaires.size() - 1; i > -1; i--) {
					final LiveNews liveNews = commentaires.get(i);

					if (liveNews.getTimestamp() > today) {
						fomatter.formatValues(liveNews);
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
		public void deliverResult(final List<LiveNews> commentaires) {
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
	public void onDirectionChanged(final Direction newDirection) {
		mDirection = newDirection;
		showLoader();
		restartLoader(null, this);
	}

}
