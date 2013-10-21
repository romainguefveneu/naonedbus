package net.naonedbus.map.layer;

import java.util.ArrayList;
import java.util.List;

import net.naonedbus.R;
import net.naonedbus.activity.impl.StopPathActivity;
import net.naonedbus.bean.Equipment;
import net.naonedbus.bean.Equipment.Type;
import net.naonedbus.bean.Route;
import net.naonedbus.intent.ParamIntent;
import net.naonedbus.manager.impl.RouteManager;
import net.naonedbus.map.ItemSelectedInfo;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.utils.FontUtils;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.twotoasters.clusterkraf.ClusterPoint;

public class EquipementMapLayer extends MapLayer {

	public EquipementMapLayer(final LayoutInflater inflater) {
		super(inflater);
	}

	@Override
	public void chooseMarker(final MarkerOptions markerOptions, final ClusterPoint clusterPoint) {
		final Equipment equipment = (Equipment) clusterPoint.getPointAtOffset(0).getTag();
		final Equipment.Type type = equipment.getType();
		final BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(type.getMapPin());
		final String title = equipment.getName();

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
				String description = item.getDetails();
				if (TextUtils.isEmpty(description)) {
					description = item.getAddress();
				}
				if (TextUtils.isEmpty(description)) {
					description = context.getString(item.getType().getTitleRes());
				}
				return description;
			}

			@Override
			public List<View> getSubview(final ViewGroup root) {
				if (item.getType() == Type.TYPE_STOP) {
					return getStationInfoContents(context, root, item);
				} else {
					return null;
				}
			}

			@Override
			public Integer getResourceAction() {
				return null;
			}

			@Override
			public Intent getIntent(final Context context) {
				if (item.getType() == Type.TYPE_STOP) {
					final ParamIntent intent = new ParamIntent(context, StopPathActivity.class);
					intent.putExtra(StopPathActivity.PARAM_ID_SATION, item.getId());
					return intent;
				} else {
					return null;
				}
			}

		};
	}

	private List<View> getStationInfoContents(final Context context, final ViewGroup root, final Equipment station) {
		final LayoutInflater layoutInflater = getLayoutInflater();
		final List<View> views = new ArrayList<View>();
		final RouteManager ligneManager = RouteManager.getInstance();
		final List<Route> lignes = ligneManager.getRoutesByStopArea(context.getContentResolver(), station.getId());

		Typeface roboto = FontUtils.getRobotoBoldCondensed(context);

		for (final Route ligneItem : lignes) {
			final TextView textView = (TextView) layoutInflater.inflate(R.layout.ligne_code_item, root, false);
			textView.setTypeface(roboto);
			textView.setTextColor(ligneItem.getFrontColor());
			textView.setBackgroundDrawable(ColorUtils.getGradiant(ligneItem.getBackColor()));
			textView.setText(ligneItem.getCode());
			views.add(textView);
		}

		return views;
	}

}
