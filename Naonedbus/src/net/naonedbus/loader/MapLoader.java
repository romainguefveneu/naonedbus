package net.naonedbus.loader;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.naonedbus.bean.Equipement;
import net.naonedbus.bean.Equipement.Type;
import net.naonedbus.map.layerloader.BiclooMapLayer;
import net.naonedbus.map.layerloader.EquipementMapLayer;
import net.naonedbus.map.layerloader.MapLayer;
import android.content.Context;
import android.os.AsyncTask;

import com.twotoasters.clusterkraf.InputPoint;

public class MapLoader extends AsyncTask<Equipement.Type, ArrayList<InputPoint>, Void> {

	private final WeakReference<MapLoaderCallback> mCallback;
	private final WeakReference<Context> mContext;

	private Map<Equipement.Type, MapLayer<?>> mLoaders;

	public MapLoader(final Context context, final MapLoaderCallback callback) {
		mContext = new WeakReference<Context>(context);
		mCallback = new WeakReference<MapLoaderCallback>(callback);

		mLoaders = new HashMap<Equipement.Type, MapLayer<?>>();
		mLoaders.put(Type.TYPE_ARRET, new EquipementMapLayer(Type.TYPE_ARRET));
		mLoaders.put(Type.TYPE_BICLOO, new BiclooMapLayer());
		mLoaders.put(Type.TYPE_COVOITURAGE, new EquipementMapLayer(Type.TYPE_COVOITURAGE));
		mLoaders.put(Type.TYPE_LILA, new EquipementMapLayer(Type.TYPE_LILA));
		mLoaders.put(Type.TYPE_MARGUERITE, new EquipementMapLayer(Type.TYPE_MARGUERITE));
		mLoaders.put(Type.TYPE_PARKING, new EquipementMapLayer(Type.TYPE_PARKING));
	}

	@Override
	protected Void doInBackground(final Equipement.Type... types) {
		final Context context = mContext.get();
		if (context != null) {
			for (final Type type : types) {
				MapLayer<?> layer = mLoaders.get(type);

				publishProgress(layer.getInputPoints(context));
			}
		}
		return null;
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