package net.naonedbus.loader;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.naonedbus.bean.Equipement;
import net.naonedbus.bean.Equipement.Type;
import net.naonedbus.map.layer.loader.BiclooMapLoader;
import net.naonedbus.map.layer.loader.EquipementMapLoader;
import net.naonedbus.map.layer.loader.MapLayerLoader;
import android.content.Context;
import android.os.AsyncTask;

import com.twotoasters.clusterkraf.InputPoint;

public class MapLoader extends AsyncTask<Equipement.Type, ArrayList<InputPoint>, Void> {

	private final WeakReference<MapLoaderCallback> mCallback;
	private final WeakReference<Context> mContext;

	private final Map<Equipement.Type, MapLayerLoader> mLoaders;

	public MapLoader(final Context context, final MapLoaderCallback callback) {
		mContext = new WeakReference<Context>(context);
		mCallback = new WeakReference<MapLoaderCallback>(callback);

		mLoaders = new HashMap<Equipement.Type, MapLayerLoader>();
		mLoaders.put(Type.TYPE_ARRET, new EquipementMapLoader(Type.TYPE_ARRET));
		mLoaders.put(Type.TYPE_BICLOO, new BiclooMapLoader());
		mLoaders.put(Type.TYPE_COVOITURAGE, new EquipementMapLoader(Type.TYPE_COVOITURAGE));
		mLoaders.put(Type.TYPE_LILA, new EquipementMapLoader(Type.TYPE_LILA));
		mLoaders.put(Type.TYPE_MARGUERITE, new EquipementMapLoader(Type.TYPE_MARGUERITE));
		mLoaders.put(Type.TYPE_PARKING, new EquipementMapLoader(Type.TYPE_PARKING));
	}

	@Override
	protected Void doInBackground(final Equipement.Type... types) {
		final Context context = mContext.get();
		if (context != null) {
			for (final Type type : types) {
				final MapLayerLoader loader = mLoaders.get(type);

				publishProgress(loader.getInputPoints(context));
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