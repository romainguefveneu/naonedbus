package net.naonedbus.map.layer;

import net.naonedbus.bean.Bicloo;
import net.naonedbus.bean.Equipement;
import net.naonedbus.utils.FormatUtils;
import android.content.Context;
import android.view.LayoutInflater;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.twotoasters.clusterkraf.ClusterPoint;

public class BiclooMapLayer extends MapLayer<Bicloo> {

	public BiclooMapLayer(final LayoutInflater inflater) {
		super(inflater);
	}

	@Override
	public void chooseMarker(final MarkerOptions markerOptions, final ClusterPoint clusterPoint) {
		final Bicloo bicloo = (Bicloo) clusterPoint.getPointAtOffset(0).getTag();
		final BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(Equipement.Type.TYPE_BICLOO.getMapPin());
		final String title = bicloo.getName();

		markerOptions.icon(icon);
		markerOptions.title(title);
	}

	@Override
	protected void bindInfoContents(final Context context, final Bicloo item) {
		setInfoTitle(item.getName());

		final int availableBikes = item.getAvailableBike();
		final int availableStands = item.getAvailableBikeStands();
		final String description = FormatUtils.formatBicloos(context, availableBikes, availableStands);
		setInfoDescription(description);
	}

}
