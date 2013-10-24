package net.naonedbus.loader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.bean.AddressResult;
import net.naonedbus.bean.Equipment;
import net.naonedbus.bean.Equipment.Type;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.manager.impl.EquipmentManager;
import net.naonedbus.utils.FormatUtils;
import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.text.TextUtils;

public class AddressLoader extends AsyncTaskLoader<AsyncResult<List<AddressResult>>> {

	public final static String PARAM_FILTER = "filter";
	public final static String PARAM_LOAD_ADDRESS = "loadAddress";

	private final static double LOWER_LEFT_LATITUDE = 47.081d;
	private final static double LOWER_LEFT_LONGITUDE = -1.843d;
	private final static double UPPER_RIGHT_LATITUDE = 47.346d;
	private final static double UPPER_RIGHT_LONGITUDE = -1.214d;

	private final Geocoder mGeocoder;
	private final EquipmentManager mEquipementManager;
	private final Bundle mBundle;
	private AsyncResult<List<AddressResult>> mResult;

	public AddressLoader(final Context context, final Bundle bundle) {
		super(context);
		mBundle = bundle;
		mGeocoder = new Geocoder(context);
		mEquipementManager = EquipmentManager.getInstance();
	}

	@Override
	public AsyncResult<List<AddressResult>> loadInBackground() {

		final String filter = mBundle == null ? null : mBundle.getString(PARAM_FILTER);
		final boolean loadAddress = mBundle == null ? false : mBundle.getBoolean(PARAM_LOAD_ADDRESS, false);

		final AsyncResult<List<AddressResult>> result = new AsyncResult<List<AddressResult>>();
		final List<AddressResult> addressResults = new ArrayList<AddressResult>();

		addCurrentLocation(addressResults);

		if (!TextUtils.isEmpty(filter)) {
			if (loadAddress) {
				addAddresses(filter, addressResults);
			}
			addEquipements(filter, addressResults);
		}

		result.setResult(addressResults);
		return result;
	}

	private void addCurrentLocation(final List<AddressResult> result) {
		final Location currentLocation = NBApplication.getLocationProvider().getLastKnownLocation();

		if (currentLocation != null && currentLocation.getLatitude() != 0 && currentLocation.getLongitude() != 0) {
			final String title = getContext().getString(R.string.my_location);
			final int icon = R.drawable.ic_action_locate_selector;

			final AddressResult addressResult = new AddressResult(title, null, null, icon, Color.TRANSPARENT,
					currentLocation.getLatitude(), currentLocation.getLongitude());
			addressResult.setCurrentLocation(true);
			addressResult.setSection(0);

			result.add(addressResult);
		}
	}

	private void addAddresses(final String filter, final List<AddressResult> result) {
		try {
			final List<Address> addresses = mGeocoder.getFromLocationName(filter, 5, LOWER_LEFT_LATITUDE,
					LOWER_LEFT_LONGITUDE, UPPER_RIGHT_LATITUDE, UPPER_RIGHT_LONGITUDE);

			for (final Address address : addresses) {
				final String[] title = FormatUtils.formatAddressTwoLine(address);
				final AddressResult addressResult = new AddressResult(title[0], title[1], null,
						R.drawable.ic_action_location_selector, Color.TRANSPARENT, address.getLatitude(),
						address.getLongitude());

				String addressString = title[0];
				if (!TextUtils.isEmpty(title[1])) {
					addressString += ", " + title[1];
				}

				addressResult.setAddress(addressString);
				addressResult.setSection(0);
				result.add(addressResult);
			}

		} catch (final IOException e) {
		}
	}

	private void addEquipements(final String filter, final List<AddressResult> result) {
		final List<Equipment> equipements = mEquipementManager.getByName(getContext().getContentResolver(),
				null, filter);

		for (final Equipment equipment : equipements) {
			final Type type = equipment.getType();
			final String title = equipment.getName();
			final String description = equipment.getAddress();
			final int color = getContext().getResources().getColor(type.getBackgroundColorRes());
			final AddressResult addressResult = new AddressResult(title, description, type, type.getDrawableRes(),
					color, equipment.getLatitude(), equipment.getLongitude());

			addressResult.setSection(type.getId() + 1);
			result.add(addressResult);
		}
	}

	/**
	 * Called when there is new data to deliver to the client. The super class
	 * will take care of delivering it; the implementation here just adds a
	 * little more logic.
	 */
	@Override
	public void deliverResult(final AsyncResult<List<AddressResult>> result) {
		mResult = result;

		if (isStarted()) {
			// If the Loader is currently started, we can immediately
			// deliver its results.
			try {
				super.deliverResult(result);
			} catch (final NullPointerException e) {

			}
		}
	}

	/**
	 * Handles a request to start the Loader.
	 */
	@Override
	protected void onStartLoading() {
		if (mResult != null) {
			// If we currently have a result available, deliver it
			// immediately.
			deliverResult(mResult);
		}

		if (takeContentChanged() || mResult == null) {
			// If the data has changed since the last time it was loaded
			// or is not currently available, start a load.
			forceLoad();
		}
	}

}
