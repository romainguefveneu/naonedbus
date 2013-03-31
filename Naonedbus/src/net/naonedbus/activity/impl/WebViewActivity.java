package net.naonedbus.activity.impl;

import net.naonedbus.R;
import net.naonedbus.activity.OneFragmentActivity;
import net.naonedbus.fragment.impl.WebViewFragment;
import android.os.Bundle;

public class WebViewActivity extends OneFragmentActivity {

	public final static String PARAM_RAW_ID = "rawId";

	public WebViewActivity() {
		super(R.layout.activity_one_fragment);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			final Bundle bundle = new Bundle();
			bundle.putInt(WebViewFragment.PARAM_RAW_ID, getIntent().getIntExtra(PARAM_RAW_ID, 0));
			addFragment(WebViewFragment.class, bundle);
		}
	}
}
