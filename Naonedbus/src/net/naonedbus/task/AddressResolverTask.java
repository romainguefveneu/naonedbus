package net.naonedbus.task;

import net.naonedbus.NBApplication;
import net.naonedbus.provider.impl.MyLocationProvider;
import android.location.Address;
import android.os.AsyncTask;

/**
 * Classe de chargement de l'adresse postale courante.
 */
public class AddressResolverTask extends AsyncTask<Void, Void, String> {

	public static interface AddressTaskListener {
		void onAddressTaskPreExecute();

		void onAddressTaskResult(String address);
	}

	private AddressTaskListener mAddressTaskListener;
	private MyLocationProvider mLocationProvider;

	public AddressResolverTask(AddressTaskListener listener) {
		mAddressTaskListener = listener;
		mLocationProvider = NBApplication.getLocationProvider();
	}

	@Override
	protected void onPreExecute() {
		if (mAddressTaskListener != null) {
			mAddressTaskListener.onAddressTaskPreExecute();
		}
	}

	@Override
	protected String doInBackground(Void... params) {
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		String result = null;
		if (mLocationProvider != null) {
			final Address address = mLocationProvider.getLastKnownAddress();

			if (address != null) {
				final StringBuilder builder = new StringBuilder();
				if (address.getMaxAddressLineIndex() > 0) {
					builder.append(address.getAddressLine(0));
					builder.append(" - ");
				}
				builder.append(address.getLocality());
				result = builder.toString();
			}
		}
		return result;
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
		mLocationProvider = null;
		mAddressTaskListener = null;
	}

	@Override
	protected void onPostExecute(String result) {
		if (!isCancelled() && mAddressTaskListener != null) {
			mAddressTaskListener.onAddressTaskResult(result);
		}
	}
}
