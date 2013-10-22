package net.naonedbus.map.layer;

import java.io.IOException;
import java.util.List;

import net.naonedbus.activity.impl.ParkDetailActivity;
import net.naonedbus.bean.Equipment;
import net.naonedbus.bean.parking.CarPark;
import net.naonedbus.bean.parking.PublicPark;
import net.naonedbus.intent.ParamIntent;
import net.naonedbus.manager.impl.PublicParkManager;
import net.naonedbus.manager.impl.ParkingRelaiManager;
import net.naonedbus.map.ItemSelectedInfo;
import net.naonedbus.utils.FormatUtils;

import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bugsense.trace.BugSenseHandler;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.twotoasters.clusterkraf.ClusterPoint;

public class ParkingMapLayer extends MapLayer {

	private SparseArray<CarPark> mParkings = new SparseArray<CarPark>();

	public ParkingMapLayer(final LayoutInflater inflater) {
		super(inflater);
	}

	@Override
	public void chooseMarker(final MarkerOptions markerOptions, final ClusterPoint clusterPoint) {
		final Equipment parking = (Equipment) clusterPoint.getPointAtOffset(0).getTag();
		final BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(Equipment.Type.TYPE_PARK.getMapPin());
		final String title = parking.getName();

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
				loadParkings(context);
				final CarPark parking = mParkings.get(item.getId());
				final String description;
				if (parking instanceof PublicPark) {
					final PublicPark parkingPublic = (PublicPark) parking;
					description = FormatUtils.formatParkingPublic(context, parkingPublic.getStatus(),
							parkingPublic.getAvailableSpaces());
				} else {
					description = item.getAddress();
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
				loadParkings(context);
				final CarPark parking = mParkings.get(item.getId());
				if (parking instanceof PublicPark) {
					final ParamIntent intent = new ParamIntent(context, ParkDetailActivity.class);
					intent.putExtra(ParkDetailActivity.PARAM_PARKING, parking);
					return intent;
				} else {
					return null;
				}
			}

		};
	}

	private void loadParkings(Context context) {
		if (mParkings.size() == 0) {
			final PublicParkManager publicManager = PublicParkManager.getInstance();
			final ParkingRelaiManager relaiManager = ParkingRelaiManager.getInstance();

			try {
				for (CarPark parking : publicManager.getAll(context)) {
					mParkings.put(parking.getId(), parking);
				}

				for (CarPark parking : relaiManager.getAll(context.getContentResolver())) {
					mParkings.put(parking.getId(), parking);
				}
			} catch (final IOException e) {
				BugSenseHandler.sendException(e);
			} catch (final JSONException e) {
				BugSenseHandler.sendException(e);
			}
		}
	}
}
