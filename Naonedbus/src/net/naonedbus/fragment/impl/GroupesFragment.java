package net.naonedbus.fragment.impl;

import net.naonedbus.R;
import net.naonedbus.bean.Groupe;
import net.naonedbus.fragment.CustomCursorFragment;
import net.naonedbus.manager.impl.GroupeManager;
import net.naonedbus.provider.impl.GroupeProvider;
import net.naonedbus.provider.table.GroupeTable;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
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

public class GroupesFragment extends CustomCursorFragment implements ActionMode.Callback, OnItemClickListener,
		OnItemLongClickListener {

	private final GroupeManager mGroupeManager;

	private ActionMode mActionMode;
	private ListView mListView;

	public GroupesFragment() {
		super(R.string.title_fragment_lignes, R.layout.fragment_listview);
		mGroupeManager = GroupeManager.getInstance();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		setEmptyMessageValues(R.string.error_title_empty_groupe, R.string.error_summary_empty_groupe, R.drawable.groupe);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mListView = getListView();
		mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_groupes, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.menu_add) {
			menuAdd();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected CursorAdapter getCursorAdapter(final Context context) {
		final Cursor c = mGroupeManager.getCursor(context.getContentResolver());

		final String[] from = new String[] { GroupeTable.NOM };
		final int[] to = new int[] { android.R.id.text1 };

		final SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(),
				android.R.layout.simple_list_item_multiple_choice, c, from, to, 0);

		return adapter;
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
			public void onClick(DialogInterface dialog, int which) {
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

	private void menuAdd() {
		final View alertDialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_input, null);
		final EditText input = (EditText) alertDialogView.findViewById(R.id.text);
		input.selectAll();

		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(alertDialogView);
		builder.setTitle(R.string.action_groupes_add);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				final Groupe groupe = new Groupe();
				groupe.setNom(input.getText().toString().trim());
				mGroupeManager.add(getActivity().getContentResolver(), groupe);
			}
		});
		builder.setNegativeButton(android.R.string.cancel, null);

		final AlertDialog alert = builder.create();
		alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		alert.show();
	}

}
