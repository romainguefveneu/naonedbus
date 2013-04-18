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

import net.naonedbus.R;
import net.naonedbus.activity.impl.WebViewActivity;
import net.naonedbus.fragment.CustomFragment;
import net.naonedbus.utils.FontUtils;
import net.naonedbus.utils.VersionUtils;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.view.MenuItem;

public class AboutFragment extends CustomFragment implements OnClickListener {

	private static final SparseArray<String> URLS = new SparseArray<String>();
	static {
		URLS.append(R.id.aboutTwitterRomain, "http://twitter.com/romainguefveneu");
		URLS.append(R.id.aboutTwitterBenoit, "http://twitter.com/benoitcotinat");
		URLS.append(R.id.aboutTwitter, "http://twitter.com/naonedbus");
		URLS.append(R.id.aboutFacebook, "http://www.facebook.com/naonedbus");
		URLS.append(R.id.aboutMail, "mailto:naonedbus@gmail.com?subject=Naonedbus&body=Bonjour,%0d%0a");
	}

	private static final SparseIntArray LICENCES_ID = new SparseIntArray();
	static {
		LICENCES_ID.append(R.id.aboutLicenceLibs, R.raw.licences_lib);
		LICENCES_ID.append(R.id.aboutLicenceIcons, R.raw.licences_icons);
	}

	private final OnClickListener mLinkOnClickListener = new OnClickListener() {
		@Override
		public void onClick(final View v) {
			final String url = URLS.get(v.getId());
			if (url != null)
				openUrl(url);
		}
	};

	private final OnClickListener mLicencesOnClickListener = new OnClickListener() {
		@Override
		public void onClick(final View v) {
			final int id = LICENCES_ID.get(v.getId());

			final Intent intent = new Intent(getActivity(), WebViewActivity.class);
			intent.putExtra(WebViewActivity.PARAM_RAW_ID, id);
			startActivity(intent);

			// InfoDialogUtils.showHtmlFromRaw(getActivity(), id);
		}
	};

	private String mVersion;
	private String mSha1;

	public AboutFragment() {
		super(R.string.title_fragment_about, R.layout.fragment_about);
	}

	@Override
	protected void bindView(final View view, final Bundle savedInstanceState) {
		final Typeface robotoLight = FontUtils.getRobotoLight(getActivity());

		mVersion = getString(R.string.version_number, VersionUtils.getVersion(getActivity()));
		mSha1 = getString(R.string.version_sha1);

		((TextView) view.findViewById(R.id.codename)).setTypeface(robotoLight);

		final TextView version = (TextView) view.findViewById(R.id.version);
		version.setText(mVersion);
		version.setTag(true);
		version.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				if ((Boolean) version.getTag() == true) {
					version.setText(mSha1);
					version.setTag(false);
				} else {
					version.setText(mVersion);
					version.setTag(true);
				}
			}
		});

		view.findViewById(R.id.aboutTwitterRomain).setOnClickListener(mLinkOnClickListener);
		view.findViewById(R.id.aboutTwitterBenoit).setOnClickListener(mLinkOnClickListener);
		view.findViewById(R.id.aboutTwitter).setOnClickListener(mLinkOnClickListener);
		view.findViewById(R.id.aboutFacebook).setOnClickListener(mLinkOnClickListener);
		view.findViewById(R.id.aboutMail).setOnClickListener(mLinkOnClickListener);

		fillThanks((ViewGroup) view.findViewById(R.id.aboutSectionThanks));
		fillTranslators((ViewGroup) view.findViewById(R.id.aboutSectionTranslators));

		view.findViewById(R.id.aboutLicenceLibs).setOnClickListener(mLicencesOnClickListener);
		view.findViewById(R.id.aboutLicenceIcons).setOnClickListener(mLicencesOnClickListener);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		return false;
	}

	private void openUrl(final String url) {
		final Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		startActivity(intent);
	}

	private void fillThanks(final ViewGroup parent) {
		fillView(parent, R.array.thanks, R.array.thanks_urls);
	}

	private void fillTranslators(final ViewGroup parent) {
		fillView(parent, R.array.translators, R.array.translators_urls);
	}

	private void fillView(final ViewGroup parent, final int idLabels, final int idUrls) {
		final String[] values = getActivity().getResources().getStringArray(idLabels);
		final String[] urls = getActivity().getResources().getStringArray(idUrls);
		final LayoutInflater inflater = LayoutInflater.from(getActivity());

		for (int i = 0; i < values.length; i++) {
			final LinearLayout view = (LinearLayout) inflater.inflate(R.layout.section_item_small, null);

			final TextView label = (TextView) view.findViewById(android.R.id.text1);
			final TextView url = (TextView) view.findViewById(android.R.id.text2);

			label.setText(values[i]);

			if (TextUtils.isEmpty(urls[i])) {
				view.setClickable(false);
				url.setVisibility(View.GONE);
			} else {
				view.setOnClickListener(this);
				view.setTag(urls[i]);
				url.setText(urls[i].substring(7) + " ↗");
			}

			parent.addView(view);

			if (i < values.length - 1) {
				inflater.inflate(R.layout.divider, parent);
			}
		}
	}

	@Override
	public void onClick(final View v) {
		final String url = (String) v.getTag();
		openUrl(url);
	}
}
