package net.naonedbus.widget.adapter.impl;

import java.util.List;

import net.naonedbus.R;
import net.naonedbus.bean.Sens;
import net.naonedbus.utils.SymbolesUtils;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class SensSpinnerAdapter extends SensArrayAdapter implements SpinnerAdapter {

	public SensSpinnerAdapter(Context context, List<Sens> objects, int textColor, Typeface typeface) {
		super(context, R.layout.list_item_sens_spinner, objects);
		setTypeface(typeface);
		setTextColor(textColor);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final TextView view = (TextView) super.getView(position, convertView, parent);
		final Sens sens = getItem(position);
		view.setText(SymbolesUtils.formatSens(sens.text));
		return view;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_icon, null);
			bindViewHolder(convertView);
		}
		bindView(convertView, position);
		return convertView;
	}
}
