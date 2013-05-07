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
package net.naonedbus.fragment;

import java.util.ArrayList;
import java.util.List;

import net.naonedbus.BuildConfig;
import net.naonedbus.R;
import net.naonedbus.utils.FontUtils;
import net.naonedbus.widget.PinnedHeaderListView;

import org.joda.time.DateTime;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;

public abstract class CustomCursorFragment extends SherlockListFragment implements CustomFragmentActions,
		LoaderCallbacks<Cursor> {

	private static final String LOG_TAG = "CustomCursorFragment";
	private static final boolean DBG = BuildConfig.DEBUG;

	private static final int LOADER_INIT = 0;
	private static final int LOADER_REFRESH = 1;

	private static final String STATE_POSITION = "position";
	private static final String STATE_TOP = "top";

	int mMessageEmptyTitleId = R.string.error_title_empty;
	int mMessageEmptySummaryId = R.string.error_summary_empty;
	int mMessageEmptyDrawableId = R.drawable.sad_face;

	protected int mTitleId;
	protected int mLayoutId;
	protected int mLayoutListHeaderId = R.layout.list_item_header;
	protected ViewGroup mFragmentView;

	private int mListViewStatePosition;
	private int mListViewStateTop;

	private CursorAdapter mCursorAdapter;

	private final List<OnScrollListener> mOnScrollListeners = new ArrayList<AbsListView.OnScrollListener>();

	/**
	 * Gestion du refraichissement
	 */
	private DateTime nextUpdate = null;
	/**
	 * Nombre de minutes pendant lesquelles le contenu est considéré comme à
	 * jour.
	 */
	private int timeToLive = 5;

	public CustomCursorFragment(final int titleId, final int layoutId) {
		mTitleId = titleId;
		mLayoutId = layoutId;
	}

	public CustomCursorFragment(final int titleId, final int layoutId, final int layoutListHeaderId) {
		this(titleId, layoutId);
		mLayoutListHeaderId = layoutListHeaderId;
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (DBG)
			Log.d(LOG_TAG, "onActivityCreated");

		mCursorAdapter = getCursorAdapter(getActivity());
		setListAdapter(mCursorAdapter);

		getLoaderManager().initLoader(LOADER_INIT, null, this);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		if (container == null) // must put this in
			return null;

		if (savedInstanceState != null) {
			mListViewStatePosition = savedInstanceState.getInt(STATE_POSITION, -1);
			mListViewStateTop = savedInstanceState.getInt(STATE_TOP, 0);
		} else {
			mListViewStatePosition = -1;
			mListViewStateTop = 0;
		}

		mFragmentView = (ViewGroup) inflater.inflate(R.layout.fragment_base, container, false);
		final View view = inflater.inflate(this.mLayoutId, container, false);
		bindView(view, savedInstanceState);

		mFragmentView.addView(view);

		setupListView(inflater, mFragmentView);

		return mFragmentView;
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		if (isAdded()) {
			final View v = getListView().getChildAt(0);
			final int top = (v == null) ? 0 : v.getTop();
			outState.putInt(STATE_POSITION, getListView().getFirstVisiblePosition());
			outState.putInt(STATE_TOP, top);
		}
		super.onSaveInstanceState(outState);
	}

	protected void bindView(final View view, final Bundle savedInstanceState) {

	}

	private void setupListView(final LayoutInflater inflater, final View view) {
		final ListView listView = (ListView) mFragmentView.findViewById(android.R.id.list);

		listView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(final AbsListView view, final int scrollState) {
			}

			@Override
			public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount,
					final int totalItemCount) {
				triggerOnScrollListeners(listView, firstVisibleItem, visibleItemCount, totalItemCount);
			}
		});

		if (listView instanceof PinnedHeaderListView) {
			final PinnedHeaderListView pinnedListView = (PinnedHeaderListView) listView;
			pinnedListView.setPinnedHeaderView(inflater.inflate(mLayoutListHeaderId, pinnedListView, false));
			addOnScrollListener(new OnScrollListener() {

				@Override
				public void onScrollStateChanged(final AbsListView view, final int scrollState) {

				}

				@Override
				public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount,
						final int totalItemCount) {
					final Adapter adapter = getListAdapter();
					if (adapter != null && adapter instanceof OnScrollListener) {
						final OnScrollListener sectionAdapter = (OnScrollListener) adapter;
						sectionAdapter.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
					}
				}
			});
		}
	}

	@Override
	public int getTitleId() {
		return mTitleId;
	}

	public void refreshContent() {
		getLoaderManager().restartLoader(LOADER_REFRESH, null, this);
	}

	public void cancelLoading() {
		getLoaderManager().destroyLoader(LOADER_INIT);
		getLoaderManager().destroyLoader(LOADER_REFRESH);
	}

	protected void addOnScrollListener(final OnScrollListener onScrollListener) {
		mOnScrollListeners.add(onScrollListener);
	}

	private void triggerOnScrollListeners(final AbsListView view, final int firstVisibleItem,
			final int visibleItemCount, final int totalItemCount) {
		for (final OnScrollListener l : mOnScrollListeners) {
			l.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
		}
	}

	/**
	 * Définir les textes et images affichés si la liste est vide.
	 * 
	 * @param titleId
	 *            L'identifiant du titre.
	 * @param summaryId
	 *            L'identifiant de la description.
	 * @param drawableId
	 *            L'identifiant du drawable.
	 */
	protected void setEmptyMessageValues(final int titleId, final int summaryId, final int drawableId) {
		mMessageEmptyTitleId = titleId;
		mMessageEmptySummaryId = summaryId;
		mMessageEmptyDrawableId = drawableId;
	}

	/**
	 * Afficher l'indicateur de chargement.
	 */
	protected void showLoader() {
		mFragmentView.findViewById(android.R.id.list).setVisibility(View.GONE);
		if (mFragmentView.findViewById(R.id.fragmentMessage) != null) {
			mFragmentView.findViewById(R.id.fragmentMessage).setVisibility(View.GONE);
		}
		mFragmentView.findViewById(R.id.fragmentLoading).setVisibility(View.VISIBLE);
	}

	/**
	 * Afficher le contenu.
	 */
	protected void showContent() {
		mFragmentView.findViewById(R.id.fragmentLoading).setVisibility(View.GONE);
		if (mFragmentView.findViewById(R.id.fragmentMessage) != null) {
			mFragmentView.findViewById(R.id.fragmentMessage).setVisibility(View.GONE);
		}
		final View content = mFragmentView.findViewById(android.R.id.list);
		if (content.getVisibility() != View.VISIBLE) {
			content.setVisibility(View.VISIBLE);
			content.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));
		}
	}

	/**
	 * Afficher le message avec un symbole d'erreur.
	 * 
	 * @param titleRes
	 *            L'identifiant du titre.
	 * @param descriptionRes
	 *            L'identifiant de la description.
	 */
	protected void showError(final int titleRes, final int descriptionRes) {
		showMessage(getString(titleRes), getString(descriptionRes), R.drawable.warning);
	}

	/**
	 * Afficher le message avec un symbole d'erreur.
	 * 
	 * @param title
	 *            Le titre.
	 * @param description
	 *            La description.
	 */
	protected void showError(final String title, final String description) {
		showMessage(title, description, R.drawable.warning);
	}

	/**
	 * Afficher le message.
	 */
	protected void showMessage() {
		showMessage(mMessageEmptyTitleId, mMessageEmptySummaryId, mMessageEmptyDrawableId);
	}

	/**
	 * Afficher le message.
	 * 
	 * @param titleRes
	 *            L'identifiant du titre.
	 * @param descriptionRes
	 *            L'identifiant de la description.
	 * @param drawableRes
	 *            L'identifiant du drawable.
	 */
	protected void showMessage(final int titleRes, final int descriptionRes, final int drawableRes) {
		showMessage(getString(titleRes), (descriptionRes != 0) ? getString(descriptionRes) : null, drawableRes);
	}

	/**
	 * Afficher un message avec une desciption et un symbole.
	 * 
	 * @param title
	 *            Le titre.
	 * @param description
	 *            La description.
	 * @param drawableRes
	 *            L'identifiant du symbole.
	 */
	protected void showMessage(final String title, final String description, final int drawableRes) {
		mFragmentView.findViewById(android.R.id.list).setVisibility(View.GONE);
		mFragmentView.findViewById(R.id.fragmentLoading).setVisibility(View.GONE);

		View message = mFragmentView.findViewById(R.id.fragmentMessage);
		if (message == null) {
			final ViewStub messageStrub = (ViewStub) mFragmentView.findViewById(R.id.fragmentMessageStub);
			message = messageStrub.inflate();
			final Typeface robotoLight = FontUtils.getRobotoLight(getActivity());
			final Typeface robotoCondensed = FontUtils.getRobotoBoldCondensed(getActivity());
			((TextView) message.findViewById(android.R.id.summary)).setTypeface(robotoLight);
			((TextView) message.findViewById(android.R.id.title)).setTypeface(robotoCondensed);
		}

		message.setVisibility(View.VISIBLE);

		final TextView titleView = (TextView) message.findViewById(android.R.id.title);
		titleView.setText(title);
		titleView.setCompoundDrawablesWithIntrinsicBounds(0, drawableRes, 0, 0);

		final TextView descriptionView = (TextView) message.findViewById(android.R.id.summary);
		if (description != null) {
			descriptionView.setText(description);
			descriptionView.setVisibility(View.VISIBLE);
		} else {
			descriptionView.setVisibility(View.GONE);
		}
	}

	/**
	 * Définir l'action du bouton lors de l'affichage du message.
	 * 
	 * @param title
	 *            Le titre du boutton.
	 * @param onClickListener
	 *            Son action.
	 */
	protected void setMessageButton(final int title, final OnClickListener onClickListener) {
		setMessageButton(getString(title), onClickListener);
	}

	/**
	 * Définir l'action du bouton lors de l'affichage du message.
	 * 
	 * @param title
	 *            Le titre du boutton.
	 * @param onClickListener
	 *            Son action.
	 */
	protected void setMessageButton(final String title, final OnClickListener onClickListener) {
		final View message = mFragmentView.findViewById(R.id.fragmentMessage);
		if (message != null) {
			final Button button = (Button) message.findViewById(android.R.id.button1);
			button.setText(title);
			button.setOnClickListener(onClickListener);
			button.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Définir le nombre de minutes pendant lesquelles les données sont
	 * considérées comme à jour
	 * 
	 * @param timeToLive
	 */
	protected void setTimeToLive(final int timeToLive) {
		this.timeToLive = timeToLive;
	}

	/**
	 * Redéfinir la date d'expiration du cache à maintenant
	 */
	protected void resetNextUpdate() {
		nextUpdate = new DateTime().plusMinutes(timeToLive);
	}

	/**
	 * Indique si les données sont toujours considérées comme à jour ou non
	 * 
	 * @return true si elle ne sont plus à jour | false si elle sont à jour
	 */
	protected boolean isNotUpToDate() {
		if (nextUpdate != null) {
			return (nextUpdate.isBeforeNow());
		} else {
			return true;
		}
	}

	/**
	 * Créer un CursorAdapter.
	 * 
	 * @param context
	 * @return le CursorAdapter.
	 */
	protected abstract CursorAdapter getCursorAdapter(final Context context);

	/**
	 * Après le chargement.
	 */
	protected void onPostExecute() {
	}

	@Override
	public abstract Loader<Cursor> onCreateLoader(int arg0, Bundle arg1);

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor result) {
		if (DBG)
			Log.d(LOG_TAG, "onLoadFinished");

		if (result == null) {
			showMessage(mMessageEmptyTitleId, mMessageEmptySummaryId, mMessageEmptyDrawableId);
			return;
		}

		if (result == null || result.getCount() == 0) {
			showMessage(mMessageEmptyTitleId, mMessageEmptySummaryId, mMessageEmptyDrawableId);
		} else {
			if (mListViewStatePosition != -1 && isAdded()) {
				getListView().setSelectionFromTop(mListViewStatePosition, mListViewStateTop);
				mListViewStatePosition = -1;
			}
			showContent();
			resetNextUpdate();
		}

		mCursorAdapter.swapCursor(result);

		onPostExecute();
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> arg0) {
		if (DBG)
			Log.d(LOG_TAG, "onLoaderReset");
		mCursorAdapter.swapCursor(null);
	}

}
