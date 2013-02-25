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
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class GroupesFragment extends CustomCursorFragment {

	public GroupesFragment() {
		super(R.string.title_fragment_lignes, R.layout.fragment_listview);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_groupes, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.menu_add) {
			final GroupeManager groupeManager = GroupeManager.getInstance();

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
					groupeManager.add(getActivity().getContentResolver(), groupe);
				}
			});
			builder.setNegativeButton(android.R.string.cancel, null);

			final AlertDialog alert = builder.create();
			alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
			alert.show();

			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected CursorAdapter getCursorAdapter(final Context context) {

		final GroupeManager manager = GroupeManager.getInstance();
		final Cursor c = manager.getCursor(context.getContentResolver());

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

}
