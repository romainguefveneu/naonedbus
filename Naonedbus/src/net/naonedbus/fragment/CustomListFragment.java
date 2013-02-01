package net.naonedbus.fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.naonedbus.R;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.widget.PinnedHeaderListView;

import org.joda.time.DateTime;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
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
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;

public abstract class CustomListFragment extends SherlockListFragment implements CustomFragmentActions,
		LoaderCallbacks<AsyncResult<ListAdapter>> {

	private static final int LOADER_INIT = 0;
	private static final int LOADER_REFRESH = 1;

	private static final String STATE_POSITION = "position";
	private static final String STATE_TOP = "top";

	int messageEmptyTitleId = R.string.error_title_empty;
	int messageEmptySummaryId = R.string.error_summary_empty;
	int messageEmptyDrawableId = R.drawable.sad_face;

	protected int titleId;
	protected int layoutId;
	protected int layoutListHeaderId = R.layout.list_item_header;
	protected ViewGroup fragmentView;

	private int mListViewStatePosition;
	private int mListViewStateTop;

	private List<OnScrollListener> mOnScrollListeners = new ArrayList<AbsListView.OnScrollListener>();

	/**
	 * Gestion du refraichissement
	 */
	private DateTime nextUpdate = null;
	/**
	 * Nombre de minutes pendant lesquelles le contenu est considéré comme à
	 * jour.
	 */
	private int timeToLive = 5;

	public CustomListFragment(final int titleId, final int layoutId) {
		this.titleId = titleId;
		this.layoutId = layoutId;
	}

	public CustomListFragment(final int titleId, final int layoutId, final int layoutListHeaderId) {
		this(titleId, layoutId);
		this.layoutListHeaderId = layoutListHeaderId;
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

		if (savedInstanceState != null) {
			mListViewStatePosition = savedInstanceState.getInt(STATE_POSITION, -1);
			mListViewStateTop = savedInstanceState.getInt(STATE_TOP, 0);
		} else {
			mListViewStatePosition = -1;
			mListViewStateTop = 0;
		}

		fragmentView = (ViewGroup) inflater.inflate(R.layout.fragment_base, container, false);
		final View view = inflater.inflate(this.layoutId, container, false);
		bindView(view, savedInstanceState);

		fragmentView.addView(view);

		setupListView(inflater, fragmentView);

		return fragmentView;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (isAdded()) {
			final View v = getListView().getChildAt(0);
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

		listView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				triggerOnScrollListeners(listView, firstVisibleItem, visibleItemCount, totalItemCount);
			}
		});

		if (listView instanceof PinnedHeaderListView) {
			final PinnedHeaderListView pinnedListView = (PinnedHeaderListView) listView;
			pinnedListView.setPinnedHeaderView(inflater.inflate(layoutListHeaderId, pinnedListView, false));
			addOnScrollListener(new OnScrollListener() {

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

	public void loadContent() {
		if (getListAdapter() == null) {
			getLoaderManager().initLoader(LOADER_INIT, null, this);
		}
	}

	public void refreshContent() {
		getLoaderManager().restartLoader(LOADER_REFRESH, null, this);
	}

	protected void addOnScrollListener(OnScrollListener onScrollListener) {
		mOnScrollListeners.add(onScrollListener);
	}

	private void triggerOnScrollListeners(AbsListView view, int firstVisibleItem, int visibleItemCount,
			int totalItemCount) {
		for (OnScrollListener l : mOnScrollListeners) {
			l.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
		}
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
		fragmentView.findViewById(android.R.id.list).setVisibility(View.GONE);
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

		final View content = fragmentView.findViewById(android.R.id.list);
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
		fragmentView.findViewById(android.R.id.list).setVisibility(View.GONE);
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

	/**
	 * Avant le chargement.
	 */
	protected void onPreExecute() {

	}

	/**
	 * Charger le contenu en background.
	 * 
	 * @return AsyncResult du resultat.
	 */
	protected abstract AsyncResult<ListAdapter> loadContent(final Context context);

	/**
	 * Après le chargement.
	 */
	protected void onPostExecute() {
	}

	@Override
	public Loader<AsyncResult<ListAdapter>> onCreateLoader(int arg0, Bundle arg1) {
		final Loader<AsyncResult<ListAdapter>> loader = new AsyncTaskLoader<AsyncResult<ListAdapter>>(getActivity()) {
			@Override
			public AsyncResult<ListAdapter> loadInBackground() {
				return loadContent(getActivity());
			}
		};
		
		onPreExecute();
		showLoader();
		loader.forceLoad();

		return loader;
	}

	@Override
	public void onLoadFinished(Loader<AsyncResult<ListAdapter>> loader, AsyncResult<ListAdapter> result) {

		if (result == null) {
			showMessage(messageEmptyTitleId, messageEmptySummaryId, messageEmptyDrawableId);
			return;
		}

		final Exception exception = result.getException();

		if (exception == null) {

			final ListAdapter adapter = result.getResult();
			if (adapter == null || adapter.getCount() == 0) {
				showMessage(messageEmptyTitleId, messageEmptySummaryId, messageEmptyDrawableId);
			} else {

				adapter.registerDataSetObserver(new DataSetObserver() {
					@Override
					public void onChanged() {
						super.onChanged();
						onListAdapterChange(adapter);
					}
				});

				setListAdapter(adapter);

				if (mListViewStatePosition != -1 && isAdded()) {
					getListView().setSelectionFromTop(mListViewStatePosition, mListViewStateTop);
					mListViewStatePosition = -1;
				}

				showContent();
				resetNextUpdate();

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

		onPostExecute();
	}

	@Override
	public void onLoaderReset(Loader<AsyncResult<ListAdapter>> arg0) {
	}

	/**
	 * Gestion du changement du contenu de l'adapter : affichage ou non du
	 * message comme quoi la liste est vide.
	 * 
	 * @param adapter
	 */
	public void onListAdapterChange(ListAdapter adapter) {
		if (adapter == null || adapter.getCount() == 0) {
			showMessage(messageEmptyTitleId, messageEmptySummaryId, messageEmptyDrawableId);
		} else {
			showContent();
		}
	}

}
