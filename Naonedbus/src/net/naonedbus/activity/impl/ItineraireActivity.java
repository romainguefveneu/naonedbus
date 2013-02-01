package net.naonedbus.activity.impl;

import net.naonedbus.R;
import net.naonedbus.activity.OneFragmentSlidingActivity;
import net.naonedbus.fragment.impl.ItineraireFragment;
import android.os.Bundle;

public class ItineraireActivity extends OneFragmentSlidingActivity {

	public ItineraireActivity() {
		super(R.layout.activity_one_fragment);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addFragment(ItineraireFragment.class);
	}

}
