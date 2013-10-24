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
import net.naonedbus.bean.Equipment;
import net.naonedbus.bean.Equipment.Type;
import net.naonedbus.bean.ItineraryWrapper;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.fragment.AbstractListFragment;
import net.naonedbus.loader.ItineraryLoader;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.widget.adapter.impl.ItineraryWrapperArrayAdapter;

import org.joda.time.MutableDateTime;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.bugsense.trace.BugSenseHandler;
import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;

public class ItineraryFragment extends AbstractListFragment implements
		LoaderCallbacks<AsyncResult<List<ItineraryWrapper>>>, OnDateSetListener, OnTimeSetListener {

	private static final String BUNDLE_LOCATION_FROM = "ItineraireFragment:locationFrom";
	private static final String BUNDLE_LOCATION_TO = "ItineraireFragment:locationTo";
	private static final String BUNDLE_ADDRESS_FROM = "ItineraireFragment:addressFrom";
	private static final String BUNDLE_ADDRESS_TO = "ItineraireFragment:addressTo";
	private static final String BUNDLE_DATE_TIME = "ItineraireFragment:dateTime";
	private static final String BUNDLE_ARRIVE_BY = "ItineraireFragment:arriveBy";
	private static final String BUNDLE_ICON_FROM = "ItineraireFragment:iconFrom";
	private static final String BUNDLE_ICON_TO = "ItineraireFragment:iconTo";
	private static final String BUNDLE_COLOR_FROM = "ItineraireFragment:colorFrom";
	private static final String BUNDLE_COLOR_TO = "ItineraireFragment:colorTo";
	private static final String BUNDLE_RESULTS = "ItineraireFragment:results";

	private static final int REQUEST_CODE_FROM = 1;
	private static final int REQUEST_CODE_TO = 2;

	private final List<ItineraryWrapper> mItineraryWrappers = new ArrayList<ItineraryWrapper>();
	private ItineraryWrapperArrayAdapter mAdapter;
	private SwingBottomInAnimationAdapter mAnimationAdapter;
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
	private View mReverseButton;

	private ImageView mIconFrom;
	private ImageView mIconTo;
	private int mIconFromColor = Color.TRANSPARENT;
	private int mIconToColor = Color.TRANSPARENT;
	private int mIconFromResId = R.drawable.ic_directions_form_destination_notselected;
	private int mIconToResId = R.drawable.ic_directions_form_destination_notselected;

	private int mIconPadding;

	private boolean mDialogLock;

	public ItineraryFragment() {
		super(R.layout.fragment_itinerary);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
		mIconPadding = getResources().getDimensionPixelSize(R.dimen.itinerary_icon_padding);

		if (savedInstanceState != null) {
			mFromLocation.set((Location) savedInstanceState.getParcelable(BUNDLE_LOCATION_FROM));
			mToLocation.set((Location) savedInstanceState.getParcelable(BUNDLE_LOCATION_TO));

			mDateTime.setMillis(savedInstanceState.getLong(BUNDLE_DATE_TIME));
			mArriveBy = savedInstanceState.getBoolean(BUNDLE_ARRIVE_BY);
			mIconFromResId = savedInstanceState.getInt(BUNDLE_ICON_FROM);
			mIconToResId = savedInstanceState.getInt(BUNDLE_ICON_TO);
			mIconFromColor = savedInstanceState.getInt(BUNDLE_COLOR_FROM);
			mIconToColor = savedInstanceState.getInt(BUNDLE_COLOR_TO);

			final List<Parcelable> results = savedInstanceState.getParcelableArrayList(BUNDLE_RESULTS);
			for (final Parcelable parcelable : results) {
				mItineraryWrappers.add((ItineraryWrapper) parcelable);
			}

		}
	};

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		outState.putParcelable(BUNDLE_LOCATION_FROM, mFromLocation);
		outState.putParcelable(BUNDLE_LOCATION_TO, mToLocation);
		outState.putString(BUNDLE_ADDRESS_FROM, mFromAddressTextView.getText().toString());
		outState.putString(BUNDLE_ADDRESS_TO, mToAddressTextView.getText().toString());
		outState.putLong(BUNDLE_DATE_TIME, mDateTime.getMillis());
		outState.putBoolean(BUNDLE_ARRIVE_BY, mArriveBy);
		outState.putInt(BUNDLE_ICON_FROM, mIconFromResId);
		outState.putInt(BUNDLE_ICON_TO, mIconToResId);
		outState.putInt(BUNDLE_COLOR_FROM, mIconFromColor);
		outState.putInt(BUNDLE_COLOR_TO, mIconToColor);
		outState.putParcelableArrayList(BUNDLE_RESULTS, new ArrayList<ItineraryWrapper>(mItineraryWrappers));
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
		super.onListItemClick(l, v, position, id);

		final Bundle bundle = new Bundle();
		bundle.putString(ItineraryDetailFragment.PARAM_ITINERARY_FROM, mFromAddressTextView.getText().toString());
		bundle.putString(ItineraryDetailFragment.PARAM_ITINERARY_TO, mToAddressTextView.getText().toString());
		bundle.putParcelable(ItineraryDetailFragment.PARAM_ITINERARY_WRAPPER, mItineraryWrappers.get(position));

		final DialogFragment dialogFragment = new ItineraryDetailDialogFragment();
		dialogFragment.setArguments(bundle);
		dialogFragment.show(getChildFragmentManager(), "ItineraryDetails");
	}

	@Override
	protected void bindView(final View view, final Bundle savedInstanceState) {
		final ListView listView = (ListView) view.findViewById(android.R.id.list);
		mProgressView = view.findViewById(android.R.id.progress);

		mAdapter = new ItineraryWrapperArrayAdapter(getActivity(), mItineraryWrappers);
		mAnimationAdapter = new SwingBottomInAnimationAdapter(mAdapter);
		mAnimationAdapter.setListView(listView);
		listView.setAdapter(mAnimationAdapter);

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
				final boolean arriveBy = position == 1;
				if (arriveBy != mArriveBy) {
					mArriveBy = arriveBy;
					onFormValueChange();
				}
			}

			@Override
			public void onNothingSelected(final AdapterView<?> parent) {

			}
		});

		mReverseButton = view.findViewById(R.id.formReverse);
		mReverseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				reverse();
			}
		});

		if (savedInstanceState != null) {
			notifyIconsChanged();
		} else {
			final Location currentLocation = NBApplication.getLocationProvider().getLastKnownLocation();
			if (currentLocation != null && currentLocation.getLatitude() != 0 && currentLocation.getLongitude() != 0) {
				mFromLocation.set(currentLocation);
				mFromAddressTextView.setText(R.string.my_location);

				mIconFromResId = R.drawable.ic_action_locate_blue;

				notifyIconsChanged();
			}
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
			final Equipment.Type type = (Type) data.getSerializableExtra("type");
			final boolean currentLocation = data.getBooleanExtra("isCurrentLocation", false);

			if (requestCode == REQUEST_CODE_FROM) {
				mFromLocation.setLatitude(latitude);
				mFromLocation.setLongitude(longitude);
				mFromAddressTextView.setText(address);
				if (type == null) {
					mIconFromResId = currentLocation ? R.drawable.ic_action_locate_blue
							: R.drawable.ic_directions_form_destination_notselected;
					mIconFromColor = Color.TRANSPARENT;
				} else {
					mIconFromResId = type.getDrawableRes();
					mIconFromColor = getActivity().getResources().getColor(type.getBackgroundColorRes());
				}
			} else {
				mToLocation.setLatitude(latitude);
				mToLocation.setLongitude(longitude);
				mToAddressTextView.setText(address);
				if (type == null) {
					mIconFromResId = currentLocation ? R.drawable.ic_action_locate_blue
							: R.drawable.ic_directions_form_destination_notselected;
					mIconToColor = Color.TRANSPARENT;
				} else {
					mIconToResId = type.getDrawableRes();
					mIconToColor = getActivity().getResources().getColor(type.getBackgroundColorRes());
				}
			}

			notifyIconsChanged();
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
		if (query != getString(R.string.my_location)) {
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

		int t = mIconToResId;
		mIconToResId = mIconFromResId;
		mIconFromResId = t;

		t = mIconFromColor;
		mIconFromColor = mIconToColor;
		mIconToColor = t;

		notifyIconsChanged();
		onFormValueChange();

		final Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.rotation_full);
		mReverseButton.startAnimation(animation);
	}

	private void notifyIconsChanged() {
		setIcon(mIconFrom, mIconFromResId, mIconFromColor);
		setIcon(mIconTo, mIconToResId, mIconToColor);
	}

	private void setIcon(final ImageView imageView, final int icoRes, final int color) {
		imageView.setImageResource(icoRes);
		if (color == Color.TRANSPARENT) {
			ColorUtils.setBackground(imageView, null);
			imageView.setPadding(0, 0, 0, 0);
		} else {
			ColorUtils.setBackgroundGradiant(imageView, color);
			imageView.setPadding(mIconPadding, mIconPadding, mIconPadding, mIconPadding);
		}
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
		mAnimationAdapter.reset();
		if (mFromLocation.bearingTo(mToLocation) == 0.0f) {
			hideProgress();
			mItineraryWrappers.add(ItineraryWrapper.getUnicornItinerary());
			mAdapter.notifyDataSetChanged();
		} else {
			final Bundle bundle = new Bundle();
			bundle.putDouble(ItineraryLoader.PARAM_FROM_LATITUDE, mFromLocation.getLatitude());
			bundle.putDouble(ItineraryLoader.PARAM_FROM_LONGITUDE, mFromLocation.getLongitude());
			bundle.putDouble(ItineraryLoader.PARAM_TO_LATITUDE, mToLocation.getLatitude());
			bundle.putDouble(ItineraryLoader.PARAM_TO_LONGITUDE, mToLocation.getLongitude());
			bundle.putLong(ItineraryLoader.PARAM_TIME, mDateTime.getMillis());
			bundle.putBoolean(ItineraryLoader.PARAM_ARRIVE_BY, mArriveBy);

			getLoaderManager().restartLoader(0, bundle, this);
		}
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
				mItineraryWrappers.add(ItineraryWrapper.getEmptyItinerary());
			} else {
				mItineraryWrappers.addAll(result.getResult());
			}
		} else {
			mItineraryWrappers.add(ItineraryWrapper.getErrorItinerary());
			BugSenseHandler.sendExceptionMessage("Erreur de calcul d'itinéraire.", null, result.getException());
		}

		mAdapter.notifyDataSetChanged();
		getListView().smoothScrollToPosition(1);

	}

	@Override
	public void onLoaderReset(final Loader<AsyncResult<List<ItineraryWrapper>>> arg0) {

	}

}
