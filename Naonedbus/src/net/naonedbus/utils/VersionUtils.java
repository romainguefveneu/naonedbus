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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.naonedbus.NBApplication;
import net.naonedbus.R;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;

import com.bugsense.trace.BugSenseHandler;

/**
 * @author romain
 * 
 */
public class VersionUtils {

	private String assetName;
	private Context context;

	public VersionUtils(Context context, String assetName) {
		this.context = context;
		this.assetName = assetName;
	}

	/**
	 * Récupérer le contenu de l'asset avec les tag remplacés.
	 */
	public String getFormattedContent() {
		String content = null;
		try {
			content = convertStreamToString(context.getAssets().open(this.assetName));

			content = content.replace("{version}", VersionUtils.getVersion(context));
			content = content.replace("{codename}", VersionUtils.getVersionName(context));

		} catch (IOException e) {
			BugSenseHandler.sendExceptionMessage(
					String.format("Erreur de chargement de l'asset '%s'.", this.assetName), null, e);
		}
		return content;
	}

	/**
	 * Récupérer les notes de la version actuelle
	 */
	public String getCurrentVersionNotes() {
		String content = null;
		String notes = "";
		try {
			content = convertStreamToString(context.getAssets().open(this.assetName));

			Pattern p = Pattern.compile("<!--notes-->(.*)<!--/notes-->", Pattern.DOTALL);
			Matcher m = p.matcher(content);

			if (m.find()) {
				Pattern pLi = Pattern.compile("<li>(.*?)</li>", Pattern.DOTALL);
				Matcher mLi = pLi.matcher(m.group(1));

				while (mLi.find()) {
					notes += "\u2022 " + mLi.group(1) + "\n";
				}
			}

		} catch (IOException e) {
			BugSenseHandler.sendExceptionMessage(
					String.format("Erreur de chargement de l'asset '%s'.", this.assetName), null, e);
		}
		return notes;
	}

	/**
	 * Récupérer la version de l'appli
	 * 
	 * @return version de l'application
	 */
	public static String getVersion(Context context) {
		try {
			ComponentName comp = new ComponentName(context, NBApplication.class);
			PackageInfo pinfo = context.getPackageManager().getPackageInfo(comp.getPackageName(), 0);
			return pinfo.versionName;
		} catch (android.content.pm.PackageManager.NameNotFoundException e) {
			return null;
		}
	}

	/**
	 * Récupérer le nom de la version
	 * 
	 * @param context
	 * @return String
	 */
	public static String getVersionName(Context context) {
		return context.getResources().getString(R.string.version_name);
	}

	/**
	 * To convert the InputStream to String we use the Reader.read(char[]
	 * buffer) method. We iterate until the Reader return -1 which means there's
	 * no more data to read. We use the StringWriter class to produce the
	 * string.
	 */
	private String convertStreamToString(InputStream is) throws IOException {
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

}
