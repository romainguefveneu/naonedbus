package net.naonedbus.map.layer;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.google.android.gms.maps.model.Marker;
import com.twotoasters.clusterkraf.ClusterPoint;
import com.twotoasters.clusterkraf.ClusterkrafInfoWindowAdapter;
import com.twotoasters.clusterkraf.OnInfoWindowClickDownstreamListener;

public class ProxyInfoWindowAdapter implements ClusterkrafInfoWindowAdapter, OnInfoWindowClickDownstreamListener {

	private final Map<Class<?>, MapLayer<?>> mLayerChoosers;
	private HashMap<Marker, ClusterPoint> mMarkerClusterPoints;
	private WeakReference<Context> mContext;

	public ProxyInfoWindowAdapter(Context context) {
		mLayerChoosers = new HashMap<Class<?>, MapLayer<?>>();
		mContext = new WeakReference<Context>(context);
	}

	public void registerMapLayer(final Class<?> layerTagClass, final MapLayer<?> mapLayer) {
		mLayerChoosers.put(layerTagClass, mapLayer);
	}

	@Override
	public View getInfoContents(Marker marker) {
		final ClusterPoint clusterPoint = mMarkerClusterPoints.get(marker);
		final Object tag = clusterPoint.getPointAtOffset(0).getTag();

		Class<?> tagClass = tag.getClass();
		MapLayer<?> mapLayer = getMapLayer(tagClass);

		return mapLayer.getInfoContents(tag);
	}

	@Override
	public View getInfoWindow(Marker marker) {
		return null;
	}

	@Override
	public final void setMarkersClustersMap(final HashMap<Marker, ClusterPoint> map) {
		mMarkerClusterPoints = map;
	}

	@Override
	public boolean onInfoWindowClick(Marker marker, ClusterPoint clusterPoint) {
		final Object tag = clusterPoint.getPointAtOffset(0).getTag();

		Class<?> tagClass = tag.getClass();
		MapLayer<?> mapLayer = getMapLayer(tagClass);

		Context context = mContext.get();
		if (context != null) {
			Intent intent = mapLayer.getClickIntent(context, tag);
			if (intent != null) {
				context.startActivity(intent);
				return true;
			}
		}
		return false;
	}

	private MapLayer getMapLayer(Class<?> tagClass) {
		MapLayer<?> mapLayer = null;
		for (Entry<Class<?>, MapLayer<?>> item : mLayerChoosers.entrySet()) {
			Class<?> key = item.getKey();
			if (tagClass.isAssignableFrom(key) || key.isAssignableFrom(tagClass)) {
				mapLayer = item.getValue();
				break;
			}
		}

		return mapLayer;
	}

}
