package net.naonedbus.map.layer.loader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.naonedbus.bean.Bicloo;
import net.naonedbus.manager.impl.BiclooManager;

import org.json.JSONException;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.twotoasters.clusterkraf.InputPoint;

public class BiclooMapLoader implements MapLayerLoader {

	@Override
	public ArrayList<InputPoint> getInputPoints(final Context context) {
		final ArrayList<InputPoint> inputPoints = new ArrayList<InputPoint>();

		final BiclooManager manager = BiclooManager.getInstance();
		List<Bicloo> bicloos;
		try {
			bicloos = manager.getAll(context);
			for (final Bicloo bicloo : bicloos) {
				inputPoints.add(createInputPoint(bicloo));
			}
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final JSONException e) {
			e.printStackTrace();
		}

		return inputPoints;
	}

	private InputPoint createInputPoint(final Bicloo bicloo) {
		final Location location = bicloo.getLocation();
		final LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
		final InputPoint inputPoint = new InputPoint(latLng);
		inputPoint.setTag(bicloo);

		return inputPoint;
	}

}
