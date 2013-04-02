package net.naonedbus.fragment.impl;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.manager.impl.HoraireManager;
import net.naonedbus.utils.CalendarUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SettingsFragments extends PreferenceFragment {

	private ListPreference calendrierDefaut;
	private Preference clearCachePlan;
	private Preference clearCacheHoraires;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

		calendrierDefaut = (ListPreference) getPreferenceScreen().findPreference(NBApplication.PREF_CALENDRIER_DEFAUT);
		clearCachePlan = getPreferenceScreen().findPreference("plan.cache.clear");
		clearCacheHoraires = getPreferenceScreen().findPreference("horaires.cache.clear");

		initCalendar(preferences);
		initClearCache(preferences);
	}

	/**
	 * Initier la liste des calendriers
	 * 
	 * @param preferences
	 */
	private void initCalendar(final SharedPreferences preferences) {
		calendrierDefaut.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(final Preference preference, final Object newValue) {
				setCalendarSummary((String) newValue);
				return true;
			}
		});
		setCalendarSummary(preferences);
		fillCalendars(calendrierDefaut);
	}

	/**
	 * Initier le vidage du cache
	 * 
	 * @param preferences
	 */
	private void initClearCache(final SharedPreferences preferences) {
		clearCachePlan.setSummary(getString(R.string.pref_cache_size, readableFileSize(getCacheSize())));

		clearCachePlan.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(final Preference preference) {
				try {
					clearCache();
					clearCachePlan.setSummary(getString(R.string.pref_cache_size, readableFileSize(getCacheSize())));
				} catch (final IOException e) {
					BugSenseHandler.sendExceptionMessage("Erreur lors de la suppression du cache des plans", null, e);
				}

				return false;
			}
		});

		clearCacheHoraires.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(final Preference preference) {
				clearCacheHoraires();
				Toast.makeText(getActivity(), R.string.msg_cache_horaire_clear, Toast.LENGTH_SHORT).show();
				return false;
			}
		});
	}

	/**
	 * Vider le cache des plans
	 * 
	 * @throws IOException
	 */
	private void clearCache() throws IOException {
		FileUtils.deleteQuietly(getActivity().getCacheDir());
		clearWebviewCache();
	}

	/**
	 * Vider le cache horaires
	 */
	private void clearCacheHoraires() {
		final HoraireManager horaireManager = HoraireManager.getInstance();
		horaireManager.clearAllHoraires(getActivity().getContentResolver());
		clearWebviewCache();
	}

	/**
	 * Supprimer le cache webView
	 */
	private void clearWebviewCache() {
		final File directory = getActivity().getFilesDir();

		final Collection<File> webviewFiles = FileUtils.listFiles(directory, webViewFilter, webViewFilter);
		for (final File file : webviewFiles) {
			file.delete();
		}
	}

	private static IOFileFilter webViewFilter = new IOFileFilter() {

		@Override
		public boolean accept(final File file) {
			return file.getName().startsWith("webview");
		}

		@Override
		public boolean accept(final File file, final String name) {
			return name.startsWith("webview");
		}

	};

	/**
	 * Lister les calendrier dans la ListPreference passée en paramètre
	 * 
	 * @param list
	 */
	private void fillCalendars(final ListPreference list) {
		CharSequence[] entriesName;
		CharSequence[] entriesId;
		final Map<Integer, String> calendars = CalendarUtils.getCalendars(getActivity().getContentResolver());

		entriesName = new String[calendars.size()];
		entriesId = new String[calendars.size()];

		int i = 0;
		for (final Entry<Integer, String> cal : calendars.entrySet()) {
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
	private void setCalendarSummary(final SharedPreferences preferences) {
		final String calendarId = preferences.getString(NBApplication.PREF_CALENDRIER_DEFAUT, null);
		setCalendarSummary(calendarId);
	}

	/**
	 * Afficher le sous-titre du calendrier
	 * 
	 * @param id
	 */
	private void setCalendarSummary(final String id) {
		if (id != null) {
			calendrierDefaut.setSummary(CalendarUtils.getCalendarName(getActivity().getContentResolver(), id));
		} else {
			calendrierDefaut.setSummary(R.string.pref_calendar_summary);
		}
	}

	/**
	 * Calculer la taille du cache
	 * 
	 * @return La taille du cache en octets
	 */
	private long getCacheSize() {
		final File cache = getActivity().getCacheDir();
		return FileUtils.sizeOfDirectory(cache);
	}

	/**
	 * Formatter la taille
	 * 
	 * @param size
	 * @return Taille compréhensible par les humains ordinaires
	 */
	private String readableFileSize(final long size) {
		if (size <= 0)
			return getString(R.string.msg_vide);
		final String[] units = new String[] { "o", "Ko", "Mo", "Go", "To" };
		final int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}
}
