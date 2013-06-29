package net.naonedbus.activity.impl;

import net.naonedbus.R;
import net.naonedbus.activity.OneFragmentActivity;
import net.naonedbus.fragment.impl.ItineraryDetailFragment;
import android.os.Bundle;

public class ItineraryDetailActivity extends OneFragmentActivity {

	public static final String PARAM_BUNDLE = "bundle";

	public ItineraryDetailActivity() {
		super(R.layout.activity_one_fragment);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			final Bundle bundle = getIntent().getBundleExtra(PARAM_BUNDLE);
			addFragment(ItineraryDetailFragment.class, bundle);
		}
	}

}
