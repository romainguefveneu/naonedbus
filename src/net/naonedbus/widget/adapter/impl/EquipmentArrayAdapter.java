/**
 * Copyright (C) 2013 Romain Guefveneu.
 *   
 *  This file is part of naonedbus.
 *   
 *  Naonedbus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  Naonedbus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.naonedbus.widget.adapter.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.naonedbus.R;
import net.naonedbus.bean.Equipment;
import net.naonedbus.bean.Route;
import net.naonedbus.bean.async.AsyncTaskInfo;
import net.naonedbus.bean.async.RouteTaskInfo;
import net.naonedbus.bean.async.PublicParkTaskInfo;
import net.naonedbus.bean.parking.PublicPark;
import net.naonedbus.bean.parking.PublicParkStatus;
import net.naonedbus.manager.Unschedulable;
import net.naonedbus.manager.impl.EquipmentManager.SubType;
import net.naonedbus.manager.impl.RouteManager;
import net.naonedbus.manager.impl.PublicParkManager;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.utils.DistanceUtils;
import net.naonedbus.utils.FontUtils;
import net.naonedbus.utils.ParkingUtils;
import net.naonedbus.widget.adapter.ArraySectionAdapter;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class EquipmentArrayAdapter extends ArraySectionAdapter<Equipment> {

	private SparseArray<EquipementTypeAdapter> adapters;
	private Map<Class<? extends AsyncTaskInfo<?>>, Unschedulable<?>> unschedulers;

	public EquipmentArrayAdapter(final Context context, final List<Equipment> objects) {
		super(context, R.layout.list_item_equipment, objects);
		initUnschedulers();
		initAdapters();
	}

	private void initAdapters() {
		final EquipementTypeAdapter defaultTypeAdapter = new DefaultTypeAdapter(this);
		adapters = new SparseArray<EquipementTypeAdapter>();
		adapters.append(Equipment.Type.TYPE_STOP.getId(), new ArretTypeAdapter(this));
		adapters.append(Equipment.Type.TYPE_PARK.getId(), new ParkingTypeAdapter(this));
		adapters.append(Equipment.Type.TYPE_BICLOO.getId(), defaultTypeAdapter);
		adapters.append(Equipment.Type.TYPE_CARPOOL.getId(), defaultTypeAdapter);
		adapters.append(Equipment.Type.TYPE_LILA.getId(), defaultTypeAdapter);
		adapters.append(Equipment.Type.TYPE_MARGUERITE.getId(), defaultTypeAdapter);
	}

	private void initUnschedulers() {
		unschedulers = new HashMap<Class<? extends AsyncTaskInfo<?>>, Unschedulable<?>>();
		unschedulers.put(RouteTaskInfo.class, RouteManager.getInstance());
		unschedulers.put(PublicParkTaskInfo.class, PublicParkManager.getInstance());
	}

	@Override
	public void bindView(final View view, final Context context, final int position) {
		final ViewHolder holder = (ViewHolder) view.getTag();
		final Equipment equipment = getItem(position);
		final Equipment.Type type = equipment.getType();
		final EquipementTypeAdapter adapter = adapters.get(type.getId());

		// Définir le fond de l'icone.
		if (equipment.getSubType() != 0) {
			final SubType sousType = SubType.getTypeByValue(equipment.getSubType());
			holder.itemSymbole.setImageResource(sousType.getDrawableRes());
		} else {
			holder.itemSymbole.setImageResource(type.getDrawableRes());
		}
		holder.itemSymbole.setBackgroundDrawable(ColorUtils.getRoundedGradiant(context.getResources().getColor(
				type.getBackgroundColorRes())));

		// Définir la distance.
		if (equipment.getDistance() == null) {
			holder.itemDistance.setText("");
		} else {
			holder.itemDistance.setText(DistanceUtils.formatDist(equipment.getDistance()));
		}

		if (adapter != null) {
			adapter.bindView(context, holder, equipment);
		}

		bindHeaderView(view, position);
	}

	@Override
	public void bindViewHolder(final View view) {
		final ViewHolder holder = new ViewHolder();
		holder.itemTitle = (TextView) view.findViewById(R.id.itemTitle);
		holder.itemDescription = (TextView) view.findViewById(R.id.itemDescription);
		holder.itemSymbole = (ImageView) view.findViewById(R.id.itemSymbole);
		holder.itemDistance = (TextView) view.findViewById(R.id.itemDistance);
		holder.itemRoutes = (ViewGroup) view.findViewById(R.id.itemLignes);
		holder.itemSecondLine = view.findViewById(R.id.secondLine);
		view.setTag(holder);
	}

	private void bindHeaderView(final View view, final int position) {
		final int section = getSectionForPosition(position);
		if (getPositionForSection(section) == position) {
			final TextView headerText = (TextView) view.findViewById(R.id.headerTitle);
			headerText.setText(getSections()[section].toString());
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends AsyncTaskInfo<?>> void unschedule(final T task) {
		final Unschedulable<T> unschedulable = (Unschedulable<T>) unschedulers.get(task.getClass());
		unschedulable.unschedule(task);
	}
}

class ViewHolder {
	TextView itemTitle;
	TextView itemDescription;
	TextView itemDistance;
	ViewGroup itemRoutes;
	View itemSecondLine;
	ImageView itemSymbole;
	AsyncTaskInfo<?> task;
}

abstract class EquipementTypeAdapter {

	private final EquipmentArrayAdapter adapter;

	public EquipementTypeAdapter(final EquipmentArrayAdapter equipementArrayAdapter) {
		adapter = equipementArrayAdapter;
	}

	protected EquipmentArrayAdapter getAdapter() {
		return adapter;
	}

	public abstract void bindView(Context context, ViewHolder holder, Equipment equipment);
}

class DefaultTypeAdapter extends EquipementTypeAdapter {

	public DefaultTypeAdapter(final EquipmentArrayAdapter equipementArrayAdapter) {
		super(equipementArrayAdapter);
	}

	@Override
	public void bindView(final Context context, final ViewHolder holder, final Equipment equipment) {
		final String details = equipment.getDetails();
		final String adresse = equipment.getAddress();

		if (holder.task != null) {
			getAdapter().unschedule(holder.task);
		}

		holder.itemTitle.setText(equipment.getName());
		if (details == null && adresse == null) {
			holder.itemSecondLine.setVisibility(View.GONE);
		} else {
			holder.itemDescription.setText((details != null) ? details : adresse);
			holder.itemDescription.setVisibility(View.VISIBLE);
			holder.itemSecondLine.setVisibility(View.VISIBLE);
		}

		holder.itemRoutes.setVisibility(View.GONE);
	}
}

class ArretTypeAdapter extends EquipementTypeAdapter {

	private final RouteManager mRouteManager;
	private final Typeface mRoboto;

	public ArretTypeAdapter(final EquipmentArrayAdapter equipementArrayAdapter) {
		super(equipementArrayAdapter);
		mRouteManager = RouteManager.getInstance();
		mRoboto = FontUtils.getRobotoBoldCondensed(equipementArrayAdapter.getContext());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void bindView(final Context context, final ViewHolder holder, final Equipment equipment) {
		final LayoutInflater layoutInflater = LayoutInflater.from(context);

		holder.itemTitle.setText(equipment.getName());
		holder.itemRoutes.removeAllViews();
		holder.itemDescription.setVisibility(View.GONE);

		if (holder.task != null) {
			getAdapter().unschedule(holder.task);
		}

		if (equipment.getTag() == null) {
			final Handler handler = new Handler() {
				@Override
				public void handleMessage(final Message msg) {
					super.handleMessage(msg);
					holder.itemRoutes.removeAllViews();
					final List<Route> lignes = (List<Route>) msg.obj;
					equipment.setTag(lignes);
					bindLignes(lignes, holder, layoutInflater);
					holder.itemRoutes.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_in_from_left));
					holder.task = null;
				}
			};
			holder.task = mRouteManager.scheduleGetRoutesByStopArea(context, equipment.getId(), handler);
		} else {
			bindLignes((List<Route>) equipment.getTag(), holder, layoutInflater);
		}

	}

	private void bindLignes(final List<Route> lignes, final ViewHolder holder, final LayoutInflater layoutInflater) {
		holder.itemSecondLine.setVisibility(View.VISIBLE);
		holder.itemDescription.setVisibility(View.GONE);
		holder.itemRoutes.setVisibility(View.VISIBLE);

		for (final Route route : lignes) {
			final TextView textView = (TextView) layoutInflater.inflate(R.layout.route_code_item, holder.itemRoutes,
					false);

			textView.setTypeface(mRoboto);
			textView.setBackgroundDrawable(ColorUtils.getGradiant(route.getBackColor()));
			textView.setText(route.getLetter());
			textView.setTextColor(route.getFrontColor());

			holder.itemRoutes.addView(textView);
		}

	}
}

class ParkingTypeAdapter extends EquipementTypeAdapter {

	private final PublicParkManager parkingPublicManager;

	public ParkingTypeAdapter(final EquipmentArrayAdapter equipementArrayAdapter) {
		super(equipementArrayAdapter);
		parkingPublicManager = PublicParkManager.getInstance();
	}

	@Override
	public void bindView(final Context context, final ViewHolder holder, final Equipment equipment) {
		holder.itemTitle.setText(equipment.getName());
		holder.itemDescription.setVisibility(View.GONE);
		holder.itemRoutes.setVisibility(View.GONE);

		if (holder.task != null) {
			getAdapter().unschedule(holder.task);
		}

		if (equipment.getTag() == null) {
			final Handler handler = new Handler() {
				@Override
				public void handleMessage(final Message msg) {
					super.handleMessage(msg);

					holder.itemRoutes.removeAllViews();

					final PublicPark parkingPublic = (PublicPark) msg.obj;
					equipment.setTag(parkingPublic);

					bindParking(context, holder, parkingPublic);

					holder.itemDescription
							.startAnimation(AnimationUtils.loadAnimation(context, android.R.anim.fade_in));
				}
			};

			holder.task = parkingPublicManager.scheduleGetParkingPublic(context, equipment.getId(), handler);
		} else {
			bindParking(context, holder, (PublicPark) equipment.getTag());
		}
	}

	private void bindParking(final Context context, final ViewHolder holder, final PublicPark parkingPublic) {
		if (parkingPublic != null) {
			int couleur;
			String detail;

			if (parkingPublic.getStatus() == PublicParkStatus.OPEN) {
				final int placesDisponibles = parkingPublic.getAvailableSpaces();
				couleur = context.getResources().getColor(ParkingUtils.getSeuilCouleurId(placesDisponibles));
				if (placesDisponibles > 0) {
					detail = context.getResources().getQuantityString(R.plurals.parking_places_disponibles,
							placesDisponibles, placesDisponibles);
				} else {
					detail = context.getString(R.string.parking_places_disponibles_zero);
				}
			} else {
				detail = context.getString(parkingPublic.getStatus().getTitleRes());
				couleur = context.getResources().getColor(parkingPublic.getStatus().getColorRes());
			}

			holder.itemSymbole.setBackgroundDrawable(ColorUtils.getRoundedGradiant(couleur));
			holder.itemDescription.setText(detail);
			holder.itemDescription.setVisibility(View.VISIBLE);
			holder.itemSecondLine.setVisibility(View.VISIBLE);
		} else {
			holder.itemSecondLine.setVisibility(View.GONE);
		}
	}

}
