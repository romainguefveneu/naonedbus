package net.naonedbus.widget.adapter.impl;

import java.util.List;

import net.naonedbus.R;
import net.naonedbus.bean.Bicloo;
import net.naonedbus.bean.Equipement;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.utils.DistanceUtils;
import net.naonedbus.utils.FormatUtils;
import net.naonedbus.widget.adapter.ArraySectionAdapter;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class BiclooArrayAdapter extends ArraySectionAdapter<Bicloo> {

	private class ViewHolder {
		TextView itemTitle;
		TextView itemDescription;
		TextView itemDistance;
		ImageView itemSymbole;
	}

	private final Drawable mSymboleBackground;
	private final int mSymboleResId;
	private SparseBooleanArray mCheckedItemPositions = new SparseBooleanArray();

	public BiclooArrayAdapter(final Context context, final List<Bicloo> objects) {
		super(context, R.layout.list_item_equipement, objects);

		mSymboleBackground = ColorUtils.getCircle(context.getResources().getColor(
				Equipement.Type.TYPE_BICLOO.getBackgroundColorRes()));
		mSymboleResId = Equipement.Type.TYPE_BICLOO.getDrawableRes();
	}

	@Override
	public void bindView(final View view, final Context context, final int position) {
		final ViewHolder holder = (ViewHolder) view.getTag();
		final Bicloo bicloo = getItem(position);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			if (mCheckedItemPositions.get(position)) {
				view.setBackgroundResource(R.color.list_activated);
			} else {
				view.setBackgroundResource(android.R.color.transparent);
			}
		}

		holder.itemTitle.setText(bicloo.getName());

		final Bicloo.Status status = bicloo.getStatus();

		if (Bicloo.Status.UNKNOWN.equals(status)) {
			holder.itemDescription.setText(R.string.bicloo_indisponible);
		} else {
			final int availableBikes = bicloo.getAvailableBike();
			final int availableStands = bicloo.getAvailableBikeStands();
			final String description = FormatUtils.formatBicloos(getContext(), availableBikes, availableStands);
			holder.itemDescription.setText(description);
			
			// Définir la distance.
			if (bicloo.getDistance() == null) {
				holder.itemDistance.setText("");
			} else {
				holder.itemDistance.setText(DistanceUtils.formatDist(bicloo.getDistance()));
			}
		}

	}

	@Override
	public void bindViewHolder(final View view) {
		final ViewHolder holder = new ViewHolder();
		holder.itemTitle = (TextView) view.findViewById(R.id.itemTitle);
		holder.itemDescription = (TextView) view.findViewById(R.id.itemDescription);
		holder.itemSymbole = (ImageView) view.findViewById(R.id.itemSymbole);
		holder.itemDistance = (TextView) view.findViewById(R.id.itemDistance);

		holder.itemSymbole.setImageResource(mSymboleResId);
		holder.itemSymbole.setBackgroundDrawable(mSymboleBackground);
		view.setTag(holder);
	}

	public void setCheckedItemPositions(final SparseBooleanArray checkedItemPositions) {
		mCheckedItemPositions = checkedItemPositions;
	}

	public void clearCheckedItemPositions() {
		mCheckedItemPositions.clear();
	}

}
