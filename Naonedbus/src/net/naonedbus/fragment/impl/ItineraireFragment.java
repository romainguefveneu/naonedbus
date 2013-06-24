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
import java.util.List;

import net.naonedbus.BuildConfig;
import net.naonedbus.R;
import net.naonedbus.activity.impl.ItineraryDetailActivity;
import net.naonedbus.bean.ItineraryWrapper;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.loader.ItineraryLoader;
import net.naonedbus.widget.AddressTextView;
import net.naonedbus.widget.AddressTextView.OnLocationEditChange;
import net.naonedbus.widget.adapter.impl.ItineraryWrapperArrayAdapter;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;

public class ItineraireFragment extends SherlockListFragment implements
		LoaderCallbacks<AsyncResult<List<ItineraryWrapper>>> {

	private static final String TAG = "ItineraireFragment";
	private static final boolean DBG = BuildConfig.DEBUG;

	private ItineraryWrapperArrayAdapter mAdapter;
	private ViewGroup mFragmentView;
	private View mProgressView;

	private AddressTextView mFromAddressTextView;
	private AddressTextView mToAddressTextView;
	private EditText mDateAndTime;

	private Button mGoButton;

	private final OnLocationEditChange mOnLocationChange = new OnLocationEditChange() {

		@Override
		public void onLocationNotFound() {
			mGoButton.setEnabled(false);
		}

		@Override
		public void onLocationFound() {
			mGoButton.setEnabled(mFromAddressTextView.hasLocation() && mToAddressTextView.hasLocation());
		}

		@Override
		public void onFocus() {
			if (mGoButton.getVisibility() != View.VISIBLE) {
				mGoButton.postDelayed(new Runnable() {
					@Override
					public void run() {
						mGoButton.setVisibility(View.VISIBLE);
					}
				}, 200);
			}
		}
	};

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	};

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_itineraire, menu);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		if (container == null) // must put this in
			return null;

		mFragmentView = (ViewGroup) inflater.inflate(R.layout.fragment_itineraire, container, false);
		bindView(mFragmentView, savedInstanceState);

		return mFragmentView;
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
		super.onListItemClick(l, v, position, id);

		final ItineraryWrapper wrapper = (ItineraryWrapper) getListView().getItemAtPosition(position);
		final Intent intent = new Intent(getActivity(), ItineraryDetailActivity.class);
		intent.putExtra(ItineraryDetailActivity.PARAM_ITINERARY, wrapper.getItinerary());
		getActivity().startActivity(intent);
	}

	protected void bindView(final View view, final Bundle savedInstanceState) {
		final ListView listView = (ListView) view.findViewById(android.R.id.list);
		mProgressView = view.findViewById(android.R.id.progress);

		mAdapter = new ItineraryWrapperArrayAdapter(getActivity(), new ArrayList<ItineraryWrapper>());
		final SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(mAdapter);
		swingBottomInAnimationAdapter.setListView(listView);

		final View formView = LayoutInflater.from(getActivity()).inflate(R.layout.itineraire_form, listView, false);
		listView.addHeaderView(formView);
		listView.setAdapter(swingBottomInAnimationAdapter);

		mGoButton = (Button) formView.findViewById(android.R.id.button1);
		mFromAddressTextView = (AddressTextView) formView.findViewById(R.id.formFrom);
		mToAddressTextView = (AddressTextView) formView.findViewById(R.id.formTo);
		mDateAndTime = (EditText) formView.findViewById(R.id.dateAndTime);

		mDateAndTime.setKeyListener(null);
		mDateAndTime.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				showDateTimePicker();
			}
		});

		mFromAddressTextView.setOnLocationEditChange(mOnLocationChange);
		mFromAddressTextView.setNextFocusView(mToAddressTextView);

		mToAddressTextView.setOnLocationEditChange(mOnLocationChange);
		mToAddressTextView.setNextFocusView(mDateAndTime);

		mGoButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				formView.requestFocus();
				mAdapter.clear();
				showProgress();
				sendRequest();

				final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
						Activity.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

				mGoButton.postDelayed(new Runnable() {
					@Override
					public void run() {
						mGoButton.setVisibility(View.GONE);
					}
				}, 100);
			}
		});

	}

	private void sendRequest() {
		final Bundle bundle = new Bundle();
		bundle.putDouble(ItineraryLoader.PARAM_FROM_LATITUDE, mFromAddressTextView.getLatitude());
		bundle.putDouble(ItineraryLoader.PARAM_FROM_LONGITUDE, mFromAddressTextView.getLongitude());
		bundle.putDouble(ItineraryLoader.PARAM_TO_LATITUDE, mToAddressTextView.getLatitude());
		bundle.putDouble(ItineraryLoader.PARAM_TO_LONGITUDE, mToAddressTextView.getLongitude());

		getLoaderManager().restartLoader(0, bundle, this);
	}

	private void showProgress() {
		mProgressView.setVisibility(View.VISIBLE);
	}

	private void hideProgress() {
		mProgressView.setVisibility(View.GONE);
	}

	private void showDateTimePicker() {

	}

	public void onDrawerStateChange(final int oldState, final int newState) {
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		return false;
	}

	@Override
	public Loader<AsyncResult<List<ItineraryWrapper>>> onCreateLoader(final int loaderId, final Bundle bundle) {
		return new ItineraryLoader(getActivity(), bundle);
	}

	@Override
	public void onLoadFinished(final Loader<AsyncResult<List<ItineraryWrapper>>> loader,
			final AsyncResult<List<ItineraryWrapper>> result) {

		hideProgress();
		if (result.getException() == null) {
			mAdapter.clear();
			mAdapter.addAll(result.getResult());
			mAdapter.notifyDataSetChanged();
			getListView().smoothScrollToPosition(1);
		}

	}

	@Override
	public void onLoaderReset(final Loader<AsyncResult<List<ItineraryWrapper>>> arg0) {

	}

}
