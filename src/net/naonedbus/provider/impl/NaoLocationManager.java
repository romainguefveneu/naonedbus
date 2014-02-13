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
package net.naonedbus.provider.impl;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import net.naonedbus.BuildConfig;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

public class NaoLocationManager implements GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

	private static final String LOG_TAG = "NaoLocationManager";
	private static final boolean DBG = BuildConfig.DEBUG;
	private static final int TIMEOUT = 10000;

	public static interface NaoLocationListener {
		void onConnecting();

		void onLocationChanged(Location location);

		void onDisconnected();

		void onLocationTimeout();
	}

	/** New Google API. */
	private LocationClient mLocationClient;
	/** Android API, to know if location service is enabled. */
	private LocationManager mLocationManager;
	/** Geo coder. */
	private final Geocoder mGeoCoder;
	/** Location listeners. */
	private Set<NaoLocationListener> mListenerList;
	/** Current service status. */
	private boolean mServiceEnabled;
	/** Last location. */
	private Location mLastLocation;
	/** Timeout handler. */
	private Handler mHandler;

	private Runnable mTimeOutRunnable = new Runnable() {
		@Override
		public void run() {
			if (mLastLocation == null) {
				dispatchOnLocationTimeout();
			}
		}
	};

	public NaoLocationManager(final Context context) {
		mLocationClient = new LocationClient(context, this, this);
		mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		mListenerList = new HashSet<NaoLocationListener>();
		mGeoCoder = new Geocoder(context, new Locale("fr", "FR"));
		mHandler = new Handler();
	}

	public void addListener(final NaoLocationListener locationListener) {
		if (DBG)
			Log.d(LOG_TAG, "addListener " + locationListener.getClass().getSimpleName());

		mListenerList.add(locationListener);

		if (isEnabled()) {
			if (!mLocationClient.isConnected() && !mLocationClient.isConnecting()) {
				connect();
			} else {
				Location lastLocation = getLastLocation();
				if (lastLocation != null) {
					locationListener.onLocationChanged(lastLocation);
				}
			}
		} else {
			locationListener.onDisconnected();
		}
	}

	public void removeListener(final NaoLocationListener locationListener) {
		if (DBG)
			Log.d(LOG_TAG, "removeListener " + locationListener.getClass().getSimpleName());

		mListenerList.remove(locationListener);

		if (mListenerList.isEmpty()) {
			disconnect();
		}
	}

	public void onResume() {
		mHandler.removeCallbacks(mTimeOutRunnable);

		boolean isEnabled = isEnabled();
		if (mServiceEnabled != isEnabled) {
			if (isEnabled) {
				if (!mLocationClient.isConnecting()) {
					mLocationClient.connect();
					dispatchOnConnecting();
				}
			} else {
				dispatchOnDisconnect();
			}
			mServiceEnabled = isEnabled;
		}
	}

	public void connect() {
		if (isEnabled()) {
			if (!mLocationClient.isConnecting()) {
				mLocationClient.connect();
				dispatchOnConnecting();
				mServiceEnabled = true;
			}
		} else {
			mServiceEnabled = false;
		}
	}

	public void disconnect() {
		mLocationClient.disconnect();
	}

	public boolean isEnabled() {
		return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
				|| mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	}

	public Location getLastLocation() {
		if (mLocationClient.isConnected()) {
			final Location lastLocation = mLocationClient.getLastLocation();
			return (lastLocation == null) ? mLastLocation : lastLocation;
		} else {
			return mLastLocation;
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		if (DBG)
			Log.d(LOG_TAG, "onConnected");

		final LocationRequest request = new LocationRequest();
		request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
		request.setSmallestDisplacement(20f);
		request.setFastestInterval(4000);
		request.setInterval(15000);

		mLocationClient.requestLocationUpdates(request, this);

		Location lastLocation = mLocationClient.getLastLocation();
		onLocationChanged(lastLocation);

		mHandler.postDelayed(mTimeOutRunnable, TIMEOUT);
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (DBG)
			Log.e(LOG_TAG, "onConnectionFailed " + result.getErrorCode());
		dispatchOnDisconnect();
	}

	@Override
	public void onLocationChanged(Location location) {
		if (DBG)
			Log.d(LOG_TAG, "onLocationChanged " + location);
		if (location != null) {
			dispatchOnLocationChanged(location);
			mLastLocation = location;
		}
	}

	@Override
	public void onDisconnected() {
		dispatchOnDisconnect();
	}

	private void dispatchOnLocationChanged(Location location) {
		for (NaoLocationListener l : mListenerList) {
			l.onLocationChanged(location);
		}
	}

	private void dispatchOnDisconnect() {
		for (NaoLocationListener l : mListenerList) {
			l.onDisconnected();
		}
	}

	private void dispatchOnConnecting() {
		for (NaoLocationListener l : mListenerList) {
			l.onConnecting();
		}
	}

	private void dispatchOnLocationTimeout() {
		for (NaoLocationListener l : mListenerList) {
			l.onLocationTimeout();
		}
	}

	/**
	 * Récupérer l'adresse de la dernière position courante
	 */
	public Address getLastKnownAddress() {
		if (DBG)
			Log.d(LOG_TAG, "getLastKnownAddress");

		final Location lastKnowLocation = getLastLocation();
		Address result = null;
		try {
			if (lastKnowLocation != null) {
				final List<Address> addresses = mGeoCoder.getFromLocation(lastKnowLocation.getLatitude(),
						lastKnowLocation.getLongitude(), 1);
				if (addresses != null && addresses.size() > 0) {
					result = addresses.get(0);
				}
			}
		} catch (final IOException e) {
			if (DBG)
				Log.w(LOG_TAG, "Erreur lors de la récupération de l'adresse.", e);
		}
		return result;
	}

}
