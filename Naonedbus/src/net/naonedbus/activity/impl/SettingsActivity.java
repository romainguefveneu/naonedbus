package net.naonedbus.activity.impl;

import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.fragment.impl.SettingsFragments;
import net.naonedbus.helper.SlidingMenuHelper;
import net.simonvt.menudrawer.MenuDrawer;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.KeyEvent;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

public class SettingsActivity extends SherlockPreferenceActivity implements OnSharedPreferenceChangeListener {

	/**
	 * Gestion du menu latéral.
	 */
	private MenuDrawer mMenuDrawer;
	private SlidingMenuHelper mSlidingMenuHelper;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		setTheme(NBApplication.THEMES_MENU_RES[NBApplication.THEME]);
		getIntent().putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, SettingsFragments.class.getName());
		getIntent().putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);

		setContentView(R.layout.fragment_listview);

		super.onCreate(savedInstanceState);

		mMenuDrawer = MenuDrawer.attach(this, MenuDrawer.MENU_DRAG_WINDOW);

		mSlidingMenuHelper = new SlidingMenuHelper(this);
		mSlidingMenuHelper.setupActionBar(getSupportActionBar());
		mSlidingMenuHelper.setupSlidingMenu(mMenuDrawer);
	}

	@Override
	public void onPostCreate(final Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mSlidingMenuHelper.onPostCreate(getIntent(), mMenuDrawer, savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		mSlidingMenuHelper.onSaveInstanceState(outState);
	}

	@Override
	public void onWindowFocusChanged(final boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		mSlidingMenuHelper.onWindowFocusChanged(hasFocus, mMenuDrawer);
	}

	/**
	 * Show the menu when home icon is clicked.
	 */
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			mMenuDrawer.toggleMenu();
			return true;
		default:
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Show the menu when menu button pressed, hide it when back is pressed
	 */
	@Override
	public boolean onKeyDown(final int keyCode, final KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU || (mMenuDrawer.isMenuVisible() && keyCode == KeyEvent.KEYCODE_BACK)) {
			mMenuDrawer.toggleMenu();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
		if (key.equals(NBApplication.PREF_THEME)) {
			restart();
		}
	}

	public void restart() {
		startActivity(new Intent(this, this.getClass()));
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		finish();
	}

}
