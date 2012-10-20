package net.naonedbus.fragment.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.naonedbus.R;
import net.naonedbus.bean.InfoTraficDetail;
import net.naonedbus.bean.InfoTraficLigne;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.fragment.CustomListFragment;
import net.naonedbus.rest.controller.impl.InfoTraficLignesController;
import net.naonedbus.widget.adapter.SectionAdapter;
import net.naonedbus.widget.adapter.impl.InfoTraficDetailArrayAdapter;
import net.naonedbus.widget.indexer.impl.InfoTraficIndexer;
import android.content.Context;
import android.os.Bundle;
import android.widget.ListAdapter;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class TanActuFragment extends CustomListFragment {

	public TanActuFragment() {
		super(R.string.title_fragment_tan_actu, R.layout.fragment_listview_section);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setRetainInstance(true);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu) {
		final MenuInflater menuInflater = getSherlockActivity().getSupportMenuInflater();
		menuInflater.inflate(R.menu.fragment_en_direct, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_refresh:
			refreshContent();
			break;
		}
		return true;
	}

	@Override
	protected AsyncResult<ListAdapter> loadContent(final Context context) {
		final AsyncResult<ListAdapter> result = new AsyncResult<ListAdapter>();
		try {
			final InfoTraficLignesController infoTraficLignesController = new InfoTraficLignesController();
			final List<InfoTraficLigne> infoTraficLignes = infoTraficLignesController.getAll();
			final List<InfoTraficDetail> infoTraficDetails = new ArrayList<InfoTraficDetail>();

			for (InfoTraficLigne infoTraficLigne : infoTraficLignes) {
				for (InfoTraficDetail infoTraficDetail : infoTraficLigne.getInfosTrafic()) {
					infoTraficDetail.setInfoTraficLigne(infoTraficLigne);
				}
				infoTraficDetails.addAll(infoTraficLigne.getInfosTrafic());
			}

			SectionAdapter<InfoTraficDetail> adapter = new InfoTraficDetailArrayAdapter(context, infoTraficDetails);
			adapter.setIndexer(new InfoTraficIndexer());

			result.setResult(adapter);
		} catch (IOException e) {
			result.setException(e);
		}
		return result;
	}

}
