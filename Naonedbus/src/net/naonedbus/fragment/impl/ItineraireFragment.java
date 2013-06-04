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
import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.activity.impl.ItineraryDetailActivity;
import net.naonedbus.bean.ItineraryWrapper;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.loader.ItineraryLoader;
import net.naonedbus.provider.impl.MyLocationProvider;
import net.naonedbus.widget.adapter.impl.AddressArrayAdapter;
import net.naonedbus.widget.adapter.impl.AddressArrayAdapter.AddressWrapper;
import net.naonedbus.widget.adapter.impl.ItineraryWrapperArrayAdapter;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.MenuItem;
import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;

public class ItineraireFragment extends SherlockListFragment implements
		LoaderCallbacks<AsyncResult<List<ItineraryWrapper>>> {

	private static final String TAG = "ItineraireFragment";
	private static final boolean DBG = BuildConfig.DEBUG;

	private ItineraryWrapperArrayAdapter mAdapter;
	private ViewGroup mFragmentView;
	private View mProgressView;

	private AutoCompleteTextView mFromTextView;
	private AutoCompleteTextView mToTextView;

	private LocationEditManager mFromLocationEditManager;
	private LocationEditManager mToLocationEditManager;

	private Button mGoButton;

	private MyLocationProvider mLocationProvider;

	private final OnLocationEditChange mOnLocationChange = new OnLocationEditChange() {

		@Override
		public void onLocationNotFound() {
			mGoButton.setEnabled(false);
		}

		@Override
		public void onLocationFound() {
			mGoButton.setEnabled(mFromLocationEditManager.hasLocation() && mToLocationEditManager.hasLocation());
		}
	};

	private final OnFocusChangeListener mOnLocationFocusChangeListener = new OnFocusChangeListener() {

		@Override
		public void onFocusChange(final View v, final boolean hasFocus) {
			if (hasFocus && mGoButton.getVisibility() != View.VISIBLE) {
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
		setRetainInstance(false);

		mLocationProvider = NBApplication.getLocationProvider();
	};

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		if (container == null) // must put this in
			return null;

		mFragmentView = (ViewGroup) inflater.inflate(R.layout.fragment_itineraire, container, false);
		bindView(mFragmentView, savedInstanceState);

		return mFragmentView;
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		mFromLocationEditManager.saveInstanceState(outState);
		mToLocationEditManager.saveInstanceState(outState);
		super.onSaveInstanceState(outState);
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
		mFromTextView = (AutoCompleteTextView) formView.findViewById(R.id.fromPlace);
		mToTextView = (AutoCompleteTextView) formView.findViewById(R.id.toPlace);

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

		setDecoratedHint(mFromTextView, getString(R.string.search_hint_itineraire_depart));
		setDecoratedHint(mToTextView, getString(R.string.search_hint_itineraire_arrivee));

		mFromLocationEditManager = new LocationEditManager(mFromTextView, mOnLocationChange, savedInstanceState);
		mToLocationEditManager = new LocationEditManager(mToTextView, mOnLocationChange, savedInstanceState);

		mFromLocationEditManager.setLocationProvider(mLocationProvider);
		mToLocationEditManager.setLocationProvider(mLocationProvider);

		mFromLocationEditManager.setNextFocusView(mToTextView);
		mToLocationEditManager.setNextFocusView(mGoButton);

		mFromTextView.setAdapter(new AddressArrayAdapter(getActivity()));
		mToTextView.setAdapter(new AddressArrayAdapter(getActivity()));

		mFromTextView.setOnFocusChangeListener(mOnLocationFocusChangeListener);
		mToTextView.setOnFocusChangeListener(mOnLocationFocusChangeListener);
	}

	private void setDecoratedHint(final TextView textview, final CharSequence hintText) {
		final SpannableStringBuilder ssb = new SpannableStringBuilder("   "); // for
																				// the
																				// icon
		ssb.append(hintText);

		final Drawable searchIcon = getActivity().getResources().getDrawable(R.drawable.ic_action_location_gray);
		final int textSize = (int) (textview.getTextSize() * 1.25);
		searchIcon.setBounds(0, 0, textSize, textSize);
		ssb.setSpan(new ImageSpan(searchIcon), 1, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		textview.setHint(ssb);
	}

	private void sendRequest() {
		final Bundle bundle = new Bundle();
		bundle.putDouble(ItineraryLoader.PARAM_FROM_LATITUDE, mFromLocationEditManager.getLatitude());
		bundle.putDouble(ItineraryLoader.PARAM_FROM_LONGITUDE, mFromLocationEditManager.getLongitude());
		bundle.putDouble(ItineraryLoader.PARAM_TO_LATITUDE, mToLocationEditManager.getLatitude());
		bundle.putDouble(ItineraryLoader.PARAM_TO_LONGITUDE, mToLocationEditManager.getLongitude());

		getLoaderManager().restartLoader(0, bundle, this);
	}

	private void showProgress() {
		mProgressView.setVisibility(View.VISIBLE);
	}

	private void hideProgress() {
		mProgressView.setVisibility(View.GONE);
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

	public interface OnLocationEditChange {
		void onLocationFound();

		void onLocationNotFound();
	}

	private static class LocationEditManager implements TextWatcher, OnItemClickListener, OnClickListener {

		private static final String BUNDLE_LATITUDE = "LocationEditManager:latitude";
		private static final String BUNDLE_LONGITUDE = "LocationEditManager:longitude";
		private static final String BUNDLE_ADDRESS = "LocationEditManager:address";

		private final AutoCompleteTextView mAutoCompleteTextView;
		private final OnLocationEditChange mOnLocationEditChange;
		private MyLocationProvider mLocationProvider;
		private View mNextFocusView;
		private double mLatitude;
		private double mLongitude;

		public LocationEditManager(final AutoCompleteTextView autoCompleteTextView,
				final OnLocationEditChange onLocationEditChange, final Bundle saveInstanceState) {
			mAutoCompleteTextView = autoCompleteTextView;

			if (saveInstanceState != null) {
				mAutoCompleteTextView.setText(saveInstanceState.getString(BUNDLE_ADDRESS));
				mLatitude = saveInstanceState.getDouble(getBundleKeyLatitude(), 0);
				mLongitude = saveInstanceState.getDouble(getBundleKeyLongitude(), 0);
				if (mLatitude != 0 && mLongitude != 0) {
					mAutoCompleteTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0,
							R.drawable.ic_checkmark_holo_light, 0);
				}
			}

			mAutoCompleteTextView.addTextChangedListener(this);
			mAutoCompleteTextView.setOnItemClickListener(this);
			mAutoCompleteTextView.setOnClickListener(this);

			mOnLocationEditChange = onLocationEditChange;
		}

		public void setLocationProvider(final MyLocationProvider locationProvider) {
			mLocationProvider = locationProvider;
		}

		public void setNextFocusView(final View nextFocusView) {
			mNextFocusView = nextFocusView;
		}

		private String getBundleKeyLatitude() {
			return BUNDLE_LATITUDE + mAutoCompleteTextView.getId();
		}

		private String getBundleKeyLongitude() {
			return BUNDLE_LONGITUDE + mAutoCompleteTextView.getId();
		}

		private String getBundleKeyAddress() {
			return BUNDLE_ADDRESS + mAutoCompleteTextView.getId();
		}

		public void saveInstanceState(final Bundle outState) {
			outState.putDouble(getBundleKeyLatitude(), mLatitude);
			outState.putDouble(getBundleKeyLongitude(), mLongitude);
			outState.putString(getBundleKeyAddress(), mAutoCompleteTextView.getText().toString());
		}

		public boolean hasLocation() {
			return mLatitude != 0;
		}

		public double getLatitude() {
			return mLatitude;
		}

		public double getLongitude() {
			return mLongitude;
		}

		private void setLocation(final Address address) {
			final StringBuilder builder = new StringBuilder();
			final int addressLineSize = address.getMaxAddressLineIndex();
			for (int i = 0; i < addressLineSize; i++) {
				builder.append(address.getAddressLine(i));
				if (i != addressLineSize - 1) {
					builder.append(", ");
				}
			}
			mAutoCompleteTextView.setText(builder);

			setAddress(address);
		}

		private void setAddress(final Address address) {
			mLatitude = address.getLatitude();
			mLongitude = address.getLongitude();
			mOnLocationEditChange.onLocationFound();
			mAutoCompleteTextView.setSelection(0);
			if (mNextFocusView != null)
				mNextFocusView.requestFocus();
		}

		@Override
		public void onClick(final View v) {
			mAutoCompleteTextView.showDropDown();
		}

		@Override
		public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
			mLatitude = 0;
			mOnLocationEditChange.onLocationNotFound();
		}

		@Override
		public void onItemClick(final AdapterView<?> adapter, final View view, final int position, final long id) {
			final AddressWrapper wrapper = (AddressWrapper) adapter.getItemAtPosition(position);
			if (wrapper.isLocateMe()) {
				setLocation(mLocationProvider.getLastKnownAddress());
			} else {
				setAddress(wrapper.getAddress());
			}
		}

		@Override
		public void afterTextChanged(final Editable s) {

		}

		@Override
		public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

		}
	}

}
