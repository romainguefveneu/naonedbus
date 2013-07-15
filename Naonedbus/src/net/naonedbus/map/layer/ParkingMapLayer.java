package net.naonedbus.map.layer;

import net.naonedbus.bean.Equipement;
import net.naonedbus.bean.parking.Parking;
import net.naonedbus.bean.parking.pub.ParkingPublic;
import net.naonedbus.utils.FormatUtils;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.twotoasters.clusterkraf.ClusterPoint;

public class ParkingMapLayer extends MapLayer<Parking> {

	public ParkingMapLayer(final LayoutInflater inflater) {
		super(inflater);
	}

	@Override
	public void chooseMarker(final MarkerOptions markerOptions, final ClusterPoint clusterPoint) {
		final Parking parking = (Parking) clusterPoint.getPointAtOffset(0).getTag();
		final BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(Equipement.Type.TYPE_PARKING.getMapPin());
		final String title = parking.getNom();

		markerOptions.icon(icon);
		markerOptions.title(title);
	}

	@Override
	protected void bindInfoContents(final Context context, final Parking item) {
		setInfoTitle(item.getNom());

		final String description;
		if (item instanceof ParkingPublic) {
			ParkingPublic parkingPublic = (ParkingPublic) item;
			description = FormatUtils.formatParkingPublic(context, parkingPublic.getStatut(),
					parkingPublic.getPlacesDisponibles());
		} else {
			description = item.getAdresse();
		}

		setInfoDescription(description);
	}

	@Override
	public Intent getIntent(Context context, Parking item) {
		// TODO Auto-generated method stub
		return null;
	}
}
