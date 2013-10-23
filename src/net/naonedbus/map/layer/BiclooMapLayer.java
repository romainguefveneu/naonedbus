package net.naonedbus.map.layer;

import java.io.IOException;
import java.util.List;

import net.naonedbus.R;
import net.naonedbus.activity.impl.BiclooDetailActivity;
import net.naonedbus.bean.Bicloo;
import net.naonedbus.bean.Equipment;
import net.naonedbus.intent.ParamIntent;
import net.naonedbus.manager.impl.BiclooManager;
import net.naonedbus.map.ItemSelectedInfo;
import net.naonedbus.utils.FormatUtils;

import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.twotoasters.clusterkraf.ClusterPoint;

public class BiclooMapLayer extends MapLayer {

	private SparseArray<Bicloo> mBicloos = new SparseArray<Bicloo>();

	public BiclooMapLayer(final LayoutInflater inflater) {
		super(inflater);
	}

	@Override
	public void chooseMarker(final MarkerOptions markerOptions, final ClusterPoint clusterPoint) {
		final Equipment bicloo = (Equipment) clusterPoint.getPointAtOffset(0).getTag();
		final BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(Equipment.Type.TYPE_BICLOO.getMapPin());
		final String title = bicloo.getName();

		markerOptions.icon(icon);
		markerOptions.title(title);
	}

	@Override
	protected ItemSelectedInfo getItemSelectedInfo(final Context context, final Equipment item) {
		return new ItemSelectedInfo() {

			@Override
			public String getTitle() {
				return item.getName();
			}

			@Override
			public String getDescription(final Context context) {
				loadBicloos(context);
				final Bicloo bicloo = mBicloos.get(item.getId());

				String description;
				if (bicloo != null) {
					final int availableBikes = bicloo.getAvailableBike();
					final int availableStands = bicloo.getAvailableBikeStands();
					description = FormatUtils.formatBicloos(context, availableBikes, availableStands);
				} else {
					description = context.getString(R.string.empty);
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
				loadBicloos(context);
				final Bicloo bicloo = mBicloos.get(item.getId());

				final ParamIntent intent = new ParamIntent(context, BiclooDetailActivity.class);
				intent.putExtra(BiclooDetailActivity.PARAM_BICLOO, bicloo);
				return intent;
			}

		};
	}

	private void loadBicloos(Context context) {
		if (mBicloos.size() == 0) {
			BiclooManager biclooManager = BiclooManager.getInstance();
			List<Bicloo> bicloos;
			try {
				bicloos = biclooManager.getAll(context);
				for (Bicloo bicloo : bicloos) {
					mBicloos.put(bicloo.getId(), bicloo);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
}
