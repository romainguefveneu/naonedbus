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
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

public class MyLocationProvider implements GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

	private static final String LOG_TAG = "MyLocationProvider";
	private static final boolean DBG = BuildConfig.DEBUG;

	private final Context mContext;
	private final Geocoder mGeoCoder;
	private LocationClient mLocationClient;
	private Location mLastKnownLocation;
	private Set<MyLocationListener> mListenerList;

	private boolean mLegacyMode;

	private LocationManager mLocationManager;
	private Criteria mProviderCriteria;
	private String mBestProvider;

	public static interface MyLocationListener {
		void onLocationConnecting();

		void onLocationChanged(Location location);

		void onLocationDisabled();
	}

	private final android.location.LocationListener mLocationListener = new android.location.LocationListener() {
		private Location lastLocation = new Location(LocationManager.GPS_PROVIDER);

		@Override
		public void onStatusChanged(final String provider, final int status, final Bundle extras) {
			if (DBG)
				Log.d(LOG_TAG, "onStatusChanged : " + provider + " / " + status);

			if (status != LocationProvider.AVAILABLE) {
				for (final MyLocationListener listener : mListenerList) {
					listener.onLocationDisabled();
				}
			}
		}

		@Override
		public void onProviderEnabled(final String provider) {
			if (DBG)
				Log.d(LOG_TAG, "onProviderEnabled : " + provider);
			setBestProvider();
		}

		@Override
		public void onProviderDisabled(final String provider) {
			if (DBG)
				Log.d(LOG_TAG, "onProviderDisabled : " + provider);
			setBestProvider();
		}

		@Override
		public void onLocationChanged(final Location location) {
			if (location != null && lastLocation.distanceTo(location) > 50.0f) {
				lastLocation = location;
				for (final MyLocationListener listener : mListenerList) {
					listener.onLocationChanged(location);
				}
			}
		}
	};

	public MyLocationProvider(final Context context) {
		mContext = context;
		mGeoCoder = new Geocoder(context, new Locale("fr", "FR"));
		mLocationClient = new LocationClient(context, this, this);
		mListenerList = new HashSet<MyLocationProvider.MyLocationListener>();
		mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
	}

	/**
	 * Initialiser le provider
	 */
	private void initialize() {
		mListenerList = new HashSet<MyLocationListener>();

		mProviderCriteria = new Criteria();
		mProviderCriteria.setAccuracy(Criteria.ACCURACY_FINE);

		setBestProvider();
	}

	private void setBestProvider() {
		mBestProvider = mLocationManager.getBestProvider(mProviderCriteria, true);
		if (DBG)
			Log.d(LOG_TAG, "BestProvider : " + mBestProvider);
	}

	/**
	 * Ajout d'un location listener
	 * 
	 * @param locationListener
	 * @throws Exception
	 */
	public void addListener(final MyLocationListener locationListener) {
		if (DBG)
			Log.d(LOG_TAG,
					"addListener " + locationListener.getClass().getSimpleName() + " (total "
							+ (mListenerList.size() + 1) + ")");

		if (!mListenerList.contains(locationListener)) {
			mListenerList.add(locationListener);
		}

		if (!mLocationClient.isConnected() && !mLocationClient.isConnecting()) {
			start();
		}
	}

	/**
	 * Enlever un listener de la liste
	 * 
	 * @param locationListener
	 * @throws Exception
	 */
	public void removeListener(final MyLocationListener locationListener) {
		if (mListenerList.contains(locationListener)) {
			mListenerList.remove(locationListener);
		}

		if (mListenerList.isEmpty()) {
			stop();
		}

		if (DBG)
			Log.d(LOG_TAG, "removeListener " + locationListener.getClass().getSimpleName() + " (remaining "
					+ mListenerList.size() + ")");
	}

	public boolean containsListener(final MyLocationListener locationListener) {
		return mListenerList.contains(locationListener);
	}

	/**
	 * Démarrer l'écoute
	 */
	private void start() {
		if (!isLocationEnabled()) {

			if (DBG)
				Log.e(LOG_TAG, "Location disabled.");

			for (MyLocationListener l : mListenerList) {
				l.onLocationDisabled();
			}
		} else {

			if (DBG)
				Log.d(LOG_TAG, "Connecting...");

			mLocationClient.connect();

			for (final MyLocationListener listener : mListenerList) {
				listener.onLocationConnecting();
			}
		}
	}

	/**
	 * Arrêter l'écoute
	 */
	private void stop() {
		if (mLegacyMode) {
			mLocationManager.removeUpdates(mLocationListener);
		} else {
			mLocationClient.disconnect();
		}
	}

	private boolean isLocationEnabled() {
		return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
				|| mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	}

	@Override
	public void onConnectionFailed(final ConnectionResult result) {
		if (DBG)
			Log.e(LOG_TAG, "onConnectionFailed " + result.getErrorCode());

		mLegacyMode = true;
		initialize();
	}

	@Override
	public void onConnected(final Bundle bundle) {
		if (DBG)
			Log.d(LOG_TAG, "onConnected");

		final LocationRequest request = new LocationRequest();
		request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
		request.setSmallestDisplacement(20f);
		request.setFastestInterval(4000);
		request.setInterval(15000);

		mLocationClient.requestLocationUpdates(request, this);

		onLocationChanged(mLocationClient.getLastLocation());
	}

	@Override
	public void onLocationChanged(final Location location) {
		if (DBG)
			Log.d(LOG_TAG, "onLocationChanged " + location);

		mLastKnownLocation = location;

		for (MyLocationListener l : mListenerList) {
			l.onLocationChanged(location);
		}
	}

	@Override
	public void onDisconnected() {
		if (DBG)
			Log.e(LOG_TAG, "onDisconnected");
	}

	/**
	 * This class provides access to the system location services. These
	 * services allow applications to obtain periodic updates of the device's
	 * geographical location, or to fire an application-specified Intent when
	 * the device enters the proximity of a given geographical location.
	 */

	public Location getLastKnownLocation() {
		if (mLegacyMode) {
			return (mBestProvider == null) ? mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
					: mLocationManager.getLastKnownLocation(mBestProvider);
		} else {
			if (mLocationClient.isConnected())
				return mLocationClient.getLastLocation();
			else
				return mLastKnownLocation;
		}
	}

	/**
	 * Indiquer si le provider déterminé est bien actif ou non. En particulier,
	 * si la localisation est désactivée sur le téléphone, le provider inactif.
	 */
	public boolean isProviderEnabled() {
		if (mLegacyMode) {
			return (mBestProvider == null) ? false : mLocationManager.isProviderEnabled(mBestProvider);

		} else {
			return mLocationClient.isConnected();
		}
	}

	/**
	 * Récupérer l'adresse de la dernière position courante
	 */
	public Address getLastKnownAddress() {
		if (DBG)
			Log.d(LOG_TAG, "getLastKnownAddress");

		final Location lastKnowLocation = getLastKnownLocation();
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
