package net.naonedbus.helper;

import java.util.ArrayList;
import java.util.List;

import net.naonedbus.R;
import net.naonedbus.bean.Ligne;
import net.naonedbus.manager.impl.LigneManager;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.utils.FormatUtils;

import org.joda.time.DateTime;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gridlayout.GridLayout;

import fr.ybo.opentripplanner.client.modele.Itinerary;
import fr.ybo.opentripplanner.client.modele.Leg;

public class ItineraryViewHelper {

	private final Context mContext;
	private final DateTimeFormatHelper mDateTimeFormatHelper;
	private final LayoutInflater mLayoutInflater;

	public ItineraryViewHelper(final Context context) {
		mContext = context;
		mLayoutInflater = LayoutInflater.from(context);
		mDateTimeFormatHelper = new DateTimeFormatHelper(context);
	}

	public View createItineraryView(final Itinerary itinerary, final ViewGroup parent) {
		final LigneManager ligneManager = LigneManager.getInstance();
		final View view = mLayoutInflater.inflate(R.layout.item_itineraire, parent, false);

		final TextView itemTitle = (TextView) view.findViewById(R.id.itemTitle);
		final TextView itemDate = (TextView) view.findViewById(R.id.itemDate);
		final TextView itemWalkTime = (TextView) view.findViewById(R.id.itemWalkTime);
		final GridLayout gridLayout = (GridLayout) view.findViewById(R.id.lignes);

		itemTitle.setText(FormatUtils.formatMinutes(mContext, itinerary.duration));
		itemDate.setText(mDateTimeFormatHelper.formatDuree(new DateTime(itinerary.startTime), new DateTime(
				itinerary.endTime)));

		final int walkTime = Math.round(itinerary.walkTime / 60);
		final String walkTimeText = mContext.getResources().getQuantityString(R.plurals.itinerary_walk_time, walkTime,
				walkTime);
		itemWalkTime.setText(walkTimeText);

		final List<Ligne> lignes = new ArrayList<Ligne>();
		final List<Leg> legs = itinerary.legs;
		for (final Leg leg : legs) {
			if ("BUS".equalsIgnoreCase(leg.mode) || "TRAM".equalsIgnoreCase(leg.mode)) {
				final Ligne ligne = ligneManager.getSingle(mContext.getContentResolver(), leg.route);
				if (ligne != null) {
					lignes.add(ligne);
				}
			}
		}

		for (final Ligne l : lignes) {
			final TextView textView = (TextView) mLayoutInflater.inflate(R.layout.ligne_code_item_medium, gridLayout,
					false);
			textView.setBackgroundDrawable(ColorUtils.getGradiant(l.getCouleur()));
			textView.setText(l.getLettre());
			textView.setTextColor(l.getCouleurTexte());

			gridLayout.addView(textView);
		}

		return view;
	}
}
