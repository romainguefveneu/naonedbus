package net.naonedbus.helper;

import net.naonedbus.R;

import org.joda.time.DateTime;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import fr.ybo.opentripplanner.client.modele.Itinerary;

public class ItineraryViewHelper {

	private final DateTimeFormatHelper mDateTimeFormatHelper;
	private final LayoutInflater mLayoutInflater;

	public ItineraryViewHelper(final Context context) {
		mLayoutInflater = LayoutInflater.from(context);
		mDateTimeFormatHelper = new DateTimeFormatHelper(context);
	}

	public View createItineraryView(final Itinerary itinerary, final ViewGroup parent) {
		final View view = mLayoutInflater.inflate(R.layout.item_itineraire, parent, false);

		final TextView itemTitle = (TextView) view.findViewById(R.id.itemTitle);
		final TextView itemDate = (TextView) view.findViewById(R.id.itemDate);
		final TextView itemWalkTime = (TextView) view.findViewById(R.id.itemWalkTime);

		itemTitle.setText(DateUtils.formatElapsedTime(itinerary.duration / 1000));
		itemDate.setText(mDateTimeFormatHelper.formatDuree(new DateTime(itinerary.startTime), new DateTime(
				itinerary.endTime)));

		return view;
	}
}
