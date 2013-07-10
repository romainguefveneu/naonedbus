package net.naonedbus.map.layerloader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.naonedbus.bean.Bicloo;
import net.naonedbus.manager.impl.BiclooManager;
import net.naonedbus.utils.FormatUtils;

import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.twotoasters.clusterkraf.InputPoint;

public class BiclooMapLayer implements MapLayer<Bicloo> {

	@Override
	public ArrayList<InputPoint> getInputPoints(Context context) {
		ArrayList<InputPoint> inputPoints = new ArrayList<InputPoint>();

		BiclooManager manager = BiclooManager.getInstance();
		List<Bicloo> bicloos;
		try {
			bicloos = manager.getAll(context);
			for (Bicloo bicloo : bicloos) {
				inputPoints.add(createInputPoint(bicloo));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return inputPoints;
	}

	private InputPoint createInputPoint(final Bicloo bicloo) {
		Location location = bicloo.getLocation();
		final LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
		final InputPoint inputPoint = new InputPoint(latLng);
		inputPoint.setTag(bicloo);

		return inputPoint;
	}

	@Override
	public View getInfoContents(Marker marker) {
		return null;
	}

	@Override
	public String getTitle(Context context, Bicloo item) {
		return item.getName();
	}

	@Override
	public String getDescription(Context context, Bicloo item) {
		final int availableBikes = item.getAvailableBike();
		final int availableStands = item.getAvailableBikeStands();
		return FormatUtils.formatBicloos(context, availableBikes, availableStands);
	}

	@Override
	public List<View> getSubview(ViewGroup root) {
		return null;
	}

	@Override
	public Integer getResourceAction(Bicloo item) {
		return null;
	}

	@Override
	public Intent getIntent(Context context, Bicloo item) {
		return null;
	}

}
