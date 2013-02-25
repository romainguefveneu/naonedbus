package net.naonedbus.fragment.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.naonedbus.BuildConfig;
import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.activity.impl.CommentaireActivity;
import net.naonedbus.activity.impl.MapActivity;
import net.naonedbus.activity.impl.ParcoursActivity;
import net.naonedbus.bean.Equipement;
import net.naonedbus.bean.Equipement.Type;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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

	private static final String LOG_TAG = "ProximiteFragment";
	private static final boolean DBG = BuildConfig.DEBUG;

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
		if (DBG)
			Log.i(LOG_TAG, "ProximiteFragment()");

		setHasOptionsMenu(true);

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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (DBG)
			Log.d(LOG_TAG, "onCreateView");

		final View view = super.onCreateView(inflater, container, savedInstanceState);
		headerTextView = (TextView) view.findViewById(R.id.text);
		imageView = (ImageView) view.findViewById(R.id.icon);

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		if (DBG)
			Log.d(LOG_TAG, "onStart");

		myLocationProvider.addListener(this);

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
		if (DBG)
			Log.d(LOG_TAG, "onStop");

		myLocationProvider.removeListener(this);

		if (mAddressResolverTask != null) {
			mAddressResolverTask.cancel(false);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_proximite, menu);
		final SubMenu filterSubMenu = menu.findItem(R.id.menu_filter).getSubMenu();

		final Equipement.Type[] types = Equipement.Type.values();
		for (Equipement.Type type : types) {
			final MenuItem item = filterSubMenu.add(MENU_GROUP_TYPES, type.getId(), 0, type.getTitleRes());
			item.setCheckable(true);
			item.setChecked((selectedTypesEquipements.contains(type)));
		}

		super.onCreateOptionsMenu(menu, inflater);
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

		final ParamIntent intent;
		if (equipement.getType().equals(Type.TYPE_ARRET)) {
			intent = new ParamIntent(getActivity(), ParcoursActivity.class);
			intent.putExtra(ParcoursActivity.Param.idStation, equipement.getId());
		} else {
			intent = new ParamIntent(getActivity(), MapActivity.class);
			intent.putExtra(MapActivity.Param.itemId, equipement.getId());
			intent.putExtra(MapActivity.Param.itemType, equipement.getType().getId());
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
