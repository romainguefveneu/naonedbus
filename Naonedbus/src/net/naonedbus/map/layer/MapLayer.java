package net.naonedbus.map.layer;

import java.util.HashMap;

import net.naonedbus.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.twotoasters.clusterkraf.ClusterPoint;
import com.twotoasters.clusterkraf.ClusterkrafInfoWindowAdapter;

public abstract class MapLayer<T> implements ClusterkrafInfoWindowAdapter {

	private final View mContentView;
	private final TextView mTitle;
	private final TextView mDescription;

	private HashMap<Marker, ClusterPoint> mMarkerClusterPoints;

	public MapLayer(final LayoutInflater inflater) {
		mContentView = inflater.inflate(R.layout.map_info_window, null);

		mTitle = ((TextView) mContentView.findViewById(R.id.itemTitle));
		mDescription = ((TextView) mContentView.findViewById(R.id.itemDescription));
	}

	@Override
	public final void setMarkersClustersMap(final HashMap<Marker, ClusterPoint> map) {
		mMarkerClusterPoints = map;
	}

	@Override
	public final View getInfoContents(final Marker marker) {
		final ClusterPoint clusterPoint = mMarkerClusterPoints.get(marker);
		@SuppressWarnings("unchecked")
		final T tag = (T) clusterPoint.getPointAtOffset(0).getTag();
		bindInfoContents(mContentView.getContext(), tag);

		return mContentView;
	}

	@Override
	public final View getInfoWindow(final Marker marker) {
		return null;
	}

	protected abstract void bindInfoContents(Context context, final T item);

	public abstract void chooseMarker(final MarkerOptions markerOptions, final ClusterPoint clusterPoint);

	protected final void setInfoTitle(final CharSequence title) {
		mTitle.setText(title);
	}

	protected final void setInfoDescription(final CharSequence description) {
		mDescription.setText(description);
	}

}
