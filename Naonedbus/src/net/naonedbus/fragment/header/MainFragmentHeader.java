package net.naonedbus.fragment.header;

import net.naonedbus.R;
import net.naonedbus.fragment.impl.nested.FavorisFragment;
import net.naonedbus.fragment.impl.nested.LignesFragment;
import net.naonedbus.fragment.impl.nested.ProximiteFragment;
import android.content.Context;

public class MainFragmentHeader implements FragmentHeader {

	private final Class<?>[] mFragments = new Class<?>[] { LignesFragment.class, FavorisFragment.class,
			ProximiteFragment.class };

	private final int[] mTitles = new int[] { R.string.title_fragment_lignes, R.string.title_fragment_favoris,
			R.string.title_fragment_proximite };

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
