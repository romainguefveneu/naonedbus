package net.naonedbus.fragment.impl;

import net.naonedbus.R;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.fragment.CustomListFragment;
import net.naonedbus.widget.adapter.impl.LegArrayAdapter;
import android.content.Context;
import android.os.Bundle;
import android.widget.ListAdapter;
import fr.ybo.opentripplanner.client.modele.Itinerary;

public class ItineraryDetailFragment extends CustomListFragment {

	public static final String PARAM_ITINERARY = "itinerary";

	private Itinerary mItinerary;

	public ItineraryDetailFragment() {
		super(R.layout.fragment_listview);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mItinerary = (Itinerary) getArguments().getSerializable(PARAM_ITINERARY);
	}

	@Override
	public void onStart() {
		super.onStart();
		loadContent();
	}

	@Override
	protected AsyncResult<ListAdapter> loadContent(final Context context, final Bundle bundle) {

		final LegArrayAdapter adapter = new LegArrayAdapter(context, mItinerary.legs);
		final AsyncResult<ListAdapter> result = new AsyncResult<ListAdapter>();
		result.setResult(adapter);

		return result;
	}

}
