package net.naonedbus.fragment.impl;

import net.naonedbus.R;
import android.annotation.TargetApi;
import android.os.Bundle;
import android.preference.PreferenceFragment;

@TargetApi(11)
public class SettingsFragments extends PreferenceFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}
