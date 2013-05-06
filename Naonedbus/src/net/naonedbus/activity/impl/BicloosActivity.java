package net.naonedbus.activity.impl;

import net.naonedbus.R;
import net.naonedbus.activity.SlidingMenuActivity;
import net.naonedbus.fragment.impl.BicloosFavorisFragment;
import net.naonedbus.fragment.impl.BicloosFragment;
import net.naonedbus.manager.impl.FavoriBiclooManager;
import android.os.Bundle;

public class BicloosActivity extends SlidingMenuActivity {

	private static int[] titles = new int[] { R.string.title_fragment_bicloos, R.string.title_fragment_favoris };
	private static Class<?>[] classes = new Class<?>[] { BicloosFragment.class, BicloosFavorisFragment.class };

	public BicloosActivity() {
		super(R.layout.activity_tabs);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			addFragments(titles, classes);

			final FavoriBiclooManager favoriManager = FavoriBiclooManager.getInstance();
			final int count = favoriManager.getAll(getContentResolver()).size();
			if (count > 0) {
				setSelectedTab(1);
			}
		}
	}

}
