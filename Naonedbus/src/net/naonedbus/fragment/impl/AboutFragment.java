package net.naonedbus.fragment.impl;

import net.naonedbus.R;
import net.naonedbus.fragment.CustomFragment;
import net.naonedbus.utils.VersionUtils;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class AboutFragment extends CustomFragment {

	public AboutFragment() {
		super(R.string.title_fragment_about, R.layout.fragment_about);
	}

	@Override
	protected void bindView(View view, Bundle savedInstanceState) {
		final Typeface robotoLight = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");

		((TextView) view.findViewById(R.id.codename)).setTypeface(robotoLight);

		final TextView version = (TextView) view.findViewById(R.id.version);
		version.setText(getString(R.string.version_number, VersionUtils.getVersion(getActivity())));

		final TextView autors = (TextView) view.findViewById(R.id.autors);
		autors.setText(Html.fromHtml(getString(R.string.about_autors_content)));

		final TextView info = (TextView) view.findViewById(R.id.info);
		info.setText(Html.fromHtml(getString(R.string.about_info_content)));

		final TextView licences = (TextView) view.findViewById(R.id.licences);
		licences.setText(Html.fromHtml(getString(R.string.about_licences_content)));
	}

	@Override
	public void onCreateOptionsMenu(Menu menu) {

	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return false;
	}

}
