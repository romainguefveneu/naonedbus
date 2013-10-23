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
package net.naonedbus.utils;

import java.io.File;
import java.io.IOException;

import net.naonedbus.R;

import org.apache.commons.io.IOUtils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.Color;
import android.view.View;
import android.webkit.WebView;
import android.widget.ScrollView;

import com.bugsense.trace.BugSenseHandler;

public abstract class InfoDialogUtils {

	private static final String MESSAGE_FOLDER = "msg";

	/**
	 * Afficher une dialog avec le titre "Information"
	 * 
	 * @param context
	 * @param messageId
	 */
	public static void show(final Context context, final int messageId) {
		show(context, R.string.information, messageId);
	}

	/**
	 * Afficher une dialog avec titre et message personnalisés
	 * 
	 * @param context
	 * @param titleId
	 * @param messageId
	 */
	public static void show(final Context context, final int titleId, final int messageId) {
		final AlertDialog moreDetailsDialog = getDialog(context, titleId, messageId);
		moreDetailsDialog.show();
	}

	/**
	 * Créer une dialog avec le titre et le contenu donné.
	 * 
	 * @param context
	 * @param titleId
	 * @param messageId
	 * @return La dialogue.
	 */
	public static AlertDialog getDialog(final Context context, final int titleId, final int messageId) {

		final AlertDialog.Builder moreDetailsDialog = new AlertDialog.Builder(context);
		moreDetailsDialog.setTitle(context.getString(titleId));
		moreDetailsDialog.setMessage(context.getString(messageId));
		moreDetailsDialog.setPositiveButton(android.R.string.ok, null);

		return moreDetailsDialog.create();
	}

	/**
	 * Afficher une dialog avec un contenu au format HTML
	 * 
	 * @param context
	 * @param html
	 */
	public static void showHtml(final Context context, final String html) {
		AlertDialog.Builder moreDetailsDialog = null;
		final WebView webView = new WebView(context);
		final ScrollView scrollView = new ScrollView(context);

		webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		webView.setBackgroundColor(Color.WHITE);
		webView.loadDataWithBaseURL("fake://not/needed", html, "text/html", "UTF-8", null); // Encoding
																							// fix
																							// for
																							// Android
																							// 3.x
																							// /
																							// 4.x
		scrollView.addView(webView);

		moreDetailsDialog = new AlertDialog.Builder(context);
		moreDetailsDialog.setIcon(android.R.drawable.ic_dialog_info);
		moreDetailsDialog.setTitle("Informations");
		moreDetailsDialog.setView(scrollView);
		moreDetailsDialog.setPositiveButton(android.R.string.ok, null);
		moreDetailsDialog.show();
	}

	/**
	 * Afficher une dialog avec un message, uniquement si elle n'a pas déjà été
	 * affichée
	 * 
	 * @param context
	 * @param messageId
	 */
	public static void showIfNecessary(final Context context, final int titreId, final int messageId) {
		final File dataFile = new File(context.getFilesDir(), MESSAGE_FOLDER + File.separator + messageId);

		createDir(context);
		if (!dataFile.exists()) {
			show(context, titreId, messageId);
			try {
				dataFile.createNewFile();
			} catch (final IOException e) {
				BugSenseHandler.sendExceptionMessage("Erreur lors de la création du marqueur", null, e);
			}
		}
	}

	/**
	 * Afficher une dialog avec un message, uniquement si elle n'a pas déjà été
	 * affichée
	 * 
	 * @param context
	 * @param messageId
	 */
	public static void showIfNecessary(final Context context, final int messageId) {
		final File dataFile = new File(context.getFilesDir(), MESSAGE_FOLDER + File.separator + messageId);

		createDir(context);
		if (!dataFile.exists()) {
			show(context, messageId);
			try {
				dataFile.createNewFile();
			} catch (final IOException e) {
				BugSenseHandler.sendExceptionMessage("Erreur lors de la création du marqueur", null, e);
			}
		}
	}

	/**
	 * Afficher un fichier des assets, uniquement s'il n'a pas déjà été affiché
	 * 
	 * @param context
	 * @param fileFromAssets
	 */
	public static void showHtmlFromRaw(final Context context, final int fileId) {
		String content;
		try {
			content = IOUtils.toString(context.getResources().openRawResource(fileId));
			showHtml(context, content);
		} catch (final NotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Créer le répertoire servant à stocker les id des messages déjà affichés
	 * 
	 * @param context
	 */
	private static void createDir(final Context context) {
		final File file = new File(context.getFilesDir(), MESSAGE_FOLDER);
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	/**
	 * Indique si un message n'a pas déjà été affiché, et le marque comme
	 * affiché.
	 * 
	 * @param context
	 * @param messageId
	 *            L'id du message en question.
	 * @return <code>true</code> si le message n'a pas encore été affiché,
	 *         <code>false</code> s'il l'a déjà été.
	 */
	public static boolean isNotAlreadyShown(final Context context, final int messageId) {
		final File dataFile = new File(context.getFilesDir(), MESSAGE_FOLDER + File.separator + messageId);
		boolean result = true;

		createDir(context);
		if (!dataFile.exists()) {
			try {
				dataFile.createNewFile();
			} catch (final IOException e) {
				BugSenseHandler.sendExceptionMessage("Erreur lors de la création du marqueur", null, e);
			}
		} else {
			result = false;
		}

		return result;

	}

}
