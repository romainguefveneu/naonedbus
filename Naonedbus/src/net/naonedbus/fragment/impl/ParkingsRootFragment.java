package net.naonedbus.fragment.impl;

import net.naonedbus.R;
import net.naonedbus.fragment.ViewPagerFragment;
import net.naonedbus.fragment.impl.nested.ParkingsPublicsFragment;
import net.naonedbus.fragment.impl.nested.ParkingsRelaisFragment;
import android.os.Bundle;

public class ParkingsRootFragment extends ViewPagerFragment {
	private static int[] titles = new int[] { R.string.title_fragment_parkings_publics,
			R.string.title_fragment_parkings_relais };

	private static Class<?>[] classes = new Class<?>[] { ParkingsPublicsFragment.class, ParkingsRelaisFragment.class };

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			addFragments(titles, classes);
		}
	}
}
