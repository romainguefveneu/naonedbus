package net.naonedbus.map;

import java.util.HashMap;

import net.naonedbus.R;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.model.Marker;
import com.twotoasters.clusterkraf.ClusterPoint;
import com.twotoasters.clusterkraf.ClusterkrafInfoWindowAdapter;

public class EquipementInfoWindowAdapter implements ClusterkrafInfoWindowAdapter {

	private final View mContentView;
	private HashMap<Marker, ClusterPoint> mMarkerClusterPoints;

	public EquipementInfoWindowAdapter(final LayoutInflater inflater) {
		mContentView = inflater.inflate(R.layout.map_info_window, null);
	}

	@Override
	public void setMarkersClustersMap(final HashMap<Marker, ClusterPoint> map) {
		mMarkerClusterPoints = map;
	}

	@Override
	public View getInfoContents(final Marker marker) {
		final ClusterPoint clusterPoint = mMarkerClusterPoints.get(marker);
		final MarkerInfo markerInfo = (MarkerInfo) clusterPoint.getPointAtOffset(0).getTag();

		((TextView) mContentView.findViewById(R.id.itemTitle)).setText(markerInfo.getTitle());
		((TextView) mContentView.findViewById(R.id.itemDescription)).setText(markerInfo.getType().name());

		return mContentView;
	}

	@Override
	public View getInfoWindow(final Marker marker) {
		return null;
	}

}
