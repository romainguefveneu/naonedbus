package net.naonedbus.card.impl;

import net.naonedbus.R;
import net.naonedbus.card.Card;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

public class SimpleCard extends Card {

	private int mMessageId;
	private String mMessageString;

	public SimpleCard(final int title, final int message) {
		super(title, R.layout.card_simple);
		mMessageId = message;
	}

	public SimpleCard(final String title, final String message) {
		super(title, R.layout.card_simple);
		mMessageString = message;
	}

	@Override
	protected void bindView(final Context context, final View view) {
		final TextView message = (TextView) view.findViewById(android.R.id.text1);
		if (mMessageString == null) {
			message.setText(mMessageId);
		} else {
			message.setText(mMessageString);
		}
	}

}
