package net.naonedbus.fragment.impl;

import android.os.Bundle;
import android.view.View;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import net.naonedbus.R;
import net.naonedbus.fragment.CustomFragment;

public class SensFragment extends CustomFragment {

	public SensFragment() {
		super(R.string.title_fragment_sens, R.layout.fragment_listview);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu) {

	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return false;
	}

	@Override
	protected void bindView(View view, Bundle savedInstanceState) {

	}

}
