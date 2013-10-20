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
package net.naonedbus.helper;

import java.util.List;

import net.naonedbus.R;
import net.naonedbus.bean.BookmarkGroup;
import net.naonedbus.manager.impl.BookmarkGroupManager;
import net.naonedbus.provider.table.StopBookmarkGroupLinkTable;
import net.naonedbus.provider.table.StopBookmarkGroupTable;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.database.Cursor;
import android.view.WindowManager;

public class GroupesHelper {

	private final Context mContext;
	private final BookmarkGroupManager mGroupeManager;

	public GroupesHelper(final Context context) {
		mContext = context;
		mGroupeManager = BookmarkGroupManager.getInstance();
	}

	public void linkFavori(final List<Integer> idFavoris, final Runnable callback) {
		final Cursor c = mGroupeManager.getCursor(mContext.getContentResolver(), idFavoris);
		final boolean[] checked = new boolean[c.getCount()];
		final String[] items = new String[c.getCount()];

		if (c.getCount() > 0) {
			c.moveToFirst();
			while (!c.isAfterLast()) {
				checked[c.getPosition()] = c.getInt(c.getColumnIndex(StopBookmarkGroupLinkTable.LINKED)) > 0;
				items[c.getPosition()] = c.getString(c.getColumnIndex(StopBookmarkGroupTable.GROUP_NAME));
				c.moveToNext();
			}
		}

		final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(R.string.dialog_title_groupes);
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				BookmarkGroup groupe;
				for (int i = 0; i < checked.length; i++) {
					c.moveToPosition(i);
					groupe = mGroupeManager.getSingleFromCursor(c);

					// TODO : Bulk insert / delete
					if (checked[i]) {
						for (final Integer idFavori : idFavoris) {
							mGroupeManager.addFavoriToGroup(mContext.getContentResolver(), groupe.getId(), idFavori);
						}
					} else {
						for (final Integer idFavori : idFavoris) {
							mGroupeManager.removeFavoriFromGroup(mContext.getContentResolver(), groupe.getId(),
									idFavori);
						}
					}
				}

				if (callback != null)
					callback.run();
			}
		});

		builder.setMultiChoiceItems(items, checked, new OnMultiChoiceClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which, final boolean isChecked) {
				checked[which] = isChecked;
				((AlertDialog) dialog).getListView().setItemChecked(which, isChecked);
			}
		});

		final AlertDialog alert = builder.create();
		alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		alert.show();
	}
}
