package net.naonedbus.map.layer;

import java.util.List;

import net.naonedbus.activity.impl.BiclooDetailActivity;
import net.naonedbus.bean.Bicloo;
import net.naonedbus.bean.Equipement;
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
	protected ItemSelectedInfo getItemSelectedInfo(final Context context, final Bicloo item) {
		return new ItemSelectedInfo() {

			@Override
			public String getTitle() {
				return item.getName();
			}

			@Override
			public String getDescription(final Context context) {
				final int availableBikes = item.getAvailableBike();
				final int availableStands = item.getAvailableBikeStands();
				final String description = FormatUtils.formatBicloos(context, availableBikes, availableStands);
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
				final ParamIntent intent = new ParamIntent(context, BiclooDetailActivity.class);
				intent.putExtra(BiclooDetailActivity.PARAM_BICLOO, item);
				return intent;
			}

		};
	}

}
