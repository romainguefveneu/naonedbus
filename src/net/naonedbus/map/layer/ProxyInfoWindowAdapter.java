package net.naonedbus.map.layer;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import net.naonedbus.bean.Equipment;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.google.android.gms.maps.model.Marker;
import com.twotoasters.clusterkraf.ClusterPoint;
import com.twotoasters.clusterkraf.ClusterkrafInfoWindowAdapter;
import com.twotoasters.clusterkraf.OnInfoWindowClickDownstreamListener;

public class ProxyInfoWindowAdapter implements ClusterkrafInfoWindowAdapter, OnInfoWindowClickDownstreamListener {

	private final Map<Equipment.Type, MapLayer> mLayerChoosers;
	private HashMap<Marker, ClusterPoint> mMarkerClusterPoints;
	private WeakReference<Context> mContext;
	private MapLayer mDefaultLayer;

	public ProxyInfoWindowAdapter(Context context) {
		mLayerChoosers = new HashMap<Equipment.Type, MapLayer>();
		mContext = new WeakReference<Context>(context);
	}

	public void registerMapLayer(final Equipment.Type type, final MapLayer mapLayer) {
		mLayerChoosers.put(type, mapLayer);
	}

	public void setDefaultMapLayer(final MapLayer mapLayer) {
		mDefaultLayer = mapLayer;
	}

	@Override
	public View getInfoContents(Marker marker) {
		final ClusterPoint clusterPoint = mMarkerClusterPoints.get(marker);
		final Equipment equipement = (Equipment) clusterPoint.getPointAtOffset(0).getTag();
		MapLayer mapLayer = mLayerChoosers.get(equipement.getType());
		if (mapLayer == null) {
			mapLayer = mDefaultLayer;
		}
		return mapLayer.getInfoContents(equipement);
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
		final Equipment equipement = (Equipment) clusterPoint.getPointAtOffset(0).getTag();
		MapLayer mapLayer = mLayerChoosers.get(equipement.getType());
		if (mapLayer == null) {
			mapLayer = mDefaultLayer;
		}
		final Context context = mContext.get();

		if (context != null) {
			Intent intent = mapLayer.getClickIntent(context, equipement);
			if (intent != null) {
				context.startActivity(intent);
				return true;
			}
		}
		return false;
	}

}
