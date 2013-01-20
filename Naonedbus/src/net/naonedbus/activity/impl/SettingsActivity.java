package net.naonedbus.activity.impl;

import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.helper.SlidingMenuHelper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.view.KeyEvent;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.slidingmenu.lib.SlidingMenu;

public class SettingsActivity extends SherlockPreferenceActivity implements OnSharedPreferenceChangeListener {

	private ListPreference theme;

	/**
	 * Gestion du menu latéral.
	 */
	private SlidingMenu mSlidingMenu;
	private SlidingMenuHelper mSlidingMenuHelper;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(NBApplication.THEMES_MENU_RES[NBApplication.THEME]);

		super.onCreate(savedInstanceState);

		mSlidingMenu = new SlidingMenu(this);
		mSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);

		mSlidingMenuHelper = new SlidingMenuHelper(this);
		mSlidingMenuHelper.setupActionBar(getSupportActionBar());
		mSlidingMenuHelper.setupSlidingMenu(mSlidingMenu);

		addPreferencesFromResource(R.xml.preferences);

		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

		theme = (ListPreference) getPreferenceScreen().findPreference(NBApplication.PREF_THEME);
//		initTheme(preferences);
	}

	@Override
	public void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mSlidingMenuHelper.onPostCreate(getIntent(), mSlidingMenu, savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mSlidingMenuHelper.onSaveInstanceState(outState);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		mSlidingMenuHelper.onWindowFocusChanged(hasFocus, mSlidingMenu);
	}

	/**
	 * Show the menu when home icon is clicked.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			mSlidingMenu.toggle();
			return true;
		default:
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Show the menu when menu button pressed, hide it when back is pressed
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU || (mSlidingMenu.isMenuShowing() && keyCode == KeyEvent.KEYCODE_BACK)) {
			mSlidingMenu.toggle();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}

	private void initTheme(SharedPreferences preferences) {
		final String[] themeOptions = this.getResources().getStringArray(R.array.themeOptions);
		theme.setSummary(themeOptions[Integer.valueOf(preferences.getString(NBApplication.PREF_THEME, "0"))]);
		theme.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				final Integer themePosition = Integer.valueOf((String) newValue);
				theme.setSummary(themeOptions[themePosition]);
				NBApplication.THEME = NBApplication.THEMES_RES[themePosition];
				return true;
			}
		});
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
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
