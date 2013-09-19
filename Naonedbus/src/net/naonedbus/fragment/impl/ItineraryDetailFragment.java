package net.naonedbus.fragment.impl;

import java.util.ArrayList;
import java.util.List;

import net.naonedbus.R;
import net.naonedbus.bean.ItineraryWrapper;
import net.naonedbus.bean.LegWrapper;
import net.naonedbus.bean.Ligne;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.fragment.CustomListFragment;
import net.naonedbus.manager.impl.LigneManager;
import net.naonedbus.utils.FormatUtils;
import net.naonedbus.widget.adapter.impl.LegWrapperArrayAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.widget.ListAdapter;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import fr.ybo.opentripplanner.client.modele.Itinerary;
import fr.ybo.opentripplanner.client.modele.Leg;
import fr.ybo.opentripplanner.client.modele.Place;

public class ItineraryDetailFragment extends CustomListFragment {

	public static final String PARAM_ITINERARY_WRAPPER = "itineraryWrapper";
	public static final String PARAM_ITINERARY_FROM = "itineraryFrom";
	public static final String PARAM_ITINERARY_TO = "itineraryTo";

	private ItineraryWrapper mItineraryWrapper;
	private Itinerary mItinerary;

	private String mFrom;
	private String mTo;
	private String mShareContent;

	public ItineraryDetailFragment() {
		super(R.layout.fragment_itineraire_detail);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		final Bundle arguments = getArguments();
		mFrom = arguments.getString(PARAM_ITINERARY_FROM);
		mTo = arguments.getString(PARAM_ITINERARY_TO);

		mItineraryWrapper = (ItineraryWrapper) arguments.getSerializable(PARAM_ITINERARY_WRAPPER);
		mItinerary = mItineraryWrapper.getItinerary();
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
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_itinerary_detail, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (item.getItemId() == R.id.menu_share) {
			share();
		}
		return super.onOptionsItemSelected(item);
	}

	private void share() {
		final Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, mShareContent);
		startActivity(Intent.createChooser(shareIntent, getString(R.string.action_share)));
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

			if (add) {
				legWrappers.add(wrapper);

			}
		}

		mShareContent = buildItineraryDescription(legWrappers);

		final LegWrapperArrayAdapter adapter = new LegWrapperArrayAdapter(context, legWrappers);
		final AsyncResult<ListAdapter> result = new AsyncResult<ListAdapter>();
		result.setResult(adapter);

		return result;
	}

	private String buildItineraryDescription(final List<LegWrapper> wrappers) {
		final StringBuilder builder = new StringBuilder(mFrom).append("\n");

		for (int i = 0; i < wrappers.size(); i++) {
			final LegWrapper legWrapper = wrappers.get(i);
			final Leg leg = legWrapper.getLeg();
			final Ligne ligne = legWrapper.getLigne();
			final Place from = leg.from;
			final Place to = leg.to;

			builder.append(legWrapper.getFromTime()).append(" : ");
			if ("WALK".equals(leg.mode)) {
				builder.append(getString(R.string.itinerary_go_to, to.name));
			} else if (ligne != null) {
				builder.append(FormatUtils.formatLigneArretSens(getActivity(), ligne.getLettre(), from.name,
						leg.headsign));
				builder.append("\n");
				builder.append(legWrapper.getToTime()).append(" : ");
				builder.append(getString(R.string.itinerary_get_off, to.name));
				if (i < wrappers.size() - 1)
					builder.append("\n");
			}
			builder.append("\n");
		}
		builder.append(mTo);

		return builder.toString();
	}
}
