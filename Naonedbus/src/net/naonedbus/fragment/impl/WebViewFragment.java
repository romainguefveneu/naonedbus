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

public class WebViewFragment extends CustomFragment {

	public static final String PARAM_RAW_ID = "rawId";

	private static final String LOG_TAG = "WebViewFragment";
	private static final String ENCODING = "UTF-8";

	public WebViewFragment() {
		super(R.string.title_fragment_versions, R.layout.fragment_webview);
	}

	@Override
	protected void bindView(final View view, final Bundle savedInstanceState) {
		try {
			final WebView wv = (WebView) view.findViewById(R.id.webView);
			wv.setBackgroundColor(getResources().getColor(R.color.activity_background_light));

			final int rawId = getArguments().getInt(PARAM_RAW_ID);

			final String content = IOUtils.toString(getResources().openRawResource(rawId), ENCODING);
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
