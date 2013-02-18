package net.naonedbus.fragment.impl;

import java.util.Map;
import java.util.Map.Entry;

import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.helper.FavorisHelper;
import net.naonedbus.utils.CalendarUtils;
import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

@TargetApi(11)
public class SettingsFragments extends PreferenceFragment {

	private Preference importFavoris;
	private ListPreference calendrierDefaut;
	private FavorisHelper favorisUtils;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

		favorisUtils = new FavorisHelper(getActivity(), null);
		calendrierDefaut = (ListPreference) getPreferenceScreen().findPreference(NBApplication.PREF_CALENDRIER_DEFAUT);
		importFavoris = getPreferenceScreen().findPreference(NBApplication.PREF_FAVORIS_IMPORT);

		initCalendar(preferences);
		initFavoris(preferences);
	}

	/**
	 * Initier la liste des calendriers
	 * 
	 * @param preferences
	 */
	private void initCalendar(SharedPreferences preferences) {
		calendrierDefaut.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				setCalendarSummary((String) newValue);
				return true;
			}
		});
		setCalendarSummary(preferences);
		fillCalendars(calendrierDefaut);
	}

	/**
	 * Initier les éléments de configuration des favoris
	 */
	private void initFavoris(SharedPreferences preferences) {
		importFavoris.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				favorisUtils.importFavoris();
				return false;
			}
		});
	}

	/**
	 * Lister les calendrier dans la ListPreference passée en paramètre
	 * 
	 * @param list
	 */
	private void fillCalendars(ListPreference list) {
		CharSequence[] entriesName;
		CharSequence[] entriesId;
		Map<Integer, String> calendars = CalendarUtils.getCalendars(getActivity().getContentResolver());

		entriesName = new String[calendars.size()];
		entriesId = new String[calendars.size()];

		int i = 0;
		for (Entry<Integer, String> cal : calendars.entrySet()) {
			entriesName[i] = cal.getValue();
			entriesId[i++] = String.valueOf(cal.getKey());
		}
		list.setEntries(entriesName);
		list.setEntryValues(entriesId);
	}

	/**
	 * Afficher le sous-titre du calendrier
	 * 
	 * @param preferences
	 */
	private void setCalendarSummary(SharedPreferences preferences) {
		String calendarId = preferences.getString(NBApplication.PREF_CALENDRIER_DEFAUT, null);
		setCalendarSummary(calendarId);
	}

	/**
	 * Afficher le sous-titre du calendrier
	 * 
	 * @param id
	 */
	private void setCalendarSummary(String id) {
		if (id != null) {
			calendrierDefaut.setSummary(CalendarUtils.getCalendarName(getActivity().getContentResolver(), id));
		} else {
			calendrierDefaut.setSummary("Sélectionnez un calendrier par défaut.");
		}
	}

}
