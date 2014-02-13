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
import net.naonedbus.provider.impl.NaoLocationManager;
import android.location.Address;
import android.os.AsyncTask;

/**
 * Classe de chargement de l'adresse postale courante.
 */
public class AddressResolverTask extends AsyncTask<Void, Void, Address> {

	public static interface AddressTaskListener {
		void onAddressTaskPreExecute();

		void onAddressTaskResult(Address address);
	}

	private AddressTaskListener mAddressTaskListener;
	private NaoLocationManager mLocationProvider;

	public AddressResolverTask(final AddressTaskListener listener) {
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
	protected Address doInBackground(final Void... params) {
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		Address result = null;
		if (mLocationProvider != null) {
			result = mLocationProvider.getLastKnownAddress();
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
	protected void onPostExecute(final Address result) {
		if (!isCancelled() && mAddressTaskListener != null) {
			mAddressTaskListener.onAddressTaskResult(result);
		}
	}
}
