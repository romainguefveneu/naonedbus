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
import net.naonedbus.bean.Equipement;
import net.naonedbus.bean.Ligne;
import net.naonedbus.bean.async.AsyncTaskInfo;
import net.naonedbus.bean.async.LignesTaskInfo;
import net.naonedbus.bean.async.ParkingPublicTaskInfo;
import net.naonedbus.bean.parking.pub.ParkingPublic;
import net.naonedbus.bean.parking.pub.ParkingPublicStatut;
import net.naonedbus.manager.Unschedulable;
import net.naonedbus.manager.impl.EquipementManager.SousType;
import net.naonedbus.manager.impl.LigneManager;
import net.naonedbus.manager.impl.ParkingPublicManager;
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

public class EquipementArrayAdapter extends ArraySectionAdapter<Equipement> {

	private SparseArray<EquipementTypeAdapter> adapters;
	private Map<Class<? extends AsyncTaskInfo<?>>, Unschedulable<?>> unschedulers;

	public EquipementArrayAdapter(final Context context, final List<Equipement> objects) {
		super(context, R.layout.list_item_equipement, objects);
		initUnschedulers();
		initAdapters();
	}

	private void initAdapters() {
		final EquipementTypeAdapter defaultTypeAdapter = new DefaultTypeAdapter(this);
		adapters = new SparseArray<EquipementTypeAdapter>();
		adapters.append(Equipement.Type.TYPE_ARRET.getId(), new ArretTypeAdapter(this));
		adapters.append(Equipement.Type.TYPE_PARKING.getId(), new ParkingTypeAdapter(this));
		adapters.append(Equipement.Type.TYPE_BICLOO.getId(), defaultTypeAdapter);
	}

	private void initUnschedulers() {
		unschedulers = new HashMap<Class<? extends AsyncTaskInfo<?>>, Unschedulable<?>>();
		unschedulers.put(LignesTaskInfo.class, LigneManager.getInstance());
		unschedulers.put(ParkingPublicTaskInfo.class, ParkingPublicManager.getInstance());
	}

	@Override
	public void bindView(final View view, final Context context, final int position) {
		final ViewHolder holder = (ViewHolder) view.getTag();
		final Equipement equipement = getItem(position);
		final Equipement.Type type = equipement.getType();
		final EquipementTypeAdapter adapter = adapters.get(type.getId());

		// Définir le fond de l'icone.
		if (equipement.getSousType() != 0) {
			final SousType sousType = SousType.getTypeByValue(equipement.getSousType());
			holder.itemSymbole.setImageResource(sousType.getDrawableRes());
		} else {
			holder.itemSymbole.setImageResource(type.getDrawableRes());
		}
		holder.itemSymbole.setBackgroundDrawable(ColorUtils.getRoundedGradiant(context.getResources().getColor(
				type.getBackgroundColorRes())));

		// Définir la distance.
		if (equipement.getDistance() == null) {
			holder.itemDistance.setText("");
		} else {
			holder.itemDistance.setText(DistanceUtils.formatDist(equipement.getDistance()));
		}

		if (adapter != null) {
			adapter.bindView(context, holder, equipement);
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
		holder.itemLignes = (ViewGroup) view.findViewById(R.id.itemLignes);
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
	ViewGroup itemLignes;
	View itemSecondLine;
	ImageView itemSymbole;
	AsyncTaskInfo<?> task;
}

abstract class EquipementTypeAdapter {

	private final EquipementArrayAdapter adapter;

	public EquipementTypeAdapter(final EquipementArrayAdapter equipementArrayAdapter) {
		adapter = equipementArrayAdapter;
	}

	protected EquipementArrayAdapter getAdapter() {
		return adapter;
	}

	public abstract void bindView(Context context, ViewHolder holder, Equipement equipement);
}

class DefaultTypeAdapter extends EquipementTypeAdapter {

	public DefaultTypeAdapter(final EquipementArrayAdapter equipementArrayAdapter) {
		super(equipementArrayAdapter);
	}

	@Override
	public void bindView(final Context context, final ViewHolder holder, final Equipement equipement) {
		final String details = equipement.getDetails();
		final String adresse = equipement.getAdresse();

		if (holder.task != null) {
			getAdapter().unschedule(holder.task);
		}

		holder.itemTitle.setText(equipement.getNom());
		if (details == null && adresse == null) {
			holder.itemSecondLine.setVisibility(View.GONE);
		} else {
			holder.itemDescription.setText((details != null) ? details : adresse);
			holder.itemDescription.setVisibility(View.VISIBLE);
			holder.itemSecondLine.setVisibility(View.VISIBLE);
		}

		holder.itemLignes.setVisibility(View.GONE);
	}
}

class ArretTypeAdapter extends EquipementTypeAdapter {

	private final LigneManager mLigneManager;
	private final Typeface mRoboto;

	public ArretTypeAdapter(final EquipementArrayAdapter equipementArrayAdapter) {
		super(equipementArrayAdapter);
		mLigneManager = LigneManager.getInstance();
		mRoboto = FontUtils.getRobotoBoldCondensed(equipementArrayAdapter.getContext());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void bindView(final Context context, final ViewHolder holder, final Equipement equipement) {
		final LayoutInflater layoutInflater = LayoutInflater.from(context);

		holder.itemTitle.setText(equipement.getNom());
		holder.itemLignes.removeAllViews();
		holder.itemDescription.setVisibility(View.GONE);

		if (holder.task != null) {
			getAdapter().unschedule(holder.task);
		}

		if (equipement.getTag() == null) {
			final Handler handler = new Handler() {
				@Override
				public void handleMessage(final Message msg) {
					super.handleMessage(msg);
					holder.itemLignes.removeAllViews();
					final List<Ligne> lignes = (List<Ligne>) msg.obj;
					equipement.setTag(lignes);
					bindLignes(lignes, holder, layoutInflater);
					holder.itemLignes.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_in_from_left));
					holder.task = null;
				}
			};
			holder.task = mLigneManager.scheduleGetLignesFromStation(context, equipement.getId(), handler);
		} else {
			bindLignes((List<Ligne>) equipement.getTag(), holder, layoutInflater);
		}

	}

	private void bindLignes(final List<Ligne> lignes, final ViewHolder holder, final LayoutInflater layoutInflater) {
		holder.itemSecondLine.setVisibility(View.VISIBLE);
		holder.itemDescription.setVisibility(View.GONE);
		holder.itemLignes.setVisibility(View.VISIBLE);

		for (final Ligne ligne : lignes) {
			final TextView textView = (TextView) layoutInflater.inflate(R.layout.ligne_code_item, holder.itemLignes,
					false);

			textView.setTypeface(mRoboto);
			textView.setBackgroundDrawable(ColorUtils.getGradiant(ligne.getCouleur()));
			textView.setText(ligne.getLettre());
			textView.setTextColor(ligne.getCouleurTexte());

			holder.itemLignes.addView(textView);
		}

	}
}

class ParkingTypeAdapter extends EquipementTypeAdapter {

	private final ParkingPublicManager parkingPublicManager;

	public ParkingTypeAdapter(final EquipementArrayAdapter equipementArrayAdapter) {
		super(equipementArrayAdapter);
		parkingPublicManager = ParkingPublicManager.getInstance();
	}

	@Override
	public void bindView(final Context context, final ViewHolder holder, final Equipement equipement) {
		holder.itemTitle.setText(equipement.getNom());
		holder.itemDescription.setVisibility(View.GONE);
		holder.itemLignes.setVisibility(View.GONE);

		if (holder.task != null) {
			getAdapter().unschedule(holder.task);
		}

		if (equipement.getTag() == null) {
			final Handler handler = new Handler() {
				@Override
				public void handleMessage(final Message msg) {
					super.handleMessage(msg);

					holder.itemLignes.removeAllViews();

					final ParkingPublic parkingPublic = (ParkingPublic) msg.obj;
					equipement.setTag(parkingPublic);

					bindParking(context, holder, parkingPublic);

					holder.itemDescription
							.startAnimation(AnimationUtils.loadAnimation(context, android.R.anim.fade_in));
				}
			};

			holder.task = parkingPublicManager.scheduleGetParkingPublic(context, equipement.getId(), handler);
		} else {
			bindParking(context, holder, (ParkingPublic) equipement.getTag());
		}
	}

	private void bindParking(final Context context, final ViewHolder holder, final ParkingPublic parkingPublic) {
		if (parkingPublic != null) {
			int couleur;
			String detail;

			if (parkingPublic.getStatut() == ParkingPublicStatut.OUVERT) {
				final int placesDisponibles = parkingPublic.getPlacesDisponibles();
				couleur = context.getResources().getColor(ParkingUtils.getSeuilCouleurId(placesDisponibles));
				if (placesDisponibles > 0) {
					detail = context.getResources().getQuantityString(R.plurals.parking_places_disponibles,
							placesDisponibles, placesDisponibles);
				} else {
					detail = context.getString(R.string.parking_places_disponibles_zero);
				}
			} else {
				detail = context.getString(parkingPublic.getStatut().getTitleRes());
				couleur = context.getResources().getColor(parkingPublic.getStatut().getColorRes());
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
