package net.naonedbus.fragment.impl;

import net.naonedbus.R;
import net.naonedbus.fragment.ViewPagerFragment;
import net.naonedbus.fragment.impl.nested.FavorisFragment;
import net.naonedbus.fragment.impl.nested.LignesFragment;
import net.naonedbus.fragment.impl.nested.ProximiteFragment;
import android.os.Bundle;

public class MainRootFragment extends ViewPagerFragment {
	private static int[] titles = new int[] { R.string.title_fragment_lignes, R.string.title_fragment_favoris,
			R.string.title_fragment_proximite };

	private static Class<?>[] classes = new Class<?>[] { LignesFragment.class, FavorisFragment.class,
			ProximiteFragment.class };

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		if (savedInstanceState == null) {
			addFragments(titles, classes);
		}
	}

}
