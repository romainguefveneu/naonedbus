package net.naonedbus.loader;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.naonedbus.BuildConfig;
import net.naonedbus.bean.Equipement;
import net.naonedbus.bean.Equipement.Type;
import net.naonedbus.map.layer.loader.BiclooMapLoader;
import net.naonedbus.map.layer.loader.EquipementMapLoader;
import net.naonedbus.map.layer.loader.MapLayerLoader;
import net.naonedbus.map.layer.loader.ParkingMapLoader;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.twotoasters.clusterkraf.InputPoint;

public class MapLoader extends AsyncTask<Equipement.Type, ArrayList<InputPoint>, Void> {

	private static String LOG_TAG = "MapLoader";
	private static boolean DBG = BuildConfig.DEBUG;

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
		mLoaders.put(Type.TYPE_PARKING, new ParkingMapLoader());
	}

	@Override
	protected Void doInBackground(final Equipement.Type... types) {
		final Context context = mContext.get();

		if (context != null) {
			for (final Type type : types) {
				final MapLayerLoader loader = mLoaders.get(type);

				publishProgress(loader.getInputPoints(context));
			}
		} else {
			if (DBG)
				Log.w(LOG_TAG, "doInBackground context null");
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
		} else {
			if (DBG)
				Log.w(LOG_TAG, "onProgressUpdate callback null");
		}
	}

	@Override
	protected void onPreExecute() {
		MapLoaderCallback callback = mCallback.get();
		if (callback != null) {
			callback.onMapLoaderStart();
		} else {
			if (DBG)
				Log.w(LOG_TAG, "onPreExecute callback null");
		}
	}

	@Override
	protected void onPostExecute(Void result) {
		MapLoaderCallback callback = mCallback.get();
		if (callback != null) {
			callback.onMapLoaderEnd();
		} else {
			if (DBG)
				Log.w(LOG_TAG, "onPostExecute callback null");
		}
	}

	public interface MapLoaderCallback {
		void onMapLoaderStart();

		void onMapLoaderEnd();

		void onLayerLoaded(ArrayList<InputPoint> result);
	}
}