/**
 * Copyright (C) 2013 Romain Guefveneu.
 *   
 *  This file is part of naonedbus.
 *   
 *  Naonedbus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  Naonedbus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.naonedbus.activity.impl;

import java.text.DateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import net.naonedbus.R;
import net.naonedbus.bean.Arret;
import net.naonedbus.bean.Ligne;
import net.naonedbus.bean.Sens;
import net.naonedbus.helper.HeaderHelper;
import net.naonedbus.utils.CalendarUtils;
import net.naonedbus.utils.FormatUtils;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;

public class AddEventActivity extends SherlockActivity {

	public static final String PARAM_LIGNE = "ligne";
	public static final String PARAM_SENS = "sens";
	public static final String PARAM_ARRET = "arret";
	public static final String PARAM_TIMESTAMP = "timestamp";

	private Map<Integer, String> mCalendars;

	private TextView mDateEvent;
	private Spinner mSpinnerCalendars;
	private Spinner mSpinnerDelai;
	private EditText mCommentText;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_addevent);

		final Ligne ligne = getIntent().getParcelableExtra(PARAM_LIGNE);
		final Sens sens = getIntent().getParcelableExtra(PARAM_SENS);
		final Arret arret = getIntent().getParcelableExtra(PARAM_ARRET);
		final long timestamp = getIntent().getLongExtra(PARAM_TIMESTAMP, 0);

		final HeaderHelper headerHelper = new HeaderHelper(this);
		headerHelper.setBackgroundColor(ligne.getCouleur(), ligne.getCouleurTexte());
		headerHelper.setCode(ligne.getLettre());
		headerHelper.setTitle(arret.getNomArret());
		headerHelper.setSubTitle(FormatUtils.formatSens(sens.text));

		final ActionBar actionBar = getSupportActionBar();

		// Inflate a "Done/Discard" custom action bar view.
		final LayoutInflater inflater = (LayoutInflater) actionBar.getThemedContext().getSystemService(
				LAYOUT_INFLATER_SERVICE);

		final View customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_done_discard, null);

		customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				final String[] delais = AddEventActivity.this.getResources().getStringArray(R.array.delais);
				addToCalendar(AddEventActivity.this, arret.getNomArret(), mCommentText.getText().toString(),
						getCalendarId(), Integer.valueOf(delais[mSpinnerDelai.getSelectedItemPosition()]), timestamp);
				Toast.makeText(getApplicationContext(), getString(R.string.add_event_toast), Toast.LENGTH_LONG).show();
				finish();
			}
		});
		customActionBarView.findViewById(R.id.actionbar_discard).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				finish();
			}
		});

		// Show the custom action bar view and hide the normal Home icon and
		// title.
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM
				| ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
		actionBar.setCustomView(customActionBarView, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));

		mDateEvent = (TextView) findViewById(R.id.dateEvent);
		mSpinnerCalendars = (Spinner) findViewById(R.id.spinnerCalendrier);
		mSpinnerDelai = (Spinner) findViewById(R.id.spinnerDelai);
		mCommentText = (EditText) findViewById(R.id.comment);

		final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.DEFAULT);
		mDateEvent.setText(dateFormat.format(new Date(timestamp)));

		mCommentText.setText(FormatUtils.formatTitle(getString(R.string.dialog_title_menu_lignes, ligne.getCode()),
				arret.getNomArret(), sens.text));

		fillCalendars();
		fillDelais();
	}

	private void fillCalendars() {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		mCalendars = CalendarUtils.getCalendars(getContentResolver());
		final String calendarId = preferences.getString("calendrier.defaut", "");
		int itemSelectedIndex = 0;

		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		int count = 0;
		for (final Entry<Integer, String> cal : mCalendars.entrySet()) {
			adapter.add(cal.getValue());
			final Integer key = cal.getKey();
			if (key != null && calendarId.equals(key.toString())) {
				itemSelectedIndex = count;
			}
			count++;
		}

		mSpinnerCalendars.setAdapter(adapter);
		mSpinnerCalendars.setSelection(itemSelectedIndex);

	}

	/**
	 * Renvoyer l'id du calendrier selectionné
	 * 
	 * @return
	 */
	private Integer getCalendarId() {
		return (Integer) mCalendars.keySet().toArray()[mSpinnerCalendars.getSelectedItemPosition()];
	}

	private void fillDelais() {
		final String[] delais = this.getResources().getStringArray(R.array.delais);

		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		for (final String delai : delais) {
			adapter.add(delai + " minutes");
		}

		mSpinnerDelai.setAdapter(adapter);
	}

	/**
	 * Adds the event to a calendar.
	 * 
	 * @param ctx
	 *            Context ( Please use the application context )
	 * @param title
	 *            title of the event
	 * @param dtstart
	 *            Start time: The value is the number of milliseconds since Jan.
	 *            1, 1970, midnight GMT.
	 * @param dtend
	 *            End time: The value is the number of milliseconds since Jan.
	 *            1, 1970, midnight GMT.
	 */
	private void addToCalendar(final Context ctx, final String title, final String content, final int calendarId,
			final int minutes, final long timestamp) {

		final ContentResolver cr = ctx.getContentResolver();
		final ContentValues cv = new ContentValues();
		cv.put("calendar_id", calendarId);
		cv.put("title", title);
		cv.put("description", content);
		cv.put("dtstart", timestamp);
		cv.put("eventTimezone", TimeZone.getDefault().getID());
		cv.put("dtend", timestamp);
		cv.put("hasAlarm", 1);

		Uri newEvent;
		if (Build.VERSION.SDK_INT >= 8)
			newEvent = cr.insert(Uri.parse("content://com.android.calendar/events"), cv);
		else
			newEvent = cr.insert(Uri.parse("content://com.android.calendar/events"), cv);

		if (newEvent != null) {
			final long id = Long.parseLong(newEvent.getLastPathSegment());
			final ContentValues values = new ContentValues();
			values.put("event_id", id);
			values.put("method", 1);
			values.put("minutes", minutes);
			if (Build.VERSION.SDK_INT >= 8)
				cr.insert(Uri.parse("content://com.android.calendar/reminders"), values);
			else
				cr.insert(Uri.parse("content://calendar/reminders"), values);

		}

	}

	@Override
	public void finish() {
		super.finish();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			overridePendingTransition(R.anim.half_fade_in, R.anim.slide_out_to_right);
	}

}
