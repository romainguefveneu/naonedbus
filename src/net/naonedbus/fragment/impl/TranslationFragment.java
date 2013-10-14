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
import net.naonedbus.fragment.CustomFragment;
import net.naonedbus.utils.FontUtils;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TranslationFragment extends CustomFragment implements OnClickListener {

	public TranslationFragment() {
		super(R.layout.fragment_translation);
	}

	@Override
	protected void bindView(final View view, final Bundle savedInstanceState) {
		final Typeface robotoLight = FontUtils.getRobotoLight(getActivity());

		fillTranslators((ViewGroup) view.findViewById(R.id.aboutSectionTranslators));
	}

	private void openUrl(final String url) {
		final Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		startActivity(intent);
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
