package net.naonedbus.activity.impl;

import net.naonedbus.R;
import net.naonedbus.activity.OneFragmentActivity;
import net.naonedbus.fragment.impl.MapFragment;
import android.os.Bundle;

public class MapActivity extends OneFragmentActivity {

	public MapActivity() {
		super(R.layout.activity_one_fragment);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			addFragment(MapFragment.class, getIntent().getExtras());
		}
	}
}
