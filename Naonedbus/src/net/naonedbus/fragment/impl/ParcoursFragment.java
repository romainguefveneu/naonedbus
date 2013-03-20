package net.naonedbus.fragment.impl;

import java.util.List;

import net.naonedbus.R;
import net.naonedbus.activity.impl.ArretDetailActivity;
import net.naonedbus.bean.Arret;
import net.naonedbus.bean.Equipement;
import net.naonedbus.bean.Equipement.Type;
import net.naonedbus.bean.Parcours;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.fragment.CustomListFragment;
import net.naonedbus.manager.impl.ArretManager;
import net.naonedbus.manager.impl.EquipementManager;
import net.naonedbus.manager.impl.ParcoursManager;
import net.naonedbus.widget.adapter.impl.ParcoursAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

public class ParcoursFragment extends CustomListFragment {

	public static final String PARAM_ID_STATION = "idStation";

	private Equipement mStation;
	private final EquipementManager mEquipementManager;

	public ParcoursFragment() {
		super(R.string.title_fragment_parcours, R.layout.fragment_listview);
		mEquipementManager = EquipementManager.getInstance();
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		final int idStation = getArguments().getInt(PARAM_ID_STATION);
		mStation = mEquipementManager.getSingle(getActivity().getContentResolver(), Type.TYPE_ARRET, idStation);

		getActivity().setTitle(mStation.getNom());

		loadContent();
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
		super.onListItemClick(l, v, position, id);
		final Parcours item = (Parcours) l.getItemAtPosition(position);

		final ArretManager arretManager = ArretManager.getInstance();
		final Arret arret = arretManager.getSingle(getActivity().getContentResolver(), item._id);

		final Intent intent = new Intent(getActivity(), ArretDetailActivity.class);
		intent.putExtra(ArretDetailActivity.PARAM_ARRET, arret);

		startActivity(intent);
	}

	@Override
	protected AsyncResult<ListAdapter> loadContent(final Context context) {
		final ParcoursManager parcoursManager = ParcoursManager.getInstance();
		final List<Parcours> parcoursList = parcoursManager.getParcoursList(context.getContentResolver(),
				mStation.getNormalizedNom());
		final ListAdapter adapter = new ParcoursAdapter(context, parcoursList);
		final AsyncResult<ListAdapter> result = new AsyncResult<ListAdapter>();
		result.setResult(adapter);
		return result;
	}

}
