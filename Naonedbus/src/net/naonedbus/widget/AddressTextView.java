package net.naonedbus.widget;

import net.naonedbus.R;
import net.naonedbus.task.AddressResolverTask;
import net.naonedbus.task.AddressResolverTask.AddressTaskListener;
import net.naonedbus.utils.FormatUtils;
import net.naonedbus.widget.adapter.impl.AddressArrayAdapter;
import net.naonedbus.widget.adapter.impl.AddressArrayAdapter.AddressWrapper;
import android.content.Context;
import android.location.Address;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AddressTextView extends RelativeLayout implements TextWatcher, OnItemClickListener, OnFocusChangeListener,
		AddressTaskListener {

	public interface OnLocationEditChange {
		void onLocationFound();

		void onLocationNotFound();
	}

	private OnLocationEditChange mOnLocationEditChange;

	private final AutoCompleteTextView mAutoCompleteTextView;
	private final TextView mFormattedText;
	private final ProgressBar mProgressBar;

	private View mNextFocusView;
	private String mAddressString = "";
	private double mLatitude;
	private double mLongitude;

	public AddressTextView(final Context context, final AttributeSet attrs) {
		super(context, attrs);

		LayoutInflater.from(context).inflate(R.layout.addresstextview, this);

		mAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.formItemAddress);
		mAutoCompleteTextView.setAdapter(new AddressArrayAdapter(context));
		mAutoCompleteTextView.addTextChangedListener(this);
		mAutoCompleteTextView.setOnItemClickListener(this);

		mProgressBar = (ProgressBar) findViewById(R.id.formItemProgress);

		mFormattedText = (TextView) findViewById(R.id.formItemAddressFormatted);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		mAutoCompleteTextView.setOnFocusChangeListener(this);
	}

	public void setOnLocationEditChange(final OnLocationEditChange onLocationEditChange) {
		mOnLocationEditChange = onLocationEditChange;
	}

	public void setNextFocusView(final View nextFocusView) {
		mNextFocusView = nextFocusView;
	}

	private void setAddress(final Address address) {
		mAddressString = FormatUtils.formatAddress(address, null);

		mFormattedText.setText(FormatUtils.formatAddressTwoLine(address));
		mFormattedText.setVisibility(View.VISIBLE);

		mLatitude = address.getLatitude();
		mLongitude = address.getLongitude();

		if (mOnLocationEditChange != null)
			mOnLocationEditChange.onLocationFound();

		if (mNextFocusView != null)
			mNextFocusView.requestFocus();
	}

	public boolean hasLocation() {
		return mLatitude != 0;
	}

	public double getLatitude() {
		return mLatitude;
	}

	public double getLongitude() {
		return mLongitude;
	}

	@Override
	public void onAddressTaskPreExecute() {
		mAutoCompleteTextView.setEnabled(false);
		mAutoCompleteTextView.setText(R.string.loading);
		mProgressBar.setVisibility(View.VISIBLE);
	}

	@Override
	public void onAddressTaskResult(final Address address) {
		mAutoCompleteTextView.setEnabled(true);
		mProgressBar.setVisibility(View.GONE);
		if (address != null) {
			setAddress(address);
		} else {
			mAutoCompleteTextView.setText("");
		}
	}

	@Override
	public void onFocusChange(final View v, final boolean hasFocus) {
		if (hasFocus) {
			mFormattedText.setVisibility(View.GONE);
			mAutoCompleteTextView.setVisibility(View.VISIBLE);

			mAutoCompleteTextView.setText(mAddressString);

			if (!mAutoCompleteTextView.isPopupShowing()) {
				mAutoCompleteTextView.showDropDown();
			}
		} else {
			mFormattedText.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
		mLatitude = 0;
		if (mOnLocationEditChange != null)
			mOnLocationEditChange.onLocationNotFound();
	}

	@Override
	public void onItemClick(final AdapterView<?> adapter, final View view, final int position, final long id) {
		final AddressWrapper wrapper = (AddressWrapper) adapter.getItemAtPosition(position);
		if (wrapper.isLocateMe()) {
			loadCurrentAddress();
		} else {
			setAddress(wrapper.getAddress());
		}
	}

	public void loadCurrentAddress() {
		new AddressResolverTask(this).execute();
		if (mNextFocusView != null)
			mNextFocusView.requestFocus();
	}

	@Override
	public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

	}

	@Override
	public void afterTextChanged(final Editable s) {

	}

}
