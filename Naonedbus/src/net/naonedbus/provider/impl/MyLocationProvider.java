/**
 *  Copyright (C) 2011 Romain Guefveneu
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
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

/**
 * @author romain
 * 
 */
public class MyLocationProvider {

	private static final String LOG_TAG = "MyLocationProvider";
	private static final boolean DBG = BuildConfig.DEBUG;

	private Context mContext;

	private Criteria mProviderCriteria;
	private LocationManager mLocationManager;
	private Geocoder mGeoCoder;
	private String mBestProvider;
	private Set<MyLocationListener> mListenerList;

	public static interface MyLocationListener {
		void onLocationChanged(Location location);

		void onLocationDisabled();
	}

	private LocationListener mLocationListener = new LocationListener() {
		private Location lastLocation = new Location(LocationManager.GPS_PROVIDER);

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			if (DBG)
				Log.d(LOG_TAG, "onStatusChanged : " + provider + " / " + status);

			if (status != LocationProvider.AVAILABLE) {
				for (MyLocationListener listener : mListenerList) {
					listener.onLocationDisabled();
				}
			}
		}

		@Override
		public void onProviderEnabled(String provider) {
			if (DBG)
				Log.d(LOG_TAG, "onProviderEnabled : " + provider);
			setBestProvider();
		}

		@Override
		public void onProviderDisabled(String provider) {
			if (DBG)
				Log.d(LOG_TAG, "onProviderDisabled : " + provider);
			setBestProvider();
		}

		@Override
		public void onLocationChanged(Location location) {
			if (location != null && lastLocation.distanceTo(location) > 50.0f) {
				lastLocation = location;
				for (MyLocationListener listener : mListenerList) {
					listener.onLocationChanged(location);
				}
			}
		}
	};

	public MyLocationProvider(Context context) {
		mContext = context;
		mGeoCoder = new Geocoder(context, new Locale("fr", "FR"));
		initialize();
	}

	/**
	 * Initialiser le provider
	 */
	private void initialize() {
		mListenerList = new HashSet<MyLocationListener>();

		mProviderCriteria = new Criteria();
		mProviderCriteria.setAccuracy(Criteria.ACCURACY_FINE);

		mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
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
	public void addListener(MyLocationListener locationListener) {
		if (!mListenerList.contains(locationListener)) {
			mListenerList.add(locationListener);
		}
	}

	/**
	 * Enlever un listener de la liste
	 * 
	 * @param locationListener
	 * @throws Exception
	 */
	public void removeListener(MyLocationListener locationListener) {
		if (mListenerList.contains(locationListener)) {
			mListenerList.remove(locationListener);
		}
	}

	public boolean containsListener(MyLocationListener locationListener) {
		return mListenerList.contains(locationListener);
	}

	/**
	 * Démarrer l'écoute
	 */
	public void start() {
		try {
			mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30000, 50, mLocationListener);
		} catch (Exception e) {
			if (DBG)
				Log.w(LOG_TAG, "Impossible de récupérer la position via le réseau.", e);
		}
		try {
			mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 50, mLocationListener);
		} catch (Exception e) {
			if (DBG)
				Log.w(LOG_TAG, "Impossible de récupérer la position via le gps.", e);
		}
	}

	/**
	 * Redémarrer l'écoute, en refraichissant le provider
	 */
	public void restart() {
		if (DBG)
			Log.d(LOG_TAG, "restart");

		initialize();
	}

	/**
	 * Arrêter l'écoute
	 */
	public void stop() {
		if (DBG)
			Log.d(LOG_TAG, "stop");

		mLocationManager.removeUpdates(mLocationListener);
	}

	/**
	 * This class provides access to the system location services. These
	 * services allow applications to obtain periodic updates of the device's
	 * geographical location, or to fire an application-specified Intent when
	 * the device enters the proximity of a given geographical location.
	 */

	public Location getLastKnownLocation() {
		return (mBestProvider == null) ? mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
				: mLocationManager.getLastKnownLocation(mBestProvider);
	}

	/**
	 * Indiquer si le provider déterminé est bien actif ou non. En particulier,
	 * si la localisation est désactivée sur le téléphone, le provider inactif.
	 */
	public boolean isProviderEnabled() {
		return (mBestProvider == null) ? false : mLocationManager.isProviderEnabled(mBestProvider);
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
