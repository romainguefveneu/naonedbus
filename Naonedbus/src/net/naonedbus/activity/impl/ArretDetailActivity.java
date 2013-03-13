package net.naonedbus.activity.impl;

import net.naonedbus.BuildConfig;
import net.naonedbus.R;
import net.naonedbus.activity.OneFragmentActivity;
import net.naonedbus.fragment.impl.ArretDetailFragment;
import android.os.Bundle;

public class ArretDetailActivity extends OneFragmentActivity {

	private static final String LOG_TAG = "ArretDetailActivity";
	private static final boolean DBG = BuildConfig.DEBUG;

	public static final String PARAM_LIGNE = "ligne";
	public static final String PARAM_SENS = "sens";
	public static final String PARAM_ARRET = "arret";

	public ArretDetailActivity() {
		super(R.layout.activity_one_fragment);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			addFragment(ArretDetailFragment.class);
		}

	}
}
