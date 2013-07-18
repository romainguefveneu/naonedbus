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

import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.activity.impl.AddressSearchActivity;
import net.naonedbus.activity.impl.ItineraryDetailActivity;
import net.naonedbus.bean.Equipement;
import net.naonedbus.bean.Equipement.Type;
import net.naonedbus.bean.ItineraryWrapper;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.fragment.AbstractListFragment;
import net.naonedbus.loader.ItineraryLoader;
import net.naonedbus.widget.adapter.impl.ItineraryWrapperArrayAdapter;

import org.joda.time.MutableDateTime;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;

public class ItineraireFragment extends AbstractListFragment implements
		LoaderCallbacks<AsyncResult<List<ItineraryWrapper>>>, OnDateSetListener, OnTimeSetListener {

	private static final int REQUEST_CODE_FROM = 1;
	private static final int REQUEST_CODE_TO = 2;

	private final List<ItineraryWrapper> mItineraryWrappers = new ArrayList<ItineraryWrapper>();
	private ItineraryWrapperArrayAdapter mAdapter;
	private View mProgressView;

	private DateFormat mDateFormat;

	private final Location mFromLocation = new Location(LocationManager.GPS_PROVIDER);
	private final Location mToLocation = new Location(LocationManager.GPS_PROVIDER);
	private final MutableDateTime mDateTime = new MutableDateTime();
	private boolean mArriveBy;

	private TextView mFromAddressTextView;
	private TextView mToAddressTextView;
	private TextView mDateAndTimeLabel;
	private Spinner mDateKindSpinner;
	private ImageView mIconFrom;
	private ImageView mIconTo;

	private boolean mDialogLock;

	public ItineraireFragment() {
		super(R.layout.fragment_itineraire);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
	};

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
		super.onListItemClick(l, v, position, id);

		final ItineraryWrapper wrapper = (ItineraryWrapper) getListView().getItemAtPosition(position);
		final Bundle bundle = new Bundle();
		bundle.putSerializable(ItineraryDetailFragment.PARAM_ITINERARY_WRAPPER, wrapper);
		bundle.putString(ItineraryDetailFragment.PARAM_FROM_PLACE, mFromAddressTextView.getText().toString());
		bundle.putString(ItineraryDetailFragment.PARAM_TO_PLACE, mToAddressTextView.getText().toString());

		final Intent intent = new Intent(getActivity(), ItineraryDetailActivity.class);
		intent.putExtra(ItineraryDetailActivity.PARAM_BUNDLE, bundle);
		getActivity().startActivity(intent);
	}

	@Override
	protected void bindView(final View view, final Bundle savedInstanceState) {
		final ListView listView = (ListView) view.findViewById(android.R.id.list);
		mProgressView = view.findViewById(android.R.id.progress);

		mAdapter = new ItineraryWrapperArrayAdapter(getActivity(), mItineraryWrappers);
		final SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(mAdapter);
		swingBottomInAnimationAdapter.setListView(listView);

		listView.setAdapter(swingBottomInAnimationAdapter);

		mIconFrom = (ImageView) view.findViewById(R.id.formIconFrom);
		mIconTo = (ImageView) view.findViewById(R.id.formIconTo);

		mFromAddressTextView = (TextView) view.findViewById(R.id.formFrom);
		mFromAddressTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				requestFromAddress();
			}
		});

		mToAddressTextView = (TextView) view.findViewById(R.id.formTo);
		mToAddressTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				requestToAddress();
			}
		});

		mDateAndTimeLabel = (TextView) view.findViewById(R.id.dateAndTimeLabel);
		mDateAndTimeLabel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				showDatePicker();
			}
		});

		mDateKindSpinner = (Spinner) view.findViewById(R.id.dateKind);
		mDateKindSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
				mArriveBy = position == 1;
				onFormValueChange();
			}

			@Override
			public void onNothingSelected(final AdapterView<?> parent) {

			}
		});

		view.findViewById(R.id.formReverse).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				reverse();
			}
		});

		final Location currentLocation = NBApplication.getLocationProvider().getLastKnownLocation();
		if (currentLocation != null && currentLocation.getLatitude() != 0 && currentLocation.getLongitude() != 0) {
			mFromLocation.set(currentLocation);
			mFromAddressTextView.setText(R.string.itineraire_current_location);
		}

		setDateValue();
	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (data != null) {
			final String address = data.getStringExtra("address");
			final double latitude = data.getDoubleExtra("latitude", 0d);
			final double longitude = data.getDoubleExtra("longitude", 0d);
			final Equipement.Type type = (Type) data.getSerializableExtra("type");

			if (requestCode == REQUEST_CODE_FROM) {
				mFromLocation.setLatitude(latitude);
				mFromLocation.setLongitude(longitude);
				mFromAddressTextView.setText(address);
				if (type == null) {
					mIconFrom.setImageResource(R.drawable.ic_directions_form_destination_notselected);
				} else {
					mIconFrom.setImageResource(type.getMapPin());
				}
			} else {
				mToLocation.setLatitude(latitude);
				mToLocation.setLongitude(longitude);
				mToAddressTextView.setText(address);
				if (type == null) {
					mIconTo.setImageResource(R.drawable.ic_directions_form_destination_notselected);
				} else {
					mIconTo.setImageResource(type.getMapPin());
				}
			}
		}

		onFormValueChange();

	}

	private void requestFromAddress() {
		requestAddress(REQUEST_CODE_FROM, mFromAddressTextView.getText());
	}

	private void requestToAddress() {
		requestAddress(REQUEST_CODE_TO, mToAddressTextView.getText());
	}

	private void requestAddress(final int requestCode, final CharSequence query) {
		final Intent intent = new Intent(getActivity(), AddressSearchActivity.class);
		if (query != getString(R.string.itineraire_current_location)) {
			intent.putExtra(AddressSearchFragment.PARAM_QUERY, query);
		}

		startActivityForResult(intent, requestCode);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			getActivity().overridePendingTransition(R.anim.slide_in_from_right, R.anim.half_fade_out);
	}

	private void showDatePicker() {
		final DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, mDateTime.getYear(),
				mDateTime.getMonthOfYear() - 1, mDateTime.getDayOfMonth());
		dialog.show();
	}

	private void showTimePicker() {
		final TimePickerDialog dialog = new TimePickerDialog(getActivity(), this, mDateTime.getHourOfDay(),
				mDateTime.getMinuteOfHour(), true);
		dialog.show();
	}

	private void reverse() {
		final CharSequence fromText = mFromAddressTextView.getText();
		final CharSequence toText = mToAddressTextView.getText();
		final Location temp = new Location(LocationManager.GPS_PROVIDER);

		mToAddressTextView.setText(fromText);
		mFromAddressTextView.setText(toText);

		temp.set(mFromLocation);
		mFromLocation.set(mToLocation);
		mToLocation.set(temp);

		onFormValueChange();
	}

	@Override
	public void onDateSet(final DatePicker view, final int year, final int monthOfYear, final int dayOfMonth) {
		mDateTime.setYear(year);
		mDateTime.setMonthOfYear(monthOfYear + 1);
		mDateTime.setDayOfMonth(dayOfMonth);

		if (!mDialogLock) {
			showTimePicker();
			mDialogLock = true;
		}
	}

	@Override
	public void onTimeSet(final TimePicker view, final int hourOfDay, final int minute) {
		mDateTime.setHourOfDay(hourOfDay);
		mDateTime.setMinuteOfHour(minute);

		mDateAndTimeLabel.setText(mDateFormat.format(mDateTime.toDate()));

		onFormValueChange();
		mDialogLock = false;
	}

	private void onFormValueChange() {
		final boolean hasLocations = mFromLocation.getLatitude() != 0 && mToLocation.getLatitude() != 0;
		if (hasLocations) {
			showProgress();
			sendRequest();
		}
	}

	private void setDateValue() {
		mDateAndTimeLabel.setText(mDateFormat.format(mDateTime.toDate()));
	}

	private void sendRequest() {
		final Bundle bundle = new Bundle();
		bundle.putDouble(ItineraryLoader.PARAM_FROM_LATITUDE, mFromLocation.getLatitude());
		bundle.putDouble(ItineraryLoader.PARAM_FROM_LONGITUDE, mFromLocation.getLongitude());
		bundle.putDouble(ItineraryLoader.PARAM_TO_LATITUDE, mToLocation.getLatitude());
		bundle.putDouble(ItineraryLoader.PARAM_TO_LONGITUDE, mToLocation.getLongitude());
		bundle.putLong(ItineraryLoader.PARAM_TIME, mDateTime.getMillis());
		bundle.putBoolean(ItineraryLoader.PARAM_ARRIVE_BY, mArriveBy);

		getLoaderManager().restartLoader(0, bundle, this);
	}

	private void showProgress() {
		mProgressView.setVisibility(View.VISIBLE);
		mAdapter.clear();
	}

	private void hideProgress() {
		mProgressView.setVisibility(View.GONE);
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
			if (result.getResult() == null) {
				mItineraryWrappers.add(new ItineraryWrapper(null));
			} else {
				mItineraryWrappers.addAll(result.getResult());
			}

			mAdapter.notifyDataSetChanged();
			getListView().smoothScrollToPosition(1);
		}

	}

	@Override
	public void onLoaderReset(final Loader<AsyncResult<List<ItineraryWrapper>>> arg0) {

	}

}
