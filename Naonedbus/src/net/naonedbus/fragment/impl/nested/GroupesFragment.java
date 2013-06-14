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
package net.naonedbus.fragment.impl.nested;

import net.naonedbus.R;
import net.naonedbus.bean.Groupe;
import net.naonedbus.fragment.CustomCursorFragment;
import net.naonedbus.manager.impl.GroupeManager;
import net.naonedbus.provider.impl.GroupeProvider;
import net.naonedbus.provider.table.GroupeTable;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.SimpleDragSortCursorAdapter;

public class GroupesFragment extends CustomCursorFragment implements ActionMode.Callback, OnItemClickListener,
		OnItemLongClickListener {

	private final GroupeManager mGroupeManager;

	private ActionMode mActionMode;
	private DragSortListView mListView;
	private Cursor mCursor;
	private CursorAdapter mAdapter;

	public GroupesFragment() {
		super(R.layout.fragment_listview_drag_drop);
		mGroupeManager = GroupeManager.getInstance();
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		setEmptyMessageValues(R.string.error_title_empty_groupe, R.string.error_summary_empty_groupe, R.drawable.groupe);
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mListView = (DragSortListView) getListView();
		mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		saveOrder();
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_groupes, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		if (item.getItemId() == R.id.menu_add) {
			menuAdd();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected CursorAdapter getCursorAdapter(final Context context) {
		mCursor = mGroupeManager.getCursor(context.getContentResolver());

		final String[] from = new String[] { GroupeTable.NOM };
		final int[] to = new int[] { android.R.id.text1 };

		mAdapter = new SimpleDragSortCursorAdapter(getActivity(), R.layout.list_item_checkable, mCursor, from, to, 0);

		return mAdapter;
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int arg0, final Bundle arg1) {
		final Uri uri = GroupeProvider.CONTENT_URI;
		final CursorLoader cursorLoader = new CursorLoader(getActivity(), uri, null, null, null, null);
		return cursorLoader;
	}

	@Override
	public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
		final MenuInflater menuInflater = getSherlockActivity().getSupportMenuInflater();
		menuInflater.inflate(R.menu.fragment_groupes_contextual, menu);

		mActionMode = mode;
		return true;
	}

	@Override
	public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
		final int checkedItems = getCheckedItemsCount();

		final MenuItem menuEdit = menu.findItem(R.id.menu_edit);
		menuEdit.setVisible(checkedItems == 1);

		mActionMode.setTitle(getResources().getQuantityString(R.plurals.selected_items, checkedItems, checkedItems));
		return true;
	}

	@Override
	public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_delete:
			deleteCheckedItems();
			mActionMode.finish();
			return true;
		case R.id.menu_edit:
			editCheckedItem();
			mActionMode.finish();
			return true;
		default:
			break;
		}
		return false;
	}

	@Override
	public void onDestroyActionMode(final ActionMode mode) {
		mActionMode = null;
		mListView.clearChoices();
		mListView.invalidateViews();
	}

	private void menuAdd() {
		final View alertDialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_input, null);
		final EditText input = (EditText) alertDialogView.findViewById(R.id.text);
		input.selectAll();

		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(alertDialogView);
		builder.setTitle(R.string.action_groupes_add);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				final Groupe groupe = new Groupe();
				groupe.setNom(input.getText().toString().trim());
				groupe.setOrdre(mCursor.getCount());
				mGroupeManager.add(getActivity().getContentResolver(), groupe);
			}
		});
		builder.setNegativeButton(android.R.string.cancel, null);

		final AlertDialog alert = builder.create();
		alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		alert.show();
	}

	private void deleteCheckedItems() {
		final SparseBooleanArray checked = mListView.getCheckedItemPositions();
		for (int i = 0; i < checked.size(); i++) {
			if (checked.valueAt(i)) {
				final int position = checked.keyAt(i);
				final int idGroupe = (int) mListView.getItemIdAtPosition(position);
				mGroupeManager.delete(getActivity().getContentResolver(), idGroupe);
			}
		}
	}

	private void editCheckedItem() {
		final int checkedItem = getFirstSelectedItemPosition();
		final CursorWrapper wrapper = (CursorWrapper) mListView.getItemAtPosition(checkedItem);
		final Groupe groupe = mGroupeManager.getSingleFromCursor(wrapper);

		final View alertDialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_input, null);
		final EditText input = (EditText) alertDialogView.findViewById(R.id.text);
		input.setText(groupe.getNom());
		input.selectAll();

		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(alertDialogView);
		builder.setTitle(R.string.action_rename);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				final String nom = input.getText().toString().trim();
				groupe.setNom((nom.length() == 0) ? null : nom);

				mGroupeManager.update(getActivity().getContentResolver(), groupe);
			}
		});
		builder.setNegativeButton(android.R.string.cancel, null);

		final AlertDialog alert = builder.create();
		alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		alert.show();
	}

	@Override
	public void onItemClick(final AdapterView<?> adapter, final View view, final int position, final long id) {
		onItemChecked();
	}

	@Override
	public boolean onItemLongClick(final AdapterView<?> adapter, final View view, final int position, final long id) {
		mListView.setItemChecked(position, true);
		onItemChecked();
		return true;
	}

	private void onItemChecked() {
		if (hasItemChecked()) {
			if (mActionMode == null) {
				getSherlockActivity().startActionMode(GroupesFragment.this);
			} else {
				mActionMode.invalidate();
			}
		} else {
			if (mActionMode != null) {
				mActionMode.finish();
			}
		}
	}

	private int getFirstSelectedItemPosition() {
		final SparseBooleanArray checkedPositions = mListView.getCheckedItemPositions();
		for (int i = 0; i < checkedPositions.size(); i++) {
			if (checkedPositions.valueAt(i)) {
				return checkedPositions.keyAt(i);
			}
		}
		return -1;
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

	private boolean hasItemChecked() {
		final SparseBooleanArray checked = mListView.getCheckedItemPositions();
		for (int i = 0; i < checked.size(); i++) {
			if (checked.valueAt(i))
				return true;
		}
		return false;
	}

	private void saveOrder() {
		final ContentResolver contentResolver = getActivity().getContentResolver();
		Groupe groupe;
		for (int i = 0; i < mListView.getCount(); i++) {
			groupe = mGroupeManager.getSingleFromCursor((CursorWrapper) mListView.getItemAtPosition(i));
			if (i != groupe.getOrdre()) {
				groupe.setOrdre(i);
				mGroupeManager.update(contentResolver, groupe);
			}
		}
	}

}
