package net.naonedbus.fragment;

import net.naonedbus.R;
import net.naonedbus.widget.PinnedHeaderListView;

import org.joda.time.DateTime;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;

public abstract class ListFragment extends SherlockListFragment implements CustomFragmentActions {

	private static final String STATE_POSITION = "position";
	private static final String STATE_TOP = "top";

	private int messageEmptyTitleId = R.string.error_title_empty;
	private int messageEmptySummaryId = R.string.error_summary_empty;
	private int messageEmptyDrawableId = R.drawable.sad_face;

	protected int titleId;
	protected int layoutId;
	protected ViewGroup fragmentView;

	/**
	 * Gestion du refraichissement
	 */
	private DateTime nextUpdate = null;
	/**
	 * Nombre de minutes pendant lesquelles le contenu est considéré comme à
	 * jour.
	 */
	private int timeToLive = 5;

	public ListFragment(final int titleId, final int layoutId) {
		this.titleId = titleId;
		this.layoutId = layoutId;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (container == null) // must put this in
			return null;

		fragmentView = (ViewGroup) inflater.inflate(R.layout.fragment_base, container, false);
		View view = inflater.inflate(this.layoutId, container, false);
		view.setId(R.id.fragmentContent);

		bindView(view, savedInstanceState);

		fragmentView.addView(view);

		setupListView(inflater, fragmentView);

		return fragmentView;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (isAdded()) {
			View v = getListView().getChildAt(0);
			int top = (v == null) ? 0 : v.getTop();
			outState.putInt(STATE_POSITION, getListView().getFirstVisiblePosition());
			outState.putInt(STATE_TOP, top);
		}
		super.onSaveInstanceState(outState);
	}

	protected void bindView(View view, Bundle savedInstanceState) {

	}

	private void setupListView(LayoutInflater inflater, View view) {
		final ListView listView = (ListView) fragmentView.findViewById(android.R.id.list);

		if (listView instanceof PinnedHeaderListView) {
			final PinnedHeaderListView pinnedListView = (PinnedHeaderListView) listView;
			pinnedListView.setPinnedHeaderView(inflater.inflate(R.layout.list_item_header, pinnedListView, false));
			pinnedListView.setOnScrollListener(new OnScrollListener() {

				@Override
				public void onScrollStateChanged(AbsListView view, int scrollState) {

				}

				@Override
				public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
					final Adapter adapter = getListAdapter();
					if (adapter != null && adapter instanceof OnScrollListener) {
						final OnScrollListener sectionAdapter = (OnScrollListener) adapter;
						sectionAdapter.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
					}
				}
			});
		}
	}

	public int getTitleId() {
		return titleId;
	}

	/**
	 * Définir les textes et images affichés si la liste est vide.
	 * 
	 * @param titleId
	 *            L'identifiant du titre.
	 * @param summaryId
	 *            L'identifiant de la description.
	 * @param drawableId
	 *            L'identifiant du drawable.
	 */
	protected void setEmptyMessageValues(int titleId, int summaryId, int drawableId) {
		this.messageEmptyTitleId = titleId;
		this.messageEmptySummaryId = summaryId;
		this.messageEmptyDrawableId = drawableId;
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
		content.setVisibility(View.VISIBLE);
		content.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));
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
	 * Afficher le message avec un symbole d'erreur.
	 * 
	 * @param title
	 *            Le titre.
	 * @param description
	 *            La description.
	 */
	protected void showError(String title, String description) {
		showMessage(title, description, R.drawable.warning);
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

	/**
	 * Définir l'action du bouton lors de l'affichage du message.
	 * 
	 * @param title
	 *            Le titre du boutton.
	 * @param onClickListener
	 *            Son action.
	 */
	protected void setMessageButton(int title, OnClickListener onClickListener) {
		setMessageButton(getString(title), onClickListener);
	}

	/**
	 * Définir l'action du bouton lors de l'affichage du message.
	 * 
	 * @param title
	 *            Le titre du boutton.
	 * @param onClickListener
	 *            Son action.
	 */
	protected void setMessageButton(String title, OnClickListener onClickListener) {
		final View message = fragmentView.findViewById(R.id.fragmentMessage);
		if (message != null) {
			final Button button = (Button) message.findViewById(android.R.id.button1);
			button.setText(title);
			button.setOnClickListener(onClickListener);
			button.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Définir le nombre de minutes pendant lesquelles les données sont
	 * considérées comme à jour
	 * 
	 * @param timeToLive
	 */
	protected void setTimeToLive(int timeToLive) {
		this.timeToLive = timeToLive;
	}

	/**
	 * Redéfinir la date d'expiration du cache à maintenant
	 */
	protected void resetNextUpdate() {
		nextUpdate = new DateTime().plusMinutes(timeToLive);
	}

	/**
	 * Indique si les données sont toujours considérées comme à jour ou non
	 * 
	 * @return true si elle ne sont plus à jour | false si elle sont à jour
	 */
	protected boolean isNotUpToDate() {
		if (nextUpdate != null) {
			return (nextUpdate.isBeforeNow());
		} else {
			return true;
		}
	}

}
