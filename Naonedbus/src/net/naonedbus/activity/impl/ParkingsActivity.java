package net.naonedbus.activity.impl;

import net.naonedbus.R;
import net.naonedbus.activity.SlidingMenuActivity;
import net.naonedbus.fragment.impl.ParkingsPublicsFragment;
import net.naonedbus.fragment.impl.ParkingsRelaisFragment;
import android.os.Bundle;

public class ParkingsActivity extends SlidingMenuActivity {

	private static int[] titles = new int[] { R.string.title_fragment_parkings_publics,
			R.string.title_fragment_parkings_relais };

	private static Class<?>[] classes = new Class<?>[] { ParkingsPublicsFragment.class, ParkingsRelaisFragment.class };

	public ParkingsActivity() {
		super(R.layout.activity_main);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			addFragments(titles, classes);
		}
	}

}
