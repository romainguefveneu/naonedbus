package net.naonedbus.fragment.impl;

import net.naonedbus.R;
import net.naonedbus.fragment.ViewPagerFragment;
import net.naonedbus.fragment.impl.nested.BicloosFavorisFragment;
import net.naonedbus.fragment.impl.nested.BicloosFragment;
import net.naonedbus.manager.impl.FavoriBiclooManager;
import android.os.Bundle;

public class BicloosRootFragment extends ViewPagerFragment {
	private static int[] titles = new int[] { R.string.title_fragment_bicloos, R.string.title_fragment_favoris };
	private static Class<?>[] classes = new Class<?>[] { BicloosFragment.class, BicloosFavorisFragment.class };

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
