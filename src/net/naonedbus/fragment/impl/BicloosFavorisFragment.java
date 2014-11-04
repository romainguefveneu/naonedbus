package net.naonedbus.fragment.impl;

import java.io.IOException;
import java.util.List;

import net.naonedbus.R;
import net.naonedbus.bean.Bicloo;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.manager.impl.BiclooManager;
import net.naonedbus.manager.impl.FavoriBiclooManager;
import net.naonedbus.provider.impl.FavoriBiclooProvider;
import net.naonedbus.widget.adapter.impl.BiclooArrayAdapter;

import org.json.JSONException;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class BicloosFavorisFragment extends BicloosFragment implements OnItemLongClickListener, ActionMode.Callback {

	private ListView mListView;
	private ActionMode mActionMode;

	private final ContentObserver mContentObserver = new ContentObserver(new Handler()) {
		@Override
		public void onChange(final boolean selfChange) {
			refreshContent();
		};
	};

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setEmptyMessageValues(R.string.error_title_empty_favori, R.string.error_summary_empty_favori_bicloo,
				R.drawable.ic_favorite_empty);
	};

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mListView = getListView();
		mListView.setOnItemLongClickListener(this);
		mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		final ContentResolver contentResolver = getActivity().getContentResolver();
		contentResolver.registerContentObserver(FavoriBiclooProvider.CONTENT_URI, true, mContentObserver);
	}

	@Override
	public void onDestroy() {
		final ContentResolver contentResolver = getActivity().getContentResolver();
		contentResolver.unregisterContentObserver(mContentObserver);

		super.onDestroy();
	}

	@Override
	public boolean onContextItemSelected(final android.view.MenuItem item) {
		return false; // Must keep this, otherwise this fragment will handle
						// onContextItemSelected of other activty's fragments.
	}

	@Override
	protected AsyncResult<ListAdapter> loadContent(final Context context, final Bundle bundle) {
		final AsyncResult<ListAdapter> result = new AsyncResult<ListAdapter>();
		final FavoriBiclooManager favoriBiclooManager = FavoriBiclooManager.getInstance();
		final BiclooManager biclooManager = BiclooManager.getInstance();

		if (bundle != null && bundle.getBoolean(BUNDKE_KEY_FORCE_UDPATE)) {
			biclooManager.clearCache();
		}

		final List<Bicloo> favoris = favoriBiclooManager.getAll(context.getContentResolver());
		try {
			final List<Bicloo> bicloos = biclooManager.getAll(context);
			for (final Bicloo bicloo : bicloos) {
				for (final Bicloo favori : favoris) {
					if (bicloo.getId() == favori.getId()) {
						favori.set(bicloo);
						break;
					}
				}
			}
		} catch (final IOException e) {
			result.setException(e);
		} catch (final JSONException e) {
			result.setException(e);
		}

		setDistances(favoris);

		final BiclooArrayAdapter adapter = new BiclooArrayAdapter(context, favoris);
		adapter.setIndexer(mIndexers.get(mCurrentSortPreference));
		adapter.sort(mComparators.get(mCurrentSortPreference));

		result.setResult(adapter);

		return result;
	}

	@Override
	public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
		final MenuInflater menuInflater = getSherlockActivity().getSupportMenuInflater();
		menuInflater.inflate(R.menu.fragment_favoris_bicloos_contextual, menu);
		return true;
	}

	@Override
	public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
		final int checkedItems = getCheckedItemsCount();
		mActionMode = mode;
		mActionMode.setTitle(getResources().getQuantityString(R.plurals.selected_items, checkedItems, checkedItems));
		return false;
	}

	@Override
	public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_delete:
			menuDelete();
			mode.finish();
			break;
		default:
			return false;
		}

		return true;
	}

	@SuppressLint("NewApi")
	private void menuDelete() {
		Bicloo item;
		final FavoriBiclooManager manager = FavoriBiclooManager.getInstance();
		final ContentResolver contentResolver = getActivity().getContentResolver();
		final BiclooArrayAdapter adapter = (BiclooArrayAdapter) getListAdapter();

		for (int i = mListView.getCount() - 1; i > -1; i--) {
			if (mListView.isItemChecked(i)) {
				item = adapter.getItem(i);
				adapter.remove(item);
				manager.remove(contentResolver, item.getId());
			}
		}

		adapter.notifyDataSetChanged();
		getSherlockActivity().invalidateOptionsMenu();
	}

	@Override
	public void onDestroyActionMode(final ActionMode mode) {
		mActionMode = null;
		mListView.clearChoices();

		final BiclooArrayAdapter adapter = (BiclooArrayAdapter) getListAdapter();
		adapter.clearCheckedItemPositions();
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
		if (mActionMode == null) {
			mListView.setItemChecked(position, false);
			super.onListItemClick(l, v, position, id);
		} else {
			onItemSelected();
		}
	}

	@Override
	public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, final long id) {
		mListView.setItemChecked(position, !mListView.isItemChecked(position));
		onItemSelected();
		return true;
	}

	public void onItemSelected() {
		final SparseBooleanArray checkedPositions = mListView.getCheckedItemPositions();
		final BiclooArrayAdapter adapter = (BiclooArrayAdapter) getListAdapter();
		adapter.setCheckedItemPositions(checkedPositions);

		if (mActionMode == null) {
			getSherlockActivity().startActionMode(this);
		} else if (hasItemChecked() == false) {
			mActionMode.finish();
		} else {
			mActionMode.invalidate();
		}
	}

	private boolean hasItemChecked() {
		final SparseBooleanArray checked = mListView.getCheckedItemPositions();
		for (int i = 0; i < checked.size(); i++) {
			if (checked.valueAt(i))
				return true;
		}
		return false;
	}

	private int getCheckedItemsCount() {
		final SparseBooleanArray checkedPositions = mListView.getCheckedItemPositions();
		int count = 0;
		for (int i = 0; i < checkedPositions.size(); i++) {
			if (checkedPositions.valueAt(i)) {
				count++;
			}
		}
		return count;
	}

}
