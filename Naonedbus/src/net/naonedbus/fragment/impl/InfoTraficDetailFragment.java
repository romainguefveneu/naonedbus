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
import android.view.ViewStub;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.actionbarsherlock.view.MenuItem;
import com.gridlayout.GridLayout;

public class InfoTraficDetailFragment extends CustomFragment {

	public static final String PARAM_INFO_TRAFIC = "infoTrafic";

	private TextView itemTitle;
	private TextView itemDescription;
	private TextView itemTime;
	private GridLayout lignes;
	protected View fragmentView;

	public InfoTraficDetailFragment() {
		super(R.string.title_fragment_trafic_detail, R.layout.fragment_infotrafic_detail);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		final InfoTrafic infoTrafic = getArguments().getParcelable(PARAM_INFO_TRAFIC);
		loadInfotrafic(infoTrafic);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return false;
	}

	@Override
	protected void bindView(View view, Bundle savedInstanceState) {
		final Typeface robotoCondensed = FontUtils.getRobotoBoldCondensed(getActivity());
		final Typeface robotoLight = FontUtils.getRobotoLight(getActivity());
		final Typeface robotoMedium = FontUtils.getRobotoMedium(getActivity());

		fragmentView = view;

		itemTitle = (TextView) view.findViewById(R.id.itemTitle);
		itemTitle.setTypeface(robotoCondensed);

		itemDescription = (TextView) view.findViewById(R.id.itemDescription);
		itemDescription.setTypeface(robotoLight);

		itemTime = (TextView) view.findViewById(R.id.itemTime);
		itemTime.setTypeface(robotoMedium);

		lignes = (GridLayout) view.findViewById(R.id.lignes);
	}

	private void loadInfotrafic(final InfoTrafic infoTrafic) {
		final LigneManager ligneManager = LigneManager.getInstance();
		final LayoutInflater layoutInflater = (LayoutInflater) LayoutInflater.from(getActivity());

		itemTitle.setText(infoTrafic.getIntitule());
		itemDescription.setText(Html.fromHtml(infoTrafic.getResume()));
		itemTime.setText(infoTrafic.getDateFormated());

		final List<String> lignesConcernees = new ArrayList<String>(infoTrafic.getLignes());
		final List<Ligne> listLignes = new ArrayList<Ligne>();

		for (final String codeLigne : lignesConcernees) {
			final Ligne ligne = ligneManager.getSingle(getActivity().getContentResolver(), codeLigne);
			if (ligne != null) {
				listLignes.add(ligne);
			}
		}
		Collections.sort(listLignes, new LigneLettreComparator());
		for (Ligne l : listLignes) {
			final TextView textView = (TextView) layoutInflater.inflate(R.layout.ligne_code_item_medium, lignes, false);
			textView.setBackgroundDrawable(ColorUtils.getGradiant(l.couleurBackground));
			textView.setText(l.lettre);
			textView.setTextColor(l.couleurTexte);
			lignes.addView(textView);
		}

	}

	/**
	 * Afficher l'indicateur de chargement.
	 */
	protected void showLoader() {
		fragmentView.findViewById(R.id.fragmentContent).setVisibility(View.GONE);
		if (fragmentView.findViewById(R.id.fragmentMessage) != null) {
			fragmentView.findViewById(R.id.fragmentMessage).setVisibility(View.GONE);
		}
		fragmentView.findViewById(R.id.fragmentLoading).setVisibility(View.VISIBLE);
	}

	/**
	 * Afficher le contenu.
	 */
	protected void showContent() {
		fragmentView.findViewById(R.id.fragmentLoading).setVisibility(View.GONE);
		if (fragmentView.findViewById(R.id.fragmentMessage) != null) {
			fragmentView.findViewById(R.id.fragmentMessage).setVisibility(View.GONE);
		}
		final View content = fragmentView.findViewById(R.id.fragmentContent);
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
	protected void showError(int titleRes, int descriptionRes) {
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
	protected void showMessage(int titleRes, int descriptionRes, int drawableRes) {
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
	protected void showMessage(String title, String description, int drawableRes) {
		fragmentView.findViewById(R.id.fragmentContent).setVisibility(View.GONE);
		fragmentView.findViewById(R.id.fragmentLoading).setVisibility(View.GONE);

		View message = fragmentView.findViewById(R.id.fragmentMessage);
		if (message == null) {
			final ViewStub messageStrub = (ViewStub) fragmentView.findViewById(R.id.fragmentMessageStub);
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
