/**
 *  Copyright (C) 2011 Romain Guefveneu
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
import net.naonedbus.bean.Favori;
import net.naonedbus.manager.impl.FavoriManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

/**
 * @author romain
 * 
 */
public class FavorisHelper {

	public static abstract class FavorisActionListener {
		/**
		 * Les favoris ont été exportés
		 */
		public void onFavorisExport() {
		};

		/**
		 * Les favoris ont été importés
		 */
		public void onFavorisImport() {
		};

		/**
		 * Un favori a été supprimé
		 */
		public void onFavoriRemoved() {
		};

		/**
		 * Un favori a été renommé
		 */
		public void onFavoriRenamed(Favori item) {
		};
	}

	private Context context;

	private FavorisActionListener favorisActionListener;

	public FavorisHelper(Context context) {
		this.context = context;
	}

	public FavorisHelper(Context context, FavorisActionListener favorisActionListener) {
		this.context = context;
		this.favorisActionListener = favorisActionListener;
	}

	/**
	 * Renommer un favori
	 * 
	 * @param idFavori
	 */
	public void renameFavori(int favoriId) {
		final FavoriManager favoriManager = FavoriManager.getInstance();

		final Favori item = favoriManager.getSingle(context.getContentResolver(), favoriId);
		final View alertDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_input, null);
		final EditText input = (EditText) alertDialogView.findViewById(R.id.text);
		input.setText(item.nomFavori);
		input.selectAll();

		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setView(alertDialogView);
		builder.setTitle(R.string.action_rename);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				final String nom = input.getText().toString().trim();
				item.nomFavori = (nom.length() == 0) ? null : nom;

				favoriManager.setFavori(context.getContentResolver(), item);
				if (favorisActionListener != null) {
					favorisActionListener.onFavoriRenamed(item);
				}
			}
		});
		builder.setNegativeButton(android.R.string.cancel, null);

		final AlertDialog alert = builder.create();
		alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		alert.show();
	}

}
