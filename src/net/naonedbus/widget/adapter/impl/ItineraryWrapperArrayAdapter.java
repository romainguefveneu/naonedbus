package net.naonedbus.widget.adapter.impl;

import java.util.List;
import java.util.Random;

import net.naonedbus.R;
import net.naonedbus.bean.ItineraryWrapper;
import net.naonedbus.bean.Route;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.utils.FontUtils;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.gridlayout.GridLayout;

public class ItineraryWrapperArrayAdapter extends ArrayAdapter<ItineraryWrapper> {

	private static final int VIEW_TYPE_NORMAL = 0;
	private static final int VIEW_TYPE_EMPTY = 1;

	private final LayoutInflater mLayoutInflater;
	private final Typeface mRobotoCondensed;
	private final Typeface mRobotoMedium;

	public ItineraryWrapperArrayAdapter(final Context context, final List<ItineraryWrapper> objects) {
		super(context, 0, objects);
		mLayoutInflater = LayoutInflater.from(context);
		mRobotoCondensed = FontUtils.getRobotoBoldCondensed(context);
		mRobotoMedium = FontUtils.getRobotoMedium(context);
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public int getItemViewType(final int position) {
		final ItineraryWrapper wrapper = getItem(position);
		return wrapper.getItinerary() == null ? VIEW_TYPE_EMPTY : VIEW_TYPE_NORMAL;
	}

	@Override
	public boolean isEnabled(final int position) {
		return getItemViewType(position) == VIEW_TYPE_NORMAL;
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		final int viewType = getItemViewType(position);
		View view = convertView;
		ViewHolder viewHolder;
		if (view == null) {
			if (viewType == VIEW_TYPE_NORMAL) {
				view = inflateNormalView(parent);
			} else {
				view = inflateEmptyView(parent);
			}
		}

		viewHolder = (ViewHolder) view.getTag();
		if (viewType == VIEW_TYPE_NORMAL) {
			bindNormalView(getItem(position), viewHolder);
		} else {
			bindEmptyView(getItem(position), viewHolder);
		}

		return view;
	}

	private View inflateNormalView(final ViewGroup parent) {
		final View view = mLayoutInflater.inflate(R.layout.list_item_itineraire, parent, false);

		final ViewHolder viewHolder = new ViewHolder();
		viewHolder.itemTitle = (TextView) view.findViewById(R.id.itemTitle);
		viewHolder.itemDate = (TextView) view.findViewById(R.id.itemDate);
		viewHolder.itemWalkTime = (TextView) view.findViewById(R.id.itemWalkTime);
		viewHolder.gridLayout = (GridLayout) view.findViewById(R.id.lignes);

		view.setTag(viewHolder);

		return view;
	}

	private void bindNormalView(final ItineraryWrapper wrapper, final ViewHolder viewHolder) {
		viewHolder.itemTitle.setText(wrapper.getTime());
		viewHolder.itemDate.setText(wrapper.getDate());
		viewHolder.itemWalkTime.setText(wrapper.getWalkTime());

		final List<Route> lignes = wrapper.getLignes();
		viewHolder.gridLayout.removeAllViews();
		for (final Route l : lignes) {
			final TextView textView = (TextView) mLayoutInflater.inflate(R.layout.route_code_item_medium,
					viewHolder.gridLayout, false);
			textView.setBackgroundDrawable(ColorUtils.getGradiant(l.getBackColor()));
			textView.setText(l.getLetter());
			textView.setTypeface(mRobotoCondensed);
			textView.setTextColor(l.getFrontColor());

			viewHolder.gridLayout.addView(textView);
		}
	}

	private void bindEmptyView(final ItineraryWrapper wrapper, final ViewHolder viewHolder) {
		if (wrapper.isUnicorn()) {
			viewHolder.itemTitle.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.unicorn, 0, 0);

			final String[] strings = getContext().getResources().getStringArray(R.array.unicorn);
			final Random rand = new Random();
			final int position = rand.nextInt(strings.length);
			viewHolder.itemTitle.setText(strings[position]);
		} else if (wrapper.isError()) {
			viewHolder.itemTitle.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.warning, 0, 0);
			viewHolder.itemTitle.setText(R.string.nao_webservice_fail);
		}
	}

	private View inflateEmptyView(final ViewGroup parent) {
		final View view = mLayoutInflater.inflate(R.layout.list_item_itinerary_empty, parent, false);

		final ViewHolder viewHolder = new ViewHolder();
		viewHolder.itemTitle = (TextView) view.findViewById(R.id.itemTitle);
		viewHolder.itemTitle.setTypeface(mRobotoCondensed);

		view.setTag(viewHolder);

		return view;
	}

	private static class ViewHolder {
		TextView itemTitle;
		TextView itemDate;
		TextView itemWalkTime;
		GridLayout gridLayout;
	}

}
