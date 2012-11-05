package net.naonedbus.fragment.impl;

import java.io.IOException;
import java.util.List;

import net.naonedbus.R;
import net.naonedbus.activity.impl.FavorisImportActivity;
import net.naonedbus.activity.impl.HoraireActivity;
import net.naonedbus.bean.Favori;
import net.naonedbus.bean.NextHoraireTask;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.fragment.CustomFragmentActions;
import net.naonedbus.fragment.CustomListFragment;
import net.naonedbus.intent.ParamIntent;
import net.naonedbus.manager.impl.FavoriManager;
import net.naonedbus.manager.impl.HoraireManager;
import net.naonedbus.widget.adapter.impl.FavoriArrayAdapter;

import org.joda.time.DateMidnight;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.bugsense.trace.BugSenseHandler;
import com.google.gson.JsonSyntaxException;

public class FavorisFragment extends CustomListFragment implements CustomFragmentActions {

	private static final String LOG_TAG = FavorisFragment.class.getSimpleName();

	private static final String FORMAT_DELAY_MIN = "dans %d min";
	private static final String FORMAT_DELAY_HOUR = "dans %d h";
	private static final String ACTION_UPDATE_DELAYS = "net.naonedbus.action.UPDATE_DELAYS";
	private static final Integer MIN_HOUR = 60;
	private static final Integer MIN_DURATION = 0;
	private static final int SORT_NOM = 0;
	private static final int SORT_DISTANCE = 1;

	private final static IntentFilter intentFilter;
	static {
		intentFilter = new IntentFilter();
		intentFilter.addAction(FavorisFragment.ACTION_UPDATE_DELAYS);
		intentFilter.addAction(Intent.ACTION_TIME_TICK);
		intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
	}

	// private final static SparseArray<Comparator<Item>> comparators = new
	// SparseArray<Comparator<Item>>();
	// static {
	// comparators.append(SORT_NOM, new FavoriComparator());
	// comparators.append(SORT_DISTANCE, new FavoriDistanceComparator());
	// }

	/**
	 * Reçoit les intents de notre intentFilter
	 */
	private final BroadcastReceiver intentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(LOG_TAG, "onReceive : " + intent);
			final int id = intent.getIntExtra("id", -1);
			if (id != -1) {
				forceLoadHorairesFavoris(id);
			} else {
				loadHorairesFavoris();
			}
		}
	};

	private FavoriManager mFavoriManager;

	public FavorisFragment() {
		super(R.string.title_fragment_favoris, R.layout.fragment_listview);
		mFavoriManager = FavoriManager.getInstance();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActivity().registerReceiver(intentReceiver, intentFilter);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_import:
			startActivity(new Intent(getActivity(), FavorisImportActivity.class));
			break;
		default:
			return false;
		}
		return true;
	}

	@Override
	public void onResume() {
		super.onResume();
		refreshContent();
	}

	@Override
	public void onDetach() {
		getActivity().unregisterReceiver(intentReceiver);
		super.onDetach();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu) {
		final MenuInflater menuInflater = getSherlockActivity().getSupportMenuInflater();
		menuInflater.inflate(R.menu.fragment_favoris, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {

	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		final Favori item = (Favori) l.getItemAtPosition(position);
		final ParamIntent intent = new ParamIntent(getActivity(), HoraireActivity.class);
		intent.putExtra(HoraireActivity.Param.idArret, item._id);
		startActivity(intent);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setEmptyMessageValues(R.string.error_title_empty_favori, R.string.error_summary_empty_favori, R.drawable.favori);
	}

	@Override
	protected AsyncResult<ListAdapter> loadContent(final Context context) {
		final HoraireManager horaireManager = HoraireManager.getInstance();
		final DateMidnight today = new DateMidnight();

		final AsyncResult<ListAdapter> result = new AsyncResult<ListAdapter>();
		final List<Favori> favoris = mFavoriManager.getAll(context.getContentResolver());

		int position = 0;
		for (Favori favori : favoris) {
			if (!horaireManager.isInDB(context.getContentResolver(), favori, today)) {
				final NextHoraireTask horaireTask = new NextHoraireTask();
				horaireTask.setContext(context).setArret(favori).setId(position).setLimit(1)
						.setActionCallback(ACTION_UPDATE_DELAYS);

				horaireManager.schedule(horaireTask);
			} else {
				loadHorairesFavoris(position);
			}
			position++;
		}

		final FavoriArrayAdapter adapter = new FavoriArrayAdapter(context, favoris);
		result.setResult(adapter);

		return result;
	}

	@Override
	protected void onPostExecute() {
		loadHorairesFavoris();
	}

	/**
	 * Lancer le chargement des tous les horaires
	 */
	private void loadHorairesFavoris(Integer... params) {
		if (getListAdapter() != null) {
			new LoadHoraires().execute(params);
		}
	}

	/**
	 * Lancer le chargement des tous les horaires, même si un thread est déjà en
	 * cours
	 */
	private void forceLoadHorairesFavoris(Integer... params) {
		if (getListAdapter() != null) {
			new LoadHoraires().execute(params);
		}
	}

	/**
	 * Classe de chargement des horaires
	 * 
	 * @author romain.guefveneu
	 * 
	 */
	private class LoadHoraires extends AsyncTask<Integer, Void, Boolean> {

		final HoraireManager horaireManager = HoraireManager.getInstance();
		final DateMidnight today = new DateMidnight();

		@Override
		protected void onPreExecute() {
			Log.d(LOG_TAG, "LoadHoraires start");
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(Integer... params) {
			Thread.currentThread().setName("LoadHoraires");
			Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
			Boolean result = true;

			try {
				if (params.length == 0) {
					for (int i = 0; i < getListAdapter().getCount(); i++) {
						updateAdapter(i);
					}
				} else {
					for (int i = 0; i < params.length; i++) {
						updateAdapter(params[i]);
					}
				}
			} catch (JsonSyntaxException e) {
				BugSenseHandler.sendExceptionMessage("Erreur lors du chargement des horaires", null, e);
				result = false;
			} catch (IOException e) {
				BugSenseHandler.sendExceptionMessage("Erreur lors du chargement des horaires", null, e);
				result = false;
			}

			return result;
		}

		/**
		 * Mettre à jour la ligne de l'adapter
		 * 
		 * @throws IOException
		 */
		private void updateAdapter(int position) throws IOException {
			if (position >= getListAdapter().getCount())
				return;

			final Favori favoriItem = (Favori) getListAdapter().getItem(position);
			if (horaireManager.isInDB(getActivity().getContentResolver(), favoriItem, today)) {
				final Integer delay = horaireManager.getMinutesToNextHoraire(getActivity().getContentResolver(),
						favoriItem);
				updateItemTime(favoriItem, delay);
				publishProgress();
			}
		}

		/**
		 * Mettre à jour les informations de délais d'un favori
		 */
		private void updateItemTime(Favori favoriItem, Integer delay) {
			if (delay != null) {
				if (delay >= MIN_DURATION) {
					if (delay == MIN_DURATION) {
						favoriItem.delay = getString(R.string.msg_depart_proche);
					} else if (delay <= MIN_HOUR) {
						favoriItem.delay = String.format(FORMAT_DELAY_MIN, delay);
					} else {
						favoriItem.delay = String.format(FORMAT_DELAY_HOUR, (delay / MIN_HOUR));
					}
				}
			} else {
				favoriItem.delay = getString(R.string.msg_aucun_depart_24h);
			}
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			((FavoriArrayAdapter) getListAdapter()).notifyDataSetChanged();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			Log.d(LOG_TAG, "LoadHoraires end");
		}

	}
}
