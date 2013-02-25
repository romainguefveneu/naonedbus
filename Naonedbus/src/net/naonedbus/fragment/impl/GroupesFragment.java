package net.naonedbus.fragment.impl;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import net.naonedbus.R;
import net.naonedbus.fragment.CustomCursorFragment;

public class GroupesFragment extends CustomCursorFragment {

	public GroupesFragment() {
		super(R.string.title_fragment_lignes, R.layout.fragment_listview);
	}

	@Override
	protected CursorAdapter getCursorAdapter(final Context context) {
		// TODO : Créer l'adapter pour les groupes
		return null;
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int arg0, final Bundle arg1) {
		return null;
	}

}
