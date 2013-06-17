package net.naonedbus.fragment.header;

import net.naonedbus.R;
import net.naonedbus.fragment.impl.SearchFragment;
import android.content.Context;

public class MapFragmentHeader implements FragmentHeader {

	private final Class<?>[] mFragments = new Class<?>[] { SearchFragment.class };

	private final int[] mTitles = new int[] { R.string.title_activity_recherche };

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
