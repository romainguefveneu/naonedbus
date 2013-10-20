package net.naonedbus.fragment.impl;

import java.util.ArrayList;
import java.util.List;

import net.naonedbus.R;
import net.naonedbus.bean.ItineraryWrapper;
import net.naonedbus.bean.LegWrapper;
import net.naonedbus.bean.LegWrapper.Type;
import net.naonedbus.bean.Route;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListAdapter;
import android.widget.TextView;

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

	private View mProgressView;

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
	protected void bindView(final View view, final Bundle savedInstanceState) {
		super.bindView(view, savedInstanceState);

		final TextView title = (TextView) view.findViewById(R.id.itemTime);
		// title.setTypeface(FontUtils.getRobotoLight(getActivity()));
		title.setText(mItineraryWrapper.getTime());

		final TextView walkTime = (TextView) view.findViewById(R.id.itemWalkTime);
		walkTime.setText(mItineraryWrapper.getWalkTime());

		final View shareView = view.findViewById(R.id.menu_share);
		shareView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				share();
			}
		});

		mProgressView = view.findViewById(android.R.id.progress);
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
	protected void showLoader() {
		mProgressView.setVisibility(View.VISIBLE);
		getListView().setVisibility(View.GONE);
	}

	@Override
	protected void showContent() {
		mProgressView.setVisibility(View.GONE);
		getListView().setVisibility(View.VISIBLE);
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

		final List<Leg> legs = mItinerary.legs;
		final int count = legs.size();
		for (int i = 0; i < count; i++) {

			boolean add = true;
			final Leg leg = legs.get(i);
			final LegWrapper fromWrapper = new LegWrapper(Type.IN);
			final LegWrapper toWrapper = new LegWrapper(Type.OUT);

			final long startTime = leg.startTime.getTime();
			final long endTime = leg.endTime.getTime();

			fromWrapper.setPlace(leg.from);
			fromWrapper.setDuration(FormatUtils.formatMinutes(context, endTime - startTime));
			fromWrapper.setDistance(FormatUtils.formatMetres(context, leg.distance));
			fromWrapper.setTime(DateUtils.formatDateTime(context, startTime, DateUtils.FORMAT_SHOW_TIME));
			fromWrapper.setHeadsign(leg.headsign);
			fromWrapper.setMode(leg.mode);

			toWrapper.setPlace(leg.to);
			toWrapper.setTime(DateUtils.formatDateTime(context, endTime, DateUtils.FORMAT_SHOW_TIME));
			toWrapper.setMode(leg.mode);

			if (!"WALK".equals(leg.mode) && !TextUtils.isEmpty(leg.route)) {
				final Route ligne = ligneManager.getSingleByLetter(context.getContentResolver(), leg.route);
				fromWrapper.setLigne(ligne);
			} else if ("WALK".equals(leg.mode) && leg.distance < 50) {
				add = false;
			}

			if (add) {
				legWrappers.add(fromWrapper);
				if (!"WALK".equals(leg.mode) || i == count - 1) {
					fromWrapper.setIsTrip(true);
					toWrapper.setIsTrip(true);

					legWrappers.add(toWrapper);
				}
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
			final Route ligne = legWrapper.getLigne();
			final Place place = legWrapper.getPlace();

			builder.append(legWrapper.getTime()).append(" : ");

			if ("WALK".equals(legWrapper.getMode())) {
				if (legWrapper.getType() == Type.IN) {
					builder.append(getString(R.string.itinerary_go_to, place.name));
				}
			} else {
				if (legWrapper.getType() == Type.IN) {
					builder.append(FormatUtils.formatLigneArretSens(getActivity(), ligne.getLetter(), place.name,
							legWrapper.getHeadsign()));
				} else {
					builder.append(getString(R.string.itinerary_get_off, place.name));
				}
			}
			builder.append("\n");
		}

		builder.append(mTo);

		return builder.toString();
	}
}
