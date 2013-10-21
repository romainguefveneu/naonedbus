package net.naonedbus.loader;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import net.naonedbus.R;
import net.naonedbus.bean.ItineraryWrapper;
import net.naonedbus.bean.Route;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.helper.DateTimeFormatHelper;
import net.naonedbus.manager.impl.RouteManager;
import net.naonedbus.utils.FormatUtils;

import org.joda.time.DateTime;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import fr.ybo.opentripplanner.client.ClientOpenTripPlanner;
import fr.ybo.opentripplanner.client.OpenTripPlannerException;
import fr.ybo.opentripplanner.client.modele.Itinerary;
import fr.ybo.opentripplanner.client.modele.Leg;
import fr.ybo.opentripplanner.client.modele.OptimizeType;
import fr.ybo.opentripplanner.client.modele.Request;
import fr.ybo.opentripplanner.client.modele.Response;
import fr.ybo.opentripplanner.client.modele.TraverseMode;
import fr.ybo.opentripplanner.client.modele.TraverseModeSet;

public class ItineraryLoader extends AsyncTaskLoader<AsyncResult<List<ItineraryWrapper>>> {

	public static final String PARAM_FROM_LATITUDE = "fromLatitude";
	public static final String PARAM_FROM_LONGITUDE = "fromLongitude";

	public static final String PARAM_TO_LATITUDE = "toLatitude";
	public static final String PARAM_TO_LONGITUDE = "toLongitude";

	public static final String PARAM_TIME = "time";
	public static final String PARAM_ARRIVE_BY = "arriveBy";

	private static final String URL_WEBSERVICE = "http://naonedbus.netapsys.fr/opentripplanner-api-webapp";

	private final Bundle mBundle;
	private AsyncResult<List<ItineraryWrapper>> mResult;

	public ItineraryLoader(final Context context, final Bundle bundle) {
		super(context);
		mBundle = bundle;
	}

	@Override
	public AsyncResult<List<ItineraryWrapper>> loadInBackground() {
		final AsyncResult<List<ItineraryWrapper>> result = new AsyncResult<List<ItineraryWrapper>>();
		final double fromLatitude = mBundle.getDouble(PARAM_FROM_LATITUDE);
		final double fromLongitude = mBundle.getDouble(PARAM_FROM_LONGITUDE);
		final double toLatitude = mBundle.getDouble(PARAM_TO_LATITUDE);
		final double toLongitude = mBundle.getDouble(PARAM_TO_LONGITUDE);
		final long time = mBundle.getLong(PARAM_TIME);
		final boolean arriveBy = mBundle.getBoolean(PARAM_ARRIVE_BY);

		final Request request = new Request(fromLatitude, fromLongitude, toLatitude, toLongitude, new Date(time));
		request.setModes(new TraverseModeSet(TraverseMode.WALK, TraverseMode.TRANSIT));
		request.setLocale(Locale.getDefault().toString());
		request.setOptimize(OptimizeType.QUICK);
		request.setMaxWalkDistance(2000d);
		request.setWalkSpeed(1.389d);
		request.setArriveBy(arriveBy);

		final ClientOpenTripPlanner client = new ClientOpenTripPlanner(URL_WEBSERVICE);

		try {
			final Response response = client.getItineraries(request);
			if (response != null && response.getPlan() != null) {
				result.setResult(wrap(response.getPlan().itineraries));
			}
		} catch (final OpenTripPlannerException e) {
			result.setException(e);
		}

		return result;
	}

	private List<ItineraryWrapper> wrap(final List<Itinerary> itineraries) {
		final List<ItineraryWrapper> wrappers = new ArrayList<ItineraryWrapper>();

		final DateTimeFormatHelper formatHelper = new DateTimeFormatHelper(getContext());
		final RouteManager ligneManager = RouteManager.getInstance();
		for (final Itinerary itinerary : itineraries) {
			final ItineraryWrapper wrapper = new ItineraryWrapper(itinerary);
			wrapper.setTime(FormatUtils.formatMinutes(getContext(), itinerary.duration));
			wrapper.setDate(formatHelper
					.formatDuree(new DateTime(itinerary.startTime), new DateTime(itinerary.endTime)));

			final int walkTime = Math.round(itinerary.walkTime / 60);
			final String walkTimeText = getContext().getResources().getQuantityString(R.plurals.itinerary_walk_time,
					walkTime, walkTime);
			wrapper.setWalkTime(walkTimeText);

			final List<Route> lignes = new ArrayList<Route>();
			final List<Leg> legs = itinerary.legs;
			for (final Leg leg : legs) {
				if ("BUS".equalsIgnoreCase(leg.mode) || "TRAM".equalsIgnoreCase(leg.mode)) {
					final Route route = ligneManager.getSingleByLetter(getContext().getContentResolver(), leg.route);
					if (route != null) {
						lignes.add(route);
					}
				}
			}
			wrapper.setLignes(lignes);
			wrappers.add(wrapper);
		}

		return wrappers;
	}

	/**
	 * Called when there is new data to deliver to the client. The super class
	 * will take care of delivering it; the implementation here just adds a
	 * little more logic.
	 */
	@Override
	public void deliverResult(final AsyncResult<List<ItineraryWrapper>> result) {
		mResult = result;

		if (isStarted()) {
			// If the Loader is currently started, we can immediately
			// deliver its results.
			try {
				super.deliverResult(result);
			} catch (final NullPointerException e) {

			}
		}
	}

	/**
	 * Handles a request to start the Loader.
	 */
	@Override
	protected void onStartLoading() {
		if (mResult != null) {
			// If we currently have a result available, deliver it
			// immediately.
			deliverResult(mResult);
		}

		if (takeContentChanged() || mResult == null) {
			// If the data has changed since the last time it was loaded
			// or is not currently available, start a load.
			forceLoad();
		}
	}

}
