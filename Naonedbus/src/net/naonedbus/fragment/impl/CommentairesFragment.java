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
package net.naonedbus.fragment.impl;

import java.util.List;

import net.naonedbus.BuildConfig;
import net.naonedbus.R;
import net.naonedbus.activity.impl.CommentaireActivity;
import net.naonedbus.bean.Commentaire;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.formatter.CommentaireFomatter;
import net.naonedbus.fragment.CustomListFragment;
import net.naonedbus.manager.impl.CommentaireManager;
import net.naonedbus.widget.adapter.impl.CommentaireArrayAdapter;
import net.naonedbus.widget.indexer.impl.CommentaireIndexer;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class CommentairesFragment extends CustomListFragment {

	private static final String LOG_TAG = "CommentairesFragment";
	private static final boolean DBG = BuildConfig.DEBUG;

	private static final String BUNDLE_FORCE_UPDATE = "forceUpdate";

	private MenuItem mRefreshMenuItem;
	private Context mContext;

	private final static IntentFilter intentFilter;
	static {
		intentFilter = new IntentFilter();
		intentFilter.addAction(CommentaireActivity.ACTION_COMMENTAIRE_SENT);
	}

	/**
	 * Reçoit les intents de notre intentFilter
	 */
	private final BroadcastReceiver intentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (DBG)
				Log.d(LOG_TAG, "onReceive : " + intent);
			final Bundle bundle = new Bundle();
			bundle.putBoolean(BUNDLE_FORCE_UPDATE, true);
			refreshContent(bundle);
		}
	};

	public CommentairesFragment() {
		super(R.layout.fragment_listview_box);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (DBG)
			Log.d(LOG_TAG, "onCreate");
		setHasOptionsMenu(true);
		getActivity().registerReceiver(intentReceiver, intentFilter);
		mContext = getActivity();
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (DBG)
			Log.d(LOG_TAG, "onActivityCreated");
	}

	@Override
	public void onStart() {
		super.onStart();
		if (!mContext.equals(getActivity())) {
			mContext.unregisterReceiver(intentReceiver);
			mContext = getActivity();
			mContext.registerReceiver(intentReceiver, intentFilter);
		}
		loadContent();
	}

	@Override
	public void onDestroy() {
		getActivity().unregisterReceiver(intentReceiver);
		super.onPause();
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		if (DBG)
			Log.d(LOG_TAG, "onCreateOptionsMenu");

		inflater.inflate(R.menu.activity_en_direct, menu);
		mRefreshMenuItem = menu.findItem(R.id.menu_refresh);

		if (getLoaderManager().hasRunningLoaders())
			showResfrehMenuLoader();

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_refresh:
			final Bundle bundle = new Bundle();
			bundle.putBoolean(BUNDLE_FORCE_UPDATE, true);
			refreshContent(bundle);
			break;
		case R.id.menu_comment:
			startActivity(new Intent(getActivity(), CommentaireActivity.class));
			return true;
		}
		return true;
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
		final Commentaire commentaire = (Commentaire) l.getItemAtPosition(position);

		final Bundle bundle = new Bundle();
		bundle.putParcelable(CommentaireDetailDialogFragment.PARAM_COMMENTAIRE, commentaire);

		final DialogFragment dialogFragment = new CommentaireDetailDialogFragment();
		dialogFragment.setArguments(bundle);
		dialogFragment.show(getChildFragmentManager(), "CommentaireDetailFragment");
	}

	private void showResfrehMenuLoader() {
		if (mRefreshMenuItem != null) {
			mRefreshMenuItem.setActionView(R.layout.action_item_refresh);
		}
	}

	private void hideResfrehMenuLoader() {
		if (mRefreshMenuItem != null) {
			mRefreshMenuItem.setActionView(null);
		}
	}

	@Override
	protected void onPreExecute() {
		if (DBG)
			Log.d(LOG_TAG, "onPreExecute");
		if (getListAdapter() == null) {
			showLoader();
		}
		showResfrehMenuLoader();
	}

	@Override
	protected void onPostExecute() {
		if (DBG)
			Log.d(LOG_TAG, "onPostExecute");
		hideResfrehMenuLoader();

		final CommentaireManager manager = CommentaireManager.getInstance();
		if (manager.isUpToDate() == false) {
			final Bundle bundle = new Bundle();
			bundle.putBoolean(BUNDLE_FORCE_UPDATE, true);
			refreshContent(bundle);
		}
	}

	@Override
	protected AsyncResult<ListAdapter> loadContent(final Context context, final Bundle bundle) {
		final AsyncResult<ListAdapter> result = new AsyncResult<ListAdapter>();
		final boolean forceUpdate = bundle != null && bundle.getBoolean(BUNDLE_FORCE_UPDATE, false);

		if (DBG)
			Log.d(LOG_TAG, "loadContent (mForceUpdate : " + forceUpdate + ")");

		try {
			final CommentaireManager manager = CommentaireManager.getInstance();
			final List<Commentaire> commentaires;

			if (forceUpdate) {
				manager.updateCache(context.getContentResolver());
			}
			commentaires = manager.getAll(context.getContentResolver(), null, null, null);

			final CommentaireFomatter fomatter = new CommentaireFomatter(context);
			final CommentaireArrayAdapter adapter = new CommentaireArrayAdapter(context);
			if (commentaires != null) {
				fomatter.appendToAdapter(adapter, commentaires);
			}

			adapter.setIndexer(new CommentaireIndexer());
			result.setResult(adapter);

		} catch (final Exception e) {
			result.setException(e);
		}

		return result;
	}
}
