package net.naonedbus.activity.impl;

import net.naonedbus.R;
import net.naonedbus.activity.OneFragmentActivity;
import net.naonedbus.fragment.impl.AddressSearchFragment;
import android.os.Bundle;

public class AddressSearchActivity extends OneFragmentActivity {

	public AddressSearchActivity() {
		super(R.layout.activity_one_fragment);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			addFragment(AddressSearchFragment.class);
		}
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.half_fade_in, R.anim.slide_out_to_right);
	}
}
