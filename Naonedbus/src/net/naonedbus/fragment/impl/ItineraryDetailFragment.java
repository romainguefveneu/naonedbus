package net.naonedbus.fragment.impl;

import java.util.ArrayList;
import java.util.List;

import net.naonedbus.R;
import net.naonedbus.bean.LegWrapper;
import net.naonedbus.bean.Ligne;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.fragment.CustomListFragment;
import net.naonedbus.manager.impl.LigneManager;
import net.naonedbus.utils.FormatUtils;
import net.naonedbus.widget.adapter.impl.LegWrapperArrayAdapter;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.widget.ListAdapter;
import fr.ybo.opentripplanner.client.modele.Itinerary;
import fr.ybo.opentripplanner.client.modele.Leg;

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
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getListView().setDivider(null);
		getListView().setDividerHeight(0);
	}

	@Override
	public void onStart() {
		super.onStart();
		loadContent();
	}

	@Override
	protected AsyncResult<ListAdapter> loadContent(final Context context, final Bundle bundle) {

		final LigneManager ligneManager = LigneManager.getInstance();

		final List<LegWrapper> legWrappers = new ArrayList<LegWrapper>();
		for (final Leg leg : mItinerary.legs) {
			boolean add = true;
			final LegWrapper wrapper = new LegWrapper(leg);

			final long startTime = leg.startTime.getTime();
			final long endTime = leg.endTime.getTime();

			wrapper.setTime(FormatUtils.formatMinutes(context, endTime - startTime));
			wrapper.setFromTime(DateUtils.formatDateTime(context, startTime, DateUtils.FORMAT_SHOW_TIME));
			wrapper.setToTime(DateUtils.formatDateTime(context, endTime, DateUtils.FORMAT_SHOW_TIME));
			wrapper.setDistance(FormatUtils.formatMetres(context, leg.distance));

			if (!"WALK".equals(leg.mode) && !TextUtils.isEmpty(leg.route)) {
				final Ligne ligne = ligneManager.getSingleByLetter(context.getContentResolver(), leg.route);
				wrapper.setLigne(ligne);
			} else if ("WALK".equals(leg.mode) && leg.distance < 50) {
				add = false;
			}

			if (add)
				legWrappers.add(wrapper);
		}

		final LegWrapperArrayAdapter adapter = new LegWrapperArrayAdapter(context, legWrappers);
		final AsyncResult<ListAdapter> result = new AsyncResult<ListAdapter>();
		result.setResult(adapter);

		return result;
	}
}
