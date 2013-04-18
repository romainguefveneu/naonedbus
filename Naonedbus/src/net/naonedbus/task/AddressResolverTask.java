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
