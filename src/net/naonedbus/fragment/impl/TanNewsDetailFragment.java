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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.naonedbus.R;
import net.naonedbus.bean.Route;
import net.naonedbus.bean.TanNews;
import net.naonedbus.comparator.RouteLetterComparator;
import net.naonedbus.fragment.CustomFragment;
import net.naonedbus.manager.impl.RouteManager;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.utils.FontUtils;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.gridlayout.GridLayout;

public class TanNewsDetailFragment extends CustomFragment {

	public static final String PARAM_TAN_NEWS = "tanNews";

	private TextView mItemTitle;
	private TextView mItemDescription;
	private TextView mItemTime;
	private GridLayout mRoutesView;
	protected View mFragmentView;

	public TanNewsDetailFragment() {
		super(R.layout.fragment_infotrafic_detail);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		final TanNews infoTrafic = getArguments().getParcelable(PARAM_TAN_NEWS);
		loadInfotrafic(infoTrafic);
	}

	@Override
	protected void bindView(final View view, final Bundle savedInstanceState) {
		final Typeface robotoBoldCondensed = FontUtils.getRobotoBoldCondensed(getActivity());

		mFragmentView = view;

		mItemTitle = (TextView) view.findViewById(R.id.itemTitle);
		mItemTitle.setTypeface(robotoBoldCondensed);

		mItemDescription = (TextView) view.findViewById(R.id.itemDescription);

		mItemTime = (TextView) view.findViewById(R.id.itemTime);

		mRoutesView = (GridLayout) view.findViewById(R.id.lignes);
	}

	private void loadInfotrafic(final TanNews infoTrafic) {
		final RouteManager ligneManager = RouteManager.getInstance();
		final LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

		mItemTitle.setText(infoTrafic.getTitle());
		mItemDescription.setText(Html.fromHtml(infoTrafic.getContent()));
		mItemTime.setText(infoTrafic.getDateFormated());

		final List<String> lignesConcernees = new ArrayList<String>(infoTrafic.getRoutes());
		final List<Route> listLignes = new ArrayList<Route>();

		for (final String routeCode : lignesConcernees) {
			final Route route = ligneManager.getSingle(getActivity().getContentResolver(), routeCode);
			if (route != null) {
				listLignes.add(route);
			}
		}
		Collections.sort(listLignes, new RouteLetterComparator());

		final ViewTreeObserver obs = mRoutesView.getViewTreeObserver();
		obs.addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				if (mRoutesView.getMeasuredWidth() != 0) {
					mRoutesView.getViewTreeObserver().removeOnPreDrawListener(this);

					final int itemWidth = getResources().getDimensionPixelSize(R.dimen.codeitem_width);
					final int smallPadding = getResources().getDimensionPixelSize(R.dimen.padding_small);
					final int dividerPadding = getResources().getDimensionPixelSize(R.dimen.codeitem_margin);
					final int innerWidth = mRoutesView.getMeasuredWidth() - smallPadding * 2;
					final int columnCount = innerWidth / (itemWidth + dividerPadding * 2);
					mRoutesView.setColumnCount(columnCount);

					final int newItemWidth = innerWidth / columnCount - dividerPadding / 2;

					final Typeface robotoCondensed = FontUtils.getRobotoBoldCondensed(getActivity());

					for (final Route l : listLignes) {
						final TextView textView = (TextView) layoutInflater.inflate(R.layout.ligne_code_item_medium,
								mRoutesView, false);
						textView.setBackgroundDrawable(ColorUtils.getGradiant(l.getBackColor()));
						textView.setText(l.getLetter());
						textView.setTypeface(robotoCondensed);
						textView.setTextColor(l.getFrontColor());

						final LayoutParams layoutParams = textView.getLayoutParams();
						layoutParams.width = newItemWidth;
						textView.setLayoutParams(layoutParams);

						mRoutesView.addView(textView);
					}
				}
				return false;
			}
		});

	}

	/**
	 * Afficher l'indicateur de chargement.
	 */
	protected void showLoader() {
		mFragmentView.findViewById(R.id.fragmentContent).setVisibility(View.GONE);
		if (mFragmentView.findViewById(R.id.fragmentMessage) != null) {
			mFragmentView.findViewById(R.id.fragmentMessage).setVisibility(View.GONE);
		}
		mFragmentView.findViewById(R.id.fragmentLoading).setVisibility(View.VISIBLE);
	}

	/**
	 * Afficher le contenu.
	 */
	protected void showContent() {
		mFragmentView.findViewById(R.id.fragmentLoading).setVisibility(View.GONE);
		if (mFragmentView.findViewById(R.id.fragmentMessage) != null) {
			mFragmentView.findViewById(R.id.fragmentMessage).setVisibility(View.GONE);
		}
		final View content = mFragmentView.findViewById(R.id.fragmentContent);
		if (content.getVisibility() != View.VISIBLE) {
			content.setVisibility(View.VISIBLE);
			content.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));
		}
	}

	/**
	 * Afficher le message avec un symbole d'erreur.
	 * 
	 * @param titleRes
	 *            L'identifiant du titre.
	 * @param descriptionRes
	 *            L'identifiant de la description.
	 */
	protected void showError(final int titleRes, final int descriptionRes) {
		showMessage(getString(titleRes), getString(descriptionRes), R.drawable.warning);
	}

	/**
	 * Afficher le message.
	 * 
	 * @param titleRes
	 *            L'identifiant du titre.
	 * @param descriptionRes
	 *            L'identifiant de la description.
	 * @param drawableRes
	 *            L'identifiant du drawable.
	 */
	protected void showMessage(final int titleRes, final int descriptionRes, final int drawableRes) {
		showMessage(getString(titleRes), (descriptionRes != 0) ? getString(descriptionRes) : null, drawableRes);
	}

	/**
	 * Afficher un message avec une desciption et un symbole.
	 * 
	 * @param title
	 *            Le titre.
	 * @param description
	 *            La description.
	 * @param drawableRes
	 *            L'identifiant du symbole.
	 */
	protected void showMessage(final String title, final String description, final int drawableRes) {
		mFragmentView.findViewById(R.id.fragmentContent).setVisibility(View.GONE);
		mFragmentView.findViewById(R.id.fragmentLoading).setVisibility(View.GONE);

		View message = mFragmentView.findViewById(R.id.fragmentMessage);
		if (message == null) {
			final ViewStub messageStrub = (ViewStub) mFragmentView.findViewById(R.id.fragmentMessageStub);
			message = messageStrub.inflate();
			final Typeface robotoLight = FontUtils.getRobotoLight(getActivity());
			((TextView) message.findViewById(android.R.id.summary)).setTypeface(robotoLight);
		}

		message.setVisibility(View.VISIBLE);

		final TextView titleView = (TextView) message.findViewById(android.R.id.title);
		titleView.setText(title);
		titleView.setCompoundDrawablesWithIntrinsicBounds(0, drawableRes, 0, 0);

		final TextView descriptionView = (TextView) message.findViewById(android.R.id.summary);
		if (description != null) {
			descriptionView.setText(description);
			descriptionView.setVisibility(View.VISIBLE);
		} else {
			descriptionView.setVisibility(View.GONE);
		}
	}

}
