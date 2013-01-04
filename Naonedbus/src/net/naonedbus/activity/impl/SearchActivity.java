package net.naonedbus.activity.impl;

import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.activity.OneFragmentActivity;
import net.naonedbus.fragment.impl.SearchFragment;
import android.os.Bundle;

public class SearchActivity extends OneFragmentActivity {

	public SearchActivity() {
		super(R.layout.activity_main);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(NBApplication.THEMES_RES[NBApplication.THEME]);
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			addFragment(SearchFragment.class);
		}
	}

}
