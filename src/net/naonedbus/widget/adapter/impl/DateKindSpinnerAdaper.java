package net.naonedbus.widget.adapter.impl;

import net.naonedbus.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class DateKindSpinnerAdaper extends ArrayAdapter<String> implements SpinnerAdapter {

	private LayoutInflater mLayoutInflater;

	public DateKindSpinnerAdaper(Context context) {
		super(context, R.layout.list_item_date_kind, context.getResources().getStringArray(
				R.array.itinerary_kind_labels));
		mLayoutInflater = LayoutInflater.from(getContext());
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		TextView result = (TextView) convertView;
		if (result == null) {
			result = (TextView) mLayoutInflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
		}

		String item = getItem(position);
		result.setText(item);

		return result;
	}
}
