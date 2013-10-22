package net.naonedbus.map;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import net.naonedbus.R;
import net.naonedbus.bean.Equipment;
import net.naonedbus.map.layer.MapLayer;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.twotoasters.clusterkraf.ClusterPoint;
import com.twotoasters.clusterkraf.MarkerOptionsChooser;

public class ToastedMarkerOptionsChooser extends MarkerOptionsChooser {

	private final WeakReference<Context> mContextRef;
	private final Paint mClusterPaintLarge;
	private final Paint mClusterPaintMedium;
	private final Paint mClusterPaintSmall;
	private final Map<Equipment.Type, MapLayer> mLayerChoosers;
	private MapLayer mDefaultLayer;

	public ToastedMarkerOptionsChooser(final Context context) {
		mContextRef = new WeakReference<Context>(context);

		mLayerChoosers = new HashMap<Equipment.Type, MapLayer>();

		final Resources res = context.getResources();

		mClusterPaintMedium = new Paint();
		mClusterPaintMedium.setColor(Color.WHITE);
		mClusterPaintMedium.setAlpha(255);
		mClusterPaintMedium.setTextAlign(Paint.Align.CENTER);
		mClusterPaintMedium.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
		mClusterPaintMedium.setTextSize(res.getDimension(R.dimen.cluster_text_size_medium));
		mClusterPaintMedium.setAntiAlias(true);

		mClusterPaintSmall = new Paint(mClusterPaintMedium);
		mClusterPaintSmall.setTextSize(res.getDimension(R.dimen.cluster_text_size_small));

		mClusterPaintLarge = new Paint(mClusterPaintMedium);
		mClusterPaintLarge.setTextSize(res.getDimension(R.dimen.cluster_text_size_large));
	}

	public void registerMapLayer(final Equipment.Type type, final MapLayer mapLayer) {
		mLayerChoosers.put(type, mapLayer);
	}

	public void setDefaultMapLayer(final MapLayer mapLayer) {
		mDefaultLayer = mapLayer;
	}

	@Override
	public void choose(final MarkerOptions markerOptions, final ClusterPoint clusterPoint) {
		final Context context = mContextRef.get();
		if (context != null) {
			final Resources res = context.getResources();
			final boolean isCluster = clusterPoint.size() > 1;
			if (isCluster) {
				final int clusterSize = clusterPoint.size();
				final BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(getClusterBitmap(res,
						R.drawable.ic_map_pin_cluster, clusterSize));
				final String title = String.valueOf(clusterSize);
				markerOptions.icon(icon);
				markerOptions.title(title);
			} else {
				final Equipment equipment = (Equipment) clusterPoint.getPointAtOffset(0).getTag();
				MapLayer mapLayer = mLayerChoosers.get(equipment.getType());
				if (mapLayer == null) {
					mapLayer = mDefaultLayer;
				}
				mapLayer.chooseMarker(markerOptions, clusterPoint);
			}
			markerOptions.anchor(0.5f, 1.0f);
		}
	}

	@SuppressLint("NewApi")
	private Bitmap getClusterBitmap(final Resources res, final int resourceId, final int clusterSize) {
		final BitmapFactory.Options options = new BitmapFactory.Options();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			options.inMutable = true;
		}
		Bitmap bitmap = BitmapFactory.decodeResource(res, resourceId, options);
		if (bitmap.isMutable() == false) {
			bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
		}

		final Canvas canvas = new Canvas(bitmap);

		Paint paint = null;
		float originY;
		if (clusterSize < 100) {
			paint = mClusterPaintLarge;
			originY = bitmap.getHeight() * 0.72f;
		} else if (clusterSize < 1000) {
			paint = mClusterPaintMedium;
			originY = bitmap.getHeight() * 0.72f;
		} else {
			paint = mClusterPaintSmall;
			originY = bitmap.getHeight() * 0.72f;
		}

		canvas.drawText(String.valueOf(clusterSize), bitmap.getWidth() * 0.52f, originY, paint);

		return bitmap;
	}
}