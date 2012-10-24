package net.naonedbus.fragment.impl;

import java.io.IOException;
import java.util.Set;

import net.naonedbus.R;
import net.naonedbus.bean.InfoTrafic;
import net.naonedbus.bean.Ligne;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.fragment.CustomFragment;
import net.naonedbus.manager.impl.InfoTraficManager;
import net.naonedbus.manager.impl.LigneManager;
import net.naonedbus.utils.ColorUtils;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class InfoTraficDetailFragment extends CustomFragment implements LoaderCallbacks<AsyncResult<InfoTrafic>> {

	public static final String PARAM_ID_INFO_TRAFIC = "idInfoTrafic";

	private String mCodeInfoTrafic;

	private TextView itemTitle;
	private TextView itemDescription;
	private TextView itemTime;
	private LinearLayout lignes;
	protected View fragmentView;

	public InfoTraficDetailFragment() {
		super(R.string.title_fragment_trafic_detail, R.layout.fragment_infotrafic_detail);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mCodeInfoTrafic = getArguments().getString(PARAM_ID_INFO_TRAFIC);

		getLoaderManager().initLoader(0, null, this);
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

	@Override
	protected void bindView(View view, Bundle savedInstanceState) {
		final Typeface robotoLight = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");

		fragmentView = view;

		itemTitle = (TextView) view.findViewById(R.id.itemTitle);
		itemTitle.setTypeface(robotoLight);
		itemDescription = (TextView) view.findViewById(R.id.itemDescription);
		itemTime = (TextView) view.findViewById(R.id.itemTime);
		itemTime.setTypeface(robotoLight);
		lignes = (LinearLayout) view.findViewById(R.id.lignes);
	}

	private void loadInfotrafic(final InfoTrafic infoTrafic) {
		final LigneManager ligneManager = LigneManager.getInstance();
		final LayoutInflater layoutInflater = (LayoutInflater) LayoutInflater.from(getActivity());

		itemTitle.setText(infoTrafic.getIntitule());
		itemDescription.setText(Html.fromHtml(infoTrafic.getResume()));
		itemTime.setText(infoTrafic.getDateFormated());

		final Set<String> lignesConcernees = infoTrafic.getLignes();
		for (final String codeLigne : lignesConcernees) {
			final Ligne ligne = ligneManager.getSingle(getActivity().getContentResolver(), codeLigne);

			if (ligne != null) {
				final TextView textView = (TextView) layoutInflater.inflate(R.layout.ligne_code_item, lignes, false);
				textView.setBackgroundDrawable(ColorUtils.getGradiant(ligne.couleurBackground));
				textView.setText(ligne.lettre);
				textView.setTextColor(ligne.couleurTexte);

				lignes.addView(textView);
			}
		}

	}

	@Override
	public Loader<AsyncResult<InfoTrafic>> onCreateLoader(int arg0, Bundle arg1) {
		final Loader<AsyncResult<InfoTrafic>> loader = new AsyncTaskLoader<AsyncResult<InfoTrafic>>(getActivity()) {
			@Override
			public AsyncResult<InfoTrafic> loadInBackground() {
				return loadContent(getActivity());
			}
		};
		showLoader();
		loader.forceLoad();

		return loader;
	}

	private AsyncResult<InfoTrafic> loadContent(Context context) {
		final AsyncResult<InfoTrafic> result = new AsyncResult<InfoTrafic>();

		try {
			final InfoTraficManager infoTraficManager = InfoTraficManager.getInstance();
			result.setResult(infoTraficManager.getById(getActivity(), Integer.valueOf(mCodeInfoTrafic)));
		} catch (IOException e) {
			result.setException(e);
		}

		return result;
	}

	@Override
	public void onLoadFinished(Loader<AsyncResult<InfoTrafic>> loader, AsyncResult<InfoTrafic> result) {
		final Exception exception = result.getException();

		if (exception == null) {
			showContent();
			if (result.getResult() != null) {
				loadInfotrafic(result.getResult());
			}
		} else {
			Log.e(getClass().getSimpleName(), "Erreur de chargement.", exception);

			// Erreur réseau ou interne ?
			if (exception instanceof IOException) {
				showMessage(R.string.error_title_network, R.string.error_summary_network, R.drawable.orage);
			} else {
				showError(R.string.error_title, R.string.error_summary);
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<AsyncResult<InfoTrafic>> arg0) {

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
