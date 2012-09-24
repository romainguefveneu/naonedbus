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

	private static final String LOG_TAG = MyLocationProvider.class.getSimpleName();

	private Context context;

	private Criteria providerCriteria;
	private LocationManager locationManager;
	private Geocoder geocoder;
	private String bestProvider = null;
	private Set<MyLocationListener> listenerList;

	public interface MyLocationListener {
		void onLocationChanged(Location location);

		void onLocationDisabled();
	}

	private LocationListener locationListener = new LocationListener() {
		private Location lastLocation = new Location(LocationManager.GPS_PROVIDER);

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			Log.d(LOG_TAG, "onStatusChanged : " + provider + " / " + status);
			if (status != LocationProvider.AVAILABLE) {
				for (MyLocationListener listener : listenerList) {
					listener.onLocationDisabled();
				}
			}
		}

		@Override
		public void onProviderEnabled(String provider) {
			Log.d(LOG_TAG, "onProviderEnabled : " + provider);
			setBestProvider();
		}

		@Override
		public void onProviderDisabled(String provider) {
			Log.d(LOG_TAG, "onProviderDisabled : " + provider);
			setBestProvider();
		}

		@Override
		public void onLocationChanged(Location location) {
			if (location != null && lastLocation.distanceTo(location) > 50.0f) {
				lastLocation = location;
				for (MyLocationListener listener : listenerList) {
					listener.onLocationChanged(location);
				}
			}
		}
	};

	public MyLocationProvider(Context context) {
		this.context = context;
		this.geocoder = new Geocoder(context, new Locale("fr", "FR"));
		initialize();
	}

	/**
	 * Initialiser le provider
	 */
	private void initialize() {
		listenerList = new HashSet<MyLocationListener>();

		providerCriteria = new Criteria();
		providerCriteria.setAccuracy(Criteria.ACCURACY_FINE);

		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		setBestProvider();
	}

	private void setBestProvider() {
		bestProvider = locationManager.getBestProvider(providerCriteria, true);
		Log.d(LOG_TAG, "BestProvider : " + bestProvider);
	}

	/**
	 * Ajout d'un location listener
	 * 
	 * @param locationListener
	 * @throws Exception
	 */
	public void addListener(MyLocationListener locationListener) {
		if (!listenerList.contains(locationListener)) {
			listenerList.add(locationListener);
		}
	}

	/**
	 * Enlever un listener de la liste
	 * 
	 * @param locationListener
	 * @throws Exception
	 */
	public void removeListener(MyLocationListener locationListener) {
		if (listenerList.contains(locationListener)) {
			listenerList.remove(locationListener);
		}
	}

	public boolean containsListener(MyLocationListener locationListener) {
		return listenerList.contains(locationListener);
	}

	/**
	 * Démarrer l'écoute
	 */
	public void start() {
		try {
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30000, 50, locationListener);
		} catch (Exception e) {
			Log.w(LOG_TAG, "Impossible de récupérer la position via le réseau.", e);
		}
		try {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 50, locationListener);
		} catch (Exception e) {
			Log.w(LOG_TAG, "Impossible de récupérer la position via le gps.", e);
		}
	}

	/**
	 * Redémarrer l'écoute, en refraichissant le provider
	 */
	public void restart() {
		initialize();
	}

	/**
	 * Arrêter l'écoute
	 */
	public void stop() {
		locationManager.removeUpdates(locationListener);
	}

	/**
	 * This class provides access to the system location services. These
	 * services allow applications to obtain periodic updates of the device's
	 * geographical location, or to fire an application-specified Intent when
	 * the device enters the proximity of a given geographical location.
	 */

	public Location getLastKnownLocation() {
		return (bestProvider == null) ? locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
				: locationManager.getLastKnownLocation(bestProvider);
	}

	/**
	 * Indiquer si le provider déterminé est bien actif ou non. En particulier,
	 * si la localisation est désactivée sur le téléphone, le provider inactif.
	 */
	public boolean isProviderEnabled() {
		return (bestProvider == null) ? false : locationManager.isProviderEnabled(bestProvider);
	}

	/**
	 * Récupérer l'adresse de la dernière position courante
	 */
	public Address getLastKnownAddress() {
		Location lastKnowLocation = getLastKnownLocation();
		Address result = null;
		try {
			if (lastKnowLocation != null) {
				final List<Address> addresses = geocoder.getFromLocation(lastKnowLocation.getLatitude(),
						lastKnowLocation.getLongitude(), 1);
				if (addresses != null && addresses.size() > 0) {
					result = addresses.get(0);
				}
			}
		} catch (IOException e) {
			Log.w(LOG_TAG, "Erreur lors de la récupération de l'adresse.", e);
		}
		return result;
	}

}
