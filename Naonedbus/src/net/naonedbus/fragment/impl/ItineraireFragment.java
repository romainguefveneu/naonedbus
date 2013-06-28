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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import net.naonedbus.BuildConfig;
import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.activity.impl.AddressSearchActivity;
import net.naonedbus.activity.impl.DatePickerActivity;
import net.naonedbus.activity.impl.ItineraryDetailActivity;
import net.naonedbus.bean.ItineraryWrapper;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.loader.ItineraryLoader;
import net.naonedbus.widget.adapter.impl.ItineraryWrapperArrayAdapter;

import org.joda.time.MutableDateTime;

import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;

public class ItineraireFragment extends SherlockListFragment implements
		LoaderCallbacks<AsyncResult<List<ItineraryWrapper>>> {

	private static final String TAG = "ItineraireFragment";
	private static final boolean DBG = BuildConfig.DEBUG;

	private static final int REQUEST_CODE_FROM = 1;
	private static final int REQUEST_CODE_TO = 2;
	private static final int REQUEST_CODE_DATE = 3;

	private final List<ItineraryWrapper> mItineraryWrappers = new ArrayList<ItineraryWrapper>();
	private ItineraryWrapperArrayAdapter mAdapter;
	private ViewGroup mFragmentView;
	private View mProgressView;

	private DateFormat mDateFormat;

	private final Location mFromLocation = new Location(LocationManager.GPS_PROVIDER);
	private final Location mToLocation = new Location(LocationManager.GPS_PROVIDER);
	private final MutableDateTime mDateTime = new MutableDateTime();

	private TextView mFromAddressTextView;
	private TextView mToAddressTextView;
	private TextView mDateAndTime;

	private Button mGoButton;

	private boolean mDialogLock;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		mDateFormat = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT);
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

		mAdapter = new ItineraryWrapperArrayAdapter(getActivity(), mItineraryWrappers);
		final SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(mAdapter);
		swingBottomInAnimationAdapter.setListView(listView);

		final View formView = LayoutInflater.from(getActivity()).inflate(R.layout.itineraire_form, listView, false);
		listView.addHeaderView(formView);
		listView.setAdapter(swingBottomInAnimationAdapter);

		mFromAddressTextView = (TextView) formView.findViewById(R.id.formFrom);
		mFromAddressTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				requestFromAddress();
			}
		});

		mToAddressTextView = (TextView) formView.findViewById(R.id.formTo);
		mToAddressTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				requestToAddress();
			}
		});

		mDateAndTime = (TextView) formView.findViewById(R.id.dateAndTime);
		mDateAndTime.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				showDatePicker();
			}
		});

		mGoButton = (Button) formView.findViewById(android.R.id.button1);
		mGoButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				formView.requestFocus();
				mAdapter.clear();
				showProgress();
				sendRequest();

				mGoButton.postDelayed(new Runnable() {
					@Override
					public void run() {
						mGoButton.setVisibility(View.GONE);
					}
				}, 100);
			}
		});

		final Location currentLocation = NBApplication.getLocationProvider().getLastKnownLocation();
		if (currentLocation != null && currentLocation.getLatitude() != 0 && currentLocation.getLongitude() != 0) {
			mFromLocation.set(currentLocation);
			mFromAddressTextView.setText(R.string.itineraire_current_location);
		}

	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (data != null) {

			if (requestCode == REQUEST_CODE_DATE) {
				mDateTime.setYear(data.getIntExtra(DatePickerActivity.PARAM_YEAR, 0));
				mDateTime.setMonthOfYear(data.getIntExtra(DatePickerActivity.PARAM_MONTH, 0));
				mDateTime.setDayOfMonth(data.getIntExtra(DatePickerActivity.PARAM_DAY, 0));
				mDateTime.setHourOfDay(data.getIntExtra(DatePickerActivity.PARAM_HOUR, 0));
				mDateTime.setMinuteOfHour(data.getIntExtra(DatePickerActivity.PARAM_MINUTE, 0));

				mDateAndTime.setText(mDateFormat.format(mDateTime.toDate()));

				onFormValueChange();
			} else {
				final String address = data.getStringExtra("address");
				final double latitude = data.getDoubleExtra("latitude", 0d);
				final double longitude = data.getDoubleExtra("longitude", 0d);

				if (requestCode == REQUEST_CODE_FROM) {
					mFromLocation.setLatitude(latitude);
					mFromLocation.setLongitude(longitude);
					mFromAddressTextView.setText(address);
				} else {
					mToLocation.setLatitude(latitude);
					mToLocation.setLongitude(longitude);
					mToAddressTextView.setText(address);
				}
			}

			onFormValueChange();
		}

	}

	private void requestFromAddress() {
		requestAddress(REQUEST_CODE_FROM);
	}

	private void requestToAddress() {
		requestAddress(REQUEST_CODE_TO);
	}

	private void requestAddress(final int requestCode) {
		final Intent intent = new Intent(getActivity(), AddressSearchActivity.class);

		startActivityForResult(intent, requestCode);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			getActivity().overridePendingTransition(R.anim.slide_in_from_right, R.anim.half_fade_out);
	}

	private void showDatePicker() {
		final Intent intent = new Intent(getActivity(), DatePickerActivity.class);
		intent.putExtra(DatePickerActivity.PARAM_YEAR, mDateTime.getYear());
		intent.putExtra(DatePickerActivity.PARAM_MONTH, mDateTime.getMonthOfYear() - 1);
		intent.putExtra(DatePickerActivity.PARAM_DAY, mDateTime.getDayOfMonth());
		intent.putExtra(DatePickerActivity.PARAM_HOUR, mDateTime.getHourOfDay());
		intent.putExtra(DatePickerActivity.PARAM_MINUTE, mDateTime.getMinuteOfHour());

		startActivityForResult(intent, REQUEST_CODE_DATE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			getActivity().overridePendingTransition(R.anim.slide_in_from_right, R.anim.half_fade_out);
	}

	private void onFormValueChange() {
		final boolean hasLocations = mFromLocation.getLatitude() != 0 && mToLocation.getLatitude() != 0;
		mGoButton.setEnabled(hasLocations);

		if (hasLocations && mGoButton.getVisibility() != View.VISIBLE) {
			mGoButton.setVisibility(View.VISIBLE);
			mAdapter.clear();
		}
	}

	private void sendRequest() {
		final Bundle bundle = new Bundle();
		bundle.putDouble(ItineraryLoader.PARAM_FROM_LATITUDE, mFromLocation.getLatitude());
		bundle.putDouble(ItineraryLoader.PARAM_FROM_LONGITUDE, mFromLocation.getLongitude());
		bundle.putDouble(ItineraryLoader.PARAM_TO_LATITUDE, mToLocation.getLatitude());
		bundle.putDouble(ItineraryLoader.PARAM_TO_LONGITUDE, mToLocation.getLongitude());
		bundle.putLong(ItineraryLoader.PARAM_TIME, mDateTime.getMillis());

		getLoaderManager().restartLoader(0, bundle, this);
	}

	private void showProgress() {
		mProgressView.setVisibility(View.VISIBLE);
	}

	private void hideProgress() {
		mProgressView.setVisibility(View.GONE);
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
			mItineraryWrappers.clear();
			mItineraryWrappers.addAll(result.getResult());
			mAdapter.notifyDataSetChanged();
			getListView().smoothScrollToPosition(1);
		}

	}

	@Override
	public void onLoaderReset(final Loader<AsyncResult<List<ItineraryWrapper>>> arg0) {

	}

}
