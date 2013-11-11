package net.naonedbus.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.SparseArray;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

public class MapState {
	private final Map<Marker, Equipment> mMarkersMap = new HashMap<Marker, Equipment>();
	private final SparseArray<Marker> mMarkersById = new SparseArray<Marker>();
	private final Map<Route, List<Polyline>> mPolylinesMap = new HashMap<Route, List<Polyline>>();
	private final Map<Route, List<Marker>> mColoredMarkers = new HashMap<Route, List<Marker>>();
	private BitmapDescriptor mDefaultMarkerIcon;

	public void setDefaultMarkerIcon(final BitmapDescriptor defaultMarkerIcon) {
		mDefaultMarkerIcon = defaultMarkerIcon;
	}

	public BitmapDescriptor getDefaultMarkerIcon() {
		return mDefaultMarkerIcon;
	}

	public void addMarker(final Marker marker, final Equipment equipment) {
		mMarkersMap.put(marker, equipment);
		mMarkersById.put(equipment.getId(), marker);
	}

	public Marker getMarker(final int equipmentId) {
		return mMarkersById.get(equipmentId);
	}

	public Equipment getEquipment(final Marker marker) {
		return mMarkersMap.get(marker);
	}

	public boolean hasPolyline(final Route route) {
		return mPolylinesMap.containsKey(route);
	}

	public void setPolyline(final Route route, final List<Polyline> polylines) {
		mPolylinesMap.put(route, polylines);
	}

	public List<Polyline> getPolylines(final Route route) {
		return mPolylinesMap.get(route);
	}

	public Map<Route, List<Polyline>> getPolylinesMap() {
		return mPolylinesMap;
	}

	public void setColoredMarker(final Route route, final Marker marker) {
		if (mColoredMarkers.get(route) == null)
			mColoredMarkers.put(route, new ArrayList<Marker>());

		mColoredMarkers.get(route).add(marker);
	}

	public void clearColoredMarkers(final Route route) {
		if (mColoredMarkers.get(route) != null)
			mColoredMarkers.get(route).clear();
	}

	public List<Marker> getColoredMarkers(final Route route) {
		return mColoredMarkers.get(route);
	}

}
