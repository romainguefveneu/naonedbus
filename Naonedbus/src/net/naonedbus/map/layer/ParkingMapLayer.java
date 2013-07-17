package net.naonedbus.map.layer;

import java.util.List;

import net.naonedbus.activity.impl.ParkingDetailActivity;
import net.naonedbus.bean.Equipement;
import net.naonedbus.bean.parking.Parking;
import net.naonedbus.bean.parking.pub.ParkingPublic;
import net.naonedbus.intent.ParamIntent;
import net.naonedbus.map.ItemSelectedInfo;
import net.naonedbus.utils.FormatUtils;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
	protected ItemSelectedInfo getItemSelectedInfo(final Context context, final Parking item) {
		return new ItemSelectedInfo() {

			@Override
			public String getTitle() {
				return item.getNom();
			}

			@Override
			public String getDescription(final Context context) {
				final String description;
				if (item instanceof ParkingPublic) {
					final ParkingPublic parkingPublic = (ParkingPublic) item;
					description = FormatUtils.formatParkingPublic(context, parkingPublic.getStatut(),
							parkingPublic.getPlacesDisponibles());
				} else {
					description = item.getAdresse();
				}

				return description;
			}

			@Override
			public List<View> getSubview(final ViewGroup root) {
				return null;
			}

			@Override
			public Integer getResourceAction() {
				return null;
			}

			@Override
			public Intent getIntent(final Context context) {
				if (item instanceof ParkingPublic) {
					final ParamIntent intent = new ParamIntent(context, ParkingDetailActivity.class);
					intent.putExtra(ParkingDetailActivity.PARAM_PARKING, item);
					return intent;
				} else {
					return null;
				}
			}

		};
	}
}
