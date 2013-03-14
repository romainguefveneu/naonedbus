package net.naonedbus.fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.naonedbus.BuildConfig;
import net.naonedbus.R;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.utils.FontUtils;
import net.naonedbus.widget.PinnedHeaderListView;

import org.joda.time.DateTime;
import org.json.JSONException;

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
import com.devspark.appmsg.AppMsg;

public abstract class CustomListFragment extends SherlockListFragment implements CustomFragmentActions,
		LoaderCallbacks<AsyncResult<ListAdapter>> {

	private static enum State {
		CONTENT, LOADER, MESSAGE;
	}

	private static final int LOADER_INIT = 0;
	private static final int LOADER_REFRESH = 1;

	private static final String STATE_POSITION = "position";
	private static final String STATE_TOP = "top";

	private static final String LOG_TAG = "CustomListFragment";
	private static final boolean DBG = BuildConfig.DEBUG;

	int mMessageEmptyTitleId = R.string.error_title_empty;
	int mMessageEmptySummaryId = R.string.error_summary_empty;
	int mMessageEmptyDrawableId = R.drawable.sad_face;

	protected int mTitleId;
	protected int mLayoutId;
	protected int mLayoutListHeaderId = R.layout.list_item_header;
	protected ViewGroup mFragmentView;

	private int mListViewStatePosition;
	private int mListViewStateTop;

	private final List<OnScrollListener> mOnScrollListeners = new ArrayList<AbsListView.OnScrollListener>();

	private State mCurrentState;
	private DateTime mNextUpdate = null;
	/** Minutes pendant lesquelles le contenu est considéré comme à jour. */
	private int mTimeToLive = 5;

	public CustomListFragment(final int titleId, final int layoutId) {
		mTitleId = titleId;
		mLayoutId = layoutId;
	}

	public CustomListFragment(final int titleId, final int layoutId, final int layoutListHeaderId) {
		this(titleId, layoutId);
		this.mLayoutListHeaderId = layoutListHeaderId;
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (DBG)
			Log.d(LOG_TAG + "$" + getClass().getSimpleName(), "onActivityCreated " + mCurrentState);

		setRetainInstance(true);

		if (mCurrentState == State.MESSAGE) {
			mCurrentState = null;
			if (getListAdapter() == null || getListAdapter().getCount() == 0) {
				showMessage(mMessageEmptyTitleId, mMessageEmptySummaryId, mMessageEmptyDrawableId);
			}
		}
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		if (container == null) // must put this in
			return null;

		if (DBG)
			Log.d(LOG_TAG + "$" + getClass().getSimpleName(), "onCreateView " + mCurrentState);

		if (savedInstanceState != null) {
			mListViewStatePosition = savedInstanceState.getInt(STATE_POSITION, -1);
			mListViewStateTop = savedInstanceState.getInt(STATE_TOP, 0);
		} else {
			mListViewStatePosition = -1;
			mListViewStateTop = 0;
		}

		mFragmentView = (ViewGroup) inflater.inflate(R.layout.fragment_base, container, false);
		final View view = inflater.inflate(mLayoutId, container, false);
		bindView(view, savedInstanceState);

		mFragmentView.addView(view);

		setupListView(inflater, mFragmentView);

		return mFragmentView;
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		if (isAdded()) {
			final View v = getListView().getChildAt(0);
			final int top = (v == null) ? 0 : v.getTop();
			outState.putInt(STATE_POSITION, getListView().getFirstVisiblePosition());
			outState.putInt(STATE_TOP, top);
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onStop() {
		super.onStop();
		if (DBG)
			Log.d(LOG_TAG + "$" + getClass().getSimpleName(), "onStop");

		// mCurrentState = null;
	}

	protected void bindView(final View view, final Bundle savedInstanceState) {

	}

	private void setupListView(final LayoutInflater inflater, final View view) {
		final ListView listView = (ListView) mFragmentView.findViewById(android.R.id.list);

		listView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(final AbsListView view, final int scrollState) {
			}

			@Override
			public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount,
					final int totalItemCount) {
				triggerOnScrollListeners(listView, firstVisibleItem, visibleItemCount, totalItemCount);
			}
		});

		if (listView instanceof PinnedHeaderListView) {
			final PinnedHeaderListView pinnedListView = (PinnedHeaderListView) listView;
			pinnedListView.setPinnedHeaderView(inflater.inflate(mLayoutListHeaderId, pinnedListView, false));
			addOnScrollListener(new OnScrollListener() {

				@Override
				public void onScrollStateChanged(final AbsListView view, final int scrollState) {

				}

				@Override
				public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount,
						final int totalItemCount) {
					final Adapter adapter = getListAdapter();
					if (adapter != null && adapter instanceof OnScrollListener) {
						final OnScrollListener sectionAdapter = (OnScrollListener) adapter;
						sectionAdapter.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
					}
				}
			});
		}
	}

	@Override
	public int getTitleId() {
		return mTitleId;
	}

	public void loadContent() {
		if (getListAdapter() == null) {
			if (DBG)
				Log.d(LOG_TAG + "$" + getClass().getSimpleName(), "loadContent " + getListAdapter());

			getLoaderManager().initLoader(LOADER_INIT, null, this);
		}
	}

	public void refreshContent() {
		getLoaderManager().restartLoader(LOADER_REFRESH, null, this);
	}

	protected void addOnScrollListener(final OnScrollListener onScrollListener) {
		mOnScrollListeners.add(onScrollListener);
	}

	private void triggerOnScrollListeners(final AbsListView view, final int firstVisibleItem,
			final int visibleItemCount, final int totalItemCount) {
		for (final OnScrollListener l : mOnScrollListeners) {
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
	protected void setEmptyMessageValues(final int titleId, final int summaryId, final int drawableId) {
		this.mMessageEmptyTitleId = titleId;
		this.mMessageEmptySummaryId = summaryId;
		this.mMessageEmptyDrawableId = drawableId;
	}

	/**
	 * Afficher l'indicateur de chargement.
	 */
	protected void showLoader() {
		if (DBG)
			Log.d(LOG_TAG + "$" + getClass().getSimpleName(), "showLoader");

		if (State.LOADER == mCurrentState) {
			if (DBG)
				Log.e(LOG_TAG + "$" + getClass().getSimpleName(), "\t showLoader NO");
			return;
		}

		if (DBG)
			Log.i(LOG_TAG + "$" + getClass().getSimpleName(), "\t showLoader OK");

		mCurrentState = State.LOADER;

		mFragmentView.findViewById(android.R.id.list).setVisibility(View.GONE);
		if (mFragmentView.findViewById(R.id.fragmentMessage) != null) {
			mFragmentView.findViewById(R.id.fragmentMessage).setVisibility(View.GONE);
		}
		mFragmentView.findViewById(R.id.fragmentLoading).setVisibility(View.VISIBLE);
	}

	/**
	 * Afficher le contenu.
	 */
	protected void showContent() {
		if (State.CONTENT == mCurrentState) {
			return;
		}

		if (DBG)
			Log.d(LOG_TAG + "$" + getClass().getSimpleName(), "showContent");

		mCurrentState = State.CONTENT;

		mFragmentView.findViewById(R.id.fragmentLoading).setVisibility(View.GONE);

		if (mFragmentView.findViewById(R.id.fragmentMessage) != null) {
			mFragmentView.findViewById(R.id.fragmentMessage).setVisibility(View.GONE);
		}

		final View content = mFragmentView.findViewById(android.R.id.list);
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
	 * Afficher le message avec un symbole d'erreur.
	 * 
	 * @param title
	 *            Le titre.
	 * @param description
	 *            La description.
	 */
	protected void showError(final String title, final String description) {
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
		if (State.MESSAGE == mCurrentState) {
			return;
		}

		if (DBG)
			Log.d(LOG_TAG + "$" + getClass().getSimpleName(), "showMessage " + title + "\t" + description + "\t"
					+ drawableRes);

		mCurrentState = State.MESSAGE;

		mFragmentView.findViewById(android.R.id.list).setVisibility(View.GONE);
		mFragmentView.findViewById(R.id.fragmentLoading).setVisibility(View.GONE);

		View message = mFragmentView.findViewById(R.id.fragmentMessage);
		if (message == null) {
			final ViewStub messageStrub = (ViewStub) mFragmentView.findViewById(R.id.fragmentMessageStub);
			message = messageStrub.inflate();
			final Typeface robotoLight = FontUtils.getRobotoLight(getActivity());
			final Typeface robotoCondensed = FontUtils.getRobotoBoldCondensed(getActivity());
			((TextView) message.findViewById(android.R.id.summary)).setTypeface(robotoLight);
			((TextView) message.findViewById(android.R.id.title)).setTypeface(robotoCondensed);
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
	protected void setMessageButton(final int title, final OnClickListener onClickListener) {
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
	protected void setMessageButton(final String title, final OnClickListener onClickListener) {
		final View message = mFragmentView.findViewById(R.id.fragmentMessage);
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
	protected void setTimeToLive(final int timeToLive) {
		this.mTimeToLive = timeToLive;
	}

	/**
	 * Redéfinir la date d'expiration du cache à maintenant
	 */
	protected void resetNextUpdate() {
		mNextUpdate = new DateTime().plusMinutes(mTimeToLive);
	}

	/**
	 * Indique si les données sont toujours considérées comme à jour ou non
	 * 
	 * @return true si elle ne sont plus à jour | false si elle sont à jour
	 */
	protected boolean isNotUpToDate() {
		if (mNextUpdate != null) {
			return (mNextUpdate.isBeforeNow());
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
	public Loader<AsyncResult<ListAdapter>> onCreateLoader(final int arg0, final Bundle arg1) {
		if (DBG)
			Log.d(LOG_TAG + "$" + getClass().getSimpleName(), "onCreateLoader");

		final Loader<AsyncResult<ListAdapter>> loader = new AsyncTaskLoader<AsyncResult<ListAdapter>>(getActivity()) {
			@Override
			public AsyncResult<ListAdapter> loadInBackground() {
				if (DBG)
					Log.d(LOG_TAG + "$" + getClass().getSimpleName(), "loadInBackground");

				return loadContent(getActivity());
			}
		};

		if (getListAdapter() == null || getListAdapter().getCount() == 0)
			showLoader();
		onPreExecute();
		loader.forceLoad();

		return loader;
	}

	@Override
	public void onLoadFinished(final Loader<AsyncResult<ListAdapter>> loader, final AsyncResult<ListAdapter> result) {
		if (DBG)
			Log.d(LOG_TAG + "$" + getClass().getSimpleName(), "onLoadFinished " + result);

		if (result == null) {
			showMessage(mMessageEmptyTitleId, mMessageEmptySummaryId, mMessageEmptyDrawableId);
			return;
		}

		final Exception exception = result.getException();

		if (exception == null) {

			final ListAdapter adapter = result.getResult();
			setListAdapter(adapter);

			if (adapter == null) {
				showMessage(mMessageEmptyTitleId, mMessageEmptySummaryId, mMessageEmptyDrawableId);
			} else {
				adapter.registerDataSetObserver(new DataSetObserver() {
					@Override
					public void onChanged() {
						super.onChanged();
						onListAdapterChange(adapter);
					}
				});

				if (adapter.getCount() > 0) {
					if (mListViewStatePosition != -1 && isAdded()) {
						getListView().setSelectionFromTop(mListViewStatePosition, mListViewStateTop);
						mListViewStatePosition = -1;
					}

					showContent();
					resetNextUpdate();
				} else {
					showMessage(mMessageEmptyTitleId, mMessageEmptySummaryId, mMessageEmptyDrawableId);
				}
			}

		} else {

			int titleRes = R.string.error_title;
			int messageRes = R.string.error_summary;
			int drawableRes = R.drawable.warning;

			// Erreur réseau ou interne ?
			if (exception instanceof IOException) {
				titleRes = R.string.error_title_network;
				messageRes = R.string.error_summary_network;
				drawableRes = R.drawable.orage;
			} else if (exception instanceof JSONException) {
				titleRes = R.string.error_title_webservice;
				messageRes = R.string.error_summary_webservice;
			}

			if (getListAdapter() == null || getListAdapter().isEmpty()) {
				showMessage(titleRes, messageRes, drawableRes);
			} else {
				AppMsg.makeText(getActivity(), titleRes, AppMsg.STYLE_ALERT).show();
			}

			Log.e(getClass().getSimpleName(), "Erreur de chargement.", exception);
		}

		onPostExecute();
	}

	@Override
	public void onLoaderReset(final Loader<AsyncResult<ListAdapter>> arg0) {
	}

	@Override
	public void setListAdapter(final ListAdapter adapter) {
		if (DBG)
			Log.d(LOG_TAG + "$" + getClass().getSimpleName(), "setListAdapter " + adapter);
		super.setListAdapter(adapter);
	}

	/**
	 * Gestion du changement du contenu de l'adapter : affichage ou non du
	 * message comme quoi la liste est vide.
	 * 
	 * @param adapter
	 */
	public void onListAdapterChange(final ListAdapter adapter) {
		if (DBG)
			Log.d(LOG_TAG + "$" + getClass().getSimpleName(), "onListAdapterChange");

		if (adapter == null || adapter.getCount() == 0) {
			showMessage(mMessageEmptyTitleId, mMessageEmptySummaryId, mMessageEmptyDrawableId);
		} else {
			showContent();
		}
	}

}
