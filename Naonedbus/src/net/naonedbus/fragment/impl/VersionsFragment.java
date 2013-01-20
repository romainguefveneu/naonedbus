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

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class VersionsFragment extends CustomFragment {

	private static final String LOG_TAG = VersionsFragment.class.getSimpleName();
	private static final String ENCODING = "UTF-8";

	public VersionsFragment() {
		super(R.string.title_fragment_versions, R.layout.fragment_webview);
	}

	@Override
	protected void bindView(View view, Bundle savedInstanceState) {
		try {
			final WebView wv = (WebView) view.findViewById(R.id.webView);
			wv.setBackgroundColor(getResources().getColor(R.color.activity_background_light));
			final String content = IOUtils.toString(getResources().openRawResource(R.raw.version), ENCODING);
			// Problème d'encodage avec le loadData.
			wv.loadDataWithBaseURL("fake://not/needed", content, "text/html", ENCODING, "");
		} catch (NotFoundException e) {
			Log.e(LOG_TAG, "Erreur de chargement des notes de versions", e);
		} catch (IOException e) {
			Log.e(LOG_TAG, "Erreur de chargement des notes de versions", e);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu) {

	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return true;
	}

}
