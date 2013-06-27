package net.naonedbus.loader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.bean.AddressResult;
import net.naonedbus.bean.Equipement;
import net.naonedbus.bean.Equipement.Type;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.manager.impl.EquipementManager;
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

	private final static double LOWER_LEFT_LATITUDE = 47.081d;
	private final static double LOWER_LEFT_LONGITUDE = -1.843d;
	private final static double UPPER_RIGHT_LATITUDE = 47.346d;
	private final static double UPPER_RIGHT_LONGITUDE = -1.214d;

	private final Geocoder mGeocoder;
	private final EquipementManager mEquipementManager;
	private final Bundle mBundle;
	private AsyncResult<List<AddressResult>> mResult;

	public AddressLoader(final Context context, final Bundle bundle) {
		super(context);
		mBundle = bundle;
		mGeocoder = new Geocoder(context);
		mEquipementManager = EquipementManager.getInstance();
	}

	@Override
	public AsyncResult<List<AddressResult>> loadInBackground() {
		final String filter = mBundle == null ? null : mBundle.getString(PARAM_FILTER);
		final AsyncResult<List<AddressResult>> result = new AsyncResult<List<AddressResult>>();
		final List<AddressResult> addressResults = new ArrayList<AddressResult>();

		addCurrentLocation(addressResults);
		if (!TextUtils.isEmpty(filter)) {
			addAddresses(filter, addressResults);
			addEquipements(filter, addressResults);
		}

		result.setResult(addressResults);
		return result;
	}

	private void addCurrentLocation(final List<AddressResult> result) {
		final Location currentLocation = NBApplication.getLocationProvider().getLastKnownLocation();

		if (currentLocation != null && currentLocation.getLatitude() != 0 && currentLocation.getLongitude() != 0) {
			final String title = getContext().getString(R.string.itineraire_current_location);
			final int icon = R.drawable.ic_action_locate_selector;

			final AddressResult addressResult = new AddressResult(title, null, icon, Color.TRANSPARENT,
					currentLocation.getLatitude(), currentLocation.getLongitude());
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
				final AddressResult addressResult = new AddressResult(title[0], title[1],
						R.drawable.ic_action_location_selector, Color.TRANSPARENT, address.getLatitude(),
						address.getLongitude());

				addressResult.setAddress(title[0] + ", " + title[1]);
				addressResult.setSection(0);
				result.add(addressResult);
			}

		} catch (final IOException e) {
		}
	}

	private void addEquipements(final String filter, final List<AddressResult> result) {
		final List<Equipement> equipements = mEquipementManager.getEquipementsByName(getContext().getContentResolver(),
				null, filter);

		for (final Equipement equipement : equipements) {
			final Type type = equipement.getType();
			final String title = equipement.getNom();
			final String description = equipement.getAdresse();
			final int color = getContext().getResources().getColor(type.getBackgroundColorRes());
			final AddressResult addressResult = new AddressResult(title, description, type.getDrawableRes(), color,
					equipement.getLatitude(), equipement.getLongitude());

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
