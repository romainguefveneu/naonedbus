package net.naonedbus.activity.impl;

import net.naonedbus.R;
import net.naonedbus.activity.OneFragmentActivity;
import net.naonedbus.fragment.impl.AddressSearchFragment;
import android.os.Build;
import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;

public class AddressSearchActivity extends OneFragmentActivity {

	public AddressSearchActivity() {
		super(R.layout.activity_one_fragment);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setProgressBarIndeterminate(true);

		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setCustomView(R.layout.search_view);

		if (savedInstanceState == null) {
			addFragment(AddressSearchFragment.class);
		}
	}

	@Override
	public void finish() {
		super.finish();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			overridePendingTransition(R.anim.half_fade_in, R.anim.slide_out_to_right);
	}
}
