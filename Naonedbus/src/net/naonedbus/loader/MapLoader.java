package net.naonedbus.loader;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import net.naonedbus.bean.Equipement;
import net.naonedbus.bean.Equipement.Type;
import net.naonedbus.manager.impl.EquipementManager;
import net.naonedbus.map.MarkerInfo;
import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;
import com.twotoasters.clusterkraf.InputPoint;

public class MapLoader extends AsyncTask<Equipement.Type, ArrayList<InputPoint>, Void> {

	private final WeakReference<MapLoaderCallback> mCallback;
	private final WeakReference<Context> mContext;

	public MapLoader(final Context context, final MapLoaderCallback callback) {
		mContext = new WeakReference<Context>(context);
		mCallback = new WeakReference<MapLoaderCallback>(callback);
	}

	@Override
	protected Void doInBackground(final Equipement.Type... types) {

		final EquipementManager manager = EquipementManager.getInstance();
		final Context context = mContext.get();
		if (context != null) {
			for (final Type type : types) {
				final List<Equipement> equipements = manager.getEquipementsByType(context.getContentResolver(), type);
				final ArrayList<InputPoint> inputPoints = new ArrayList<InputPoint>(equipements.size());

				for (final Equipement equipement : equipements) {
					final InputPoint inputPoint = createInputPoint(equipement);
					inputPoints.add(inputPoint);
				}

				publishProgress(inputPoints);
			}
		}
		return null;
	}

	private InputPoint createInputPoint(final Equipement equipement) {
		final LatLng latLng = new LatLng(equipement.getLatitude(), equipement.getLongitude());
		final InputPoint inputPoint = new InputPoint(latLng);
		final MarkerInfo markerInfo = new MarkerInfo(equipement.getId(), equipement.getNom(), equipement.getType());
		inputPoint.setTag(markerInfo);

		return inputPoint;
	}

	@Override
	protected void onProgressUpdate(final ArrayList<InputPoint>... values) {
		final MapLoaderCallback callback = mCallback.get();
		if (callback != null) {
			for (final ArrayList<InputPoint> value : values) {
				callback.onLayerLoaded(value);
			}
		}
	}

	public interface MapLoaderCallback {
		void onLayerLoaded(ArrayList<InputPoint> result);
	}
}