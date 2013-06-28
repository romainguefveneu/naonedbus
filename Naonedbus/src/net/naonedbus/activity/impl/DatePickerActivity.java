package net.naonedbus.activity.impl;

import net.naonedbus.R;

import org.joda.time.DateTime;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;

public class DatePickerActivity extends SherlockActivity {

	public static final String PARAM_YEAR = "year";
	public static final String PARAM_MONTH = "month";
	public static final String PARAM_DAY = "day";
	public static final String PARAM_HOUR = "hour";
	public static final String PARAM_MINUTE = "minute";
	public static final String PARAM_ARRIVE_BY = "arriveBy";

	private Spinner mKind;
	private DatePicker mDatePicker;
	private TimePicker mTimePicker;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_date_picker);

		final ActionBar actionBar = getSupportActionBar();

		// Inflate a "Done/Discard" custom action bar view.
		final LayoutInflater inflater = (LayoutInflater) actionBar.getThemedContext().getSystemService(
				LAYOUT_INFLATER_SERVICE);

		final View customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_done_discard, null);

		customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				final Intent data = new Intent();
				data.putExtra(PARAM_YEAR, mDatePicker.getYear());
				data.putExtra(PARAM_MONTH, mDatePicker.getMonth() + 1);
				data.putExtra(PARAM_DAY, mDatePicker.getDayOfMonth());
				data.putExtra(PARAM_HOUR, mTimePicker.getCurrentHour());
				data.putExtra(PARAM_MINUTE, mTimePicker.getCurrentMinute());
				data.putExtra(PARAM_ARRIVE_BY, mKind.getSelectedItemPosition() == 1);

				setResult(1, data);
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

		mKind = (Spinner) findViewById(R.id.dateKind);
		mDatePicker = (DatePicker) findViewById(R.id.datePicker);
		mTimePicker = (TimePicker) findViewById(R.id.timePicker);

		initDateTime();
		initSpinner();
	}

	private void initDateTime() {
		final DateTime now = new DateTime();

		final Intent intent = getIntent();
		final int year = intent.getIntExtra(PARAM_YEAR, now.getYear());
		final int month = intent.getIntExtra(PARAM_MONTH, now.getMonthOfYear());
		final int day = intent.getIntExtra(PARAM_DAY, now.getDayOfMonth());
		final int hour = intent.getIntExtra(PARAM_HOUR, now.getHourOfDay());
		final int minute = intent.getIntExtra(PARAM_MINUTE, now.getMinuteOfHour());

		mDatePicker.init(year, month, day, null);

		mTimePicker.setIs24HourView(DateFormat.is24HourFormat(this));
		mTimePicker.setCurrentHour(hour);
		mTimePicker.setCurrentMinute(minute);
	}

	private void initSpinner() {
		final Intent intent = getIntent();
		mKind.setSelection(intent.getBooleanExtra(PARAM_ARRIVE_BY, false) ? 1 : 0);
	}

	@Override
	public void finish() {
		super.finish();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			overridePendingTransition(R.anim.half_fade_in, R.anim.slide_out_to_right);
	}

}
