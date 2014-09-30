package net.naonedbus.loader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import net.naonedbus.BuildConfig;
import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.bean.AddressResult;
import net.naonedbus.bean.Equipement;
import net.naonedbus.bean.Equipement.Type;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.manager.impl.EquipementManager;
import net.naonedbus.rest.controller.impl.PlacesController;
import net.naonedbus.utils.FormatUtils;

import org.apache.http.HttpException;
import org.json.JSONException;

import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.text.TextUtils;
import android.util.Log;

public class PlacesLoader extends AsyncTaskLoader<AsyncResult<List<AddressResult>>> {

	private static final String TAG = "PlacesLoader";
	private static final boolean DBG = BuildConfig.DEBUG;

	private static final String KEYWORD = "keyword";
	private static final String LOAD_ADDRESS = "loadAddress";

	public static final Bundle create(final String keyword, boolean loadAddress) {
		final Bundle bundle = new Bundle();
		bundle.putString(KEYWORD, keyword);
		bundle.putBoolean(LOAD_ADDRESS, loadAddress);

		return bundle;
	}

	private final EquipementManager mEquipementManager;
	private final String mKeyword;
	private final boolean mLoadAddress;

	private AsyncResult<List<AddressResult>> mResult;

	public PlacesLoader(final Context context, final Bundle bundle) {
		super(context);

		mKeyword = bundle.getString(KEYWORD);
		mLoadAddress = bundle.getBoolean(LOAD_ADDRESS);
		mEquipementManager = EquipementManager.getInstance();
	}

	@Override
	public AsyncResult<List<AddressResult>> loadInBackground() {
		if (DBG)
			Log.d(TAG, "loadInBackground " + mKeyword);

		final AsyncResult<List<AddressResult>> result = new AsyncResult<List<AddressResult>>();
		final List<AddressResult> addressResults = new ArrayList<AddressResult>();

		addCurrentLocation(addressResults);

		if (!TextUtils.isEmpty(mKeyword)) {
			addEquipements(mKeyword, addressResults);
			if (mLoadAddress) {
				addAddresses(mKeyword, addressResults);
			}
		}

		result.setResult(addressResults);
		return result;
	}

	private void addCurrentLocation(final List<AddressResult> result) {
		final Location currentLocation = NBApplication.getLocationProvider().getLastLocation();
		final Address lastKnownAddress = NBApplication.getLocationProvider().getLastKnownAddress();

		if (currentLocation != null && lastKnownAddress != null && currentLocation.getLatitude() != 0
				&& currentLocation.getLongitude() != 0) {
			final String title = FormatUtils.formatAddress(lastKnownAddress, null);
			final int icon = R.drawable.ic_action_locate_selector;

			final AddressResult addressResult = new AddressResult(title, null, null, icon, Color.TRANSPARENT,
					currentLocation.getLatitude(), currentLocation.getLongitude());
			addressResult.setCurrentLocation(true);
			addressResult.setSection(7);

			result.add(addressResult);
		}
	}

	private void addAddresses(String filter, List<AddressResult> result) {
		try {
			final PlacesController placesController = new PlacesController();
			List<AddressResult> addresses = placesController.getPlaces(getContext().getResources(), mKeyword);
			if (addresses != null) {
				result.addAll(addresses);
			}
		} catch (final MalformedURLException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final JSONException e) {
			e.printStackTrace();
		} catch (final HttpException e) {
			e.printStackTrace();
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
			final AddressResult addressResult = new AddressResult(title, description, type, type.getDrawableRes(),
					color, equipement.getLatitude(), equipement.getLongitude());

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
