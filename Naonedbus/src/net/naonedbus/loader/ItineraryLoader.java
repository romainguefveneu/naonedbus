package net.naonedbus.loader;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import net.naonedbus.bean.async.AsyncResult;
import android.content.Context;
import android.os.Bundle;
import fr.ybo.opentripplanner.client.ClientOpenTripPlanner;
import fr.ybo.opentripplanner.client.OpenTripPlannerException;
import fr.ybo.opentripplanner.client.modele.Itinerary;
import fr.ybo.opentripplanner.client.modele.OptimizeType;
import fr.ybo.opentripplanner.client.modele.Request;
import fr.ybo.opentripplanner.client.modele.Response;
import fr.ybo.opentripplanner.client.modele.TraverseMode;
import fr.ybo.opentripplanner.client.modele.TraverseModeSet;

public class ItineraryLoader {

	public static final String PARAM_FROM_LATITUDE = "fromLatitude";
	public static final String PARAM_FROM_LONGITUDE = "fromLongitude";

	public static final String PARAM_TO_LATITUDE = "toLatitude";
	public static final String PARAM_TO_LONGITUDE = "toLongitude";

	private static final String URL_WEBSERVICE = "http://naonedbus.netapsys.fr/opentripplanner-api-webapp";

	private final Bundle mBundle;

	public ItineraryLoader(final Context context, final Bundle bundle) {
		mBundle = bundle;
	}

	public AsyncResult<List<Itinerary>> loadInBackground() {
		final AsyncResult<List<Itinerary>> result = new AsyncResult<List<Itinerary>>();
		final double fromLatitude = mBundle.getDouble(PARAM_FROM_LATITUDE);
		final double fromLongitude = mBundle.getDouble(PARAM_FROM_LONGITUDE);
		final double toLatitude = mBundle.getDouble(PARAM_TO_LATITUDE);
		final double toLongitude = mBundle.getDouble(PARAM_TO_LONGITUDE);

		final Request request = new Request(fromLatitude, fromLongitude, toLatitude, toLongitude, new Date());
		request.setModes(new TraverseModeSet(TraverseMode.WALK, TraverseMode.TRANSIT));
		request.setLocale(Locale.getDefault().toString());
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

}
