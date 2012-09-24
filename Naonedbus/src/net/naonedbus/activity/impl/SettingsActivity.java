package net.naonedbus.activity.impl;

import net.naonedbus.NBApplication;
import net.naonedbus.R;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;

public class SettingsActivity extends SherlockPreferenceActivity implements OnSharedPreferenceChangeListener {

	private ListPreference theme;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(NBApplication.THEME);

		super.onCreate(savedInstanceState);

		final ActionBar actionBar = getSupportActionBar();
		actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_back));
		actionBar.setIcon(R.drawable.ic_launcher);

		addPreferencesFromResource(R.xml.preferences);

		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

		theme = (ListPreference) getPreferenceScreen().findPreference(NBApplication.PREF_THEME);
		initTheme(preferences);
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
