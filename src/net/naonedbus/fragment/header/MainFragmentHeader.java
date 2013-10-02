package net.naonedbus.fragment.header;

import net.naonedbus.R;
import net.naonedbus.fragment.impl.FavorisFragment;
import net.naonedbus.fragment.impl.LignesFragment;
import net.naonedbus.fragment.impl.ProximiteFragment;
import net.naonedbus.manager.impl.FavoriManager;
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
		final FavoriManager favoriManager = FavoriManager.getInstance();
		final int count = favoriManager.getAll(context.getContentResolver()).size();
		return count > 0 ? 1 : 0;
	}

}
