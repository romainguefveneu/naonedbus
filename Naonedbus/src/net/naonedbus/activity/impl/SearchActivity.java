package net.naonedbus.activity.impl;

import net.naonedbus.R;
import net.naonedbus.activity.OneFragmentSlidingActivity;
import net.naonedbus.fragment.impl.SearchFragment;
import net.naonedbus.widget.ModalSearchView;
import net.simonvt.menudrawer.MenuDrawer;
import android.content.Context;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;

import com.actionbarsherlock.app.ActionBar;

public class SearchActivity extends OneFragmentSlidingActivity {

	private ModalSearchView mModalSearchView;

	public SearchActivity() {
		super(R.layout.activity_one_fragment);
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

		mModalSearchView = (ModalSearchView) actionBar.getCustomView();
		mModalSearchView.setOnQueryTextListener(searchFragment);
	}

	@Override
	public void onDrawerStateChange(int oldState, int newState) {
		if (newState == MenuDrawer.STATE_CLOSED) {
			mModalSearchView.requestFocus();
			final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

}
