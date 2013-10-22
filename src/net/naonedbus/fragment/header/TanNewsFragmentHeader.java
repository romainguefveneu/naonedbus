package net.naonedbus.fragment.header;

import net.naonedbus.R;
import net.naonedbus.fragment.impl.LiveNewsFragment;
import net.naonedbus.fragment.impl.TanNewsFragment;
import android.content.Context;

public class TanNewsFragmentHeader implements FragmentHeader {

	private final Class<?>[] mFragments = new Class<?>[] { LiveNewsFragment.class, TanNewsFragment.class };

	private final int[] mTitles = new int[] { R.string.title_fragment_en_direct, R.string.title_fragment_tan_actu };

	@Override
	public int[] getFragmentsTitles() {
		return mTitles;
	}

	@Override
	public Class<?>[] getFragmentsClasses() {
		return mFragments;
	}

	@Override
	public int getSelectedPosition(final Context context) {
		return 0;
	}

}
