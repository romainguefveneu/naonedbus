package net.naonedbus.fragment.impl;

import net.naonedbus.R;
import net.naonedbus.fragment.CustomFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class DonateFragment extends CustomFragment implements OnClickListener {

	private static String URL_FLATTR = "https://flattr.com/thing/1059601/naonedbus";
	private static String URL_PAYPAL = "http://t.co/4uKK33eu";

	public DonateFragment() {
		super(R.layout.fragment_donate);
	}

	@Override
	protected void bindView(final View view, final Bundle savedInstanceState) {
		final View donatePaypal = view.findViewById(R.id.donatePaypal);
		final View donateFlattr = view.findViewById(R.id.donateFlattr);
		final TextView summary = (TextView) view.findViewById(android.R.id.summary);

		donatePaypal.setOnClickListener(this);
		donateFlattr.setOnClickListener(this);

		summary.setText(R.string.donate_summary);
	}

	@Override
	public void onClick(final View v) {
		switch (v.getId()) {
		case R.id.donatePaypal:
			openUrl(URL_PAYPAL);
			break;
		case R.id.donateFlattr:
			openUrl(URL_FLATTR);
			break;
		default:
			break;
		}
	}

	private void openUrl(final String url) {
		final Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		startActivity(intent);
	}

}
