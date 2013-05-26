package net.naonedbus.activity.impl;

import net.naonedbus.R;
import net.naonedbus.activity.OneFragmentActivity;
import net.naonedbus.fragment.impl.ItineraryDetailFragment;
import android.os.Bundle;
import fr.ybo.opentripplanner.client.modele.Itinerary;

public class ItineraryDetailActivity extends OneFragmentActivity {

	public static final String PARAM_ITINERARY = "itinerary";

	public ItineraryDetailActivity() {
		super(R.layout.activity_one_fragment);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			final Itinerary itinerary = (Itinerary) getIntent().getSerializableExtra(PARAM_ITINERARY);
			final Bundle bundle = new Bundle();
			bundle.putSerializable(ItineraryDetailFragment.PARAM_ITINERARY, itinerary);
			addFragment(ItineraryDetailFragment.class, bundle);
		}
	}

}
