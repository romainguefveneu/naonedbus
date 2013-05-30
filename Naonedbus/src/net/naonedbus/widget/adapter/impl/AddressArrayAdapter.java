package net.naonedbus.widget.adapter.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

public class AddressArrayAdapter extends ArrayAdapter<Address> {

	private final static double LOWER_LEFT_LATITUDE = 47.081d;
	private final static double LOWER_LEFT_LONGITUDE = -1.843d;
	private final static double UPPER_RIGHT_LATITUDE = 47.346d;
	private final static double UPPER_RIGHT_LONGITUDE = -1.214d;

	private final StringBuilder mSb = new StringBuilder();
	private final Geocoder mGeocoder;

	public AddressArrayAdapter(final Context context) {
		super(context, android.R.layout.simple_dropdown_item_1line);
		mGeocoder = new Geocoder(context);
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		final TextView view = (TextView) super.getView(position, convertView, parent);
		final Address address = getItem(position);
		view.setText(createFormattedAddressFromAddress(address));
		return view;
	}

	private String createFormattedAddressFromAddress(final Address address) {
		mSb.setLength(0);
		final int addressLineSize = address.getMaxAddressLineIndex();
		for (int i = 0; i < addressLineSize; i++) {
			mSb.append(address.getAddressLine(i));
			if (i != addressLineSize - 1) {
				mSb.append(", ");
			}
		}
		return mSb.toString();
	}

	@Override
	public Filter getFilter() {
		final Filter myFilter = new Filter() {
			@Override
			protected FilterResults performFiltering(final CharSequence constraint) {

				Log.d("AddressArrayAdapter", "performFiltering " + constraint);

				List<Address> addressList = null;
				if (constraint != null) {
					try {
						addressList = mGeocoder.getFromLocationName(constraint.toString(), 5, LOWER_LEFT_LATITUDE,
								LOWER_LEFT_LONGITUDE, UPPER_RIGHT_LATITUDE, UPPER_RIGHT_LONGITUDE);
					} catch (final IOException e) {
					}
				}
				if (addressList == null) {
					addressList = new ArrayList<Address>();
				}

				final FilterResults filterResults = new FilterResults();
				filterResults.values = addressList;
				filterResults.count = addressList.size();

				return filterResults;
			}

			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(final CharSequence contraint, final FilterResults results) {
				clear();
				for (final Address address : (List<Address>) results.values) {
					add(address);
				}
				if (results.count > 0) {
					notifyDataSetChanged();
				} else {
					notifyDataSetInvalidated();
				}
			}

			@Override
			public CharSequence convertResultToString(final Object resultValue) {
				return resultValue == null ? "" : createFormattedAddressFromAddress((Address) resultValue);
			}
		};
		return myFilter;
	}

}
