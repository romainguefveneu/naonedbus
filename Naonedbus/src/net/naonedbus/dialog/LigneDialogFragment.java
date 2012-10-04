package net.naonedbus.dialog;

import net.naonedbus.R;
import net.naonedbus.bean.Ligne;
import net.naonedbus.bean.Sens;
import net.naonedbus.manager.impl.SensManager;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.widget.PinnedHeaderListView;
import net.naonedbus.widget.adapter.SectionAdapter;
import net.naonedbus.widget.adapter.impl.LigneDialogAdapter;
import net.naonedbus.widget.indexer.impl.LigneDialogIndexer;
import net.naonedbus.widget.item.SectionItem;
import android.app.Dialog;
import android.graphics.PorterDuff.Mode;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class LigneDialogFragment extends DialogFragment {

	public static final String BUNDLE_LIGNE = "ligne";

	private Ligne mLigne;
	private Typeface mRobotoLight;
	private SensManager mSensManager;
	private ImageView mMenuCarte;

	private OnSensClickListener mOnSensClickListener;
	private OnMapClickListener mOnMapClickListener;

	public static interface OnSensClickListener {
		void onSensClickListener(int id);
	}

	public static interface OnMapClickListener {
		void onMapClickListener();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mRobotoLight = Typeface.createFromAsset(dialog.getContext().getAssets(), "fonts/Roboto-Light.ttf");
		mSensManager = SensManager.getInstance();
		if (savedInstanceState != null) {
			mLigne = (Ligne) savedInstanceState.get(BUNDLE_LIGNE);
		}
		return dialog;
	}

	@Override
	public void setArguments(Bundle args) {
		super.setArguments(args);
		mLigne = (Ligne) args.get(BUNDLE_LIGNE);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(BUNDLE_LIGNE, mLigne);
	}

	@SuppressWarnings("deprecation")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.dialog_ligne, container);
		final View header = view.findViewById(R.id.ligneDialogHeader);
		final TextView code = (TextView) view.findViewById(R.id.ligneDialogCode);
		final ListView listView = (ListView) view.findViewById(android.R.id.list);

		mMenuCarte = (ImageView) view.findViewById(R.id.menu_navigation);
		mMenuCarte.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mOnMapClickListener != null) {
					mOnMapClickListener.onMapClickListener();
				}
			}
		});
		mMenuCarte.setColorFilter(mLigne.couleurTexte, Mode.MULTIPLY);

		setupListView(inflater, listView);

		header.setBackgroundDrawable(ColorUtils.getGradiant(mLigne.couleurBackground));
		code.setText(mLigne.lettre);
		code.setTextColor(mLigne.couleurTexte);
		code.setTypeface(mRobotoLight);

		final SectionAdapter<SectionItem> adapter = new LigneDialogAdapter(getActivity(), mSensManager.getAll(
				getActivity().getContentResolver(), mLigne.code));
		adapter.setIndexer(new LigneDialogIndexer());

		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
				final Sens sens = (Sens) adapter.getItemAtPosition(position);
				if (mOnSensClickListener != null) {
					mOnSensClickListener.onSensClickListener(sens._id);
				}
			}
		});

		return view;
	}

	public void setOnSensClickListener(OnSensClickListener onSensClickListener) {
		this.mOnSensClickListener = onSensClickListener;
	}

	public void setOnMapClickListener(OnMapClickListener onMapClickListener) {
		this.mOnMapClickListener = onMapClickListener;
	}

	private void setupListView(final LayoutInflater inflater, final ListView listView) {
		if (listView instanceof PinnedHeaderListView) {
			final PinnedHeaderListView pinnedListView = (PinnedHeaderListView) listView;
			pinnedListView.setPinnedHeaderView(inflater.inflate(R.layout.list_item_header, pinnedListView, false));
			pinnedListView.setOnScrollListener(new OnScrollListener() {
				@Override
				public void onScrollStateChanged(AbsListView view, int scrollState) {

				}

				@Override
				public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
					final Adapter adapter = listView.getAdapter();
					if (adapter != null && adapter instanceof OnScrollListener) {
						final OnScrollListener sectionAdapter = (OnScrollListener) adapter;
						sectionAdapter.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
					}
				}
			});
		}
	}
}
