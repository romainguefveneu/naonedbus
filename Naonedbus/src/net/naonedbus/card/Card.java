package net.naonedbus.card;

import net.naonedbus.utils.FontUtils;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public abstract class Card {
	private final int mLayoutId;
	private final String mTitleString;
	private final int mTitleId;

	public Card(final String title, final int layoutId) {
		mTitleString = title;
		mLayoutId = layoutId;
		mTitleId = -1;
	}

	public Card(final int title, final int layoutId) {
		mTitleId = title;
		mLayoutId = layoutId;
		mTitleString = null;
	}

	public View getView(final Context context, final ViewGroup root) {
		final View view = LayoutInflater.from(context).inflate(mLayoutId, root, false);
		final Typeface robotoLight = FontUtils.getRobotoLight(context);
		final TextView title = (TextView) view.findViewById(android.R.id.title);
		title.setTypeface(robotoLight);
		if (mTitleString == null) {
			title.setText(mTitleId);
		} else {
			title.setText(mTitleString);
		}
		bindView(context, view);
		return view;
	}

	protected abstract void bindView(final Context context, final View view);

}
