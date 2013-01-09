package net.naonedbus.widget.adapter.impl;

import net.naonedbus.R;
import net.naonedbus.bean.Equipement;
import net.naonedbus.manager.impl.EquipementManager.SousType;
import net.naonedbus.provider.table.EquipementTable;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.widget.adapter.CursorSectionAdapter;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class EquipementCursorAdapter extends CursorSectionAdapter {

	private SparseArray<EquipementTypeAdapter> adapters;

	private int mColIdType;
	private int mColIdSousType;
	private int mColNom;
	private int mColAdresse;
	private int mColDetails;

	public EquipementCursorAdapter(Context context, Cursor c) {
		super(context, c, R.layout.list_item_equipement);
		if (c != null) {
			initColumns();
		}
		initAdapters();
	}

	@Override
	public void changeCursor(Cursor cursor) {
		super.changeCursor(cursor);
		if (cursor != null) {
			initColumns();
		}
	}

	private void initAdapters() {
		final EquipementTypeAdapter defaultTypeAdapter = new DefaultTypeAdapter(this);
		adapters = new SparseArray<EquipementTypeAdapter>();
		adapters.append(Equipement.Type.TYPE_ARRET.getId(), defaultTypeAdapter);
		adapters.append(Equipement.Type.TYPE_PARKING.getId(), defaultTypeAdapter);
		adapters.append(Equipement.Type.TYPE_BICLOO.getId(), defaultTypeAdapter);
		adapters.append(Equipement.Type.TYPE_COVOITURAGE.getId(), defaultTypeAdapter);
		adapters.append(Equipement.Type.TYPE_LILA.getId(), defaultTypeAdapter);
		adapters.append(Equipement.Type.TYPE_MARGUERITE.getId(), defaultTypeAdapter);
	}

	private void initColumns() {
		final Cursor c = getCursor();
		mColIdType = c.getColumnIndex(EquipementTable.ID_TYPE);
		mColIdSousType = c.getColumnIndex(EquipementTable.ID_SOUS_TYPE);
		mColNom = c.getColumnIndex(EquipementTable.NOM);
		mColAdresse = c.getColumnIndex(EquipementTable.ADRESSE);
		mColDetails = c.getColumnIndex(EquipementTable.DETAILS);
	}

	@Override
	protected void bindViewHolder(View view) {
		final ViewHolder holder;
		holder = new ViewHolder();
		holder.itemTitle = (TextView) view.findViewById(R.id.itemTitle);
		holder.itemDescription = (TextView) view.findViewById(R.id.itemDescription);
		holder.itemSymbole = (ImageView) view.findViewById(R.id.itemSymbole);
		holder.itemDistance = (TextView) view.findViewById(R.id.itemDistance);
		holder.itemLignes = (ViewGroup) view.findViewById(R.id.itemLignes);
		view.setTag(holder);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		super.bindView(view, context, cursor);

		final ViewHolder holder = (ViewHolder) view.getTag();

		final String nom = cursor.getString(mColNom);
		final int typeId = cursor.getInt(mColIdType);
		final int sousTypeId = cursor.getInt(mColIdSousType);

		final Equipement.Type type = Equipement.Type.getTypeById(typeId);
		final EquipementTypeAdapter adapter = adapters.get(type.getId());

		holder.itemTitle.setText(nom);

		// Définir le fond de l'icone.
		if (sousTypeId != 0) {
			final SousType sousType = SousType.getTypeByValue(sousTypeId);
			holder.itemSymbole.setImageResource(sousType.getDrawableRes());
		} else {
			holder.itemSymbole.setImageResource(type.getDrawableRes());
		}
		holder.itemSymbole.setBackgroundDrawable(ColorUtils.getRoundedGradiant(context.getResources().getColor(
				type.getBackgroundColorRes())));

		if (adapter != null) {
			adapter.bindView(context, holder, cursor);
		}
	}

	public int getColIdType() {
		return mColIdType;
	}

	public int getColIdSousType() {
		return mColIdSousType;
	}

	public int getColNom() {
		return mColNom;
	}

	public int getColAdresse() {
		return mColAdresse;
	}

	public int getColDetails() {
		return mColDetails;
	}

	private class ViewHolder {
		TextView itemTitle;
		TextView itemDescription;
		TextView itemDistance;
		ViewGroup itemLignes;
		ImageView itemSymbole;
	}

	// -------------------------------------------------------------------
	// Inner adapter
	// -------------------------------------------------------------------

	private abstract class EquipementTypeAdapter {

		private EquipementCursorAdapter adapter;

		public EquipementTypeAdapter(EquipementCursorAdapter equipementCursorAdapter) {
			adapter = equipementCursorAdapter;
		}

		protected EquipementCursorAdapter getAdapter() {
			return adapter;
		}

		public abstract void bindView(Context context, ViewHolder holder, Cursor cursor);
	}

	private class DefaultTypeAdapter extends EquipementTypeAdapter {

		public DefaultTypeAdapter(EquipementCursorAdapter equipementCursorAdapter) {
			super(equipementCursorAdapter);
		}

		@Override
		public void bindView(Context context, ViewHolder holder, Cursor cursor) {
			final String details = cursor.getString(getAdapter().getColDetails());
			final String adresse = cursor.getString(getAdapter().getColAdresse());

			// if (holder.task != null) {
			// getAdapter().unschedule(holder.task);
			// }

			if (TextUtils.isEmpty(details) && TextUtils.isEmpty(adresse)) {
				holder.itemDescription.setVisibility(View.GONE);
			} else {
				holder.itemDescription.setText((details != null) ? details : adresse);
				holder.itemDescription.setVisibility(View.VISIBLE);
			}

			holder.itemLignes.setVisibility(View.GONE);
		}
	}

}