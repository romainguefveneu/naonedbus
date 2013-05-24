package net.naonedbus.loader;

import java.util.Date;
import java.util.List;

import net.naonedbus.bean.async.AsyncResult;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import fr.ybo.opentripplanner.client.ClientOpenTripPlanner;
import fr.ybo.opentripplanner.client.OpenTripPlannerException;
import fr.ybo.opentripplanner.client.modele.Itinerary;
import fr.ybo.opentripplanner.client.modele.OptimizeType;
import fr.ybo.opentripplanner.client.modele.Request;
import fr.ybo.opentripplanner.client.modele.Response;
import fr.ybo.opentripplanner.client.modele.TraverseMode;
import fr.ybo.opentripplanner.client.modele.TraverseModeSet;

public class ItineraryLoader extends AsyncTaskLoader<AsyncResult<List<Itinerary>>> {

	public static final String PARAM_FROM_LATITUDE = "fromLatitude";
	public static final String PARAM_FROM_LONGITUDE = "fromLongitude";

	public static final String PARAM_TO_LATITUDE = "toLatitude";
	public static final String PARAM_TO_LONGITUDE = "toLongitude";

	private static final String URL_WEBSERVICE = "http://naonedbus.netapsys.fr/opentripplanner-api-webapp";

	private final Bundle mBundle;
	private AsyncResult<List<Itinerary>> mResult;

	public ItineraryLoader(final Context context, final Bundle bundle) {
		super(context);
		mBundle = bundle;
	}

	@Override
	public AsyncResult<List<Itinerary>> loadInBackground() {
		final AsyncResult<List<Itinerary>> result = new AsyncResult<List<Itinerary>>();
		final double fromLatitude = mBundle.getDouble(PARAM_FROM_LATITUDE);
		final double fromLongitude = mBundle.getDouble(PARAM_FROM_LONGITUDE);
		final double toLatitude = mBundle.getDouble(PARAM_TO_LATITUDE);
		final double toLongitude = mBundle.getDouble(PARAM_TO_LONGITUDE);

		final Request request = new Request(fromLatitude, fromLongitude, toLatitude, toLongitude, new Date());
		request.setModes(new TraverseModeSet(TraverseMode.WALK, TraverseMode.TRANSIT));
		request.setOptimize(OptimizeType.QUICK);
		request.setMaxWalkDistance(2000d);
		request.setWalkSpeed(1.389d);

		final ClientOpenTripPlanner client = new ClientOpenTripPlanner(URL_WEBSERVICE);

		try {
			final Response response = client.getItineraries(request);
			result.setResult((response.getPlan() != null) ? response.getPlan().itineraries : null);
		} catch (final OpenTripPlannerException e) {
			result.setException(e);
		}

		return result;
	}

	/**
	 * Called when there is new data to deliver to the client. The super class
	 * will take care of delivering it; the implementation here just adds a
	 * little more logic.
	 */
	@Override
	public void deliverResult(final AsyncResult<List<Itinerary>> result) {
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
