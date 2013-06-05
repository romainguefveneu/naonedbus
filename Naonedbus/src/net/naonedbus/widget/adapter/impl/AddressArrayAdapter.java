package net.naonedbus.widget.adapter.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.naonedbus.R;
import net.naonedbus.utils.FormatUtils;
import net.naonedbus.widget.adapter.impl.AddressArrayAdapter.AddressWrapper;
import android.content.Context;
import android.content.res.ColorStateList;
import android.location.Address;
import android.location.Geocoder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

public class AddressArrayAdapter extends ArrayAdapter<AddressWrapper> {

	private final static double LOWER_LEFT_LATITUDE = 47.081d;
	private final static double LOWER_LEFT_LONGITUDE = -1.843d;
	private final static double UPPER_RIGHT_LATITUDE = 47.346d;
	private final static double UPPER_RIGHT_LONGITUDE = -1.214d;

	private final StringBuilder mSb = new StringBuilder();
	private final Geocoder mGeocoder;
	private final AddressWrapper mCurrenPositionWrapper;
	private final ColorStateList mHoloBlueColor;
	private final ColorStateList mTextColorColor;
	private final int mPadding;

	public AddressArrayAdapter(final Context context) {
		super(context, android.R.layout.simple_dropdown_item_1line);
		mGeocoder = new Geocoder(context);
		mCurrenPositionWrapper = AddressWrapper.createLocateMe(context.getString(R.string.itineraire_current_location));
		mHoloBlueColor = context.getResources().getColorStateList(R.color.card_selectable_text);
		mTextColorColor = context.getResources().getColorStateList(android.R.color.primary_text_light);
		mPadding = context.getResources().getDimensionPixelSize(R.dimen.padding_small);

		add(mCurrenPositionWrapper);
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		final TextView view = (TextView) super.getView(position, convertView, parent);

		final AddressWrapper item = getItem(position);
		view.setText(item.getTitle());

		if (item.isLocateMe()) {
			view.setTextColor(mHoloBlueColor);
			view.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_locate, 0, 0, 0);
			view.setCompoundDrawablePadding(mPadding);
		} else {
			view.setTextColor(mTextColorColor);
			view.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		}

		return view;
	}

	@Override
	public Filter getFilter() {
		final Filter myFilter = new Filter() {
			@Override
			protected FilterResults performFiltering(final CharSequence constraint) {

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

				add(mCurrenPositionWrapper);

				for (final Address address : (List<Address>) results.values) {
					add(AddressWrapper.createAddressWrapper(address, FormatUtils.formatAddress(address, mSb)));
				}

				if (results.count > 0) {
					notifyDataSetChanged();
				} else {
					notifyDataSetInvalidated();
				}
			}

			@Override
			public CharSequence convertResultToString(final Object resultValue) {
				return resultValue == null ? "" : ((AddressWrapper) resultValue).title;
			}
		};
		return myFilter;
	}

	public static class AddressWrapper {
		private boolean locateMe;
		private String title;
		private Address address;

		private AddressWrapper() {
		}

		public static AddressWrapper createAddressWrapper(final Address address, final String title) {
			final AddressWrapper wrapper = new AddressWrapper();
			wrapper.title = title;
			wrapper.address = address;
			wrapper.locateMe = false;
			return wrapper;
		}

		public static AddressWrapper createLocateMe(final String title) {
			final AddressWrapper wrapper = new AddressWrapper();
			wrapper.title = title;
			wrapper.locateMe = true;
			return wrapper;
		}

		public boolean isLocateMe() {
			return locateMe;
		}

		public String getTitle() {
			return title;
		}

		public Address getAddress() {
			return address;
		}

	}

}
