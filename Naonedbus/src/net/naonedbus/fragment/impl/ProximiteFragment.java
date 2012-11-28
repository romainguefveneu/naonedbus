package net.naonedbus.fragment.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.activity.impl.CommentaireActivity;
import net.naonedbus.activity.impl.MapActivity;
import net.naonedbus.bean.Equipement;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.comparator.EquipementDistanceComparator;
import net.naonedbus.fragment.CustomFragmentActions;
import net.naonedbus.fragment.CustomListFragment;
import net.naonedbus.intent.ParamIntent;
import net.naonedbus.manager.impl.EquipementManager;
import net.naonedbus.provider.impl.MyLocationProvider;
import net.naonedbus.provider.impl.MyLocationProvider.MyLocationListener;
import net.naonedbus.task.AddressResolverTask;
import net.naonedbus.task.AddressResolverTask.AddressTaskListener;
import net.naonedbus.widget.adapter.impl.EquipementArrayAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;

public class ProximiteFragment extends CustomListFragment implements CustomFragmentActions, MyLocationListener,
		AddressTaskListener {

	private static final int MAX_EQUIPEMENTS = 25;
	private static final int MENU_GROUP_TYPES = 1;

	private static final String PREF_PROXIMITE_LAYER = "proximite.layer.";

	private SharedPreferences preferences;
	private MyLocationProvider myLocationProvider;
	private Set<Equipement.Type> selectedTypesEquipements;
	private AddressResolverTask mAddressResolverTask;

	private TextView headerTextView;
	private ImageView imageView;

	public ProximiteFragment() {
		super(R.string.title_fragment_proximite, R.layout.fragment_proximite);

		myLocationProvider = NBApplication.getLocationProvider();
		preferences = NBApplication.getPreferences();

		selectedTypesEquipements = new HashSet<Equipement.Type>();
		final Equipement.Type[] types = Equipement.Type.values();
		for (Equipement.Type type : types) {
			if (isLayerPreferenceEnabled(type.getId())) {
				selectedTypesEquipements.add(type);
			}
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		headerTextView = (TextView) getView().findViewById(R.id.text);
		imageView = (ImageView) getView().findViewById(R.id.icon);

		myLocationProvider.addListener(this);
		myLocationProvider.start();
		if (myLocationProvider.isProviderEnabled() == false) {
			onLocationDisabled();
		} else {
			loadContent();
		}

		loadAddress();
	}

	@Override
	public void onStop() {
		super.onStop();

		if (mAddressResolverTask != null) {
			mAddressResolverTask.cancel(false);
		}
	}

	@Override
	public void onDestroy() {
		myLocationProvider.removeListener(this);
		super.onDestroy();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu) {
		final MenuInflater menuInflater = getSherlockActivity().getSupportMenuInflater();
		menuInflater.inflate(R.menu.fragment_proximite, menu);

		final SubMenu filterSubMenu = menu.findItem(R.id.menu_filter).getSubMenu();

		final Equipement.Type[] types = Equipement.Type.values();
		for (Equipement.Type type : types) {
			final MenuItem item = filterSubMenu.add(MENU_GROUP_TYPES, type.getId(), 0, type.getTitleRes());
			item.setCheckable(true);
			item.setChecked((selectedTypesEquipements.contains(type)));
		}
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getGroupId() == MENU_GROUP_TYPES) {
			final Equipement.Type type = Equipement.Type.getTypeById(item.getItemId());

			item.setChecked(!item.isChecked());
			setLayerPreference(type.getId(), item.isChecked());

			if (item.isChecked()) {
				selectedTypesEquipements.add(type);
			} else {
				selectedTypesEquipements.remove(type);
			}

			refreshContent();
			return true;
		} else {
			switch (item.getItemId()) {
			case R.id.menu_edit:
				startActivity(new Intent(getActivity(), CommentaireActivity.class));
				break;
			default:
				return false;
			}
		}

		return false;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		final Equipement equipement = (Equipement) getListAdapter().getItem(position);
		final ParamIntent intent = new ParamIntent(getActivity(), MapActivity.class);
		intent.putExtra(MapActivity.Param.itemId, equipement.getId());
		intent.putExtra(MapActivity.Param.itemType, equipement.getType().getId());
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
	protected AsyncResult<ListAdapter> loadContent(final Context context) {
		final AsyncResult<ListAdapter> result = new AsyncResult<ListAdapter>();
		EquipementArrayAdapter adapter = null;

		try {

			if (selectedTypesEquipements.size() > 0) {
				final EquipementManager equipementManager = EquipementManager.getInstance();
				final Location location = myLocationProvider.getLastKnownLocation();

				if (location != null) {
					final List<Equipement> list = equipementManager.getEquipementsByLocation(
							context.getContentResolver(), selectedTypesEquipements, location, MAX_EQUIPEMENTS);

					setDistances(list);
					Collections.sort(list, new EquipementDistanceComparator<Equipement>());

					adapter = new EquipementArrayAdapter(context, list);
				}
			}

			result.setResult(adapter);

		} catch (Exception e) {
			result.setException(e);
		}

		return result;
	}

	protected void setDistances(List<Equipement> equipements) {
		final Location location = new Location(LocationManager.GPS_PROVIDER);
		final Location currentLocation = myLocationProvider.getLastKnownLocation();

		if (currentLocation != null) {
			for (final Equipement item : equipements) {
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
	public void onLocationChanged(Location location) {
		loadAddress();
		refreshContent();
	}

	@Override
	public void onLocationDisabled() {
		showMessage(R.string.msg_error_location_title, R.string.msg_error_location_desc, R.drawable.location);
		setMessageButton(R.string.btn_geolocation_service, new OnClickListener() {
			@Override
			public void onClick(View v) {
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
		if (id == Equipement.Type.TYPE_ARRET.getId()) {
			return preferences.getBoolean(PREF_PROXIMITE_LAYER + id, true);
		} else {
			return preferences.getBoolean(PREF_PROXIMITE_LAYER + id, false);
		}
	}

	/**
	 * Changer la valeur d'activation d'un calque.
	 * 
	 * @param id
	 * @param enabled
	 */
	private void setLayerPreference(final Integer id, final boolean enabled) {
		preferences.edit().putBoolean(PREF_PROXIMITE_LAYER + id, enabled).commit();
	}

	@Override
	public void onAddressTaskPreExecute() {
		imageView.setVisibility(View.INVISIBLE);
		headerTextView.setText(R.string.msg_loading_address);
	}

	@Override
	public void onAddressTaskResult(String address) {
		if (address != null) {
			headerTextView.setText(address);
			imageView.setVisibility(View.VISIBLE);
		} else {
			headerTextView.setText("Adresse inconnue.");
		}
	}

}
