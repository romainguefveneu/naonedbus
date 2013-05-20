package net.naonedbus.activity.impl;

import net.naonedbus.R;
import net.naonedbus.activity.OneFragmentSlidingActivity;
import net.naonedbus.fragment.impl.DonateFragment;
import android.os.Bundle;

public class DonateActivity extends OneFragmentSlidingActivity {

	public DonateActivity() {
		super(R.layout.activity_one_fragment);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			addFragment(DonateFragment.class);
		}
	}
}
