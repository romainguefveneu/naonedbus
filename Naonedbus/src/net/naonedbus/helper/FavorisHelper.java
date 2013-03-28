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

import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.bean.Favori;
import net.naonedbus.manager.impl.FavoriManager;
import net.naonedbus.rest.controller.impl.FavoriController;
import net.naonedbus.utils.InfoDialogUtils;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.text.ClipboardManager;
import android.text.InputType;
import android.util.Log;
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
		public void onFavoriRenamed(final Favori item) {
		};
	}

	private ImportTask mImportTask;
	private ExportTask mExportTask;
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
		final FavoriManager favoriManager = FavoriManager.getInstance();

		final Favori item = favoriManager.getSingle(mContext.getContentResolver(), favoriId);
		final View alertDialogView = LayoutInflater.from(mContext).inflate(R.layout.dialog_input, null);
		final EditText input = (EditText) alertDialogView.findViewById(R.id.text);
		input.setText(item.nomFavori);
		input.selectAll();

		final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setView(alertDialogView);
		builder.setTitle(R.string.action_rename);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				final String nom = input.getText().toString().trim();
				item.nomFavori = (nom.length() == 0) ? null : nom;

				favoriManager.setFavori(mContext.getContentResolver(), item);
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
		if (mExportTask == null || mExportTask.getStatus() == AsyncTask.Status.FINISHED) {
			mExportTask = (ExportTask) new ExportTask().execute();
		}
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
		builder.setTitle(R.string.dialog_title_favoris_import);
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
		InfoDialogUtils.show(mContext, R.string.msg_error_export_import, R.string.msg_error_content_favoris_key);
	}

	private void onImport(final String id) {
		if (mImportTask == null || mImportTask.getStatus() == AsyncTask.Status.FINISHED) {
			mImportTask = (ImportTask) new ImportTask().execute(id);
		}
	}

	/**
	 * Classe d'import des favoris
	 * 
	 * @author romain
	 */
	private class ImportTask extends AsyncTask<String, Void, Void> {
		protected ProgressDialog progressDialog;
		private Exception exception = null;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = ProgressDialog.show(mContext, null, mContext.getString(R.string.msg_importing_favoris),
					true);
			progressDialog.show();
		}

		@Override
		protected Void doInBackground(final String... params) {
			final FavoriManager favoriManager = FavoriManager.getInstance();

			try {
				favoriManager.importFavoris(mContext.getContentResolver(), params[0]);
			} catch (final Exception e) {
				exception = e;
			}

			return null;
		}

		@Override
		protected void onPostExecute(final Void result) {
			super.onPostExecute(result);
			progressDialog.dismiss();

			if (exception == null) {
				if (mFavorisActionListener != null) {
					mFavorisActionListener.onFavorisImport();
				}
			} else {
				showErrorKeyNoValid();
				Log.w(NBApplication.LOG_TAG, "Erreur lors de l'import des favoris", exception);
			}
		}

	}

	/**
	 * Classe d'export des favoris
	 * 
	 * @author romain
	 * 
	 */
	private class ExportTask extends AsyncTask<Void, Void, String> {
		protected ProgressDialog progressDialog;
		private Exception exception = null;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = ProgressDialog.show(mContext, "", "Transfert en cours...", true);
			progressDialog.show();
		}

		@Override
		protected String doInBackground(final Void... params) {
			final FavoriManager favoriManager = FavoriManager.getInstance();
			final FavoriController favoriController = new FavoriController();
			String cle = null;
			try {
				final String content = favoriManager.toJson(mContext.getContentResolver());
				cle = favoriController.post(content);
			} catch (final Exception e) {
				exception = e;
			}
			return cle;
		}

		@Override
		protected void onPostExecute(final String result) {
			AlertDialog.Builder adb;

			super.onPostExecute(result);
			progressDialog.dismiss();

			if (exception == null) {
				final LayoutInflater factory = LayoutInflater.from(mContext);
				final View alertDialogView = factory.inflate(R.layout.dialog_readonly, null);
				final EditText input = (EditText) alertDialogView.findViewById(R.id.text);
				final TextView label = (TextView) alertDialogView.findViewById(R.id.comment);
				label.setText(R.string.dialog_content_export);
				input.setText(result);
				input.setFocusable(false);

				adb = new AlertDialog.Builder(mContext);
				adb.setView(alertDialogView);
				adb.setTitle(R.string.dialog_title_export);
				adb.setPositiveButton(android.R.string.ok, new OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog, final int which) {
						mFavorisActionListener.onFavorisImport();
					}
				});
				adb.setNeutralButton(R.string.copy, new OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog, final int which) {
						final ClipboardManager clipboard = (ClipboardManager) mContext
								.getSystemService(Context.CLIPBOARD_SERVICE);
						clipboard.setText(result);

						if (mFavorisActionListener != null) {
							mFavorisActionListener.onFavorisImport();
						}
					}
				});
				adb.show();

			} else {
				Log.w("Erreur lors de l'export des favoris", exception);
				InfoDialogUtils.show(mContext, R.string.msg_error_title, R.string.msg_error_export_favoris);
			}
		}
	}

}
