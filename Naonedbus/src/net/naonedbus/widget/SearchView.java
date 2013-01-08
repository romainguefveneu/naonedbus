package net.naonedbus.widget;

import net.naonedbus.R;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class SearchView extends LinearLayout {

	private EditText mQueryTextView;
	private ImageView mCloseButton;

	public SearchView(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		mQueryTextView = (EditText) findViewById(R.id.searchViewText);
		mCloseButton = (ImageView) findViewById(R.id.searchViewClose);

		mQueryTextView.setHint(getDecoratedHint("Test"));
	}

	private CharSequence getDecoratedHint(CharSequence hintText) {
		SpannableStringBuilder ssb = new SpannableStringBuilder("   "); // for
																		// the
																		// icon
		ssb.append(hintText);
		Drawable searchIcon = getContext().getResources().getDrawable(R.drawable.ic_action_search);
		int textSize = (int) (mQueryTextView.getTextSize() * 1.25);
		searchIcon.setBounds(0, 0, textSize, textSize);
		ssb.setSpan(new ImageSpan(searchIcon), 1, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return ssb;
	}

}
