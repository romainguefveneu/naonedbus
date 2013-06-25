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
		overridePendingTransition(0, R.anim.slide_in_from_right);
		super.finish();
	}
}
