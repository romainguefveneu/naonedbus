package net.naonedbus.activity.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import net.naonedbus.R;
import net.naonedbus.bean.Equipment;
import net.naonedbus.bean.Equipment.Type;
import net.naonedbus.bean.MapState;
import net.naonedbus.bean.Route;
import net.naonedbus.bean.Stop;
import net.naonedbus.comparator.StopOrderComparator;
import net.naonedbus.loader.EquipmentLoader;
import net.naonedbus.manager.impl.RouteManager;
import net.naonedbus.manager.impl.StopManager;
import net.naonedbus.utils.ColorUtils;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Pair;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class Map2Activity extends SherlockFragmentActivity implements LoaderCallbacks<List<Equipment>>,
		OnMarkerClickListener, GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

	public static final int WALK_CIRCLE_COLOR = 0x50888888;

	private static final float RADIUS_DEFAULT = 8f;
	private static final float RADIUS_SELECTED = 10f;

	private LocationClient mLocationClient;
	private SupportMapFragment mMapFragment;
	private GoogleMap mGoogleMap;
	private MapState mMapState;
	private Circle mWalkCircle;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_map);

		mLocationClient = new LocationClient(this, this, this);

		mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
		mGoogleMap = mMapFragment.getMap();
		mGoogleMap.setMyLocationEnabled(true);
		mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
		mGoogleMap.setOnMarkerClickListener(this);

		mMapState = new MapState();
		final Bitmap markerBitmap = createMarker(
				getResources().getColor(Equipment.Type.TYPE_STOP.getBackgroundColorRes()), RADIUS_DEFAULT);
		mMapState.setDefaultMarkerIcon(BitmapDescriptorFactory.fromBitmap(markerBitmap));

		final List<Route> routes = new ArrayList<Route>();
		routes.add(RouteManager.getInstance().getSingle(getContentResolver(), "1"));
		final RouteDrawTask t = new RouteDrawTask(this, routes, mMapState, mGoogleMap);
		t.execute();

		getSupportLoaderManager().initLoader(0, null, this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		mLocationClient.connect();
	}

	@Override
	protected void onStop() {
		mLocationClient.disconnect();
		super.onStop();
	}

	@Override
	public boolean onMarkerClick(final Marker marker) {
		final Equipment equipment = mMapState.getEquipment(marker);
		if (equipment != null) {
			final List<Route> routes = RouteManager.getInstance().getRoutesByStopArea(getContentResolver(),
					equipment.getId());
			final RouteDrawTask t = new RouteDrawTask(this, routes, mMapState, mGoogleMap);
			t.execute();
		}
		return false;
	}

	@Override
	public Loader<List<Equipment>> onCreateLoader(final int loaderId, final Bundle bundle) {
		return new EquipmentLoader(this, Type.TYPE_STOP);
	}

	@Override
	public void onLoadFinished(final Loader<List<Equipment>> loader, final List<Equipment> equipement) {
		for (final Equipment equipment : equipement) {

			final MarkerOptions markerOptions = new MarkerOptions();
			markerOptions.position(new LatLng(equipment.getLatitude(), equipment.getLongitude()));
			markerOptions.icon(mMapState.getDefaultMarkerIcon());
			markerOptions.title(equipment.getName());
			markerOptions.flat(true);
			markerOptions.anchor(0.5f, 0.5f);

			final Marker marker = mGoogleMap.addMarker(markerOptions);
			mMapState.addMarker(marker, equipment);
		}
	}

	@Override
	public void onLoaderReset(final Loader<List<Equipment>> loader) {

	}

	private static class RouteDrawTask extends AsyncTask<Route, Pair<Route, List<PolylineOptions>>, Void> {

		private final Context mContext;
		private final List<Route> mRoutes;
		private final GoogleMap mGoogleMap;
		private final MapState mMapState;

		public RouteDrawTask(final Context context, final List<Route> routes, final MapState mapState,
				final GoogleMap googleMap) {
			mContext = context;
			mRoutes = routes;
			mGoogleMap = googleMap;
			mMapState = mapState;
		}

		@Override
		protected void onPreExecute() {
			synchronized (mMapState) {
				final Iterator<Entry<Route, List<Polyline>>> iterator = mMapState.getPolylinesMap().entrySet()
						.iterator();

				while (iterator.hasNext()) {
					final Entry<Route, List<Polyline>> entry = iterator.next();
					final Route route = entry.getKey();
					if (!mRoutes.contains(route)) {
						for (final Polyline polyline : entry.getValue()) {
							polyline.remove();
						}
						iterator.remove();

						final List<Marker> coloredMarkers = mMapState.getColoredMarkers(route);
						if (coloredMarkers != null) {
							for (final Marker marker : coloredMarkers) {
								marker.setIcon(mMapState.getDefaultMarkerIcon());
							}
							mMapState.clearColoredMarkers(route);
						}
					}
				}

			}
		}

		@Override
		protected Void doInBackground(final Route... params) {
			Thread.currentThread().setPriority(Thread.NORM_PRIORITY);

			for (final Route route : mRoutes) {

				final List<Stop> stops = StopManager.getInstance().getAll(mContext.getContentResolver(),
						route.getCode(), "1");
				Collections.sort(stops, new StopOrderComparator());

				final List<PolylineOptions> polylineOptions = buildPolylines(route, stops, 0);

				publishProgress(new Pair<Route, List<PolylineOptions>>(route, polylineOptions));
			}

			return null;
		}

		private List<PolylineOptions> buildPolylines(final Route route, final List<Stop> stops, final int start) {
			final List<PolylineOptions> result = new ArrayList<PolylineOptions>();

			final PolylineOptions mainPolylineOptions = new PolylineOptions().width(10).color(route.getBackColor());
			final PolylineOptions borderPolylineOptions = new PolylineOptions().width(14).color(
					ColorUtils.getDarkerColor(route.getBackColor()));

			int i = start;
			final Stop first = stops.get(start);
			final int currentDepth = first.getStepDepth();

			if (first.getStepOrientationTop() == Stop.ORIENTATION_LEFT_RIGHT) {
				final Stop previous = (i > 0) ? stops.get(i - 1) : null;
				final LatLng position = getLatLng(previous);
				mainPolylineOptions.add(position);
				borderPolylineOptions.add(position);
			}

			while (i < stops.size()) {
				final Stop stop = stops.get(i);

				final int stopDepth = stop.getStepDepth();
				if (stopDepth == currentDepth) {
					final LatLng position = getLatLng(stop);
					borderPolylineOptions.add(position);
					mainPolylineOptions.add(position);
				} else if (stopDepth > currentDepth) {
					result.addAll(buildPolylines(route, stops, i));
				} else {
					final Stop previous = (i > 0) ? stops.get(i - 1) : null;
					if (previous != null && previous.getStepOrientationBottom() == Stop.ORIENTATION_RIGHT_LEFT) {
						final LatLng position = getLatLng(stop);
						borderPolylineOptions.add(position);
						mainPolylineOptions.add(position);
					}

					break;
				}

				i++;
			}

			result.add(borderPolylineOptions);
			result.add(mainPolylineOptions);
			return result;
		}

		private LatLng getLatLng(final Stop stop) {
			return new LatLng(stop.getLatitude(), stop.getLongitude());
		}

		@Override
		protected void onProgressUpdate(final Pair<Route, List<PolylineOptions>>... values) {
			synchronized (mMapState) {

				for (final Pair<Route, List<PolylineOptions>> item : values) {
					final Route route = item.first;
					final List<PolylineOptions> polylineOptions = item.second;

					if (!mMapState.hasPolyline(route)) {
						final List<Polyline> polylines = new ArrayList<Polyline>();
						for (final PolylineOptions polylineOption : polylineOptions) {
							polylines.add(mGoogleMap.addPolyline(polylineOption));
						}
						mMapState.setPolyline(route, polylines);

						final Bitmap coloredMarkerBitmap = createMarker(route.getBackColor(), RADIUS_SELECTED);
						final BitmapDescriptor coloredIcon = BitmapDescriptorFactory.fromBitmap(coloredMarkerBitmap);

						final List<Stop> stops = StopManager.getInstance().getAll(mContext.getContentResolver(),
								route.getCode(), "1");
						for (final Stop stop : stops) {
							final Marker marker = mMapState.getMarker(stop.getIdStation());
							if (marker != null) {
								marker.setIcon(coloredIcon);
								mMapState.setColoredMarker(route, marker);
							}
						}

					}
				}

			}
		}
	}

	@Override
	public void onConnectionFailed(final ConnectionResult arg0) {

	}

	@Override
	public void onConnected(final Bundle arg0) {
		centerCamera(mLocationClient.getLastLocation());
	}

	@Override
	public void onDisconnected() {

	}

	private void centerCamera(final Location location) {
		drawWalkCircle(location);

		final LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
		final CameraPosition position = new CameraPosition(latlng, 15, 0f, 0f);
		final CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(position);
		mGoogleMap.moveCamera(cameraUpdate);
	}

	private void drawWalkCircle(final Location location) {
		final LatLng center = new LatLng(location.getLatitude(), location.getLongitude());

		if (mWalkCircle != null) {
			mWalkCircle.setCenter(center);
		} else {
			final CircleOptions circleOptions = new CircleOptions().center(center).radius(415);
			circleOptions.strokeColor(WALK_CIRCLE_COLOR);
			circleOptions.strokeWidth(8f);

			mWalkCircle = mGoogleMap.addCircle(circleOptions);
		}
	}

	private static Bitmap createMarker(final int color, final float radius) {
		final Paint paint = new Paint();
		paint.setStrokeWidth(4f);
		paint.setAntiAlias(true);

		final Bitmap.Config conf = Bitmap.Config.ARGB_8888;
		final Bitmap marker = Bitmap.createBitmap(32, 32, conf);
		final Canvas canvas = new Canvas(marker);

		paint.setStyle(Style.STROKE);
		paint.setColor(ColorUtils.getDarkerColor(color));
		canvas.drawCircle(marker.getWidth() / 2, marker.getHeight() / 2, radius, paint);

		paint.setStyle(Style.FILL);
		paint.setColor(color);
		canvas.drawCircle(marker.getWidth() / 2, marker.getHeight() / 2, radius, paint);

		return marker;
	}

}
