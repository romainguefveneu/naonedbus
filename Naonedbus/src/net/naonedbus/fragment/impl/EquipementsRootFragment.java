package net.naonedbus.fragment.impl;

import net.naonedbus.R;
import net.naonedbus.fragment.ViewPagerFragment;
import net.naonedbus.fragment.impl.nested.CoVoituragesFragment;
import net.naonedbus.fragment.impl.nested.LilasFragment;
import net.naonedbus.fragment.impl.nested.MargueritesFragment;
import android.os.Bundle;

public class EquipementsRootFragment extends ViewPagerFragment {

	private static int[] titles = new int[] { R.string.title_fragment_marguerites, R.string.title_fragment_covoiturage,
			R.string.title_fragment_lila };

	private static Class<?>[] classes = new Class<?>[] { MargueritesFragment.class, CoVoituragesFragment.class,
			LilasFragment.class };

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			addFragments(titles, classes);
		}
	}

}
