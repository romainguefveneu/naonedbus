package net.naonedbus.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import net.naonedbus.R;

import org.apache.commons.io.IOUtils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.Color;
import android.text.Html;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bugsense.trace.BugSenseHandler;

public abstract class InfoDialogUtils {

	private static final String MESSAGE_FOLDER = "msg";

	/**
	 * Afficher une dialog avec le titre "Information"
	 * 
	 * @param context
	 * @param messageId
	 */
	public static void show(Context context, int messageId) {
		show(context, R.string.dialog_title_information, messageId);
	}

	/**
	 * Afficher une dialog avec titre et message personnalisés
	 * 
	 * @param context
	 * @param titleId
	 * @param messageId
	 */
	public static void show(Context context, int titleId, int messageId) {
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
	public static AlertDialog getDialog(Context context, int titleId, int messageId) {
		final TextView textView = new TextView(context);
		final ScrollView scrollView = new ScrollView(context);

		textView.setAutoLinkMask(Linkify.ALL);
		textView.setTextSize(TypedValue.TYPE_DIMENSION, 2.5f);
		textView.setPadding(25, 25, 25, 25);
		textView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		textView.setScrollbarFadingEnabled(true);
		textView.setText(Html.fromHtml(context.getString(messageId)));
		textView.setTextColor(context.getResources().getColor(R.color.item_primary_text_dark));
		textView.setGravity(Gravity.CENTER_VERTICAL);
		scrollView.addView(textView);

		final AlertDialog.Builder moreDetailsDialog = new AlertDialog.Builder(context);
		moreDetailsDialog.setIcon(android.R.drawable.ic_dialog_info);
		moreDetailsDialog.setTitle(context.getString(titleId));
		moreDetailsDialog.setView(scrollView);
		moreDetailsDialog.setPositiveButton(android.R.string.ok, null);

		return moreDetailsDialog.create();
	}

	/**
	 * Afficher une dialog avec un contenu au format HTML
	 * 
	 * @param context
	 * @param html
	 */
	public static void showHtml(Context context, String html) {
		AlertDialog.Builder moreDetailsDialog = null;
		WebView webView = new WebView(context);
		ScrollView scrollView = new ScrollView(context);

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
	public static void showIfNecessary(Context context, int titreId, int messageId) {
		final File dataFile = new File(context.getFilesDir(), MESSAGE_FOLDER + File.separator + messageId);

		createDir(context);
		if (!dataFile.exists()) {
			show(context, titreId, messageId);
			try {
				dataFile.createNewFile();
			} catch (IOException e) {
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
	public static void showIfNecessary(Context context, int messageId) {
		final File dataFile = new File(context.getFilesDir(), MESSAGE_FOLDER + File.separator + messageId);

		createDir(context);
		if (!dataFile.exists()) {
			show(context, messageId);
			try {
				dataFile.createNewFile();
			} catch (IOException e) {
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
	public static void showHtmlFromRaw(Context context, int fileId) {
		String content;
		try {
			content = IOUtils.toString(context.getResources().openRawResource(fileId));
			showHtml(context, content);
		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Afficher la dialog d'accueil
	 * 
	 * @param context
	 */
	public static void showWelcomeDialog(final Context context) {
		AlertDialog.Builder moreDetailsDialog = null;

		final LayoutInflater factory = LayoutInflater.from(context);
		final View alertDialogView = factory.inflate(R.layout.dialog_changelog, null);
		final ScrollView scrollView = new ScrollView(context);
		final TextView title = (TextView) alertDialogView.findViewById(R.id.title);
		final TextView codename = (TextView) alertDialogView.findViewById(R.id.codename);
		final TextView versionNotes = (TextView) alertDialogView.findViewById(R.id.versionNotes);
		final VersionUtils versionUtils = new VersionUtils(context, "version.html");

		title.setText("Version " + VersionUtils.getVersion(context));
		codename.setText(VersionUtils.getVersionName(context));
		versionNotes.setText(versionUtils.getCurrentVersionNotes());

		scrollView.addView(alertDialogView);

		moreDetailsDialog = new AlertDialog.Builder(context);
		moreDetailsDialog.setIcon(android.R.drawable.ic_dialog_info);
		moreDetailsDialog.setTitle("Nouvelle version");
		moreDetailsDialog.setView(scrollView);
		moreDetailsDialog.setPositiveButton(android.R.string.ok, null);
		// moreDetailsDialog.setNeutralButton(R.string.action_show_howto, null);
		moreDetailsDialog.show();
	}

	/**
	 * Afficher la dialog d'accueil, uniquement si pas déjà affichées.
	 * 
	 * @param context
	 */
	public static void showWelcomeDialogIfNecessary(Context context) {
		final String version = VersionUtils.getVersion(context);
		final File dataFile = new File(context.getFilesDir(), MESSAGE_FOLDER + File.separator + version);

		createDir(context);
		if (!dataFile.exists()) {
			try {
				showWelcomeDialog(context);
				dataFile.createNewFile();
			} catch (IOException e) {
				BugSenseHandler.sendExceptionMessage("Erreur lors de la création du marqueur", null, e);
			}
		}
	}

	/**
	 * Afficher les notes de version, uniquement si pas déjà affichées.
	 * 
	 * @param context
	 * @param fileFromAssets
	 */
	public static void showVersionNote(Context context, String fileFromAssets) {
		VersionUtils versionUtils = new VersionUtils(context, fileFromAssets);
		showHtml(context, versionUtils.getFormattedContent());
	}

	/**
	 * Afficher les notes de version, uniquement si pas déjà affichées.
	 * 
	 * @param context
	 * @param fileFromAssets
	 */
	public static void showVersionNoteIfNecessary(Context context, String fileFromAssets) {
		final VersionUtils versionUtils = new VersionUtils(context, fileFromAssets);
		final String version = VersionUtils.getVersion(context);
		final File dataFile = new File(context.getFilesDir(), MESSAGE_FOLDER + File.separator + version);

		createDir(context);
		if (!dataFile.exists()) {
			try {
				showHtml(context, versionUtils.getFormattedContent());
				dataFile.createNewFile();
			} catch (IOException e) {
				BugSenseHandler.sendExceptionMessage("Erreur lors de la création du marqueur", null, e);
			}
		}
	}

	/**
	 * Créer le répertoire servant à stocker les id des messages déjà affichés
	 * 
	 * @param context
	 */
	private static void createDir(Context context) {
		final File file = new File(context.getFilesDir(), MESSAGE_FOLDER);
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	/**
	 * To convert the InputStream to String we use the Reader.read(char[]
	 * buffer) method. We iterate until the Reader return -1 which means there's
	 * no more data to read. We use the StringWriter class to produce the
	 * string.
	 */
	private static String convertStreamToString(InputStream is) throws IOException {
		if (is != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {
			return "";
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
	public static boolean isNotAlreadyShown(final Context context, int messageId) {
		final File dataFile = new File(context.getFilesDir(), MESSAGE_FOLDER + File.separator + messageId);
		boolean result = true;

		createDir(context);
		if (!dataFile.exists()) {
			try {
				dataFile.createNewFile();
			} catch (IOException e) {
				BugSenseHandler.sendExceptionMessage("Erreur lors de la création du marqueur", null, e);
			}
		} else {
			result = false;
		}

		return result;

	}

}
