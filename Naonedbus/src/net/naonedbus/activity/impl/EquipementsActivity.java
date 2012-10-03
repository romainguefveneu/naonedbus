package net.naonedbus.activity.impl;

import net.naonedbus.R;
import net.naonedbus.activity.SlidingMenuActivity;
import net.naonedbus.fragment.impl.BicloosFragment;
import net.naonedbus.fragment.impl.CoVoituragesFragment;
import net.naonedbus.fragment.impl.MargueritesFragment;
import android.os.Bundle;

public class EquipementsActivity extends SlidingMenuActivity {

	private static int[] titles = new int[] { R.string.title_fragment_bicloos, R.string.title_fragment_marguerites,
			R.string.title_fragment_covoiturage };

	private static Class<?>[] classes = new Class<?>[] { BicloosFragment.class, MargueritesFragment.class,
			CoVoituragesFragment.class };

	public EquipementsActivity() {
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
