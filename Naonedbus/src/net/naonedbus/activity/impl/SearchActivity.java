package net.naonedbus.activity.impl;

import net.naonedbus.R;
import net.naonedbus.activity.OneFragmentSlidingActivity;
import net.naonedbus.fragment.impl.SearchFragment;
import net.naonedbus.widget.ModaleSearchView;
import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;

public class SearchActivity extends OneFragmentSlidingActivity {

	public SearchActivity() {
		super(R.layout.activity_main);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			addFragment(SearchFragment.class);
		}

		final SearchFragment searchFragment = (SearchFragment) getCurrentFragment();
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setCustomView(R.layout.search_view);

		final ModaleSearchView searchViewLayout = (ModaleSearchView) actionBar.getCustomView();
		searchViewLayout.setOnQueryTextListener(searchFragment);
	}

}
