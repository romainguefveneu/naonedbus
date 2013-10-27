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

import net.naonedbus.R;
import net.naonedbus.bean.StopBookmark;
import net.naonedbus.manager.impl.StopBookmarkManager;
import net.naonedbus.service.FavoriService;
import net.naonedbus.utils.InfoDialogUtils;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.text.ClipboardManager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

/**
 * @author romain
 * 
 */
public class FavorisHelper {

	public static abstract class FavorisActionListener {
		/**
		 * Un favori a été renommé
		 */
		public void onFavoriRenamed(final StopBookmark item) {
		};
	}

	private final Context mContext;

	private FavorisActionListener mFavorisActionListener;

	public FavorisHelper(final Context context) {
		mContext = context;
	}

	public FavorisHelper(final Context context, final FavorisActionListener favorisActionListener) {
		mContext = context;
		mFavorisActionListener = favorisActionListener;
	}

	/**
	 * Renommer un favori
	 * 
	 * @param idFavori
	 */
	public void renameFavori(final int favoriId) {
		final StopBookmarkManager favoriManager = StopBookmarkManager.getInstance();

		final StopBookmark item = favoriManager.getSingle(mContext.getContentResolver(), favoriId);
		final View alertDialogView = LayoutInflater.from(mContext).inflate(R.layout.dialog_input, null);
		final EditText input = (EditText) alertDialogView.findViewById(R.id.text);
		input.setText(item.getBookmarkName());
		input.selectAll();

		final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setView(alertDialogView);
		builder.setTitle(R.string.rename);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				final String nom = input.getText().toString().trim();
				item.setBookmarkName((nom.length() == 0) ? null : nom);

				favoriManager.update(mContext.getContentResolver(), item);
				if (mFavorisActionListener != null) {
					mFavorisActionListener.onFavoriRenamed(item);
				}
			}
		});
		builder.setNegativeButton(android.R.string.cancel, null);

		final AlertDialog alert = builder.create();
		alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		alert.show();
	}

	/**
	 * Sauvegarder les favoris au format Json et envoyer ça dans le cloud
	 */
	public void exportFavoris() {
		final Intent intent = new Intent(mContext, FavoriService.class);
		intent.setAction(FavoriService.INTENT_ACTION_EXPORT);

		mContext.startService(intent);
	}

	/**
	 * Importer les favoris depuis les could
	 */
	public void importFavoris() {

		final LayoutInflater factory = LayoutInflater.from(mContext);
		final View alertDialogView = factory.inflate(R.layout.dialog_input, null);
		final EditText input = (EditText) alertDialogView.findViewById(R.id.text);
		input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);

		final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(R.string.you_key);
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.setPositiveButton(R.string.action_import, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				final String id = input.getText().toString().trim();
				if (id.trim().length() == 0) {
					showErrorKeyNoValid();
				} else {
					onImport(id);
				}
			}
		});
		builder.setView(alertDialogView);

		final AlertDialog alert = builder.create();
		alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		alert.show();

	}

	private void showErrorKeyNoValid() {
		InfoDialogUtils.show(mContext, R.string.import_fail, R.string.bookmark_key_fail);
	}

	private void onImport(final String key) {
		final Intent intent = new Intent(mContext, FavoriService.class);
		intent.setAction(FavoriService.INTENT_ACTION_IMPORT);
		intent.putExtra(FavoriService.INTENT_PARAM_KEY, key);

		mContext.startService(intent);
	}

	public void showExportKey(final String key) {
		final LayoutInflater factory = LayoutInflater.from(mContext);
		final View alertDialogView = factory.inflate(R.layout.dialog_readonly, null);
		final EditText input = (EditText) alertDialogView.findViewById(R.id.text);
		final TextView label = (TextView) alertDialogView.findViewById(R.id.comment);
		label.setText(R.string.keep_this_key);
		input.setText(key);
		input.setFocusable(false);

		final AlertDialog.Builder adb = new AlertDialog.Builder(mContext);
		adb.setView(alertDialogView);
		adb.setTitle(R.string.bookmark_exported);
		adb.setPositiveButton(android.R.string.ok, null);
		adb.setCancelable(false);
		adb.setNeutralButton(R.string.copy, new OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				final ClipboardManager clipboard = (ClipboardManager) mContext
						.getSystemService(Context.CLIPBOARD_SERVICE);
				clipboard.setText(key);
			}
		});
		adb.show();
	}

}
