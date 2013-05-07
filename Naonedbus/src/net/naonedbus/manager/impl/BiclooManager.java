package net.naonedbus.manager.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.naonedbus.BuildConfig;
import net.naonedbus.bean.Bicloo;
import net.naonedbus.bean.Equipement;
import net.naonedbus.provider.impl.EquipementProvider;
import net.naonedbus.provider.table.EquipementTable;
import net.naonedbus.rest.controller.impl.BiclooController;

import org.joda.time.DateTime;
import org.json.JSONException;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

public class BiclooManager {

	public static abstract class BiclooObserver {
		private final Handler mHandler;

		public BiclooObserver(final Handler handler) {
			mHandler = handler;
		}

		private final void dispatchChange() {
			if (mHandler == null) {
				onChange();
			} else {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						BiclooObserver.this.onChange();
					}
				});
			}
		}

		public abstract void onChange();
	}

	private static final String LOG_TAG = "BiclooManager";
	private static final boolean DBG = BuildConfig.DEBUG;

	private static final int CACHE_LIMITE_MINUTES = 5;
	private static BiclooManager sInstance;

	private final List<BiclooObserver> mObservers;

	private final ExecutorService mExecutor;
	private List<Bicloo> mCache;
	private DateTime mDateLimit;

	public static synchronized BiclooManager getInstance() {
		if (sInstance == null) {
			sInstance = new BiclooManager();
		}

		return sInstance;
	}

	private BiclooManager() {
		mCache = new ArrayList<Bicloo>();
		mExecutor = Executors.newSingleThreadExecutor();
		mObservers = new ArrayList<BiclooManager.BiclooObserver>();
	}

	/**
	 * Charger les données et gérer le cache.
	 * 
	 * @throws IOException
	 * @throws JSONException
	 */
	private synchronized void init(final Context context) throws IOException, JSONException {
		final DateTime now = new DateTime();

		if (mCache.isEmpty() || now.isAfter(mDateLimit)) {
			final BiclooController controller = new BiclooController();
			mCache.clear();
			mCache = controller.getAll(context.getResources());
			mDateLimit = now.plusMinutes(CACHE_LIMITE_MINUTES);

			for (final BiclooObserver observer : mObservers)
				observer.dispatchChange();

			saveToDatabase(context);
		}
	}

	public void clearCache() {
		mCache.clear();
	}

	private void saveToDatabase(final Context context) {
		final Runnable task = new Runnable() {
			@Override
			public void run() {
				if (DBG)
					Log.d(LOG_TAG, Integer.toHexString(hashCode()) + "\tDébut de sauvegarde des données bicloos...");

				final Uri.Builder builder = EquipementProvider.CONTENT_URI.buildUpon();
				builder.path(EquipementProvider.EQUIPEMENTS_TYPE_URI_PATH_QUERY);
				builder.appendPath(String.valueOf(Equipement.Type.TYPE_BICLOO.getId()));
				context.getContentResolver().delete(builder.build(), null, null);

				fillDB(context.getContentResolver(), mCache);

				if (DBG)
					Log.d(LOG_TAG, Integer.toHexString(hashCode()) + "\tFin de sauvegarde des données bicloos.");
			}
		};

		mExecutor.submit(task);
	}

	private void fillDB(final ContentResolver contentResolver, final List<Bicloo> bicloos) {
		// Ajouter les horaires dans la db
		final ContentValues[] values = new ContentValues[bicloos.size()];
		for (int i = 0; i < bicloos.size(); i++) {
			values[i] = getContentValues(bicloos.get(i));
		}

		contentResolver.bulkInsert(EquipementProvider.CONTENT_URI, values);
	}

	private ContentValues getContentValues(final Bicloo bicloo) {
		final ContentValues values = new ContentValues();
		values.put(EquipementTable._ID, bicloo.getNumber());
		values.put(EquipementTable.ID_TYPE, Equipement.Type.TYPE_BICLOO.getId());
		values.put(EquipementTable.NOM, bicloo.getName());
		values.put(EquipementTable.NORMALIZED_NOM, bicloo.getName());
		values.put(EquipementTable.ADRESSE, bicloo.getAddress());
		values.put(EquipementTable.LATITUDE, bicloo.getLocation().getLatitude());
		values.put(EquipementTable.LONGITUDE, bicloo.getLocation().getLongitude());

		return values;
	}

	/**
	 * Récupérer les bicloos.
	 * 
	 * @return La liste des bicloos
	 * @throws IOException
	 * @throws JSONException
	 */
	public List<Bicloo> getAll(final Context context) throws IOException, JSONException {
		init(context);
		return new ArrayList<Bicloo>(mCache);
	}

	public void registerObserver(final BiclooObserver observer) {
		mObservers.add(observer);
	}

	public void unregisterObserver(final BiclooObserver observer) {
		mObservers.remove(observer);
	}

}
