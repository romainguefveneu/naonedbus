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
package net.naonedbus.fragment.impl;

import java.io.IOException;

import net.naonedbus.R;
import net.naonedbus.fragment.CustomFragment;

import org.apache.commons.io.IOUtils;

import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;

import com.actionbarsherlock.view.MenuItem;

public class VersionsFragment extends CustomFragment {

	private static final String LOG_TAG = VersionsFragment.class.getSimpleName();
	private static final String ENCODING = "UTF-8";

	public VersionsFragment() {
		super(R.string.title_fragment_versions, R.layout.fragment_webview);
	}

	@Override
	protected void bindView(final View view, final Bundle savedInstanceState) {
		try {
			final WebView wv = (WebView) view.findViewById(R.id.webView);
			wv.setBackgroundColor(getResources().getColor(R.color.activity_background_light));
			final String content = IOUtils.toString(getResources().openRawResource(R.raw.version), ENCODING);
			// Problème d'encodage avec le loadData.
			wv.loadDataWithBaseURL("fake://not/needed", content, "text/html", ENCODING, "");
		} catch (final NotFoundException e) {
			Log.e(LOG_TAG, "Erreur de chargement des notes de versions", e);
		} catch (final IOException e) {
			Log.e(LOG_TAG, "Erreur de chargement des notes de versions", e);
		}
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		return true;
	}

}
