package net.naonedbus.fragment.header;

import net.naonedbus.R;
import net.naonedbus.fragment.impl.ItineraryFragment;
import android.content.Context;

public class ItineraryFragmentHeader implements FragmentHeader {

	private final Class<?>[] mFragments = new Class<?>[] { ItineraryFragment.class };

	private final int[] mTitles = new int[] { R.string.title_activity_itineraire };

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
