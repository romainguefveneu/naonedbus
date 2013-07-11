package net.naonedbus.map.layer;

import net.naonedbus.bean.Equipement;
import android.content.Context;
import android.view.LayoutInflater;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.twotoasters.clusterkraf.ClusterPoint;

public class EquipementMapLayer extends MapLayer<Equipement> {

	public EquipementMapLayer(final LayoutInflater inflater) {
		super(inflater);
	}

	@Override
	public void chooseMarker(final MarkerOptions markerOptions, final ClusterPoint clusterPoint) {
		final Equipement equipement = (Equipement) clusterPoint.getPointAtOffset(0).getTag();
		final Equipement.Type type = equipement.getType();
		final BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(type.getMapPin());
		final String title = equipement.getNom();

		markerOptions.icon(icon);
		markerOptions.title(title);
	}

	@Override
	protected void bindInfoContents(final Context context, final Equipement item) {
		setInfoTitle(item.getNom());
		setInfoDescription(item.getDetails());
	}

}
