package net.naonedbus.fragment;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public interface CustomFragmentActions {

	int getTitleId();

	void onCreateOptionsMenu(Menu menu);
	
	void onPrepareOptionsMenu(Menu menu);

	boolean onOptionsItemSelected(MenuItem item);

}
