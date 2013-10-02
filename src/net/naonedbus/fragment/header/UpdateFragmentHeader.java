package net.naonedbus.fragment.header;

import net.naonedbus.fragment.impl.UpdateFragment;
import android.content.Context;

public class UpdateFragmentHeader implements FragmentHeader {

	private final Class<?>[] mFragments = new Class<?>[] { UpdateFragment.class };

	private final int[] mTitles = new int[] { 0 };

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
