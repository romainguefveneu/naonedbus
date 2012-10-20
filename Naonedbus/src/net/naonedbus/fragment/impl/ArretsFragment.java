package net.naonedbus.fragment.impl;

import java.util.Comparator;
import java.util.List;

import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.activity.impl.ArretsActivity.OnChangeSens;
import net.naonedbus.activity.impl.HoraireActivity;
import net.naonedbus.bean.Arret;
import net.naonedbus.bean.Sens;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.comparator.ArretComparator;
import net.naonedbus.comparator.ArretOrdreComparator;
import net.naonedbus.fragment.CustomFragmentActions;
import net.naonedbus.fragment.CustomListFragment;
import net.naonedbus.intent.ParamIntent;
import net.naonedbus.manager.impl.ArretManager;
import net.naonedbus.provider.impl.MyLocationProvider;
import net.naonedbus.widget.adapter.impl.ArretArrayAdapter;
import net.naonedbus.widget.adapter.impl.ArretArrayAdapter.ViewType;
import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class ArretsFragment extends CustomListFragment implements CustomFragmentActions, OnChangeSens {

	private final static int SORT_NOM = R.id.menu_sort_name;
	private final static int SORT_ORDRE = R.id.menu_sort_ordre;

	protected final SparseArray<Comparator<Arret>> mComparators;

	protected MyLocationProvider mLocationProvider;
	protected int mCurrentSortPreference = SORT_ORDRE;

	private Sens mSens;

	public ArretsFragment() {
		super(R.string.title_fragment_arrets, R.layout.fragment_listview);
		this.mLocationProvider = NBApplication.getLocationProvider();

		this.mComparators = new SparseArray<Comparator<Arret>>();
		this.mComparators.append(SORT_NOM, new ArretComparator());
		this.mComparators.append(SORT_ORDRE, new ArretOrdreComparator());
	}

	@Override
	public void onCreateOptionsMenu(Menu menu) {
		final MenuInflater menuInflater = getSherlockActivity().getSupportMenuInflater();
		menuInflater.inflate(R.menu.fragment_arrets, menu);
		menu.findItem(mCurrentSortPreference).setChecked(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		item.setChecked(true);
		final ArretArrayAdapter adapter = (ArretArrayAdapter) getListAdapter();

		switch (item.getItemId()) {
		case R.id.menu_sort_name:
			mCurrentSortPreference = SORT_NOM;
			adapter.setViewType(ViewType.TYPE_STANDARD);
			sort();
			break;
		case R.id.menu_sort_ordre:
			mCurrentSortPreference = SORT_ORDRE;
			adapter.setViewType(ViewType.TYPE_METRO);
			sort();
			break;
		}
		return false;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		final Arret item = (Arret) l.getItemAtPosition(position);
		final ParamIntent intent = new ParamIntent(getActivity(), HoraireActivity.class);
		intent.putExtra(HoraireActivity.Param.idArret, item._id);
		startActivity(intent);
	}

	/**
	 * Trier les parkings selon les préférences.
	 */
	private void sort() {
		final ArretArrayAdapter adapter = (ArretArrayAdapter) getListAdapter();
		sort(adapter);
		adapter.notifyDataSetChanged();
	}

	/**
	 * Trier les parkings selon les préférences.
	 * 
	 * @param adapter
	 */
	private void sort(ArretArrayAdapter adapter) {
		final Comparator<Arret> comparator = mComparators.get(mCurrentSortPreference);
		adapter.sort(comparator);
	}

	@Override
	protected AsyncResult<ListAdapter> loadContent(final Context context) {
		if (mSens == null)
			cancelLoading();

		final AsyncResult<ListAdapter> result = new AsyncResult<ListAdapter>();
		try {
			final ArretManager arretManager = ArretManager.getInstance();
			final List<Arret> arrets = arretManager.getAll(context.getContentResolver(), mSens.codeLigne, mSens.code);
			final ArretArrayAdapter adapter = new ArretArrayAdapter(context, arrets);

			result.setResult(adapter);
		} catch (Exception e) {
			result.setException(e);
		}
		return result;
	}

	@Override
	protected void onPostExecute() {
		sort();
	}

	@Override
	public void onChangeSens(Sens sens) {
		mSens = sens;
		refreshContent();
	}

}
