package net.naonedbus.map.layer.loader;

import java.io.IOException;
import java.util.ArrayList;

import net.naonedbus.bean.parking.Parking;
import net.naonedbus.manager.impl.ParkingPublicManager;
import net.naonedbus.manager.impl.ParkingRelaiManager;

import org.json.JSONException;

import android.content.Context;

import com.bugsense.trace.BugSenseHandler;
import com.google.android.gms.maps.model.LatLng;
import com.twotoasters.clusterkraf.InputPoint;

public class ParkingMapLoader implements MapLayerLoader {

	@Override
	public ArrayList<InputPoint> getInputPoints(final Context context) {
		final ArrayList<InputPoint> inputPoints = new ArrayList<InputPoint>();

		try {
			final ParkingPublicManager publicManager = ParkingPublicManager.getInstance();
			final ParkingRelaiManager relaiManager = ParkingRelaiManager.getInstance();

			for (Parking parking : publicManager.getAll(context)) {
				inputPoints.add(createInputPoint(parking));
			}

			for (Parking parking : relaiManager.getAll(context.getContentResolver())) {
				inputPoints.add(createInputPoint(parking));
			}
		} catch (final IOException e) {
			BugSenseHandler.sendException(e);
		} catch (final JSONException e) {
			BugSenseHandler.sendException(e);
		}

		return inputPoints;
	}

	private InputPoint createInputPoint(final Parking parking) {
		final LatLng latLng = new LatLng(parking.getLatitude(), parking.getLongitude());
		final InputPoint inputPoint = new InputPoint(latLng);
		inputPoint.setTag(parking);

		return inputPoint;
	}

}
