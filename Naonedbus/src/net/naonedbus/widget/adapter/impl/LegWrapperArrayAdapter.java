package net.naonedbus.widget.adapter.impl;

import java.util.List;

import net.naonedbus.R;
import net.naonedbus.bean.LegWrapper;
import net.naonedbus.bean.LegWrapper.Type;
import net.naonedbus.bean.Ligne;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.utils.FontUtils;
import net.naonedbus.utils.FormatUtils;
import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import fr.ybo.opentripplanner.client.modele.Place;

public class LegWrapperArrayAdapter extends ArrayAdapter<LegWrapper> {

	private final LayoutInflater mLayoutInflater;
	private final int mSecondaryColor;
	private final Typeface mRobotoCondensed;

	public LegWrapperArrayAdapter(final Context context, final List<LegWrapper> objects) {
		super(context, 0, objects);
		mLayoutInflater = LayoutInflater.from(context);

		mRobotoCondensed = FontUtils.getRobotoBoldCondensed(context);

		// Lazily get the URL color from the current theme.
		final TypedValue colorValue = new TypedValue();
		context.getTheme().resolveAttribute(R.attr.secondaryColor, colorValue, true);
		mSecondaryColor = context.getResources().getColor(colorValue.resourceId);
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(final int position) {
		return false;
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		View view = convertView;
		ViewHolder viewHolder;
		if (view == null) {
			view = mLayoutInflater.inflate(R.layout.list_item_leg, parent, false);

			viewHolder = new ViewHolder();
			viewHolder.itemIcon = (ImageView) view.findViewById(R.id.itemIcon);
			viewHolder.itemSymbole = (TextView) view.findViewById(R.id.itemSymbole);
			viewHolder.itemTime = (TextView) view.findViewById(R.id.itemTime);
			viewHolder.itemPlace = (TextView) view.findViewById(R.id.itemPlace);
			viewHolder.itemMetroPoint = view.findViewById(R.id.itemMetroPoint);

			viewHolder.itemSymbole.setTypeface(mRobotoCondensed);

			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}

		final LegWrapper legWrapper = getItem(position);
		final Ligne ligne = legWrapper.getLigne();
		final Place place = legWrapper.getPlace();

		if ("WALK".equals(legWrapper.getMode())) {

			viewHolder.itemSymbole.setVisibility(View.INVISIBLE);

			if (legWrapper.getType() == Type.IN) {

				final String direction = FormatUtils.formatWithDot(legWrapper.getDuration(), legWrapper.getDistance());
				final CharSequence title = TextUtils.concat(getContext()
						.getString(R.string.itinerary_go_to, place.name) + "\n",
						FormatUtils.formatColorAndSize(getContext(), mSecondaryColor, direction));

				viewHolder.itemIcon.setVisibility(View.VISIBLE);
				viewHolder.itemPlace.setText(title);
			} else {
				viewHolder.itemIcon.setVisibility(View.GONE);
				viewHolder.itemPlace.setText(place.name);
			}

		} else {

			if (legWrapper.getType() == Type.IN) {
				viewHolder.itemPlace.setText(place.name);

				final String direction = FormatUtils.formatSens(legWrapper.getHeadsign());
				final CharSequence title = TextUtils.concat(place.name + "\n",
						FormatUtils.formatColorAndSize(getContext(), mSecondaryColor, direction));

				viewHolder.itemPlace.setText(title);
			} else {
				viewHolder.itemPlace.setText(place.name);
			}

			if (ligne != null) {
				viewHolder.itemIcon.setVisibility(View.GONE);
				viewHolder.itemSymbole.setVisibility(View.VISIBLE);

				viewHolder.itemSymbole.setText(ligne.getLettre());
				viewHolder.itemSymbole.setTextColor(ligne.getCouleurTexte());
				viewHolder.itemSymbole.setBackgroundDrawable(ColorUtils.getRoundedGradiant(ligne.getCouleur()));
			} else {
				viewHolder.itemIcon.setVisibility(View.INVISIBLE);
				viewHolder.itemSymbole.setVisibility(View.INVISIBLE);
			}
		}

		if (legWrapper.isTrip()) {
			if (legWrapper.getType() == Type.IN) {
				viewHolder.itemMetroPoint.setBackgroundResource(R.drawable.ic_arret_first_silver);
			} else {
				viewHolder.itemMetroPoint.setBackgroundResource(R.drawable.ic_arret_last_silver);
			}
			viewHolder.itemMetroPoint.setVisibility(View.VISIBLE);
		} else {
			viewHolder.itemMetroPoint.setVisibility(View.GONE);
		}

		viewHolder.itemTime.setText(legWrapper.getTime());

		return view;
	}

	private static class ViewHolder {
		ImageView itemIcon;
		TextView itemSymbole;
		TextView itemTime;
		TextView itemPlace;
		View itemMetroPoint;
	}

}
