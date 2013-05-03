package net.naonedbus.widget.adapter.impl;

import java.util.List;

import net.naonedbus.R;
import net.naonedbus.bean.Bicloo;
import net.naonedbus.bean.Equipement;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.utils.SymbolesUtils;
import net.naonedbus.widget.adapter.ArraySectionAdapter;
import android.content.Context;
import android.graphics.drawable.Drawable;
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

	public BiclooArrayAdapter(final Context context, final List<Bicloo> objects) {
		super(context, R.layout.list_item_equipement, objects);

		mSymboleBackground = ColorUtils.getRoundedGradiant(context.getResources().getColor(
				Equipement.Type.TYPE_BICLOO.getBackgroundColorRes()));
		mSymboleResId = Equipement.Type.TYPE_BICLOO.getDrawableRes();
	}

	@Override
	public void bindView(final View view, final Context context, final int position) {
		final ViewHolder holder = (ViewHolder) view.getTag();
		final Bicloo bicloo = getItem(position);

		holder.itemTitle.setText(bicloo.getName());

		final int availableBikes = bicloo.getAvailableBike();
		final int availableStands = bicloo.getAvailableBikeStands();
		final String bikes = context.getResources().getQuantityString(R.plurals.bicloo_velos_disponibles,
				availableBikes, availableBikes);
		final String stands = context.getResources().getQuantityString(R.plurals.bicloo_places_disponibles,
				availableStands, availableStands);

		holder.itemDescription.setText(SymbolesUtils.formatWithDot(bikes, stands));
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

}
