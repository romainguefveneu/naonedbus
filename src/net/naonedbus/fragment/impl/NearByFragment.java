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
package net.naonedbus.fragment.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.naonedbus.BuildConfig;
import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.activity.impl.MapActivity;
import net.naonedbus.activity.impl.SendNewsActivity;
import net.naonedbus.activity.impl.StopPathActivity;
import net.naonedbus.bean.Equipment;
import net.naonedbus.bean.Equipment.Type;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.comparator.EquipmentDistanceComparator;
import net.naonedbus.fragment.CustomListFragment;
import net.naonedbus.manager.impl.EquipmentManager;
import net.naonedbus.provider.impl.MyLocationProvider;
import net.naonedbus.provider.impl.MyLocationProvider.MyLocationListener;
import net.naonedbus.task.AddressResolverTask;
import net.naonedbus.task.AddressResolverTask.AddressTaskListener;
import net.naonedbus.utils.FormatUtils;
import net.naonedbus.widget.adapter.impl.EquipmentArrayAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;

public class NearByFragment extends CustomListFragment implements MyLocationListener, AddressTaskListener {

	private static final String LOG_TAG = "NearByFragment";
	private static final boolean DBG = BuildConfig.DEBUG;

	private static final int MAX_EQUIPEMENTS = 25;
	private static final int MENU_GROUP_TYPES = 1;

	private static final String PREF_PROXIMITE_LAYER = "proximite.layer.";

	private final SharedPreferences mPreferences;
	private final MyLocationProvider mLocationProvider;
	private final Set<Equipment.Type> mSelectedTypesEquipements;
	private AddressResolverTask mAddressResolverTask;

	private TextView headerTextView;

	public NearByFragment() {
		super(R.layout.fragment_nearby);
		if (DBG)
			Log.i(LOG_TAG, "ProximiteFragment()");

		setHasOptionsMenu(true);

		mLocationProvider = NBApplication.getLocationProvider();
		mPreferences = NBApplication.getPreferences();

		mSelectedTypesEquipements = new HashSet<Equipment.Type>();
		final Equipment.Type[] types = Equipment.Type.values();
		for (final Equipment.Type type : types) {
			if (isLayerPreferenceEnabled(type.getId())) {
				mSelectedTypesEquipements.add(type);
			}
		}
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		if (DBG)
			Log.d(LOG_TAG, "onCreateView");

		final View view = super.onCreateView(inflater, container, savedInstanceState);
		headerTextView = (TextView) view.findViewById(R.id.text);

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		if (DBG)
			Log.d(LOG_TAG, "onStart");

		mLocationProvider.addListener(this);

		if (mLocationProvider.isProviderEnabled() == false) {
			onLocationDisabled();
		} else {
			loadContent();
		}

		loadAddress();
	}

	@Override
	public void onStop() {
		super.onStop();
		if (DBG)
			Log.d(LOG_TAG, "onStop");

		mLocationProvider.removeListener(this);

		if (mAddressResolverTask != null) {
			mAddressResolverTask.cancel(false);
		}
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_nearby, menu);
		final SubMenu filterSubMenu = menu.findItem(R.id.menu_filter).getSubMenu();

		final Equipment.Type[] types = Equipment.Type.values();
		for (final Equipment.Type type : types) {
			final MenuItem item = filterSubMenu.add(MENU_GROUP_TYPES, type.getId(), 0, type.getTitleRes());
			item.setCheckable(true);
			item.setChecked((mSelectedTypesEquipements.contains(type)));
		}

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onPrepareOptionsMenu(final Menu menu) {

	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		if (item.getGroupId() == MENU_GROUP_TYPES) {
			final Equipment.Type type = Equipment.Type.getTypeById(item.getItemId());

			item.setChecked(!item.isChecked());
			setLayerPreference(type.getId(), item.isChecked());

			if (item.isChecked()) {
				mSelectedTypesEquipements.add(type);
			} else {
				mSelectedTypesEquipements.remove(type);
			}

			refreshContent();
			return true;
		} else {
			switch (item.getItemId()) {
			case R.id.menu_edit:
				startActivity(new Intent(getActivity(), SendNewsActivity.class));
				break;
			default:
				return false;
			}
		}

		return false;
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
		final Equipment equipment = (Equipment) getListAdapter().getItem(position);

		final Intent intent;
		if (equipment.getType().equals(Type.TYPE_STOP)) {
			intent = new Intent(getActivity(), StopPathActivity.class);
			intent.putExtra(StopPathActivity.PARAM_ID_SATION, equipment.getId());
		} else {
			intent = new Intent(getActivity(), MapActivity.class);
			intent.putExtra(MapFragment.PARAM_ITEM_ID, equipment.getId());
			intent.putExtra(MapFragment.PARAM_ITEM_TYPE, equipment.getType().getId());
		}
		startActivity(intent);

	}

	/**
	 * Lance la récupération de l'adresse courante.
	 * 
	 * @param location
	 */
	private void loadAddress() {
		if (mAddressResolverTask != null) {
			mAddressResolverTask.cancel(true);
		}
		mAddressResolverTask = (AddressResolverTask) new AddressResolverTask(this).execute();
	}

	@Override
	protected AsyncResult<ListAdapter> loadContent(final Context context, final Bundle bundle) {
		final AsyncResult<ListAdapter> result = new AsyncResult<ListAdapter>();
		EquipmentArrayAdapter adapter = null;

		try {

			if (mSelectedTypesEquipements.size() > 0) {
				final EquipmentManager equipementManager = EquipmentManager.getInstance();
				final Location location = mLocationProvider.getLastKnownLocation();

				if (location != null) {
					final List<Equipment> list = equipementManager.getByLocation(context.getContentResolver(),
							mSelectedTypesEquipements, location, MAX_EQUIPEMENTS);

					setDistances(list);
					Collections.sort(list, new EquipmentDistanceComparator<Equipment>());

					adapter = new EquipmentArrayAdapter(context, list);
				}
			}

			result.setResult(adapter);

		} catch (final Exception e) {
			result.setException(e);
		}

		return result;
	}

	protected void setDistances(final List<Equipment> equipements) {
		final Location location = new Location(LocationManager.GPS_PROVIDER);
		final Location currentLocation = mLocationProvider.getLastKnownLocation();

		if (currentLocation != null) {
			for (final Equipment item : equipements) {
				final double latitude = item.getLatitude();
				final double longitude = item.getLongitude();
				if (latitude != 0) {
					location.setLatitude(latitude);
					location.setLongitude(longitude);
					item.setDistance(currentLocation.distanceTo(location));
				}
			}
		}
	}

	@Override
	public void onLocationChanged(final Location location) {
		if (DBG)
			Log.d(LOG_TAG, "onLocationChanged " + location);

		loadAddress();
		refreshContent();
	}

	@Override
	public void onLocationDisabled() {
		if (DBG)
			Log.d(LOG_TAG, "onLocationDisabled");

		showMessage(R.string.localization_disabled, R.string.please_enable_localization, R.drawable.location);
		setMessageButton(R.string.localization_services, new OnClickListener() {
			@Override
			public void onClick(final View v) {
				final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				getActivity().startActivity(intent);
			}
		});
	}

	/**
	 * Indique si le calque est activé ou non dans les préférences. Le calque
	 * {@code TYPE_STATION} est actif par défaut.
	 * 
	 * @param id
	 * @return Vrai si le calque est activé.
	 */
	private boolean isLayerPreferenceEnabled(final int id) {
		if (id == Equipment.Type.TYPE_STOP.getId()) {
			return mPreferences.getBoolean(PREF_PROXIMITE_LAYER + id, true);
		} else {
			return mPreferences.getBoolean(PREF_PROXIMITE_LAYER + id, false);
		}
	}

	/**
	 * Changer la valeur d'activation d'un calque.
	 * 
	 * @param id
	 * @param enabled
	 */
	private void setLayerPreference(final Integer id, final boolean enabled) {
		mPreferences.edit().putBoolean(PREF_PROXIMITE_LAYER + id, enabled).commit();
	}

	@Override
	public void onAddressTaskPreExecute() {
		headerTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		headerTextView.setText(R.string.loading_address);
	}

	@Override
	public void onAddressTaskResult(final Address address) {
		if (address != null) {
			headerTextView.setText(FormatUtils.formatAddress(address, null));
			headerTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_my_location_small, 0, 0, 0);
		} else {
			headerTextView.setText(R.string.current_address_unknown);
		}
	}
}
