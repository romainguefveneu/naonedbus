package net.naonedbus.fragment.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.naonedbus.R;
import net.naonedbus.bean.InfoTrafic;
import net.naonedbus.bean.Ligne;
import net.naonedbus.comparator.LigneLettreComparator;
import net.naonedbus.fragment.CustomFragment;
import net.naonedbus.manager.impl.LigneManager;
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

import com.actionbarsherlock.view.MenuItem;
import com.gridlayout.GridLayout;

public class InfoTraficDetailFragment extends CustomFragment {

	public static final String PARAM_INFO_TRAFIC = "infoTrafic";

	private TextView mItemTitle;
	private TextView mItemDescription;
	private TextView mItemTime;
	private GridLayout mLignesView;
	protected View mFragmentView;

	public InfoTraficDetailFragment() {
		super(R.string.title_fragment_trafic_detail, R.layout.fragment_infotrafic_detail);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		final InfoTrafic infoTrafic = getArguments().getParcelable(PARAM_INFO_TRAFIC);
		loadInfotrafic(infoTrafic);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		return false;
	}

	@Override
	protected void bindView(final View view, final Bundle savedInstanceState) {
		final Typeface robotoCondensed = FontUtils.getRobotoBoldCondensed(getActivity());
		final Typeface robotoMedium = FontUtils.getRobotoMedium(getActivity());

		mFragmentView = view;

		mItemTitle = (TextView) view.findViewById(R.id.itemTitle);
		mItemTitle.setTypeface(robotoCondensed);

		mItemDescription = (TextView) view.findViewById(R.id.itemDescription);

		mItemTime = (TextView) view.findViewById(R.id.itemTime);
		mItemTime.setTypeface(robotoMedium);

		mLignesView = (GridLayout) view.findViewById(R.id.lignes);
	}

	private void loadInfotrafic(final InfoTrafic infoTrafic) {
		final LigneManager ligneManager = LigneManager.getInstance();
		final LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

		mItemTitle.setText(infoTrafic.getIntitule());
		mItemDescription.setText(Html.fromHtml(infoTrafic.getResume()));
		mItemTime.setText(infoTrafic.getDateFormated());

		final List<String> lignesConcernees = new ArrayList<String>(infoTrafic.getLignes());
		final List<Ligne> listLignes = new ArrayList<Ligne>();

		for (final String codeLigne : lignesConcernees) {
			final Ligne ligne = ligneManager.getSingle(getActivity().getContentResolver(), codeLigne);
			if (ligne != null) {
				listLignes.add(ligne);
			}
		}
		Collections.sort(listLignes, new LigneLettreComparator());

		final ViewTreeObserver obs = mLignesView.getViewTreeObserver();
		obs.addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				if (mLignesView.getMeasuredWidth() != 0) {
					mLignesView.getViewTreeObserver().removeOnPreDrawListener(this);

					final int itemWidth = getResources().getDimensionPixelSize(R.dimen.codeitem_width);
					final int smallPadding = getResources().getDimensionPixelSize(R.dimen.padding_small);
					final int dividerPadding = getResources().getDimensionPixelSize(R.dimen.codeitem_margin);
					final int innerWidth = mLignesView.getMeasuredWidth() - smallPadding * 2;
					final int columnCount = innerWidth / (itemWidth + dividerPadding * 2);
					mLignesView.setColumnCount(columnCount);

					final int newItemWidth = innerWidth / columnCount - dividerPadding / 2;

					for (final Ligne l : listLignes) {
						final TextView textView = (TextView) layoutInflater.inflate(R.layout.ligne_code_item_medium,
								mLignesView, false);
						textView.setBackgroundDrawable(ColorUtils.getGradiant(l.couleurBackground));
						textView.setText(l.lettre);
						textView.setTextColor(l.couleurTexte);

						final LayoutParams layoutParams = textView.getLayoutParams();
						layoutParams.width = newItemWidth;
						textView.setLayoutParams(layoutParams);

						mLignesView.addView(textView);
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
			final Typeface robotoLight = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");
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
