package net.naonedbus.fragment.impl;

import net.naonedbus.R;
import net.naonedbus.fragment.CustomFragment;
import net.naonedbus.utils.InfoDialogUtils;
import net.naonedbus.utils.VersionUtils;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class AboutFragment extends CustomFragment {

	private static final SparseArray<String> URLS = new SparseArray<String>();
	static {
		URLS.append(R.id.aboutTwitterRomain, "http://twitter.com/romainguefveneu");
		URLS.append(R.id.aboutTwitterBenoit, "http://twitter.com/benoitcotinat");
		URLS.append(R.id.aboutTwitter, "http://twitter.com/naonedbus");
		URLS.append(R.id.aboutFacebook, "http://www.facebook.com/naonedbus");
		URLS.append(R.id.aboutGooglePlus, "http://plus.google.com/106014045430708857945");
	}

	private static final SparseIntArray LICENCES_ID = new SparseIntArray();
	static {
		LICENCES_ID.append(R.id.aboutLicenceLibs, R.raw.licences_lib);
		LICENCES_ID.append(R.id.aboutLicenceIcons, R.raw.licences_icons);
	}

	private final OnClickListener mLinkOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			final String url = URLS.get(v.getId());
			if (url != null)
				openUrl(url);
		}
	};

	private final OnClickListener mLicencesOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			int id = LICENCES_ID.get(v.getId());
			if (id != 0)
				InfoDialogUtils.showHtmlFromRaw(getActivity(), id);
		}
	};

	public AboutFragment() {
		super(R.string.title_fragment_about, R.layout.fragment_about);
	}

	@Override
	protected void bindView(View view, Bundle savedInstanceState) {
		final Typeface robotoLight = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");

		((TextView) view.findViewById(R.id.codename)).setTypeface(robotoLight);

		final TextView version = (TextView) view.findViewById(R.id.version);
		version.setText(getString(R.string.version_number, VersionUtils.getVersion(getActivity())));

		view.findViewById(R.id.aboutTwitterRomain).setOnClickListener(mLinkOnClickListener);
		view.findViewById(R.id.aboutTwitterBenoit).setOnClickListener(mLinkOnClickListener);
		view.findViewById(R.id.aboutTwitter).setOnClickListener(mLinkOnClickListener);
		view.findViewById(R.id.aboutFacebook).setOnClickListener(mLinkOnClickListener);
		view.findViewById(R.id.aboutGooglePlus).setOnClickListener(mLinkOnClickListener);

		view.findViewById(R.id.aboutLicenceLibs).setOnClickListener(mLicencesOnClickListener);
		view.findViewById(R.id.aboutLicenceIcons).setOnClickListener(mLicencesOnClickListener);
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

	private void openUrl(String url) {
		final Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		startActivity(intent);
	}

}
