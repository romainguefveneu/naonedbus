package net.naonedbus.activity.impl;

import net.naonedbus.R;
import net.naonedbus.activity.SlidingMenuActivity;
import net.naonedbus.fragment.impl.BicloosFragment;
import android.os.Bundle;

public class BicloosActivity extends SlidingMenuActivity {

	private static int[] titles = new int[] { R.string.title_fragment_bicloos };
	private static Class<?>[] classes = new Class<?>[] { BicloosFragment.class };

	public BicloosActivity() {
		super(R.layout.activity_tabs);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			addFragments(titles, classes);
		}
	}

}
