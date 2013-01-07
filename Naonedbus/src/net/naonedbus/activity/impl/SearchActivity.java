package net.naonedbus.activity.impl;

import net.naonedbus.R;
import net.naonedbus.activity.OneFragmentActivity;
import net.naonedbus.fragment.impl.SearchFragment;
import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;

public class SearchActivity extends OneFragmentActivity {

	public SearchActivity() {
		super(R.layout.activity_main);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			addFragment(SearchFragment.class);

			ActionBar actionBar = getSupportActionBar();
			actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayShowHomeEnabled(true);

			// final View searchView =
			// LayoutInflater.from(this).inflate(R.layout.search_view, null);
			actionBar.setCustomView(R.layout.search_view);
		}
	}

}
